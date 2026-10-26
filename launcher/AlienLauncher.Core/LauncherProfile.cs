using System.Text.RegularExpressions;
namespace AlienLauncher.Core;

public sealed record LauncherProfile(string Version, bool Alien)
{
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
        if (!Regex.IsMatch(Version, @"^\d{1,3}\.\d{1,3}(\.\d{1,3})?$", RegexOptions.CultureInvariant) || (Alien && Version != MinecraftVersion))
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
            if (new FileInfo(file).Length > 16384) throw new InvalidDataException();
            var value = System.Text.Json.JsonSerializer.Deserialize<LauncherPreferences>(File.ReadAllText(file)) ?? new();
            value.Normalize(); return value;
        }
        catch (Exception e) when (e is IOException or System.Text.Json.JsonException)
        { return new(); }
    }
    public void Save(string root)
    {
        Normalize(); Directory.CreateDirectory(root);
        string file = Path.Combine(root, "settings.json"), temp = file + "." + Guid.NewGuid().ToString("N") + ".tmp";
        try { File.WriteAllText(temp, System.Text.Json.JsonSerializer.Serialize(this)); File.Move(temp, file, true); }
        finally { if (File.Exists(temp)) File.Delete(temp); }
    }
}
