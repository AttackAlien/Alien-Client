using System.IO.Compression;
using System.Security.Cryptography;
using System.Text.Json;
namespace AlienLauncher.Core;

public sealed class ClientPayload(string directory)
{
    public string DirectoryPath { get; } = Path.GetFullPath(directory);
    public async Task VerifyAsync(CancellationToken ct = default)
    {
        string jar = Path.Combine(DirectoryPath, LauncherProfile.JarName);
        if (!File.Exists(jar)) throw new FileNotFoundException("Pacote Alien ausente. Extraia o ZIP completo do launcher.");
        await VerifyFileAsync(jar, ct);
        var checksum = Path.Combine(DirectoryPath, "SHA256SUMS.txt");
        if (!File.Exists(checksum) || (await File.ReadAllTextAsync(checksum, ct)).Trim() != LauncherProfile.ClientSha256 + "  " + LauncherProfile.JarName)
            throw new InvalidDataException("O manifesto de integridade não corresponde ao client validado.");
    }
    public static async Task VerifyFileAsync(string file, CancellationToken ct = default)
    {
        if (new FileInfo(file).Length is <= 0 or > 32 * 1024 * 1024) throw new InvalidDataException("Tamanho inválido do pacote Alien.");
        await using var stream = File.OpenRead(file);
        string hash = Convert.ToHexString(await SHA256.HashDataAsync(stream, ct)).ToLowerInvariant();
        if (hash != LauncherProfile.ClientSha256) throw new InvalidDataException("Integridade do Alien não confere com a compilação validada.");
        stream.Position = 0;
        using var zip = new ZipArchive(stream, ZipArchiveMode.Read, true);
        if (zip.GetEntry("dev/alien/client/AlienClient.class") == null) throw new InvalidDataException("Mod Alien ausente do JAR.");
        using var edition = JsonDocument.Parse(ReadEntry(zip, "META-INF/alienclient/edition.json"));
        if (edition.RootElement.GetProperty("id").GetString() != "unified") throw new InvalidDataException("O pacote não é Unified.");
        using var info = JsonDocument.Parse(ReadEntry(zip, "mcmod.info"));
        bool correct = info.RootElement.EnumerateArray().Any(m => m.GetProperty("modid").GetString() == "alienclient" && m.GetProperty("version").GetString() == LauncherProfile.ClientVersion);
        if (!correct || zip.Entries.Any(e => e.FullName.Contains("/smoke/", StringComparison.OrdinalIgnoreCase))) throw new InvalidDataException("Versão ou conteúdo incorreto do pacote Alien.");
    }
    private static string ReadEntry(ZipArchive zip, string name)
    {
        var entry = zip.GetEntry(name) ?? throw new InvalidDataException("Metadado ausente: " + name);
        if (entry.Length > 65536) throw new InvalidDataException("Metadado inválido no JAR.");
        using var reader = new StreamReader(entry.Open()); return reader.ReadToEnd();
    }
    public async Task<string[]> DeployAsync(string instance, CancellationToken ct = default)
    {
        await VerifyAsync(ct);
        string mods = Path.Combine(instance, "mods"); Directory.CreateDirectory(mods);
        string destination = Path.Combine(mods, LauncherProfile.JarName), staging = destination + "." + Guid.NewGuid().ToString("N") + ".tmp";
        var moved = new List<(string Source, string Backup)>();
        try
        {
            await using (var input = File.OpenRead(Path.Combine(DirectoryPath, LauncherProfile.JarName)))
            await using (var output = File.Create(staging)) await input.CopyToAsync(output, ct);
            await VerifyFileAsync(staging, ct); ct.ThrowIfCancellationRequested();
            var candidates = Directory.EnumerateFiles(mods, "*.jar").Concat(Directory.Exists(Path.Combine(mods, "1.8.9")) ? Directory.EnumerateFiles(Path.Combine(mods, "1.8.9"), "*.jar") : []);
            foreach (var old in candidates.Where(p => p == destination || IsAlien(p)))
            {
                string backup = Path.Combine(instance, "alien-backups", DateTime.UtcNow.ToString("yyyyMMdd-HHmmss") + "-" + Guid.NewGuid().ToString("N"), Path.GetRelativePath(mods, old));
                Directory.CreateDirectory(Path.GetDirectoryName(backup)!); File.Move(old, backup); moved.Add((old, backup));
            }
            ct.ThrowIfCancellationRequested(); File.Move(staging, destination); return moved.Select(m => m.Backup).ToArray();
        }
        catch
        {
            foreach (var old in moved.AsEnumerable().Reverse()) if (!File.Exists(old.Source)) File.Move(old.Backup, old.Source);
            throw;
        }
        finally { if (File.Exists(staging)) File.Delete(staging); }
    }
    private static bool IsAlien(string path)
    {
        try
        {
            using var zip = ZipFile.OpenRead(path);
            if (zip.GetEntry("META-INF/alienclient/edition.json") != null) return true;
            if (zip.GetEntry("mcmod.info") == null) return false;
            using var doc = JsonDocument.Parse(ReadEntry(zip, "mcmod.info"));
            return doc.RootElement.ValueKind == JsonValueKind.Array && doc.RootElement.EnumerateArray().Any(m => m.TryGetProperty("modid", out var id) && id.GetString() == "alienclient");
        }
        catch (Exception e) when (e is IOException or InvalidDataException or JsonException or InvalidOperationException) { return false; }
    }
}
