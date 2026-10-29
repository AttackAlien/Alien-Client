using System.Diagnostics;
using System.Drawing.Drawing2D;
using System.Runtime.InteropServices;
using System.Drawing.Text;
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
    private readonly StudioButton cancel = new(), signOut = new(), diagnostics = new(), chooseJava = new(), resetJava = new();
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
    private readonly Label engineLine = new();
    
    private readonly StudioProgress progress = new();
    private readonly MemoryPicker memory = new();
    private readonly StudioButton signIn = new();
    private readonly StudioButton install = new();
    private readonly StudioButton play = new();
    private readonly StudioButton folderButton = new();
    private readonly ProfilePicker gameChoice = new();
    private readonly StudioButton home = new(), settings = new(), quickSettings = new();
    private readonly List<Control> homeControls = [], settingsControls = [];
    private bool settingsPage;
    private Image? scene;
    [DllImport("user32.dll")] private static extern bool ReleaseCapture();
    [DllImport("user32.dll")] private static extern IntPtr SendMessage(IntPtr hwnd, int message, IntPtr wparam, IntPtr lparam);
    private readonly StudioButton refreshVersions = new();
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
            if (!Regex.IsMatch(id, @"\A[0-9]{1,3}\.[0-9]{1,3}(\.[0-9]{1,3})?\z")) continue;
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
        Directory.CreateDirectory(root); preferences = LauncherPreferences.Load(root);
        launcher = new MinecraftLauncher(new MinecraftPath(new LauncherProfile(GameVersion, true).DirectoryAt(root)));
        login = new JELoginHandlerBuilder().WithAccountManager(Path.Combine(root, "accounts.json")).Build();
        Text = "Alien Launcher 0.3 alpha / Client " + LauncherProfile.ClientVersion;
        StartPosition = FormStartPosition.CenterScreen; ClientSize = new Size(1000, 680);
        FormBorderStyle = FormBorderStyle.None; MaximizeBox = false; AutoScaleMode = AutoScaleMode.Dpi;
        BackColor = Studio.Background; ForeColor = Studio.Text; Font = new Font("Segoe UI", 10f); DoubleBuffered = true;
        using (var resource = typeof(LauncherWindow).Assembly.GetManifestResourceStream("AlienLauncher.Assets.scene.png"))
        { if (resource != null) { using var source = Image.FromStream(resource); scene = new Bitmap(source); } }
        var brand = AddLabel("ALIEN", "brand", 67, 31, 100, 25, 15, Studio.Text, true);
        var brandSub = AddLabel("L A U N C H E R", "launcherTitle", 68, 57, 105, 18, 7, Studio.Muted);
        brand.MouseDown += (_, e) => DragWindow(e); brandSub.MouseDown += (_, e) => DragWindow(e);
        Configure(home, "Jogar", "home", 16, 132, 148, 44); home.Name = "home"; home.AlignLeft = true; home.Click += (_, _) => ShowPage(false);
        Configure(settings, "Configurações", "settings", 16, 184, 148, 44); settings.Name = "settings"; settings.AlignLeft = true; settings.Quiet = true; settings.Click += (_, _) => ShowPage(true);
        Configure(folderButton, "Meus arquivos", "folder", 16, 236, 148, 42); folderButton.AlignLeft = true; folderButton.Quiet = true;
        folderButton.Click += (_, _) => { string directory = SelectedGame.Alien ? Path.Combine(InstanceFor(SelectedGame), "mods") : InstanceFor(SelectedGame); Directory.CreateDirectory(directory); Process.Start(new ProcessStartInfo(directory) { UseShellExecute = true }); };
        Configure(diagnostics, "Diagnóstico", "logs", 16, 284, 148, 42); diagnostics.Name = "diagnostics"; diagnostics.AlignLeft = true; diagnostics.Quiet = true; diagnostics.Click += (_, _) => ShowDiagnostics();
        Configure(signIn, "Conectar conta", "microsoft", 16, 518, 148, 42); signIn.Name = "signIn"; signIn.AccessibleName = "Entrar com Microsoft"; signIn.Font = new Font("Segoe UI", 8.5f); signIn.Click += async (_, _) => await RunTaskAsync(Authenticate);
        account.Name = "account"; account.SetBounds(24, 574, 140, 35); account.Font = new Font("Segoe UI", 8.5f); account.ForeColor = Studio.Muted; account.Text = "Ainda não conectado"; Controls.Add(account);
        var minimize = new StudioButton(); Configure(minimize, "", "minimize", 916, 4, 36, 28); minimize.Quiet = true; minimize.AccessibleName = "Minimizar"; minimize.Click += (_, _) => WindowState = FormWindowState.Minimized;
        var close = new StudioButton(); Configure(close, "", "close", 958, 4, 36, 28); close.Quiet = true; close.AccessibleName = "Fechar launcher"; close.Click += (_, _) => Close();

        gameChoice.Name = "profile"; gameChoice.SetBounds(232, 434, 416, 41); Controls.Add(gameChoice); homeControls.Add(gameChoice);
        AddGameChoice(new GameChoice("Alien Unified " + LauncherProfile.ClientVersion, GameVersion, true));
        foreach (string version in new[] { "1.8.9", "1.12.2", "1.16.5", "1.20.1", "1.21.1" }) AddGameChoice(new GameChoice("Minecraft " + version + " · Vanilla", version, false));
        gameChoice.SelectedIndexChanged += (_, _) =>
        {
            var target = SelectedGame; engineLine.Text = target.Alien ? "FORGE 1.8.9  /  JAVA 8" : "MINECRAFT " + target.Version + "  /  VANILLA OFICIAL";
            play.Text = target.Alien ? "Jogar Alien" : "Jogar Minecraft";
            heroTitle.Text = target.Alien ? "Seu mundo.\nSua identidade." : "Minecraft.\nDo seu jeito.";
            heroDescription.Text = target.Alien ? "HUD, cosméticos e efeitos. Tudo com a sua assinatura." : "Uma instância Vanilla independente, na versão que você escolheu.";
            Invalidate(); SetStatus(target.Alien ? "Escolha seu perfil e entre com Microsoft." : "Vanilla " + target.Version + " selecionado. Alien permanece no Forge 1.8.9.");
        };
        engineLine.Name = "engine"; engineLine.SetBounds(232, 487, 415, 18); engineLine.Font = new Font("Segoe UI", 8.5f); engineLine.ForeColor = Studio.Muted; engineLine.BackColor = Studio.Card; Controls.Add(engineLine); homeControls.Add(engineLine);
        Configure(refreshVersions, "", "refresh", 640, 397, 28, 28); refreshVersions.Name = "refreshVersions"; refreshVersions.Quiet = true; refreshVersions.AccessibleName = "Atualizar versões oficiais"; homeControls.Add(refreshVersions); refreshVersions.Click += async (_, _) => await RunTaskAsync(RefreshVersionsAsync);
        Configure(install, "Instalar / reparar", "download", 220, 509, 180, 27); install.Name = "install"; install.Quiet = true; install.Font = new Font("Segoe UI", 8.5f); install.ForeColor = Studio.Accent; homeControls.Add(install); install.Click += async (_, _) => await RunTaskAsync(InstallAsync);
        Configure(quickSettings, "Ajustar recursos  →", "", 704, 503, 220, 30); quickSettings.Quiet = true; quickSettings.ForeColor = Studio.Accent; quickSettings.Font = new Font("Segoe UI", 8.5f); homeControls.Add(quickSettings); quickSettings.Click += (_, _) => ShowPage(true);
        Configure(play, "Jogar Alien", "play", 734, 578, 214, 56); play.Name = "play"; play.Primary = true; play.Font = new Font("Segoe UI", 11f, FontStyle.Bold); homeControls.Add(play); play.Click += async (_, _) => await RunTaskAsync(PlayAsync);
        status.Name = "status"; status.SetBounds(232, 581, 470, 43); status.Font = new Font("Segoe UI", 9f); status.ForeColor = Studio.Muted; status.BackColor = Studio.Card; status.Text = "Escolha seu perfil e entre com Microsoft."; Controls.Add(status);
        progress.SetBounds(232, 638, 716, 3); progress.Visible = false; Controls.Add(progress);
        Configure(cancel, "Cancelar", "", 788, 645, 160, 27); cancel.Name = "cancel"; cancel.Enabled = false; cancel.Visible = false; cancel.Quiet = true; cancel.Click += (_, _) => operation?.Cancel();

        memory.Name = "memory"; memory.Value = preferences.MemoryMb; memory.SetBounds(232, 182, 716, 65); memory.BackColor = Studio.Card; Controls.Add(memory); settingsControls.Add(memory); memory.ValueChanged += (_, _) => Invalidate();
        javaPath.Name = "java8"; javaPath.SetBounds(248, 326, 508, 25); javaPath.ReadOnly = true; javaPath.BorderStyle = BorderStyle.None; javaPath.BackColor = Color.FromArgb(31, 34, 48); javaPath.ForeColor = Studio.Text; javaPath.Font = new Font("Segoe UI", 9f); javaPath.Text = preferences.Java8Path; javaPath.PlaceholderText = "Automático · Java gerenciado pelo launcher"; Controls.Add(javaPath); settingsControls.Add(javaPath);
        Configure(chooseJava, "Procurar", "", 780, 315, 168, 42); settingsControls.Add(chooseJava);
        chooseJava.Click += async (_, _) => { using var picker = new OpenFileDialog { Filter = "Java (java.exe)|java.exe", CheckFileExists = true }; if (picker.ShowDialog(this) == DialogResult.OK) await RunTaskAsync(async () => { await RuntimePolicy.CheckAsync(picker.FileName, 8, operation!.Token); javaPath.Text = picker.FileName; SavePreferences(); SetStatus("Java 8 selecionado e verificado."); }); };
        Configure(resetJava, "Restaurar automático", "", 232, 366, 210, 30); resetJava.Quiet = true; resetJava.ForeColor = Studio.Accent; settingsControls.Add(resetJava); resetJava.Click += (_, _) => { javaPath.Clear(); SavePreferences(); SetStatus("Java gerenciado será verificado na instalação."); };
        Configure(signOut, "Sair da conta", "", 752, 450, 196, 40); signOut.Name = "signOut"; settingsControls.Add(signOut);
        signOut.Click += async (_, _) => await RunTaskAsync(async () => { await login.Signout(operation!.Token); session = null; account.Text = "Ainda não conectado"; SetStatus("Conta removida do launcher."); });
        if (!preferences.Alien) AddGameChoice(new GameChoice("Minecraft " + preferences.Version + " · Vanilla", preferences.Version, false));
        var saved = gameChoice.Items.Cast<GameChoice>().FirstOrDefault(c => c.Version == preferences.Version && c.Alien == preferences.Alien);
        gameChoice.SelectedItem = saved ?? gameChoice.Items[0]; ShowPage(false);
        if (!string.IsNullOrEmpty(preferences.RecoveryMessage))
        {
            SetStatus("Configuração inválida: valores padrão recuperados. Consulte o diagnóstico para detalhes do arquivo original.");
            AddLog(preferences.RecoveryMessage);
        }
        AddLog("Launcher " + LauncherProfile.LauncherVersion + " / pacote Alien " + LauncherProfile.ClientVersion + ": " + (File.Exists(Path.Combine(Payload, JarName)) ? "localizado" : "extraia o ZIP completo"));
    }

    private Label AddLabel(string text, string name, int x, int y, int width, int height, float size, Color color, bool bold = false)
    {
        var label = new Label { Text = text, Name = name, Bounds = new Rectangle(x, y, width, height), Font = new Font("Segoe UI", size, bold ? FontStyle.Bold : FontStyle.Regular), ForeColor = color, BackColor = Studio.Sidebar }; Controls.Add(label); return label;
    }
    private void Configure(StudioButton button, string text, string glyph, int x, int y, int width, int height)
    { button.Text = text; button.Glyph = glyph; button.SurfaceColor = x < 180 ? Studio.Sidebar : y < 35 ? Studio.Background : Studio.Card; button.SetBounds(x, y, width, height); button.AccessibleName = text; Controls.Add(button); }
    private void ShowPage(bool showSettings)
    {
        settingsPage = showSettings; foreach (var control in homeControls) control.Visible = !showSettings; foreach (var control in settingsControls) control.Visible = showSettings;
        settings.Selected = showSettings; home.Selected = !showSettings; settings.Invalidate(); home.Invalidate(); Invalidate();
    }
    private void DragWindow(MouseEventArgs e) { if (e.Button == MouseButtons.Left) { ReleaseCapture(); SendMessage(Handle, 0xA1, (IntPtr)2, IntPtr.Zero); } }
    protected override void OnMouseDown(MouseEventArgs e) { if (e.Y < 100) DragWindow(e); base.OnMouseDown(e); }
    protected override void OnPaintBackground(PaintEventArgs e)
    {
        base.OnPaintBackground(e); var g = e.Graphics; g.SmoothingMode = SmoothingMode.AntiAlias; g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;
        using var side = new SolidBrush(Studio.Sidebar); g.FillRectangle(side, 0, 0, 180, Height);
        using var line = new Pen(Studio.Border); g.DrawLine(line, 180, 0, 180, Height);
        Studio.Alien(g, new(25, 31, 31, 35));
        Studio.Write(g, "BIBLIOTECA", new(24, 103, 138, 18), 7.5f, Studio.Muted, true);
        Studio.Write(g, "ALIEN LAUNCHER", new(24, 625, 148, 18), 7.5f, Studio.Muted);
        Studio.Write(g, LauncherProfile.LauncherVersion, new(24, 646, 148, 18), 8, Studio.Muted);
        Studio.Write(g, settingsPage ? "Biblioteca / Preferências" : "Biblioteca / Alien Client", new(208, 20, 500, 20), 8, Studio.Muted);
        Studio.Write(g, settingsPage ? "Tudo do seu jeito." : "Vamos jogar.", new(206, 46, 600, 44), 23, Studio.Text, true);
        Studio.Fill(g, new(814, 57, 158, 27), Color.FromArgb(34, 29, 50), 13);
        Studio.Write(g, "UNIFIED  /  0.6 ALPHA", new(827, 63, 145, 20), 8, Studio.Accent, true);
        if (settingsPage) { PaintSettings(g); return; }
        var hero = new Rectangle(208, 116, 764, 256); using var clip = Studio.Round(hero, 18); var state = g.Save(); g.SetClip(clip);
        if (scene != null) g.DrawImage(scene, hero, new Rectangle(0, 150, 850, 480), GraphicsUnit.Pixel);
        using (var shade = new LinearGradientBrush(hero, Color.FromArgb(250, 19, 18, 34), Color.FromArgb(90, 26, 22, 48), 0f)) g.FillRectangle(shade, hero);
        using (var bottom = new LinearGradientBrush(hero, Color.FromArgb(0, 20, 20, 32), Color.FromArgb(220, 20, 19, 33), 90f)) g.FillRectangle(bottom, hero);
        g.Restore(state); Studio.Outline(g, new(208, 116, 764, 256), Color.FromArgb(56, 48, 77), 18);
        Studio.Write(g, SelectedGame.Alien ? "ALIEN CLIENT   /   JAVA EDITION" : "MINECRAFT   /   VANILLA", new(236, 140, 500, 22), 8, Studio.Accent, true);
        Studio.Write(g, heroTitle.Text, new(232, 176, 530, 122), 32, Studio.Text, true);
        Studio.Write(g, heroDescription.Text, new(236, 324, 710, 30), 10, Color.FromArgb(202, 200, 220));
        Studio.Fill(g, new(799, 181, 124, 124), Color.FromArgb(145, 20, 18, 34), 35);
        Studio.Outline(g, new(799, 181, 124, 124), Color.FromArgb(100, 157, 127, 230), 35); Studio.Alien(g, new(827, 207, 69, 77));
        Studio.Fill(g, new(208, 392, 468, 150), Studio.Card); Studio.Outline(g, new(208, 392, 468, 150), Studio.Border);
        Studio.Fill(g, new(692, 392, 280, 150), Studio.Card); Studio.Outline(g, new(692, 392, 280, 150), Studio.Border);
        Studio.Write(g, "PERFIL DE JOGO", new(232, 407, 370, 22), 8, Studio.Muted, true);
        Studio.Write(g, "RECURSOS", new(716, 407, 232, 22), 8, Studio.Muted, true);
        Studio.Write(g, (memory.Value / 1024).ToString("0.#") + " GB", new(712, 436, 220, 48), 26, Studio.Text, true);
        Studio.Write(g, "de memória dedicada", new(716, 478, 220, 20), 8.5f, Studio.Muted);
        Studio.Fill(g, new(208, 558, 764, 95), Studio.Card); Studio.Outline(g, new(208, 558, 764, 95), Studio.Border);
        Studio.Write(g, "Instâncias isoladas. Seu Minecraft original, preservado.", new(208, 660, 660, 18), 7.5f, Studio.Muted);
        Studio.Outline(g, new(0, 0, Width - 1, Height - 1), Studio.Border, 1);
    }
    private void PaintSettings(Graphics g)
    {
        Studio.Fill(g, new(208, 116, 764, 145), Studio.Card); Studio.Outline(g, new(208, 116, 764, 145), Studio.Border);
        Studio.Write(g, "Memória dedicada", new(232, 137, 570, 30), 13, Studio.Text, true);
        Studio.Write(g, (memory.Value / 1024).ToString("0.#") + " GB", new(849, 133, 100, 32), 17, Studio.Accent, true);
        Studio.Fill(g, new(208, 277, 764, 132), Studio.Card); Studio.Outline(g, new(208, 277, 764, 132), Studio.Border);
        Studio.Write(g, "Java 8 para o Alien", new(232, 290, 520, 24), 11, Studio.Text, true);
        Studio.Fill(g, new(232, 315, 532, 42), Color.FromArgb(31, 34, 48), 9);
        Studio.Fill(g, new(208, 425, 764, 82), Studio.Card); Studio.Outline(g, new(208, 425, 764, 82), Studio.Border);
        Studio.Write(g, "Conta Microsoft", new(232, 440, 500, 25), 12, Studio.Text, true);
        Studio.Write(g, account.Text, new(232, 471, 500, 24), 9, Studio.Muted);
        Studio.Write(g, "O Java dos perfis Vanilla é escolhido pelo requisito oficial da versão.", new(232, 526, 710, 30), 9, Studio.Muted);
        Studio.Fill(g, new(208, 558, 764, 95), Studio.Card); Studio.Outline(g, new(208, 558, 764, 95), Studio.Border);
        Studio.Write(g, "As alterações são salvas automaticamente ao sair.", new(232, 629, 710, 20), 8, Studio.Muted);
    }
    protected override void Dispose(bool disposing) { if (disposing) scene?.Dispose(); base.Dispose(disposing); }

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
        cancel.Enabled = busy; cancel.Visible = busy; progress.Visible = busy;
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
        status.Text = PrivateDiagnostics.Clean(text, session?.AccessToken); AddLog(text); Invalidate();
    }
    private void ShowDiagnostics()
    {
        var dialog = new Form { Text = "Alien / Diagnóstico", ClientSize = new Size(840, 470), StartPosition = FormStartPosition.CenterParent, BackColor = Studio.Background, ForeColor = Studio.Text };
        var header = new Label { Text = "Diagnóstico da sessão", Location = new Point(24, 21), AutoSize = true, Font = new Font("Segoe UI", 18f, FontStyle.Bold), ForeColor = Studio.Text };
        var view = new RichTextBox { Bounds = new Rectangle(24, 78, 792, 306), ReadOnly = true, BackColor = Studio.Card, ForeColor = Studio.Muted, WordWrap = false, BorderStyle = BorderStyle.None, Font = new Font("Consolas", 10f), Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right };
        lock (logLock) view.Text = string.Join(Environment.NewLine, safeLog);
        var copy = new StudioButton { Text = "Copiar diagnóstico", Primary = true, SurfaceColor = Studio.Background, Bounds = new Rectangle(590, 406, 226, 40), Anchor = AnchorStyles.Bottom | AnchorStyles.Right }; copy.Click += (_, _) => Clipboard.SetText(view.Text);
        var hint = new Label { Text = "Credenciais são removidas do diagnóstico.", Bounds = new Rectangle(24, 418, 540, 24), ForeColor = Studio.Muted, Font = new Font("Segoe UI", 9f), Anchor = AnchorStyles.Bottom | AnchorStyles.Left };
        dialog.Controls.AddRange([header, view, copy, hint]); dialog.Show(this);
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
