using System.Diagnostics;
using System.Security.Cryptography;
using AlienLauncher.Core;
using Windows.Media.Control;

namespace AlienLauncher.Media;

/// <summary>Reads Windows SMTC metadata. Never reads tab titles, browser history or Spotify tokens.</summary>
public sealed class WindowsMediaReader
{
    private GlobalSystemMediaTransportControlsSessionManager? manager;
    private string previousKey = "", artwork = "";
    private GlobalSystemMediaTransportControlsSessionMediaProperties? properties;
    private DateTimeOffset propertiesAt, artworkAt;
    public async Task<NowPlayingSnapshot> ReadAsync(string directory, string preference, bool includeArtwork, CancellationToken ct)
    {
        manager ??= await GlobalSystemMediaTransportControlsSessionManager.RequestAsync().AsTask(ct).WaitAsync(TimeSpan.FromSeconds(4), ct);
        var sessions = manager.GetSessions().Where(s => NowPlayingSnapshot.MatchesSource(s.SourceAppUserModelId, preference)).ToArray();
        var current = manager.GetCurrentSession();
        var selected = sessions.FirstOrDefault(s => s.Equals(current) && s.GetPlaybackInfo().PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing)
            ?? sessions.FirstOrDefault(s => s.GetPlaybackInfo().PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing)
            ?? sessions.FirstOrDefault(s => s.Equals(current) && s.GetPlaybackInfo().PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Paused)
            ?? sessions.FirstOrDefault(s => s.GetPlaybackInfo().PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Paused);
        if (selected == null) { previousKey = artwork = ""; properties = null; return new(); }
        var playback = selected.GetPlaybackInfo();
        string status = playback.PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing ? "playing" :
            playback.PlaybackStatus == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Paused ? "paused" : "idle";
        if (status == "idle") return new();
        var now = DateTimeOffset.UtcNow;
        if (properties == null || now - propertiesAt >= TimeSpan.FromSeconds(1) || !previousKey.StartsWith(selected.SourceAppUserModelId + "\n", StringComparison.Ordinal))
        {
            properties = await selected.TryGetMediaPropertiesAsync().AsTask(ct).WaitAsync(TimeSpan.FromSeconds(2), ct);
            propertiesAt = now;
            string key = selected.SourceAppUserModelId + "\n" + properties.Title + "\n" + properties.Artist + "\n" + properties.AlbumTitle;
            if (key != previousKey) { previousKey = key; artwork = ""; }
            if (artwork.Length == 0 && now - artworkAt >= TimeSpan.FromSeconds(2))
            {
                artworkAt = now;
                if (includeArtwork && properties.Thumbnail != null)
                {
                    try
                    {
                        using var random = await properties.Thumbnail.OpenReadAsync().AsTask(ct).WaitAsync(TimeSpan.FromSeconds(2), ct);
                        if (random.Size is > 0 and <= 2097152)
                        {
                            using var stream = random.AsStreamForRead();
                            using var image = System.Drawing.Image.FromStream(stream);
                            if (image.Width <= 4096 && image.Height <= 4096)
                            {
                                using var cover = new System.Drawing.Bitmap(192, 192);
                                using (var graphics = System.Drawing.Graphics.FromImage(cover))
                                {
                                    graphics.Clear(System.Drawing.Color.FromArgb(22, 24, 34));
                                    float ratio = Math.Min(192f / image.Width, 192f / image.Height);
                                    int w = (int)(image.Width * ratio), h = (int)(image.Height * ratio);
                                    graphics.DrawImage(image, (192 - w) / 2, (192 - h) / 2, w, h);
                                }
                                using var bytes = new MemoryStream(); cover.Save(bytes, System.Drawing.Imaging.ImageFormat.Png);
                                var data = bytes.ToArray(); artwork = Convert.ToHexString(SHA256.HashData(data)).ToLowerInvariant();
                                Directory.CreateDirectory(directory); string temp = Path.Combine(directory, ".cover-" + Guid.NewGuid().ToString("N"));
                                try { await File.WriteAllBytesAsync(temp, data, ct); File.Move(temp, Path.Combine(directory, "cover.png"), true); }
                                finally { if (File.Exists(temp)) File.Delete(temp); }
                            }
                        }
                    }
                    catch (Exception) when (!ct.IsCancellationRequested) { artwork = ""; }
                }
            }
        }
        var timeline = selected.GetTimelineProperties();
        double rate = playback.PlaybackRate ?? 1;
        if (!double.IsFinite(rate) || rate is <= 0 or > 4) rate = 1;
        long? duration = timeline.EndTime > timeline.StartTime ? (long)(timeline.EndTime - timeline.StartTime).TotalMilliseconds : null;
        long? position = timeline.Position >= timeline.StartTime ? (long)(timeline.Position - timeline.StartTime).TotalMilliseconds : null;
        now = DateTimeOffset.UtcNow;
        if (status == "playing" && position.HasValue && timeline.LastUpdatedTime <= now && timeline.LastUpdatedTime.Year >= 2020)
        {
            double advance = (now - timeline.LastUpdatedTime).TotalMilliseconds * (double.IsFinite(rate) && rate > 0 ? rate : 1);
            position += (long)Math.Min(604800000, Math.Max(0, advance));
        }
        return new NowPlayingSnapshot { Status = status, Title = properties?.Title ?? "", Artist = properties?.Artist ?? "", Album = properties?.AlbumTitle ?? "",
            Source = NowPlayingSnapshot.SourceName(selected.SourceAppUserModelId), CapturedAt = now.ToUnixTimeMilliseconds(), PositionMs = position,
            DurationMs = duration, Rate = rate, Artwork = includeArtwork ? artwork : "" }.Normalize();
    }
}

internal static class WindowsNowPlayingBridge
{
    public static async Task<int> RunAsync(string[] args)
    {
        string? output = null; int parent = 0; string source = "Auto"; bool art = true;
        try
        {
            for (int i = 1; i < args.Length; i++)
            {
                string key = args[i], value = args[++i];
                switch (key) { case "--output": output = Path.GetFullPath(value); break; case "--parent": parent = int.Parse(value); break;
                    case "--source": source = value; break; case "--art": art = value == "true"; break; default: return 2; }
            }
            if (output == null || parent <= 0 || source is not ("Auto" or "Spotify" or "Browser")) return 2;
            using var owner = Process.GetProcessById(parent); using var cancellation = new CancellationTokenSource();
            var reader = new WindowsMediaReader();
            while (!owner.HasExited)
            {
                NowPlayingSnapshot state;
                try { state = OperatingSystem.IsWindowsVersionAtLeast(10, 0, 17763) ? await reader.ReadAsync(output, source, art, cancellation.Token) : new() { Status = "unsupported" }; }
                catch (Exception) { state = new() { Status = "error" }; }
                await NowPlayingSnapshot.WriteAsync(output, state, cancellation.Token);
                await Task.Delay(500, cancellation.Token);
            }
            await NowPlayingSnapshot.WriteAsync(output, new(), CancellationToken.None);return 0;
        }
        catch (Exception) { return 1; }
        finally
        {
            if (output != null)
            {
                // Only files owned by this helper; never recursively remove a caller's directory.
                try
                {
                    foreach (string file in Directory.EnumerateFiles(output))
                    {
                        string name = Path.GetFileName(file);
                        if (name is "now-playing.json" or "cover.png" || name.StartsWith(".state-") || name.StartsWith(".cover-")) File.Delete(file);
                    }
                    if (!Directory.EnumerateFileSystemEntries(output).Any()) Directory.Delete(output);
                }
                catch (IOException) { }
                catch (UnauthorizedAccessException) { }
            }
        }
    }
}
