using System.Diagnostics;
using System.Drawing.Drawing2D;
using System.Security.Cryptography;
using System.Text.RegularExpressions;
using AlienLauncher.Core;
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
    private readonly string root;
    private readonly string Payload = Path.Combine(AppContext.BaseDirectory, "payload");
    private readonly MinecraftLauncher launcher;
    private readonly JELoginHandler login;
    private readonly LauncherPreferences preferences;
    private readonly Button cancel = new(), signOut = new(), diagnostics = new(), chooseJava = new(), resetJava = new();
    private readonly TextBox javaPath = new();
    private readonly Queue<string> safeLog = new();
    private readonly object logLock = new();
    private CancellationTokenSource? operation;
    private Process? gameProcess;
    private bool alienLoaded;
    private MSession? session;
    private readonly Label account = new();
    private readonly Label status = new();
    private readonly Label heroTitle = new();
    private readonly Label heroDescription = new();
    
    private readonly ProgressBar progress = new();
    private readonly NumericUpDown memory = new();
    private readonly Button signIn = new();
    private readonly Button install = new();
    private readonly Button play = new();
    private readonly Button folderButton = new();
    private readonly ComboBox gameChoice = new();
    private readonly Button refreshVersions = new();
    private bool busy;

    private sealed record GameChoice(string Label, string Version, bool Alien)
    {
        public override string ToString() => Label;
    }

    private GameChoice SelectedGame => gameChoice.SelectedItem as GameChoice
        ?? throw new InvalidOperationException("Escolha uma versão antes de continuar.");

    private string InstanceFor(GameChoice selected) => new LauncherProfile(selected.Version, selected.Alien).DirectoryAt(root);

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
        var versions = await launcher.GetAllVersionsAsync(operation!.Token);
        string? activeVersion = (gameChoice.SelectedItem as GameChoice)?.Version;
        bool activeAlien = (gameChoice.SelectedItem as GameChoice)?.Alien ?? true;
        int discovered = 0;
        foreach (var version in versions)
        {
            string id = version.Name;
            if (!Regex.IsMatch(id, @"^\d+\.\d+(\.\d+)?$")) continue;
            // Only official numeric release IDs; snapshots and prereleases are excluded.
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

    public LauncherWindow(string? dataRoot = null)
    {
        root = dataRoot ?? Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "AlienLauncher");
        Directory.CreateDirectory(root);
        preferences = LauncherPreferences.Load(root);
        launcher = new MinecraftLauncher(new MinecraftPath(new LauncherProfile(GameVersion, true).DirectoryAt(root)));
        login = new JELoginHandlerBuilder().WithAccountManager(Path.Combine(root, "accounts.json")).Build();
        Text = "Alien Launcher 0.2 alpha / Client " + LauncherProfile.ClientVersion;
        StartPosition = FormStartPosition.CenterScreen;
        MinimumSize = new Size(936, 699);
        ClientSize = new Size(920, 660);
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
        var title = Title("CLIENTE UNIFICADO 0.6", 46, 154, 23, FontStyle.Bold,
            Color.FromArgb(248, 249, 254));
        heroTitle.Text = title.Text;
        heroTitle.Location = title.Location;
        heroTitle.AutoSize = true;
        heroTitle.Font = title.Font;
        heroTitle.ForeColor = title.ForeColor;
        heroTitle.BackColor = Color.Transparent;
        Controls.Add(heroTitle);

        var tagline = Title("HUD Ultimate, Scene Studio, cosméticos e ferramentas Unified.",
            46, 194, 10, FontStyle.Regular, Color.FromArgb(175, 182, 203));
        heroDescription.Text = tagline.Text;
        heroDescription.Location = tagline.Location;
        heroDescription.AutoSize = true;
        heroDescription.Font = tagline.Font;
        heroDescription.ForeColor = tagline.ForeColor;
        heroDescription.BackColor = Color.Transparent;
        Controls.Add(heroDescription);

        Controls.Add(Title("VERSÃO DO JOGO", 46, 218, 10, FontStyle.Bold,
            Color.FromArgb(161, 170, 197)));
        gameChoice.Location = new Point(180, 213);
        gameChoice.Size = new Size(466, 29);
        gameChoice.DropDownStyle = ComboBoxStyle.DropDownList;
        gameChoice.DropDownHeight = 360;
        gameChoice.BackColor = Color.FromArgb(27, 32, 52);
        gameChoice.ForeColor = Color.White;
        AddGameChoice(new GameChoice("Alien Client 0.6.0-alpha.2 (Forge 1.8.9)", GameVersion, true));
        foreach (string v in new[] { "1.8.9", "1.12.2", "1.16.5", "1.20.1", "1.21.1" })
            AddGameChoice(new GameChoice("Minecraft " + v + " (Vanilla)", v, false));
        gameChoice.SelectedIndex = 0;
        gameChoice.SelectedIndexChanged += (_, _) =>
        {
            GameChoice target = SelectedGame;
            play.Text = target.Alien ? "JOGAR ALIEN" : "JOGAR MINECRAFT";
            heroTitle.Text = target.Alien ? "CLIENTE UNIFICADO 0.6" : "MINECRAFT " + target.Version;
            heroDescription.Text = target.Alien
                ? "HUD Ultimate, Scene Studio, cosméticos e ferramentas Unified."
                : "Versão oficial Vanilla. O Alien Client continua disponível apenas no Forge 1.8.9.";
            folderButton.Text = target.Alien ? "ABRIR PASTA DE MODS" : "ABRIR PASTA DO PERFIL";
            SetStatus(target.Alien
                ? "Alien Client 0.6.0-alpha.2 + Forge 1.8.9 — recursos e combate disponíveis."
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
        ConfigureButton(install, "INSTALAR / REPARAR", 286, 97, 243,
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
        memory.Value = preferences.MemoryMb;
        memory.BackColor = Color.FromArgb(23, 28, 45);
        memory.ForeColor = Color.White;
        Controls.Add(memory);
        Controls.Add(Title("MB", 270, 442, 10, FontStyle.Regular,
            Color.FromArgb(161, 170, 197)));

        folderButton.Text = "ABRIR PASTA DE MODS";
        folderButton.Location = new Point(570, 435);
        folderButton.Size = new Size(305, 34);
        folderButton.FlatStyle = FlatStyle.Flat;
        folderButton.BackColor = Color.FromArgb(31, 37, 58);
        folderButton.ForeColor = Color.White;
        folderButton.Cursor = Cursors.Hand;
        folderButton.FlatAppearance.BorderSize = 0;
        folderButton.Click += (_, _) =>
        {
            var selected = SelectedGame;
            string directory = selected.Alien
                ? Path.Combine(InstanceFor(selected), "mods") : InstanceFor(selected);
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

        Controls.Add(Title("JAVA 8", 47, 483, 10, FontStyle.Bold, Color.FromArgb(161, 170, 197)));
        javaPath.SetBounds(172, 478, 389, 27); javaPath.ReadOnly = true;
        javaPath.Text = preferences.Java8Path; javaPath.PlaceholderText = "Automático / Java gerenciado"; Controls.Add(javaPath);
        foreach (var button in new[] { chooseJava, resetJava, cancel, signOut, diagnostics })
        { button.FlatStyle = FlatStyle.Flat; button.BackColor = Color.FromArgb(36, 40, 64); button.ForeColor = Color.White; Controls.Add(button); }
        chooseJava.Text = "SELECIONAR JAVA 8"; chooseJava.SetBounds(579, 477, 151, 29);
        chooseJava.Click += async (_, _) => { using var picker = new OpenFileDialog { Filter = "Java (java.exe)|java.exe", CheckFileExists = true }; if (picker.ShowDialog(this) == DialogResult.OK) await RunTaskAsync(async () => { await RuntimePolicy.CheckAsync(picker.FileName, 8, operation!.Token); javaPath.Text = picker.FileName; SavePreferences(); SetStatus("Java 8 selecionado e verificado."); }); };
        resetJava.Text = "AUTOMÁTICO"; resetJava.SetBounds(741, 477, 134, 29); resetJava.Click += (_, _) => { javaPath.Clear(); SavePreferences(); SetStatus("Java gerenciado será verificado na instalação."); };
        status.SetBounds(46, 526, 830, 30); progress.SetBounds(45, 565, 829, 8);
        cancel.Text = "CANCELAR"; cancel.SetBounds(45, 588, 126, 29); cancel.Enabled = false; cancel.Click += (_, _) => operation?.Cancel();
        signOut.Text = "SAIR DA CONTA"; signOut.SetBounds(182, 588, 142, 29); signOut.Click += async (_, _) => await RunTaskAsync(async () => { await login.Signout(operation!.Token); session = null; account.Text = "Ainda não conectado"; SetStatus("Conta removida do launcher."); });
        diagnostics.Text = "VER DIAGNÓSTICO"; diagnostics.SetBounds(570, 588, 305, 29); diagnostics.Click += (_, _) => ShowDiagnostics();
        Controls.Add(Title("Instalação e contas isoladas: sua pasta .minecraft original é preservada.", 45, 633, 9, FontStyle.Regular, Color.FromArgb(135, 145, 178)));
        signIn.Name = "signIn"; install.Name = "install"; play.Name = "play"; gameChoice.Name = "profile";
        memory.Name = "memory"; javaPath.Name = "java8"; diagnostics.Name = "diagnostics"; cancel.Name = "cancel"; account.Name = "account";
        var saved = gameChoice.Items.Cast<GameChoice>().FirstOrDefault(c => c.Version == preferences.Version && c.Alien == preferences.Alien);
        if (saved != null) gameChoice.SelectedItem = saved;
        AddLog("Launcher 0.2 / pacote Alien " + LauncherProfile.ClientVersion + ": " + (File.Exists(Path.Combine(Payload, JarName)) ? "localizado" : "extraia o ZIP completo"));
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

    private void SavePreferences()
    {
        preferences.MemoryMb = (int)memory.Value; preferences.Java8Path = javaPath.Text;
        preferences.Version = SelectedGame.Version; preferences.Alien = SelectedGame.Alien; preferences.Save(root);
    }
    protected override void OnFormClosing(FormClosingEventArgs e)
    {
        operation?.Cancel(); SavePreferences(); base.OnFormClosing(e);
    }
    private void EnableActions()
    {
        bool running = gameProcess is { HasExited: false };
        signIn.Enabled = install.Enabled = play.Enabled = refreshVersions.Enabled = gameChoice.Enabled = !busy && !running;
        chooseJava.Enabled = resetJava.Enabled = signOut.Enabled = memory.Enabled = !busy && !running;
        cancel.Enabled = busy;
    }
    private async Task RunTaskAsync(Func<Task> action)
    {
        if (busy) return; busy = true; operation = new CancellationTokenSource(); EnableActions(); progress.Style = ProgressBarStyle.Marquee;
        try { await action(); SavePreferences(); }
        catch (OperationCanceledException) { SetStatus("Operação cancelada; você pode tentar novamente."); }
        catch (Exception ex)
        {
            string clean = PrivateDiagnostics.Clean(ex.Message, session?.AccessToken);
            SetStatus("Falha: " + clean);
            MessageBox.Show(this, clean + "\n\nConsulte VER DIAGNÓSTICO. Se o login não abrir, confira o Microsoft Edge WebView2 Runtime.", "Alien Launcher", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }
        finally { operation.Dispose(); operation = null; busy = false; EnableActions(); progress.Style = ProgressBarStyle.Blocks; progress.Value = 0; }
    }
    private void AddLog(string text)
    {
        string line = DateTime.Now.ToString("HH:mm:ss") + " " + PrivateDiagnostics.Clean(text, session?.AccessToken);
        lock (logLock)
        {
            safeLog.Enqueue(line); while (safeLog.Count > 500) safeLog.Dequeue();
            string directory = Path.Combine(root, "logs"); Directory.CreateDirectory(directory);
            string file = Path.Combine(directory, "launcher-" + DateTime.Now.ToString("yyyyMMdd") + ".log");
            if (!File.Exists(file) || new FileInfo(file).Length < 2 * 1024 * 1024) File.AppendAllText(file, line + Environment.NewLine);
        }
    }
    private void SetStatus(string text)
    {
        if (IsDisposed) return;
        if (InvokeRequired) { BeginInvoke(() => SetStatus(text)); return; }
        status.Text = PrivateDiagnostics.Clean(text, session?.AccessToken); AddLog(text);
    }
    private void ShowDiagnostics()
    {
        var dialog = new Form { Text = "Alien / Diagnóstico", Size = new Size(820, 430), StartPosition = FormStartPosition.CenterParent, BackColor = BackColor, ForeColor = ForeColor };
        var view = new RichTextBox { Dock = DockStyle.Fill, ReadOnly = true, BackColor = BackColor, ForeColor = ForeColor, WordWrap = false };
        lock (logLock) view.Text = string.Join(Environment.NewLine, safeLog);
        var copy = new Button { Text = "COPIAR DIAGNÓSTICO", Dock = DockStyle.Bottom, Height = 34 }; copy.Click += (_, _) => Clipboard.SetText(view.Text);
        dialog.Controls.Add(view); dialog.Controls.Add(copy); dialog.Show(this);
    }
    private async Task Authenticate()
    {
        SetStatus("Abrindo login Microsoft / verificação de Minecraft Java...");
        session = await login.Authenticate(operation!.Token);
        account.Text = "Conectado: " + session.Username; SetStatus("Conta Minecraft Java autenticada.");
    }
    private async Task<InstalledProfile> InstallCoreAsync(GameChoice selected, bool repair = false)
    {
        var updates = new Progress<LauncherProgress>(p =>
        {
            if (IsDisposed) return; SetStatus(p.Message);
            if (p.Percent.HasValue) { progress.Style = ProgressBarStyle.Blocks; progress.Value = p.Percent.Value; }
            else progress.Style = ProgressBarStyle.Marquee;
        });
        return await new InstanceInstaller(root, new ClientPayload(Payload)).InstallAsync(new LauncherProfile(selected.Version, selected.Alien), javaPath.Text, updates, repair, operation!.Token);
    }
    private async Task InstallAsync()
    {
        var selected = SelectedGame; await InstallCoreAsync(selected, true);
        SetStatus(selected.Alien ? "Alien " + LauncherProfile.ClientVersion + " verificado. Clique em JOGAR ALIEN." : "Minecraft " + selected.Version + " verificado.");
    }
    private async Task PlayAsync()
    {
        var selected = SelectedGame;
        // Refresh the official session each launch; never accept a hand-written/offline UI identity.
        await Authenticate();
        if (session == null) throw new InvalidOperationException("Login Microsoft necessário.");
        var installed = await InstallCoreAsync(selected);
        var process = await InstanceInstaller.BuildAsync(installed, session, (int)memory.Value, operation!.Token);
        process.StartInfo.UseShellExecute = false; process.StartInfo.RedirectStandardOutput = true; process.StartInfo.RedirectStandardError = true;
        alienLoaded = false; string? token = session.AccessToken;
        void Line(string? line)
        {
            if (line == null) return; AddLog(PrivateDiagnostics.Clean(line, token));
            if (selected.Alien && line.Contains("Alien Client " + LauncherProfile.ClientVersion + " initialized", StringComparison.Ordinal))
            { alienLoaded = true; SetStatus("Minecraft em execução / Alien " + LauncherProfile.ClientVersion + " carregado."); }
        }
        process.OutputDataReceived += (_, e) => Line(e.Data); process.ErrorDataReceived += (_, e) => Line(e.Data);
        if (!process.Start()) throw new InvalidOperationException("O processo do Minecraft não iniciou.");
        gameProcess = process; process.BeginOutputReadLine(); process.BeginErrorReadLine();
        SetStatus("Processo do Minecraft aberto; aguardando carregamento...");
        _ = ObserveExitAsync(process, selected);
    }
    private async Task ObserveExitAsync(Process process, GameChoice selected)
    {
        await process.WaitForExitAsync(); int exit = process.ExitCode; AddLog("Minecraft encerrou / código " + exit);
        if (!IsDisposed) BeginInvoke(() => { SetStatus(exit == 0 ? (selected.Alien && !alienLoaded ? "Minecraft encerrou sem confirmar o carregamento do Alien. Consulte o diagnóstico." : "Minecraft encerrado.") : "Minecraft encerrou com código " + exit + ". Consulte o diagnóstico."); EnableActions(); });
        process.Dispose(); gameProcess = null;
    }
}
