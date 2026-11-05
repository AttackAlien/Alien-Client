using System.Text.Json;

namespace AlienLauncher.Core;

/// <summary>Current local media only. No history, credentials or playback commands.</summary>
public sealed record NowPlayingSnapshot
{
    public int Schema { get; init; } = 1;
    public string Status { get; init; } = "idle";
    public string Title { get; init; } = "";
    public string Artist { get; init; } = "";
    public string Album { get; init; } = "";
    public string Source { get; init; } = "";
    public long CapturedAt { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
    public long? PositionMs { get; init; }
    public long? DurationMs { get; init; }
    public double Rate { get; init; } = 1;
    public string Artwork { get; init; } = "";

    public NowPlayingSnapshot Normalize()
    {
        string status = Status is "playing" or "paused" or "idle" or "error" or "unsupported" ? Status : "error";
        if (status is not ("playing" or "paused")) return new() { Status = status, CapturedAt = CapturedAt };
        long? duration = DurationMs is > 0 and <= 604800000 ? DurationMs : null;
        long? position = PositionMs is >= 0 ? Math.Min(PositionMs.Value, duration ?? 604800000) : null;
        return this with { Status = status, Title = Clean(Title), Artist = Clean(Artist), Album = Clean(Album), Source = Clean(Source),
            DurationMs = duration, PositionMs = position, Rate = double.IsFinite(Rate) && Rate is > 0 and <= 4 ? Rate : 1,
            Artwork = Artwork is { Length: 64 } && Artwork.All(Uri.IsHexDigit) ? Artwork.ToLowerInvariant() : "" };
    }
    public static string Clean(string? text) => new string((text ?? "").Where(c => !char.IsControl(c) && c != '\u00a7').Take(512).ToArray()).Trim();
    public static string SourceName(string id) => id.Contains("spotify", StringComparison.OrdinalIgnoreCase) ? "Spotify" :
        id.Contains("msedge", StringComparison.OrdinalIgnoreCase) ? "Edge" : id.Contains("chrome", StringComparison.OrdinalIgnoreCase) ? "Chrome" :
        id.Contains("firefox", StringComparison.OrdinalIgnoreCase) ? "Firefox" : id.Contains("brave", StringComparison.OrdinalIgnoreCase) ? "Brave" : "Reprodutor";
    public static bool MatchesSource(string id, string preference) => preference switch
    {
        "Spotify" => SourceName(id) == "Spotify",
        "Browser" => SourceName(id) is "Chrome" or "Edge" or "Firefox" or "Brave" || id.Contains("opera", StringComparison.OrdinalIgnoreCase),
        _ => true
    };
    public static async Task WriteAsync(string directory, NowPlayingSnapshot snapshot, CancellationToken ct)
    {
        Directory.CreateDirectory(directory);
        string path = Path.Combine(directory, "now-playing.json"), temp = Path.Combine(directory, ".state-" + Guid.NewGuid().ToString("N"));
        try
        {
            string json = JsonSerializer.Serialize(snapshot.Normalize(), new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });
            await File.WriteAllTextAsync(temp, json, new System.Text.UTF8Encoding(false), ct);
            File.Move(temp, path, true);
        }
        finally { if (File.Exists(temp)) File.Delete(temp); }
    }
}
