using System.Drawing.Imaging;
using System.Runtime.InteropServices;
using System.Text.Json;
using AlienLauncher;
using AlienLauncher.Core;

internal static class Program
{
    [DllImport("user32.dll")] private static extern bool SetForegroundWindow(IntPtr hwnd);
    [DllImport("user32.dll")] private static extern bool PrintWindow(IntPtr hwnd, IntPtr dc, uint flags);
    private static readonly List<string> checks = [];
    private static readonly List<object> captures = [];
    private static string output = "";
    [STAThread] private static int Main(string[] args)
    {
        output = Path.GetFullPath(args.Single()); Directory.CreateDirectory(output);
        string root = Path.Combine(Path.GetTempPath(), "AlienLauncher-UiSmoke-" + Guid.NewGuid().ToString("N"));
        Application.SetHighDpiMode(HighDpiMode.SystemAware); Application.EnableVisualStyles(); Application.SetCompatibleTextRenderingDefault(false);
        try { VerifyRestoredProfiles(); VerifyRecoverableSettings(); }
        catch (Exception failure)
        {
            File.WriteAllText(Path.Combine(output, "failure.txt"), PrivateDiagnostics.Clean(failure.ToString()));
            return 1;
        }
        var window = new LauncherWindow(root); int result = 1;
        window.Shown += async (_, _) =>
        {
            try
            {
                await Task.Delay(900); Check(window.Text.Contains(LauncherProfile.ClientVersion), "Actual window displays the current client version");
                foreach (Control c in window.Controls) if (c.Visible) Check(c.Bounds.Left >= 0 && c.Bounds.Top >= 0 && c.Bounds.Right <= window.ClientSize.Width && c.Bounds.Bottom <= window.ClientSize.Height, "Control fits actual form / " + (c.Name.Length > 0 ? c.Name : c.GetType().Name));
                Check(Control<Button>(window, "play").Enabled, "Play is a real enabled production control");
                Check(!Control<Label>(window, "brand").Bounds.IntersectsWith(Control<Label>(window, "launcherTitle").Bounds), "Actual brand labels do not overlap");
                Check(!Control<Button>(window, "cancel").Enabled, "Cancellation is disabled when no operation is running");
                Check(Control<Label>(window, "account").Text.Contains("não conectado"), "UI does not fabricate an authenticated account");
                Capture(window, "01-launcher-alien");
                var choices = Control<ProfilePicker>(window, "profile"); int vanilla = -1;
                for (int i = 0; i < choices.Items.Count; i++) if (choices.Items[i]!.ToString()!.Contains("1.21.1")) vanilla = i;
                Check(vanilla >= 0, "Independent modern Vanilla profile exists"); choices.SelectedIndex = vanilla;
                await Task.Delay(600); Check(Control<Button>(window, "play").Text == "Jogar Minecraft", "Selecting Vanilla invokes actual selection handler");
                Check(Control<Label>(window, "engine").Text.Contains("1.21.1") && !Control<Label>(window, "engine").Text.Contains("FORGE"), "Modern Vanilla does not claim to run Forge 1.8.9");
                Capture(window, "02-launcher-vanilla");
                Control<Button>(window, "settings").PerformClick(); await Task.Delay(400);
                var memory = Control<MemoryPicker>(window, "memory");
                Check(memory.Visible && !Control<Button>(window, "play").Visible, "Settings navigation shows real resources and hides the launch panel");
                memory.Value = 4096; memory.Focus(); SendKeys.SendWait("{RIGHT}"); await Task.Delay(200);
                Check(memory.Value == 4608, "Actual keyboard interaction adjusts RAM in 512 MB steps");
                memory.Value = 6144; Capture(window, "03-launcher-settings");
                Control<Button>(window, "home").PerformClick(); await Task.Delay(250);
                Check(Control<Button>(window, "play").Visible && !memory.Visible, "Returning home restores the actual launch panel");
                Control<Button>(window, "diagnostics").PerformClick(); await Task.Delay(500);
                var dialog = window.OwnedForms.Single(); Check(dialog.Text.Contains("Diagnóstico"), "Diagnostics button opens actual log window");
                Check(dialog.Controls.OfType<RichTextBox>().Single().Text.Contains("Launcher 0.3"), "Diagnostics shows real launcher log, not sample text");
                Capture(dialog, "04-launcher-diagnostics"); dialog.Close();
                Check(Control<Label>(window, "account").Text.Contains("não conectado"), "UI test never claimed a Microsoft login");
                window.Close();
                var preferences = LauncherPreferences.Load(root); Check(preferences.MemoryMb == 6144 && !preferences.Alien && preferences.Version == "1.21.1", "Closing persists actual profile and RAM preferences");
                result = 0;
            }
            catch (Exception failure) { File.WriteAllText(Path.Combine(output, "failure.txt"), PrivateDiagnostics.Clean(failure.ToString())); }
            finally
            {
                File.WriteAllText(Path.Combine(output, "result.txt"), (result == 0 ? "PASS " : "FAIL ") + checks.Count + " checks\n" + string.Join('\n', checks));
                File.WriteAllText(Path.Combine(output, "captures.json"), JsonSerializer.Serialize(captures, new JsonSerializerOptions { WriteIndented = true }));
                window.Close(); Application.ExitThread();
            }
        };
        Application.Run(window); return result;
    }
    private static T Control<T>(Form form, string name) where T : Control => (T)form.Controls.Find(name, true).Single();
    private static void VerifyRestoredProfiles()
    {
        foreach (string version in new[] { "1.21.1", "1.21.4" })
        {
            string root = Path.Combine(Path.GetTempPath(), "AlienLauncher-Restore-" + Guid.NewGuid().ToString("N"));
            try
            {
                new LauncherPreferences { Alien = false, Version = version, MemoryMb = 6144 }.Save(root);
                using var form = new LauncherWindow(root); form.Show(); Application.DoEvents();
                Check(Control<ProfilePicker>(form, "profile").Text.Contains(version) && Control<Button>(form, "play").Text == "Jogar Minecraft", "Saved Vanilla version restored without switching to Alien / " + version);
                if (version == "1.21.4") Capture(form, "05-launcher-restored-vanilla");
                form.Close(); Application.DoEvents();
                var saved = LauncherPreferences.Load(root);
                Check(!saved.Alien && saved.Version == version && saved.MemoryMb == 6144, "Closing retains saved Vanilla profile and RAM / " + version);
            }
            finally { if (Directory.Exists(root)) Directory.Delete(root, true); }
        }
    }
    private static void VerifyRecoverableSettings()
    {
        string root = Path.Combine(Path.GetTempPath(), "AlienLauncher-Recover-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(root);
        string original = "{\"MemoryMb\":2048}" + new string(' ', 17000);
        try
        {
            File.WriteAllText(Path.Combine(root, "settings.json"), original);
            using var form = new LauncherWindow(root); form.Show(); Application.DoEvents();
            Check(Control<Label>(form, "status").Text.Contains("recuperados"), "Oversized settings open the real window with a recovery message");
            string backup = Directory.EnumerateFiles(root, "settings.invalid-*.json").Single();
            Check(File.ReadAllText(backup) == original, "Startup preserves exact rejected settings");
            Capture(form, "06-launcher-settings-recovery");
            Control<Button>(form, "diagnostics").PerformClick(); Application.DoEvents();
            var diagnostics = form.OwnedForms.Single();
            Check(diagnostics.Controls.OfType<RichTextBox>().Single().Text.Contains(Path.GetFileName(backup)), "Recovery diagnostics retain the exact backup filename");
            diagnostics.Close();
            form.Close(); Application.DoEvents();
            Check(LauncherPreferences.Load(root).MemoryMb == 4096, "Recovered window saves usable defaults on close");
        }
        finally { Directory.Delete(root, true); }
    }
    private static void Check(bool value, string description) { if (!value) throw new InvalidOperationException(description); checks.Add(description); }
    private static void Capture(Form form, string name)
    {
        SetForegroundWindow(form.Handle); form.Refresh(); form.Update(); using var image = new Bitmap(form.ClientSize.Width, form.ClientSize.Height);
        string method = "CopyFromScreen / actual Windows desktop";
        try { using var graphics = Graphics.FromImage(image); graphics.CopyFromScreen(form.PointToScreen(Point.Empty), Point.Empty, image.Size); }
        catch { method = "PrintWindow / native HWND rendering (desktop unavailable)"; RenderNative(form, image); }
        if (!HasContent(image)) { method = "PrintWindow / native HWND rendering (desktop unavailable)"; RenderNative(form, image); }
        // Preserve failed captures too, so a real rendering failure can be inspected.
        image.Save(Path.Combine(output, name + ".png"), ImageFormat.Png);
        Check(HasContent(image), "Actual native window capture has nonblank content / " + name);
        captures.Add(new { file = name + ".png", method, utc = DateTimeOffset.UtcNow, width = image.Width, height = image.Height });
    }
    private static void RenderNative(Form form, Bitmap image)
    {
        using var graphics = Graphics.FromImage(image); IntPtr dc = graphics.GetHdc();
        try { if (!PrintWindow(form.Handle, dc, 3)) throw new InvalidOperationException("Native window screenshot unavailable"); }
        finally { graphics.ReleaseHdc(dc); }
    }
    private static bool HasContent(Bitmap image)
    {
        // Diagnostics has a small genuine palette; sparse sampling used to mistake
        // its dark background and short log for a blank desktop. Inspect densely.
        var counts = new Dictionary<int, int>(); int total = 0;
        for (int y = 2; y < image.Height; y += 3) for (int x = 2; x < image.Width; x += 3)
        { int color = image.GetPixel(x, y).ToArgb(); counts[color] = counts.GetValueOrDefault(color) + 1; total++; }
        return counts.Count >= 3 && total - counts.Values.Max() >= Math.Max(50, total / 100);
    }
}
