using System.Diagnostics;
using System.Text.Json;
using AlienLauncher.Core;
using Windows.Media;
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
    string cover=Path.Combine(output,"diagnostic-cover.png");
    using(var bitmap=new System.Drawing.Bitmap(128,128)) {
        using(var g=System.Drawing.Graphics.FromImage(bitmap)) {g.Clear(System.Drawing.Color.MediumPurple);g.FillEllipse(System.Drawing.Brushes.Black,30,30,68,68);}
        bitmap.Save(cover,System.Drawing.Imaging.ImageFormat.Png);
    }
    DiagnosticPublisher.SetAppId("AlienClient.NowPlaying.Diagnostic");
    var ready = new TaskCompletionSource<nint>(TaskCreationOptions.RunContinuationsAsynchronously);
    var ui = new Thread(() => {using var form=new System.Windows.Forms.Form {Text="Alien diagnostic OS media publisher",Width=500,Height=180};
        form.Shown+=(_,_)=>ready.TrySetResult(form.Handle);System.Windows.Forms.Application.Run(form);});
    ui.SetApartmentState(ApartmentState.STA);ui.IsBackground=true;ui.Start();
    nint hwnd=await ready.Task.WaitAsync(TimeSpan.FromSeconds(10));
    var controls=DiagnosticPublisher.ForWindow(hwnd);
    controls.IsEnabled=true;controls.IsPlayEnabled=true;controls.IsPauseEnabled=true;
    controls.DisplayUpdater.Type=MediaPlaybackType.Music;
    controls.DisplayUpdater.MusicProperties.Title="ALIEN MEDIA TEST — Windows OS session";
    controls.DisplayUpdater.MusicProperties.Artist="Local diagnostic publisher";
    controls.DisplayUpdater.MusicProperties.AlbumTitle="SMTC integration test";
    controls.DisplayUpdater.Thumbnail=RandomAccessStreamReference.CreateFromFile(await StorageFile.GetFileFromPathAsync(cover));
    controls.DisplayUpdater.Update();
    controls.ButtonPressed+=(_,_)=>{};
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
    var manager=await Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager.RequestAsync();
    await Task.Delay(1000);
    await File.WriteAllTextAsync(Path.Combine(output,"os-session-diagnostics.json"),JsonSerializer.Serialize(new {os=System.Runtime.InteropServices.RuntimeInformation.OSDescription,interactive=Environment.UserInteractive,processSession=Process.GetCurrentProcess().SessionId,enabled=controls.IsEnabled,publisherStatus=controls.PlaybackStatus.ToString(),sessionCount=manager.GetSessions().Count}));
    JsonElement first=await WaitFor(s=>s.GetProperty("status").GetString()=="playing" && s.GetProperty("title").GetString()!.StartsWith("ALIEN MEDIA TEST"));
    await File.WriteAllTextAsync(Path.Combine(output,"playing.json"),first.GetRawText());
    Check(first.GetProperty("artist").GetString()=="Local diagnostic publisher","Actual native bridge reads metadata from a real local Windows SMTC publisher");
    Check(first.GetProperty("source").GetString()!="Spotify","Diagnostic publisher is never identified as Spotify");
    JsonElement art=await WaitFor(s=>s.GetProperty("artwork").GetString()!.Length==64);
    string actual=Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(await File.ReadAllBytesAsync(Path.Combine(transport,"cover.png")))).ToLowerInvariant();
    Check(actual==art.GetProperty("artwork").GetString(),"Actual OS artwork arrives with matching checksum");
    Check(first.GetProperty("durationMs").GetInt64()==180000,"Actual OS diagnostic timeline supplies duration");
    controls.PlaybackStatus=MediaPlaybackStatus.Paused;Timeline(55);
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
    await File.WriteAllLinesAsync(Path.Combine(output,"result.txt"),new[]{"PASS "+checks.Count+" checks",System.Runtime.InteropServices.RuntimeInformation.OSDescription+"; real OS SMTC diagnostic publisher. No Spotify or YouTube account tested."}.Concat(checks.Select(c=>"PASS "+c)));
    return 0;
}
catch(Exception failure) {await File.WriteAllTextAsync(Path.Combine(output,"result.txt"),"FAIL after "+checks.Count+" checks\n"+failure);return 1;}
finally {if(helper is {HasExited:false})helper.Kill();if(owner is {HasExited:false})owner.Kill();helper?.Dispose();owner?.Dispose();}

internal static class DiagnosticPublisher
{
    [System.Runtime.InteropServices.DllImport("shell32.dll", CharSet=System.Runtime.InteropServices.CharSet.Unicode, EntryPoint="SetCurrentProcessExplicitAppUserModelID")]
    public static extern int SetAppId(string appId);
    [System.Runtime.InteropServices.DllImport("combase.dll", CharSet=System.Runtime.InteropServices.CharSet.Unicode)]
    private static extern int WindowsCreateString(string text,int length,out nint value);
    [System.Runtime.InteropServices.DllImport("combase.dll")]
    private static extern int WindowsDeleteString(nint value);
    [System.Runtime.InteropServices.DllImport("combase.dll")]
    private static extern int RoGetActivationFactory(nint name,in Guid iid,out nint factory);
    // Official desktop HWND interop; unlike MediaPlayer it needs no audio endpoint on a hosted runner.
    public static unsafe SystemMediaTransportControls ForWindow(nint window)
    {
        const string name="Windows.Media.SystemMediaTransportControls";
        nint hstring=0,factory=0,result=0;
        try
        {
            System.Runtime.InteropServices.Marshal.ThrowExceptionForHR(WindowsCreateString(name,name.Length,out hstring));
            Guid interop=new("ddb0472d-c911-4a1f-86d9-dc3d71a95f5a"),smtc=new("99fa3ff4-1742-42a6-902e-087d41f965ec");
            System.Runtime.InteropServices.Marshal.ThrowExceptionForHR(RoGetActivationFactory(hstring,interop,out factory));
            var get=(delegate* unmanaged[Stdcall]<nint,nint,Guid*,nint*,int>)(*(nint**)factory)[6];
            System.Runtime.InteropServices.Marshal.ThrowExceptionForHR(get(factory,window,&smtc,&result));
            return WinRT.MarshalInterface<SystemMediaTransportControls>.FromAbi(result);
        }
        finally
        {
            if(result!=0)System.Runtime.InteropServices.Marshal.Release(result);
            if(factory!=0)System.Runtime.InteropServices.Marshal.Release(factory);
            if(hstring!=0)WindowsDeleteString(hstring);
        }
    }
}
