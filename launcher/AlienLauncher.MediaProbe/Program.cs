using System.Diagnostics;
using System.Text.Json;
using AlienLauncher.Core;
using Windows.Media;
using Windows.Media.Core;
using Windows.Media.Playback;
using Windows.Storage;
using Windows.Storage.Streams;

// Test-only local publisher. This is an actual Windows SMTC session, not Spotify/YouTube.
if (args.Length==1 && args[0]=="--owner") { Thread.Sleep(60000); return 0; }
string output=Path.GetFullPath(args[0]),bridge=Path.GetFullPath(args[1]);
Directory.CreateDirectory(output);
var checks=new List<string>();
void Check(bool condition,string text) { if(!condition) throw new InvalidOperationException(text); checks.Add(text); }
Process? helper=null,owner=null;
try
{
    string audio=Path.Combine(output,"diagnostic-silence.wav"),cover=Path.Combine(output,"diagnostic-cover.png");
    using(var file=File.Create(audio)) using(var w=new BinaryWriter(file)) {
        int size=8000*180*2;w.Write("RIFF"u8);w.Write(size+36);w.Write("WAVEfmt "u8);w.Write(16);w.Write((short)1);w.Write((short)1);w.Write(8000);w.Write(16000);w.Write((short)2);w.Write((short)16);w.Write("data"u8);w.Write(size);w.Write(new byte[size]);
    }
    using(var bitmap=new System.Drawing.Bitmap(128,128)) {
        using(var g=System.Drawing.Graphics.FromImage(bitmap)) {g.Clear(System.Drawing.Color.MediumPurple);g.FillEllipse(System.Drawing.Brushes.Black,30,30,68,68);}
        bitmap.Save(cover,System.Drawing.Imaging.ImageFormat.Png);
    }
    using var player=new MediaPlayer {IsMuted=true,IsLoopingEnabled=true};
    player.CommandManager.IsEnabled=false;
    player.Source=MediaSource.CreateFromStorageFile(await StorageFile.GetFileFromPathAsync(audio));
    player.Play();
    var controls=player.SystemMediaTransportControls;
    controls.IsEnabled=true;controls.IsPlayEnabled=true;controls.IsPauseEnabled=true;
    controls.DisplayUpdater.Type=MediaPlaybackType.Music;
    controls.DisplayUpdater.MusicProperties.Title="ALIEN MEDIA TEST — Windows OS session";
    controls.DisplayUpdater.MusicProperties.Artist="Local diagnostic publisher";
    controls.DisplayUpdater.MusicProperties.AlbumTitle="SMTC integration test";
    controls.DisplayUpdater.Thumbnail=RandomAccessStreamReference.CreateFromFile(await StorageFile.GetFileFromPathAsync(cover));
    controls.DisplayUpdater.Update();
    void Timeline(int position) => controls.UpdateTimelineProperties(new SystemMediaTransportControlsTimelineProperties {
        StartTime=TimeSpan.Zero,MinSeekTime=TimeSpan.Zero,Position=TimeSpan.FromSeconds(position),EndTime=TimeSpan.FromSeconds(180),MaxSeekTime=TimeSpan.FromSeconds(180)});
    controls.PlaybackStatus=MediaPlaybackStatus.Playing;Timeline(20);
    owner=Process.Start(new ProcessStartInfo(Environment.ProcessPath!,"--owner") {UseShellExecute=false})!;
    var start=new ProcessStartInfo(bridge) {UseShellExecute=false};
    string transport=Path.Combine(output,"transport");
    foreach(string a in new[]{"--media-bridge","--output",transport,"--parent",owner.Id.ToString(),"--source","Auto","--art","true"}) start.ArgumentList.Add(a);
    helper=Process.Start(start)!;
    async Task<JsonElement> WaitFor(Func<JsonElement,bool> match,int timeout=25000) {
        var timer=Stopwatch.StartNew();string last="no snapshot";
        while(timer.ElapsedMilliseconds<timeout) {
            if(helper.HasExited)throw new InvalidOperationException("Native bridge exited: "+helper.ExitCode);
            try {
                string text=await File.ReadAllTextAsync(Path.Combine(transport,"now-playing.json"));last=text;
                using var json=JsonDocument.Parse(text);if(match(json.RootElement))return json.RootElement.Clone();
            }catch(IOException) {}catch(JsonException) {}
            await Task.Delay(300);
        }
        throw new InvalidOperationException("OS session was not received. Last bridge state: "+last);
    }
    JsonElement first=await WaitFor(s=>s.GetProperty("status").GetString()=="playing" && s.GetProperty("title").GetString()!.StartsWith("ALIEN MEDIA TEST"));
    await File.WriteAllTextAsync(Path.Combine(output,"playing.json"),first.GetRawText());
    Check(first.GetProperty("artist").GetString()=="Local diagnostic publisher","Actual native bridge reads metadata from a real local Windows SMTC publisher");
    Check(first.GetProperty("source").GetString()!="Spotify","Diagnostic publisher is never identified as Spotify");
    JsonElement art=await WaitFor(s=>s.GetProperty("artwork").GetString()!.Length==64);
    string actual=Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(await File.ReadAllBytesAsync(Path.Combine(transport,"cover.png")))).ToLowerInvariant();
    Check(actual==art.GetProperty("artwork").GetString(),"Actual OS artwork arrives with matching checksum");
    Check(first.GetProperty("durationMs").GetInt64()==180000,"Actual OS diagnostic timeline supplies duration");
    player.Pause();controls.PlaybackStatus=MediaPlaybackStatus.Paused;Timeline(55);
    JsonElement paused=await WaitFor(s=>s.GetProperty("status").GetString()=="paused");
    await File.WriteAllTextAsync(Path.Combine(output,"paused.json"),paused.GetRawText());
    await Task.Delay(1800);
    JsonElement stable=await WaitFor(s=>s.GetProperty("status").GetString()=="paused");
    Check(Math.Abs(stable.GetProperty("positionMs").GetInt64()-paused.GetProperty("positionMs").GetInt64())<1000,"Paused native timeline stays fixed between bridge polls");
    controls.PlaybackStatus=MediaPlaybackStatus.Stopped;
    await WaitFor(s=>s.GetProperty("status").GetString()=="idle");
    Check(true,"Stopped OS session clears listening metadata");
    owner.Kill();await owner.WaitForExitAsync();
    using var deadline=new CancellationTokenSource(TimeSpan.FromSeconds(12));await helper.WaitForExitAsync(deadline.Token);
    Check(helper.ExitCode==0,"Bridge exits when the game owner process ends");
    Check(!File.Exists(Path.Combine(transport,"now-playing.json")),"Owner exit removes temporary listening metadata");
    await File.WriteAllLinesAsync(Path.Combine(output,"result.txt"),new[]{"PASS "+checks.Count+" checks","Windows 2022 hosted runner; real OS SMTC diagnostic publisher. No Spotify or YouTube account tested."}.Concat(checks.Select(c=>"PASS "+c)));
    return 0;
}
catch(Exception failure) {await File.WriteAllTextAsync(Path.Combine(output,"result.txt"),"FAIL after "+checks.Count+" checks\n"+failure);return 1;}
finally {if(helper is {HasExited:false})helper.Kill();if(owner is {HasExited:false})owner.Kill();helper?.Dispose();owner?.Dispose();}
