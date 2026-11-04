using System.Diagnostics;
using System.Text.RegularExpressions;
namespace AlienLauncher.Core;

public static class RuntimePolicy
{
    public static int ParseMajor(string text)
    {
        var match = Regex.Match(text, "(?:openjdk|java) version \\\"(?:1\\.)?(\\d+)", RegexOptions.IgnoreCase);
        if (!match.Success || !int.TryParse(match.Groups[1].Value, out int major)) throw new InvalidDataException("Não foi possível identificar a versão do Java selecionado.");
        return major;
    }
    public static async Task<int> CheckAsync(string java, int required, CancellationToken ct = default)
    {
        if (!File.Exists(java)) throw new FileNotFoundException("Java não encontrado. Use a seleção de Java 8 ou repare a instalação.");
        using var timeout = CancellationTokenSource.CreateLinkedTokenSource(ct); timeout.CancelAfter(TimeSpan.FromSeconds(12));
        using var process = new Process { StartInfo = new ProcessStartInfo(java) { RedirectStandardError = true, RedirectStandardOutput = true, UseShellExecute = false, CreateNoWindow = true } };
        process.StartInfo.ArgumentList.Add("-version"); process.Start();
        try
        {
            var stdout = process.StandardOutput.ReadToEndAsync(timeout.Token); var stderr = process.StandardError.ReadToEndAsync(timeout.Token);
            await process.WaitForExitAsync(timeout.Token);
            return ValidateVersionResult(process.ExitCode, await stdout + "\n" + await stderr, required);
        }
        finally { if (!process.HasExited) process.Kill(true); }
    }
    public static int ValidateVersionResult(int exitCode, string output, int required)
    {
        if (exitCode != 0) throw new InvalidDataException("A consulta ao Java falhou com código " + exitCode + ". Selecione outro Java ou repare a instalação.");
        int major = ParseMajor(output);
        if (major != required) throw new InvalidDataException($"Este perfil requer Java {required}; o executável selecionado é Java {major}.");
        return major;
    }
}

public static class PrivateDiagnostics
{
    public static string Clean(string text, string? accessToken = null)
    {
        if (!string.IsNullOrEmpty(accessToken)) text = text.Replace(accessToken, "[token omitido]", StringComparison.Ordinal);
        text = Regex.Replace(text, @"(?i)(bearer\s+)[^\s\""']+", "$1[token omitido]");
        text = Regex.Replace(text, @"(?i)((?:access_?token|refresh_?token|authorization|client_secret|identity_?token|token|device_code|user_code)[\""']?\s*[:=]\s*[\""']?)[^\s\""',}]+", "$1[token omitido]");
        text = Regex.Replace(text, @"https?://\S+", "[link omitido]", RegexOptions.IgnoreCase);
        text = Regex.Replace(text, @"eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+", "[token omitido]");
        return text.Length > 2048 ? text[..2048] : text;
    }
}
