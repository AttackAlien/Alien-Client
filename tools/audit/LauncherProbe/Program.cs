// Audit fixture only. Uses disposable files and never starts Minecraft or authenticates.
using AlienLauncher.Core;
using CmlLib.Core;
using CmlLib.Core.Auth;
using System.Text.Json;

string root = Path.Combine(Path.GetTempPath(), "alien-audit-" + Guid.NewGuid().ToString("N"));
Directory.CreateDirectory(root);
var observations = new List<object>();
try
{
    string oversized = Path.Combine(root, "oversized"); Directory.CreateDirectory(oversized);
    await File.WriteAllTextAsync(Path.Combine(oversized, "settings.json"), "{\"MemoryMb\":4096}" + new string(' ', 17000));
    try { LauncherPreferences.Load(oversized); observations.Add(new { test = "oversized_settings", exception = "none" }); }
    catch (Exception e) { observations.Add(new { test = "oversized_settings", exception = e.GetType().FullName, invalidDataIsIOException = e is IOException }); }

    if (OperatingSystem.IsLinux())
    {
        string java = Path.Combine(root, "failed-java");
        await File.WriteAllTextAsync(java, "#!/bin/sh\nprintf 'openjdk version \"1.8.0_472\"\\n' >&2\nexit 42\n");
        File.SetUnixFileMode(java, UnixFileMode.UserRead | UnixFileMode.UserWrite | UnixFileMode.UserExecute);
        int accepted = await RuntimePolicy.CheckAsync(java, 8);
        observations.Add(new { test = "java_exit_42", acceptedMajor = accepted, actualExit = 42, fixture = "synthetic version-output wrapper; not a real JVM" });
    }
    observations.Add(new { test = "release_id_with_newline", accepted = Accepted("1.8.9\n") });

    if (args.Length == 2)
    {
        string instance = Path.GetFullPath(args[0]);
        var runtime = new MinecraftLauncher(new MinecraftPath(instance));
        var installed = new InstalledProfile(runtime, "1.8.9-forge1.8.9-11.15.1.2318-1.8.9", args[1], instance, 8);
        using var process = await InstanceInstaller.BuildAsync(installed, MSession.CreateOfflineSession("AlienAuditFixture"), 2048);
        string arguments = process.StartInfo.Arguments + " " + string.Join(' ', process.StartInfo.ArgumentList);
        observations.Add(new { test = "logging_configuration_arguments", log4jArgument = arguments.Contains("-Dlog4j.configurationFile="), officialConfig = arguments.Contains("client-1.7.xml"), processStarted = false, microsoftAuthenticationTested = false });
    }
    Console.WriteLine(JsonSerializer.Serialize(observations, new JsonSerializerOptions { WriteIndented = true }));
}
finally { Directory.Delete(root, true); }

static bool Accepted(string version)
{
    try { new LauncherProfile(version, false).Validate(); return true; }
    catch (InvalidDataException) { return false; }
}
