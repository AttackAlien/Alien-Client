using System.IO.Compression;
using System.Text;
using AlienLauncher.Core;
using Xunit;
namespace AlienLauncher.Tests;

public sealed class LauncherTests : IDisposable
{
    private readonly string root = Path.Combine(Path.GetTempPath(), "alien-launcher-test-" + Guid.NewGuid().ToString("N"));
    private static string Payload => Environment.GetEnvironmentVariable("ALIEN_TEST_PAYLOAD") ?? throw new InvalidOperationException("Set ALIEN_TEST_PAYLOAD to the verified CI payload.");
    public LauncherTests() => Directory.CreateDirectory(root);
    public void Dispose() => Directory.Delete(root, true);
    [Theory]
    [InlineData("../1.8.9")][InlineData("1.8.9/../../")][InlineData("latest")][InlineData("26w1a")][InlineData("1.8.9;anything")]
    public void InvalidVersionNeverCreatesAProfile(string id) => Assert.Throws<InvalidDataException>(() => new LauncherProfile(id, false).DirectoryAt(root));
    [Fact] public void AlienCannotBeInjectedIntoModernMinecraft() => Assert.Throws<InvalidDataException>(() => new LauncherProfile("1.21.1", true).Validate());
    [Fact] public void ProfilesHaveIndependentSavesModsAndSettings()
    {
        string alien = new LauncherProfile("1.8.9", true).DirectoryAt(root), vanilla = new LauncherProfile("1.8.9", false).DirectoryAt(root), modern = new LauncherProfile("1.21.1", false).DirectoryAt(root);
        Assert.NotEqual(alien, vanilla); Assert.NotEqual(vanilla, modern); Assert.StartsWith(root, modern); Assert.DoesNotContain(".minecraft", alien);
    }
    [Fact] public void SettingsRoundTripAndCorruptSettingsDoNotCrash()
    {
        var settings = new LauncherPreferences { MemoryMb = 98765, Version = "1.21.1", Alien = false, Java8Path = "C:\\Java 8\\bin\\java.exe" };
        settings.Save(root); var read = LauncherPreferences.Load(root); Assert.Equal(16384, read.MemoryMb); Assert.Equal(settings.Java8Path, read.Java8Path); Assert.False(read.Alien);
        File.WriteAllText(Path.Combine(root, "settings.json"), "broken"); Assert.True(LauncherPreferences.Load(root).Alien);
    }
    [Theory][InlineData("java version \"1.8.0_472\"", 8)][InlineData("openjdk version \"21.0.9\" 2026", 21)][InlineData("openjdk version \"17.0.12\"", 17)]
    public void JavaVersionIsParsedFromActualVersionFormat(string text, int expected) => Assert.Equal(expected, RuntimePolicy.ParseMajor(text));
    [Fact] public async Task RealJava8IsRequiredForLegacyRuntime()
    {
        string java = Environment.GetEnvironmentVariable("ALIEN_TEST_JAVA8") ?? throw new InvalidOperationException("Set ALIEN_TEST_JAVA8");
        Assert.Equal(8, await RuntimePolicy.CheckAsync(java, 8)); await Assert.ThrowsAsync<InvalidDataException>(() => RuntimePolicy.CheckAsync(java, 21));
    }
    [Fact] public void DiagnosticsRemoveTokensBearerJsonUrlsAndJwt()
    {
        string secret = "actual-access-token-test-value";
        string input = $"Bearer bearer-secret accessToken={secret} refresh_token=refresh-secret Authorization: hidden-secret https://account.example/?code=login-secret eyJhbGciOiJIUzI1NiJ9.abc123.def456";
        string clean = PrivateDiagnostics.Clean(input, secret);
        foreach (string s in new[] { "bearer-secret", secret, "refresh-secret", "hidden-secret", "login-secret", "eyJhbGciOiJIUzI1NiJ9" }) Assert.DoesNotContain(s, clean);
    }
    [Fact] public async Task RealPinnedUnifiedJarAndVersionAreVerified() => await new ClientPayload(Payload).VerifyAsync();
    [Fact] public async Task TamperingWithBothLocalChecksumAndJarDoesNotReplaceTrustedClient()
    {
        string p = Path.Combine(root, "payload"); Directory.CreateDirectory(p);
        byte[] bytes = await File.ReadAllBytesAsync(Path.Combine(Payload, LauncherProfile.JarName)); bytes[bytes.Length / 2] ^= 1;
        await File.WriteAllBytesAsync(Path.Combine(p, LauncherProfile.JarName), bytes);
        string forged = Convert.ToHexString(System.Security.Cryptography.SHA256.HashData(bytes)).ToLowerInvariant();
        await File.WriteAllTextAsync(Path.Combine(p, "SHA256SUMS.txt"), forged + "  " + LauncherProfile.JarName);
        await Assert.ThrowsAsync<InvalidDataException>(() => new ClientPayload(p).VerifyAsync());
    }
    [Fact] public async Task UpdateBacksUpOldAlienCopiesAndKeepsOtherModsAndUserFiles()
    {
        string instance = new LauncherProfile("1.8.9", true).DirectoryAt(root), mods = Path.Combine(instance, "mods"), versioned = Path.Combine(mods, "1.8.9");
        Directory.CreateDirectory(versioned); Directory.CreateDirectory(Path.Combine(instance, "saves", "user-world"));
        File.WriteAllText(Path.Combine(instance, "saves", "user-world", "data.txt"), "keep");
        OldJar(Path.Combine(mods, "old-alien.jar"), "alienclient"); OldJar(Path.Combine(versioned, "legit-old.jar"), "alienclient"); OldJar(Path.Combine(mods, "other.jar"), "another-mod");
        string[] backups = await new ClientPayload(Payload).DeployAsync(instance);
        Assert.Equal(2, backups.Length); Assert.All(backups, p => Assert.True(File.Exists(p))); Assert.True(File.Exists(Path.Combine(mods, "other.jar")));
        Assert.Equal("keep", File.ReadAllText(Path.Combine(instance, "saves", "user-world", "data.txt")));
        await ClientPayload.VerifyFileAsync(Path.Combine(mods, LauncherProfile.JarName));
        Assert.False(File.Exists(Path.Combine(mods, "old-alien.jar"))); Assert.False(File.Exists(Path.Combine(versioned, "legit-old.jar")));
    }
    [Fact] public async Task CancelledDeploymentLeavesInstalledModUntouched()
    {
        string instance = new LauncherProfile("1.8.9", true).DirectoryAt(root); Directory.CreateDirectory(Path.Combine(instance, "mods"));
        string existing = Path.Combine(instance, "mods", LauncherProfile.JarName); File.Copy(Path.Combine(Payload, LauncherProfile.JarName), existing);
        using var cancellation = new CancellationTokenSource(); cancellation.Cancel();
        await Assert.ThrowsAnyAsync<OperationCanceledException>(() => new ClientPayload(Payload).DeployAsync(instance, cancellation.Token));
        await ClientPayload.VerifyFileAsync(existing); Assert.Empty(Directory.EnumerateFiles(Path.GetDirectoryName(existing)!, "*.tmp"));
    }
    [Theory]
    [InlineData("{\"access_token\":\"hide-this\"}")]
    [InlineData("{\"Token\":\"hide-this\"}")]
    [InlineData("Authorization: Bearer hide-this")]
    public void StructuredCredentialErrorsAreSanitized(string message) => Assert.DoesNotContain("hide-this", PrivateDiagnostics.Clean(message));
    [Fact] public async Task FailedFinalPlacementRestoresPreviouslyBackedUpMods()
    {
        string instance = new LauncherProfile("1.8.9", true).DirectoryAt(root), mods = Path.Combine(instance, "mods");
        Directory.CreateDirectory(Path.Combine(mods, LauncherProfile.JarName));
        OldJar(Path.Combine(mods, "old-alien.jar"), "alienclient");
        var error = await Record.ExceptionAsync(() => new ClientPayload(Payload).DeployAsync(instance));
        Assert.True(error is IOException or UnauthorizedAccessException);
        Assert.True(File.Exists(Path.Combine(mods, "old-alien.jar")));
        Assert.Empty(Directory.EnumerateFiles(mods, "*.tmp"));
    }
    private static void OldJar(string file, string mod)
    {
        using var zip = ZipFile.Open(file, ZipArchiveMode.Create); using var writer = new StreamWriter(zip.CreateEntry("mcmod.info").Open(), Encoding.UTF8); writer.Write("[{\"modid\":\"" + mod + "\",\"version\":\"old\"}]");
    }
}
