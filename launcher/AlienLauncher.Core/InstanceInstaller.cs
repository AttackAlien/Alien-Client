using System.Diagnostics;
using CmlLib.Core;
using CmlLib.Core.Auth;
using CmlLib.Core.Installer.Forge;
using CmlLib.Core.Installer.Forge.Versions;
using CmlLib.Core.ProcessBuilder;
namespace AlienLauncher.Core;

public sealed record LauncherProgress(string Message, int? Percent = null);
public sealed record InstalledProfile(MinecraftLauncher Runtime, string VersionId, string JavaPath, string Directory, int JavaMajor);

public sealed class InstanceInstaller(string root, ClientPayload payload)
{
    public string Root { get; } = Path.GetFullPath(root);
    public async Task<InstalledProfile> InstallAsync(LauncherProfile profile, string java8Override, IProgress<LauncherProgress>? progress = null, bool repair = false, CancellationToken ct = default)
    {
        profile.Validate();
        if (profile.Alien) await payload.VerifyAsync(ct);
        string directory = profile.DirectoryAt(Root); Directory.CreateDirectory(directory);
        var runtime = new MinecraftLauncher(new MinecraftPath(directory));
        long lastReport = 0;
        runtime.FileProgressChanged += (_, e) =>
        {
            long now = Environment.TickCount64;
            if (now - Interlocked.Read(ref lastReport) < 120 && e.ProgressedTasks < e.TotalTasks) return;
            Interlocked.Exchange(ref lastReport, now);
            progress?.Report(new($"Arquivos: {e.ProgressedTasks}/{e.TotalTasks} — {e.Name}", e.TotalTasks > 0 ? Math.Clamp((int)(e.ProgressedTasks * 100L / e.TotalTasks), 0, 100) : null));
        };
        progress?.Report(new("Verificando Minecraft " + profile.Version + " e o Java gerenciado..."));
        await runtime.InstallAsync(profile.Version, ct);
        var version = await runtime.GetVersionAsync(profile.Version, ct);
        int major = profile.Alien ? 8 : int.TryParse(version.JavaVersion?.MajorVersion, out int required) ? required : 8;
        string java = major == 8 && !string.IsNullOrWhiteSpace(java8Override) ? Path.GetFullPath(java8Override) : runtime.GetJavaPath(version) ?? throw new FileNotFoundException("O Java gerenciado não foi instalado; use Instalar / Reparar.");
        await RuntimePolicy.CheckAsync(java, major, ct);
        string id = profile.Version;
        if (profile.Alien)
        {
            progress?.Report(new("Instalando/verificando Forge " + LauncherProfile.ForgeBuild + " com Java 8..."));
            var metadata = new ForgeVersion(profile.Version, LauncherProfile.ForgeBuild)
            {
                Files = [new ForgeVersionFile { Type = "installer", SHA1 = "ec0293ff0776b8831f2ed90511bab76e635dda0c", DirectUrl = "https://maven.minecraftforge.net/net/minecraftforge/forge/1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9-installer.jar" }]
            };
            var forge = new ForgeInstallerVersionMapper().CreateInstaller(metadata); id = forge.VersionName;
            bool hasVersion = File.Exists(runtime.MinecraftPath.GetVersionJsonPath(id));
            if (repair || !hasVersion)
            {
                await forge.Install(runtime.MinecraftPath, runtime.GameInstaller, new ForgeInstallOptions
                {
                    JavaPath = java, SkipIfAlreadyInstalled = !repair, CancellationToken = ct,
                    InstallerOutput = new Progress<string>(_ => progress?.Report(new("Instalando Forge com Java 8...")))
                });
                await runtime.GetAllVersionsAsync(ct);
            }
            if (!id.StartsWith("1.8.9", StringComparison.Ordinal) || !id.Contains(LauncherProfile.ForgeBuild, StringComparison.Ordinal) || !id.Contains("forge", StringComparison.OrdinalIgnoreCase))
                throw new InvalidDataException("O instalador retornou um perfil Forge inesperado.");
            await runtime.InstallAsync(id, ct);
            await payload.DeployAsync(directory, ct);
        }
        ct.ThrowIfCancellationRequested();
        await File.WriteAllTextAsync(Path.Combine(directory, "alien-installation.json"), System.Text.Json.JsonSerializer.Serialize(new
        {
            profile.Version, profile.Alien, VersionId = id, ClientVersion = profile.Alien ? LauncherProfile.ClientVersion : null,
            ClientSha256 = profile.Alien ? LauncherProfile.ClientSha256 : null, JavaMajor = major, VerifiedUtc = DateTimeOffset.UtcNow
        }), ct);
        progress?.Report(new("Instalação verificada. Java " + major + ".", 100));
        return new(runtime, id, java, directory, major);
    }
    public static async Task<Process> BuildAsync(InstalledProfile installed, MSession session, int ram, CancellationToken ct = default)
    {
        ArgumentNullException.ThrowIfNull(session);
        if (string.IsNullOrWhiteSpace(session.AccessToken) || string.IsNullOrWhiteSpace(session.Username)) throw new InvalidOperationException("É necessário autenticar sua conta Minecraft Java.");
        await RuntimePolicy.CheckAsync(installed.JavaPath, installed.JavaMajor, ct);
        return await installed.Runtime.BuildProcessAsync(installed.VersionId, new MLaunchOption
        {
            Session = session, MaximumRamMb = Math.Clamp(ram, 1024, 16384), JavaPath = installed.JavaPath,
            ScreenWidth = 1280, ScreenHeight = 720, GameLauncherName = "AlienLauncher", GameLauncherVersion = "0.2.0-alpha.1"
        }, ct);
    }
}
