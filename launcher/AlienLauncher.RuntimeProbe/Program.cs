// Test-only installer/launch fixture. Never bundled with the production launcher.
using AlienLauncher.Core;
using CmlLib.Core.Auth;
using System.Text.Json;
if (args.Length < 3) throw new ArgumentException("Usage: probe <isolated-root> <payload> <java8> [--run]");
string? vanilla = args.FirstOrDefault(a => a.StartsWith("--vanilla=", StringComparison.Ordinal));
var profile = vanilla == null ? new LauncherProfile("1.8.9", true) : new LauncherProfile(vanilla[10..], false); var payload = new ClientPayload(args[1]);
using var deadline = new CancellationTokenSource(TimeSpan.FromMinutes(12));
var installer = new InstanceInstaller(args[0], payload);
var installed = await installer.InstallAsync(profile, args[2], new Progress<LauncherProgress>(p => Console.WriteLine(p.Message)), ct: deadline.Token);
Console.WriteLine("INSTALL_PASS " + JsonSerializer.Serialize(new { installed.VersionId, installed.JavaMajor, installed.Directory }));
if (!args.Contains("--run")) return;
// Same anonymous local-development identity used by Minecraft mod test harnesses.
// This does not validate Microsoft login and is inaccessible from the shipping launcher UI.
using var process = await InstanceInstaller.BuildAsync(installed, MSession.CreateOfflineSession("AlienRuntimeProbe"), 2048, deadline.Token);
process.StartInfo.UseShellExecute = false; process.StartInfo.RedirectStandardOutput = true; process.StartInfo.RedirectStandardError = true;
var loaded = new TaskCompletionSource(TaskCreationOptions.RunContinuationsAsynchronously);
void Line(string? text) { if (text == null) return; Console.WriteLine(PrivateDiagnostics.Clean(text)); if (text.Contains("Alien Client " + LauncherProfile.ClientVersion + " initialized", StringComparison.Ordinal)) loaded.TrySetResult(); }
process.OutputDataReceived += (_, e) => Line(e.Data); process.ErrorDataReceived += (_, e) => Line(e.Data);
process.Start(); process.BeginOutputReadLine(); process.BeginErrorReadLine();
try
{
    var exit = process.WaitForExitAsync(deadline.Token);
    var winner = await Task.WhenAny(loaded.Task, exit, Task.Delay(TimeSpan.FromMinutes(3), deadline.Token));
    if (winner != loaded.Task) throw new InvalidOperationException("Runtime did not confirm actual Alien initialization; exit=" + (process.HasExited ? process.ExitCode : -1));
    Console.WriteLine("RUNTIME_ALIEN_LOADED " + LauncherProfile.ClientVersion);
    await Task.Delay(TimeSpan.FromSeconds(20), deadline.Token);
    if (process.HasExited) throw new InvalidOperationException("Minecraft exited early: " + process.ExitCode);
    Console.WriteLine("RUNTIME_ALIVE_PASS");
}
finally { if (!process.HasExited) { process.Kill(true); await process.WaitForExitAsync(); } }
