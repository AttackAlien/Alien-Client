using System.Text.RegularExpressions;
using System.Reflection;
using System.Text.Json;
using System.Text.Json.Serialization;
namespace AlienLauncher.Core;

public sealed record LauncherProfile(string Version, bool Alien)
{
    public static readonly string LauncherVersion = typeof(LauncherProfile).Assembly
        .GetCustomAttribute<AssemblyInformationalVersionAttribute>()!.InformationalVersion.Split('+')[0];
    public const string ClientVersion = "0.6.0-alpha.2";
    public const string MinecraftVersion = "1.8.9";
    public const string ForgeBuild = "11.15.1.2318";
    public const string JarName = "AlienClient-Unified-1.8.9.jar";
    public const string ClientSource = "32829c561b8dc53719a71b7a6621c58cb5a2e8f5";
    public const string ClientRun = "40136496598";
    public const string ClientSha256 = "17a1627a25b66423c1cf60bb745ca88b4888c5a376f93cb9cc736ae2e888c6fc";
    public string Label => Alien ? $"Alien {ClientVersion} / Forge {MinecraftVersion}" : $"Minecraft {Version} / Vanilla";
    public void Validate()
    {
        if (Version is null || !Regex.IsMatch(Version, @"\A[0-9]{1,3}\.[0-9]{1,3}(\.[0-9]{1,3})?\z", RegexOptions.CultureInvariant) || (Alien && Version != MinecraftVersion))
            throw new InvalidDataException("Escolha uma versão oficial válida; Alien requer Minecraft 1.8.9.");
    }
    public string DirectoryAt(string root)
    {
        Validate();
        return Path.Combine(Path.GetFullPath(root), "instances", Alien ? "unified-1.8.9" : "vanilla-" + Version);
    }
    public override string ToString() => Label;
}

public sealed class LauncherPreferences
{
    private const int MaximumBytes = 16384;
    [JsonIgnore] public string RecoveryMessage { get; private set; } = "";
    public int MemoryMb { get; set; } = 4096;
    public string Java8Path { get; set; } = "";
    public string Version { get; set; } = LauncherProfile.MinecraftVersion;
    public bool Alien { get; set; } = true;
    public void Normalize()
    {
        MemoryMb = Math.Clamp(MemoryMb, 1024, 16384);
        if (Java8Path is null || Java8Path.Length > 4096) Java8Path = "";
        try { new LauncherProfile(Version, Alien).Validate(); }
        catch { Version = LauncherProfile.MinecraftVersion; Alien = true; }
    }
    public static LauncherPreferences Load(string root)
    {
        var file = Path.Combine(root, "settings.json");
        if (!File.Exists(file)) return new();
        try
        {
            using var input = File.OpenRead(file);
            if (input.Length > MaximumBytes) throw new InvalidDataException("Configuração maior que 16 KiB.");
            using var buffer = new MemoryStream();
            var block = new byte[4096]; int count;
            while ((count = input.Read(block, 0, block.Length)) > 0)
            {
                if (buffer.Length + count > MaximumBytes) throw new InvalidDataException("Configuração maior que 16 KiB.");
                buffer.Write(block, 0, count);
            }
            var value = JsonSerializer.Deserialize<LauncherPreferences>(buffer.ToArray()) ?? new();
            value.Normalize(); return value;
        }
        catch (Exception e) when (e is IOException or InvalidDataException or JsonException or UnauthorizedAccessException)
        {
            var defaults = new LauncherPreferences { RecoveryMessage = "Configuração inválida; os valores padrão foram recuperados." };
            try
            {
                if (File.Exists(file))
                {
                    string backup = Path.Combine(root, "settings.invalid-" + DateTime.UtcNow.ToString("yyyyMMdd-HHmmss") + "-" + Guid.NewGuid().ToString("N") + ".json");
                    File.Move(file, backup);
                    defaults.RecoveryMessage += " O arquivo original foi preservado em " + Path.GetFileName(backup) + ".";
                }
            }
            catch (Exception backupError) when (backupError is IOException or UnauthorizedAccessException)
            { defaults.RecoveryMessage += " Não foi possível mover o arquivo original; verifique as permissões."; }
            return defaults;
        }
    }
    public void Save(string root)
    {
        Normalize(); Directory.CreateDirectory(root);
        string file = Path.Combine(root, "settings.json"), temp = file + "." + Guid.NewGuid().ToString("N") + ".tmp";
        byte[] bytes = JsonSerializer.SerializeToUtf8Bytes(this);
        if (bytes.Length > MaximumBytes) throw new InvalidDataException("Configuração maior que 16 KiB; o arquivo anterior foi preservado.");
        try { File.WriteAllBytes(temp, bytes); File.Move(temp, file, true); }
        finally { if (File.Exists(temp)) File.Delete(temp); }
    }
}
