// Evidence-only audit probe. Does not authenticate, install, or launch a game.
using AlienLauncher.Core;
using System.Reflection;
using System.Text.Json;

internal static class Program
{
    [STAThread] private static int Main(string[] args)
    {
        string output = Path.GetFullPath(args.Single()); Directory.CreateDirectory(output);
        Application.SetHighDpiMode(HighDpiMode.SystemAware); Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Type window = Assembly.Load("AlienLauncher").GetType("AlienLauncher.LauncherWindow", true)!;
        var evidence = new List<object>();
        foreach (string version in new[] { "1.21.1", "1.21.4" })
        {
            string root = Path.Combine(Path.GetTempPath(), "alien-audit-ui-" + Guid.NewGuid().ToString("N"));
            try
            {
                new LauncherPreferences { Alien = false, Version = version, MemoryMb = 4096 }.Save(root);
                using var form = (Form)Activator.CreateInstance(window, new object[] { root })!;
                form.Show(); Application.DoEvents();
                var selector = form.Controls.Find("profile", true).Single();
                object selected = selector.GetType().GetProperty("SelectedItem")!.GetValue(selector)!;
                string actual = (string)selected.GetType().GetProperty("Version")!.GetValue(selected)!;
                bool alien = (bool)selected.GetType().GetProperty("Alien")!.GetValue(selected)!;
                string caption = selector.Text;
                form.Close(); Application.DoEvents();
                var saved = LauncherPreferences.Load(root);
                evidence.Add(new { test = "saved_vanilla_profile", requestedVersion = version, actualVersion = actual, actualAlien = alien, caption,
                    persistedAfterClose = new { saved.Version, saved.Alien }, profileRestored = actual == version && !alien });
            }
            catch (Exception e) { File.WriteAllText(Path.Combine(output, "probe-error.txt"), PrivateDiagnostics.Clean(e.ToString())); return 1; }
            finally { if (Directory.Exists(root)) Directory.Delete(root, true); }
        }
        File.WriteAllText(Path.Combine(output, "windows-profile-evidence.json"), JsonSerializer.Serialize(evidence, new JsonSerializerOptions { WriteIndented = true }));
        File.WriteAllText(Path.Combine(output, "NOTICE.txt"), "Evidence job only: completion means the probe executed, not that the product is defect-free. No Microsoft login or game launch was attempted.\n");
        return 0;
    }
}
