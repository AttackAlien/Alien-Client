using System.Diagnostics;
using System.Drawing.Drawing2D;
using System.Security.Cryptography;
using System.Text.RegularExpressions;
using CmlLib.Core;
using CmlLib.Core.Auth;
using CmlLib.Core.Auth.Microsoft;
using CmlLib.Core.Installer.Forge;
using CmlLib.Core.ProcessBuilder;

namespace AlienLauncher;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new LauncherWindow());
    }
}

internal sealed class LauncherWindow : Form
{
    private const string GameVersion = "1.8.9";
    private const string ForgeBuild = "11.15.1.2318";
    private const string JarName = "AlienClient-Unified-1.8.9.jar";
    private static readonly string Instance = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "AlienLauncher", "instances", "unified-1.8.9");
    private static readonly string Payload = Path.Combine(AppContext.BaseDirectory, "payload", JarName);

    private readonly MinecraftLauncher launcher = new(new MinecraftPath(Instance));
    private readonly JELoginHandler login = JELoginHandlerBuilder.BuildDefault();
    private MSession? session;
    private readonly Label account = new();
    private readonly Label status = new();
    private readonly RichTextBox events = new();
    private readonly ProgressBar progress = new();
    private readonly NumericUpDown memory = new();
    private readonly Button signIn = new();
    private readonly Button install = new();
    private readonly Button play = new();
    private readonly ComboBox gameChoice = new();
    private readonly Button refreshVersions = new();
    private readonly Label versionHint = new();
    private bool busy;

    private sealed record GameChoice(string Label, string Version, bool Alien)
    {
        public override string ToString() => Label;
    }

    private GameChoice SelectedGame => gameChoice.SelectedItem as GameChoice
        ?? throw new InvalidOperationException("Escolha uma versão antes de continuar.");

    private static string InstanceFor(GameChoice selected)
    {
        if (selected.Alien) return Instance;
        if (!Regex.IsMatch(selected.Version, @"^\d+\.\d+(\.\d+)?$"))
            throw new InvalidOperationException("Identificador de versão inválido.");
        return Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "AlienLauncher", "instances", "vanilla-" + selected.Version);
    }

    private void AddGameChoice(GameChoice selected)
    {
        foreach (object item in gameChoice.Items)
        {
            if (item is GameChoice found && found.Version == selected.Version && found.Alien == selected.Alien)
                return;
        }
        gameChoice.Items.Add(selected);
    }

    private async Task RefreshVersionsAsync()
    {
        SetStatus("Consultando versões oficiais do Minecraft...");
        var versions = await launcher.GetAllVersionsAsync();
        string? activeVersion = (gameChoice.SelectedItem as GameChoice)?.Version;
        bool activeAlien = (gameChoice.SelectedItem as GameChoice)?.Alien ?? true;
        int discovered = 0;
        foreach (var version in versions)
        {
            string id = version.Name;
            if (!Regex.IsMatch(id, @"^\d+\.\d+(\.\d+)?$")) continue;
            if (!string.Equals(version.GetVersionType().ToString(), "release",
                StringComparison.OrdinalIgnoreCase)) continue;
            AddGameChoice(new GameChoice("Minecraft " + id + " (Vanilla)", id, false));
            discovered++;
        }
        foreach (object item in gameChoice.Items)
        {
            if (item is GameChoice c && c.Version == activeVersion && c.Alien == activeAlien)
            {
                gameChoice.SelectedItem = item;
                break;
            }
        }
        SetStatus("Versões oficiais carregadas: " + discovered +
            ". Alien Client permanece exclusivo da versão 1.8.9.");
    }

    public LauncherWindow()
    {
        Text = "Alien Launcher | 0.4.0 alpha";
        StartPosition = FormStartPosition.CenterScreen;
        MinimumSize = new Size(840, 570);
        ClientSize = new Size(920, 590);
        BackColor = Color.FromArgb(11, 14, 25);
        ForeColor = Color.FromArgb(239, 243, 252);
        Font = new Font("Segoe UI", 10f);
        DoubleBuffered = true;
        FormBorderStyle = FormBorderStyle.FixedSingle;
        MaximizeBox = false;

        Controls.Add(Title("ALIEN", 44, 29, 30, FontStyle.Bold, Color.FromArgb(144, 113, 255)));
        Controls.Add(Title("LAUNCHER", 162, 40, 14, FontStyle.Bold, Color.FromArgb(220, 220, 244)));
        Controls.Add(Title("MINECRAFT 1.8.9  /  FORGE 11.15.1.2318", 46, 87, 11,
            FontStyle.Regular, Color.FromArgb(143, 157, 185)));
        Controls.Add(Title("CLIENTE UNIFICADO 0.4", 46, 154, 23, FontStyle.Bold,
            Color.FromArgb(248, 249, 254)));
        Controls.Add(Title("Interface premium, Combat Studio, cosméticos, skybox e Performance Engine V2.",
            46, 194, 10, FontStyle.Regular, Color.FromArgb(175, 182, 203)));

        Controls.Add(Title("VERSÃO DO JOGO", 46, 218, 10, FontStyle.Bold,
            Color.FromArgb(161, 170, 197)));
        gameChoice.Location = new Point(180, 213);
        gameChoice.Size = new Size(466, 29);
        gameChoice.DropDownStyle = ComboBoxStyle.DropDownList;
        gameChoice.DropDownHeight = 360;
        gameChoice.BackColor = Color.FromArgb(27, 32, 52);
        gameChoice.ForeColor = Color.White;
        AddGameChoice(new GameChoice("Alien Client 0.4 (Forge 1.8.9)", GameVersion, true));
        foreach (string v in new[] { "1.8.9", "1.12.2", "1.16.5", "1.20.1", "1.21.1" })
            AddGameChoice(new GameChoice("Minecraft " + v + " (Vanilla)", v, false));
        gameChoice.SelectedIndex = 0;
        gameChoice.SelectedIndexChanged += (_, _) =>
        {
            GameChoice target = SelectedGame;
            play.Text = target.Alien ? "JOGAR ALIEN" : "JOGAR MINECRAFT";
            SetStatus(target.Alien
                ? "Alien Client 0.4 + Forge 1.8.9 — recursos e combate disponíveis."
                : "Minecraft " + target.Version + " Vanilla — sem os módulos do Alien.");
        };
        Controls.Add(gameChoice);

        refreshVersions.Text = "ATUALIZAR LISTA";
        refreshVersions.Location = new Point(668, 212);
        refreshVersions.Size = new Size(207, 29);
        refreshVersions.FlatStyle = FlatStyle.Flat;
        refreshVersions.FlatAppearance.BorderSize = 0;
        refreshVersions.BackColor = Color.FromArgb(48, 51, 78);
        refreshVersions.ForeColor = Color.White;
        refreshVersions.Cursor = Cursors.Hand;
        refreshVersions.Click += async (_, _) => await RunTaskAsync(RefreshVersionsAsync);
        Controls.Add(refreshVersions);

        var container = new Panel
        {
            Location = new Point(42, 244), Size = new Size(836, 176),
            BackColor = Color.FromArgb(23, 28, 45)
        };
        Controls.Add(container);

        container.Controls.Add(Title("CONTA MINECRAFT", 21, 15, 10, FontStyle.Bold,
            Color.FromArgb(162, 169, 196)));
        account.Text = "Ainda não conectado";
        account.Location = new Point(21, 42);
        account.AutoSize = true;
        account.ForeColor = Color.White;
        account.Font = new Font("Segoe UI", 12f, FontStyle.Bold);
        container.Controls.Add(account);

        ConfigureButton(signIn, "ENTRAR COM MICROSOFT", 21, 97, 248,
            Color.FromArgb(44, 67, 121));
        ConfigureButton(install, "INSTALAR / ATUALIZAR", 286, 97, 243,
            Color.FromArgb(53, 57, 84));
        ConfigureButton(play, "JOGAR ALIEN", 546, 97, 268,
            Color.FromArgb(104, 66, 200));
        container.Controls.Add(signIn);
        container.Controls.Add(install);
        container.Controls.Add(play);
        signIn.Click += async (_, _) => await RunTaskAsync(Authenticate);
        install.Click += async (_, _) => await RunTaskAsync(InstallAsync);
        play.Click += async (_, _) => await RunTaskAsync(PlayAsync);

        Controls.Add(Title("MEMÓRIA RAM", 47, 442, 10, FontStyle.Bold,
            Color.FromArgb(161, 170, 197)));
        memory.Location = new Point(172, 438);
        memory.Size = new Size(92, 28);
        memory.Minimum = 1024;
        memory.Maximum = 16384;
        memory.Increment = 512;
        memory.Value = 4096;
        memory.BackColor = Color.FromArgb(23, 28, 45);
        memory.ForeColor = Color.White;
        Controls.Add(memory);
        Controls.Add(Title("MB", 270, 442, 10, FontStyle.Regular,
            Color.FromArgb(161, 170, 197)));

        var folderButton = new Button
        {
            Text = "ABRIR PASTA DE MODS", Location = new Point(570, 435),
            Size = new Size(305, 34), FlatStyle = FlatStyle.Flat,
            BackColor = Color.FromArgb(31, 37, 58), ForeColor = Color.White,
            Cursor = Cursors.Hand
        };
        folderButton.FlatAppearance.BorderSize = 0;
        folderButton.Click += (_, _) =>
        {
            string directory = Path.Combine(InstanceFor(SelectedGame), "mods");
            Directory.CreateDirectory(directory);
            Process.Start(new ProcessStartInfo(directory) { UseShellExecute = true });
        };
        Controls.Add(folderButton);

        status.Location = new Point(46, 490);
        status.Size = new Size(830, 24);
        status.ForeColor = Color.FromArgb(180, 191, 223);
        status.Text = "Pronto para configurar seu Minecraft.";
        Controls.Add(status);

        progress.Location = new Point(45, 520);
        progress.Size = new Size(829, 8);
        progress.Style = ProgressBarStyle.Blocks;
        Controls.Add(progress);

        events.Visible = false; // Avoid logging private authentication data in the UI.
        events.ReadOnly = true;

        Controls.Add(Title("Instalação isolada: não altera sua pasta .minecraft original.",
            45, 543, 9, FontStyle.Regular, Color.FromArgb(135, 145, 178)));
        AddLog("Pacote Alien: " + (File.Exists(Payload) ? "localizado" : "ausente"));
    }

    protected override void OnPaintBackground(PaintEventArgs e)
    {
        base.OnPaintBackground(e);
        using var brush = new LinearGradientBrush(
            new Rectangle(0, 0, Width, 240),
            Color.FromArgb(27, 21, 57), BackColor, 95f);
        e.Graphics.FillRectangle(brush, 0, 0, Width, 240);
        using var pen = new Pen(Color.FromArgb(111, 68, 218), 3);
        e.Graphics.DrawLine(pen, 44, 118, 871, 118);
    }

    private static Label Title(string content, int x, int y, float fontSize,
        FontStyle style, Color color) => new()
    {
        Text = content, Location = new Point(x, y), AutoSize = true,
        Font = new Font("Segoe UI", fontSize, style), ForeColor = color,
        BackColor = Color.Transparent
    };

    private static void ConfigureButton(Button button, string label, int x, int y,
        int width, Color color)
    {
        button.Text = label;
        button.Location = new Point(x, y);
        button.Size = new Size(width, 51);
        button.BackColor = color;
        button.ForeColor = Color.White;
        button.Font = new Font("Segoe UI", 10f, FontStyle.Bold);
        button.FlatStyle = FlatStyle.Flat;
        button.FlatAppearance.BorderSize = 0;
        button.Cursor = Cursors.Hand;
    }

    private async Task RunTaskAsync(Func<Task> action)
    {
        if (busy) return;
        busy = true;
        signIn.Enabled = install.Enabled = play.Enabled = refreshVersions.Enabled = gameChoice.Enabled = false;
        progress.Style = ProgressBarStyle.Marquee;
        try { await action(); }
        catch (Exception ex)
        {
            // Do not display or persist credentials, tokens or raw authentication URLs.
            SetStatus("Falha. Consulte a mensagem e verifique sua conexão / Java.");
            MessageBox.Show(this, "Não foi possível concluir a operação.\n\n" +
                ex.GetType().Name + ": " + SafeMessage(ex.Message),
                "Alien Launcher", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }
        finally
        {
            busy = false;
            signIn.Enabled = install.Enabled = play.Enabled = refreshVersions.Enabled = gameChoice.Enabled = true;
            progress.Style = ProgressBarStyle.Blocks;
            progress.Value = 0;
        }
    }

    private static string SafeMessage(string message)
    {
        // Network exception messages can contain bearer tokens in URLs.
        string clean = Regex.Replace(message, @"https?://\S+", "[link omitido]",
            RegexOptions.IgnoreCase);
        return clean.Length > 240 ? clean[..240] : clean;
    }

    private void AddLog(string text) => SetStatus(text);

    private void SetStatus(string text)
    {
        if (IsDisposed) return;
        if (InvokeRequired) BeginInvoke(() => SetStatus(text));
        else status.Text = text;
    }

    private async Task Authenticate()
    {
        SetStatus("Abrindo login seguro da Microsoft...");
        session = await login.Authenticate();
        account.Text = "Conectado: " + session.Username;
        SetStatus("Conta Microsoft autenticada.");
    }

    private static void VerifyAndCopyAlien()
    {
        if (!File.Exists(Payload))
            throw new FileNotFoundException("Pacote do Alien ausente. Extraia o ZIP completo.");
        string checksums = Path.Combine(AppContext.BaseDirectory, "payload", "SHA256SUMS.txt");
        if (!File.Exists(checksums))
            throw new FileNotFoundException("Checksum do pacote não encontrado.");
        string expected = File.ReadAllText(checksums).Trim();
        if (!Regex.IsMatch(expected, @"^[0-9a-fA-F]{64}  AlienClient-Unified-1\.8\.9\.jar$"))
            throw new InvalidDataException("Manifesto SHA256 inválido.");
        string actual = Convert.ToHexString(SHA256.HashData(File.ReadAllBytes(Payload)));
        if (!string.Equals(actual, expected[..64], StringComparison.OrdinalIgnoreCase))
            throw new InvalidDataException("Assinatura SHA256 do mod não confere.");

        string mods = Path.Combine(Instance, "mods");
        Directory.CreateDirectory(mods);
        string destination = Path.Combine(mods, JarName);
        string staging = destination + ".tmp";
        File.Copy(Payload, staging, true);
        File.Move(staging, destination, true);
    }

    private async Task<string> InstallCoreAsync()
    {
        VerifyAndCopyAlien(); // Only our jar in an isolated instance; user-added mods remain untouched.
        SetStatus("Instalando Forge 1.8.9 e verificando bibliotecas...");
        var forgeInstaller = new ForgeInstaller(launcher);
        string version = await forgeInstaller.Install(GameVersion, ForgeBuild, new ForgeInstallOptions
        {
            InstallerOutput = new Progress<string>(_ => SetStatus("Instalando Forge...")),
            SkipIfAlreadyInstalled = true
        });
        SetStatus("Baixando/verificando arquivos oficiais de Minecraft...");
        await launcher.InstallAsync(version);
        return version;
    }

    private async Task InstallAsync()
    {
        await InstallCoreAsync();
        SetStatus("Alien Client 0.4 instalado. Faça login e clique JOGAR.");
    }

    private async Task PlayAsync()
    {
        if (session == null) await Authenticate();
        if (session == null) throw new InvalidOperationException("Login Microsoft necessário.");
        string version = await InstallCoreAsync();
        SetStatus("Iniciando Minecraft com Alien Client...");
        var process = await launcher.BuildProcessAsync(version, new MLaunchOption
        {
            Session = session,
            MaximumRamMb = (int)memory.Value
        });
        if (!process.Start()) throw new InvalidOperationException("O processo do Minecraft não iniciou.");
        SetStatus("Minecraft iniciado. Boa partida!");
    }
}
