using System.Drawing.Drawing2D;
using System.Drawing.Text;

namespace AlienLauncher;

internal static class Studio
{
    public static readonly Color Background = Color.FromArgb(13, 15, 22), Sidebar = Color.FromArgb(17, 19, 28), Card = Color.FromArgb(23, 26, 37), Border = Color.FromArgb(40, 43, 59), Text = Color.FromArgb(242, 243, 250), Muted = Color.FromArgb(145, 150, 172), Accent = Color.FromArgb(174, 147, 255);
    public static GraphicsPath Round(RectangleF r, float radius = 14)
    {
        float d = Math.Min(radius * 2, Math.Min(r.Width, r.Height)); var p = new GraphicsPath();
        p.AddArc(r.X, r.Y, d, d, 180, 90); p.AddArc(r.Right - d, r.Y, d, d, 270, 90); p.AddArc(r.Right - d, r.Bottom - d, d, d, 0, 90); p.AddArc(r.X, r.Bottom - d, d, d, 90, 90); p.CloseFigure(); return p;
    }
    public static void Fill(Graphics g, RectangleF r, Color color, float radius = 14) { using var p = Round(r, radius); using var b = new SolidBrush(color); g.FillPath(b, p); }
    public static void Outline(Graphics g, RectangleF r, Color color, float radius = 14) { using var p = Round(r, radius); using var pen = new Pen(color); g.DrawPath(pen, p); }
    public static void Write(Graphics g, string text, RectangleF bounds, float size = 10, Color? color = null, bool bold = false)
    {
        using var f = new Font("Segoe UI", size, bold ? FontStyle.Bold : FontStyle.Regular); using var b = new SolidBrush(color ?? Text);
        using var format = new StringFormat { Trimming = StringTrimming.EllipsisCharacter }; g.DrawString(text, f, b, bounds, format);
    }
    public static void Alien(Graphics g, RectangleF r, Color color)
    {
        var state = g.Save(); g.TranslateTransform(r.X, r.Y); g.ScaleTransform(r.Width / 100, r.Height / 100);
        using var path = new GraphicsPath(); path.AddBezier(50, 98, 40, 84, 9, 64, 8, 36); path.AddBezier(8, 36, 6, -9, 94, -9, 92, 36); path.AddBezier(92, 36, 91, 64, 60, 84, 50, 98);
        using var fill = new SolidBrush(color); g.FillPath(fill, path);
        using var eye = new SolidBrush(Color.FromArgb(18, 19, 30));
        g.FillClosedCurve(eye, (Point[])[new(23, 35), new(45, 45), new(40, 58), new(23, 48)]);
        g.FillClosedCurve(eye, (Point[])[new(77, 35), new(55, 45), new(60, 58), new(77, 48)]); g.Restore(state);
    }
    public static void Icon(Graphics g, string icon, Rectangle r, Color color)
    {
        var state = g.Save(); g.TranslateTransform(r.X, r.Y); g.ScaleTransform(r.Width / 24f, r.Height / 24f);
        using var p = new Pen(color, 1.7f) { StartCap = LineCap.Round, EndCap = LineCap.Round, LineJoin = LineJoin.Round };
        if (icon == "home") { g.DrawLines(p, (Point[])[new(3, 11), new(12, 4), new(21, 11)]); g.DrawLines(p, (Point[])[new(6, 10), new(6, 20), new(18, 20), new(18, 10)]); }
        else if (icon == "settings") { for (int i = 0; i < 3; i++) { int y = 6 + i * 6; g.DrawLine(p, 3, y, 21, y); using var b = new SolidBrush(Card); g.FillEllipse(b, i == 1 ? 13 : 6, y - 3, 6, 6); g.DrawEllipse(p, i == 1 ? 13 : 6, y - 3, 6, 6); } }
        else if (icon == "folder") g.DrawLines(p, (Point[])[new(3, 7), new(10, 7), new(12, 10), new(21, 10), new(20, 20), new(3, 20), new(3, 7)]);
        else if (icon == "logs") { g.DrawRectangle(p, 4, 3, 16, 18); g.DrawLine(p, 8, 8, 16, 8); g.DrawLine(p, 8, 12, 16, 12); g.DrawLine(p, 8, 16, 12, 16); }
        else if (icon == "download") { g.DrawLines(p, (Point[])[new(7, 11), new(12, 16), new(17, 11)]); g.DrawLine(p, 12, 3, 12, 16); g.DrawLines(p, (Point[])[new(3, 17), new(3, 21), new(21, 21), new(21, 17)]); }
        else if (icon == "refresh") { g.DrawArc(p, 4, 4, 16, 16, 35, 285); g.DrawLines(p, (Point[])[new(15, 3), new(20, 4), new(20, 9)]); }
        else if (icon == "play") { using var b = new SolidBrush(color); g.FillPolygon(b, (Point[])[new(8, 5), new(19, 12), new(8, 19)]); }
        else if (icon == "microsoft") { using var b = new SolidBrush(color); foreach (var xy in new[] { (3, 3), (13, 3), (3, 13), (13, 13) }) g.FillRectangle(b, xy.Item1, xy.Item2, 8, 8); }
        else if (icon == "close") { g.DrawLine(p, 7, 7, 17, 17); g.DrawLine(p, 17, 7, 7, 17); }
        else if (icon == "minimize") g.DrawLine(p, 6, 12, 18, 12);
        g.Restore(state);
    }
}

internal class StudioButton : Button
{
    public bool Primary { get; set; }
    public bool Selected { get; set; }
    public bool Quiet { get; set; }
    public string Glyph { get; set; } = "";
    public bool AlignLeft { get; set; }
    public Color SurfaceColor { get; set; } = Studio.Card;
    public Color? FillColor { get; set; }
    private bool hover, pressed;
    public StudioButton() { SetStyle(ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint | ControlStyles.OptimizedDoubleBuffer | ControlStyles.Opaque, true); FlatStyle = FlatStyle.Flat; FlatAppearance.BorderSize = 0; BackColor = Studio.Card; ForeColor = Studio.Text; Cursor = Cursors.Hand; Font = new Font("Segoe UI", 9.5f); }
    protected override void OnMouseEnter(EventArgs e) { hover = true; Invalidate(); base.OnMouseEnter(e); }
    protected override void OnMouseLeave(EventArgs e) { hover = pressed = false; Invalidate(); base.OnMouseLeave(e); }
    protected override void OnMouseDown(MouseEventArgs e) { pressed = true; Invalidate(); base.OnMouseDown(e); }
    protected override void OnMouseUp(MouseEventArgs e) { pressed = false; Invalidate(); base.OnMouseUp(e); }
    protected override void OnEnabledChanged(EventArgs e) { Invalidate(); base.OnEnabledChanged(e); }
    protected override void OnPaintBackground(PaintEventArgs e) => e.Graphics.Clear(SurfaceColor);
    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics; g.Clear(SurfaceColor); g.SmoothingMode = SmoothingMode.AntiAlias; g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;
        Color fill = !Enabled ? Color.FromArgb(35, 37, 49) : Primary ? (pressed ? Color.FromArgb(145, 112, 233) : hover ? Color.FromArgb(190, 167, 255) : Studio.Accent) : Selected ? Color.FromArgb(43, 35, 65) : hover ? Color.FromArgb(38, 41, 56) : Quiet ? SurfaceColor : FillColor ?? Studio.Card;
        if (fill.A > 0) Studio.Fill(g, new RectangleF(0, 0, Width - 1, Height - 1), fill, 9);
        Color foreground = !Enabled ? Studio.Muted : Primary ? Color.FromArgb(24, 18, 40) : Selected ? Studio.Accent : ForeColor;
        int icon = Glyph.Length > 0 ? 22 : 0;
        if (icon > 0) Studio.Icon(g, Glyph, new Rectangle(Text.Length == 0 ? (Width - 20) / 2 : AlignLeft ? 14 : 18, (Height - 20) / 2, 20, 20), foreground);
        var rect = new Rectangle(AlignLeft ? (icon > 0 ? 45 : 14) : icon > 0 ? 38 : 8, 0, Width - (icon > 0 ? 48 : 16), Height);
        TextRenderer.DrawText(g, Text, Font, rect, foreground, TextFormatFlags.VerticalCenter | TextFormatFlags.EndEllipsis | TextFormatFlags.NoPrefix | (AlignLeft ? TextFormatFlags.Left : TextFormatFlags.HorizontalCenter));
        if (Focused && ShowFocusCues) Studio.Outline(g, new RectangleF(2, 2, Width - 5, Height - 5), foreground, 8);
    }
}

internal sealed class ProfilePicker : StudioButton
{
    public List<object> Items { get; } = [];
    private int selected = -1;
    public int SelectedIndex { get => selected; set { if (value < 0 || value >= Items.Count) throw new ArgumentOutOfRangeException(nameof(value)); selected = value; Text = Items[value].ToString() ?? ""; SelectedIndexChanged?.Invoke(this, EventArgs.Empty); Invalidate(); } }
    public object? SelectedItem { get => selected >= 0 ? Items[selected] : null; set { int i = Items.IndexOf(value!); if (i >= 0) SelectedIndex = i; } }
    public event EventHandler? SelectedIndexChanged;
    public ProfilePicker() { AlignLeft = true; FillColor = Color.FromArgb(32, 35, 49); AccessibleRole = AccessibleRole.ComboBox; }
    protected override void OnClick(EventArgs e)
    {
        base.OnClick(e); var menu = new ContextMenuStrip { BackColor = Studio.Card, ForeColor = Studio.Text, ShowImageMargin = false, Renderer = new DarkMenuRenderer(), MaximumSize = new Size(460, 400) };
        for (int i = 0; i < Items.Count; i++) { int index = i; var item = new ToolStripMenuItem(Items[i].ToString()) { Checked = i == selected, Padding = new Padding(10, 7, 10, 7) }; item.Click += (_, _) => SelectedIndex = index; menu.Items.Add(item); }
        menu.Closed += (_, _) => menu.Dispose(); menu.Show(this, new Point(0, Height + 4));
    }
    protected override void OnPaint(PaintEventArgs e) { base.OnPaint(e); using var p = new Pen(Studio.Muted, 1.4f); e.Graphics.DrawLines(p, (Point[])[new(Width - 24, Height / 2 - 2), new(Width - 19, Height / 2 + 3), new(Width - 14, Height / 2 - 2)]); }
    private sealed class DarkMenuRenderer : ToolStripProfessionalRenderer
    {
        protected override void OnRenderMenuItemBackground(ToolStripItemRenderEventArgs e) { using var b = new SolidBrush(e.Item.Selected ? Color.FromArgb(53, 44, 78) : Studio.Card); e.Graphics.FillRectangle(b, new Rectangle(Point.Empty, e.Item.Size)); }
        protected override void OnRenderToolStripBorder(ToolStripRenderEventArgs e) { using var p = new Pen(Studio.Border); e.Graphics.DrawRectangle(p, 0, 0, e.ToolStrip.Width - 1, e.ToolStrip.Height - 1); }
    }
}

internal sealed class MemoryPicker : Control
{
    private decimal value = 4096;
    public decimal Value { get => value; set { this.value = Math.Clamp(Math.Round(value / 512) * 512, 1024, 16384); Invalidate(); ValueChanged?.Invoke(this, EventArgs.Empty); } }
    public event EventHandler? ValueChanged;
    public MemoryPicker() { DoubleBuffered = true; TabStop = true; Cursor = Cursors.Hand; AccessibleRole = AccessibleRole.Slider; AccessibleName = "Memória RAM, de 1 a 16 gigabytes"; }
    protected override void OnMouseDown(MouseEventArgs e) { Focus(); Capture = true; MoveValue(e.X); base.OnMouseDown(e); }
    protected override void OnMouseMove(MouseEventArgs e) { if (Capture) MoveValue(e.X); base.OnMouseMove(e); }
    protected override void OnMouseUp(MouseEventArgs e) { Capture = false; base.OnMouseUp(e); }
    private void MoveValue(int x) => Value = 1024 + (decimal)Math.Clamp((x - 12f) / (Width - 24), 0, 1) * 15360m;
    protected override bool IsInputKey(Keys keyData) => keyData is Keys.Left or Keys.Right or Keys.Home or Keys.End || base.IsInputKey(keyData);
    protected override void OnKeyDown(KeyEventArgs e) { if (e.KeyCode == Keys.Left) Value -= 512; if (e.KeyCode == Keys.Right) Value += 512; if (e.KeyCode == Keys.Home) Value = 1024; if (e.KeyCode == Keys.End) Value = 16384; base.OnKeyDown(e); }
    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics; g.SmoothingMode = SmoothingMode.AntiAlias; float end = 12 + (Width - 24) * (float)((Value - 1024) / 15360);
        Studio.Fill(g, new RectangleF(12, 18, Width - 24, 5), Studio.Border, 2); if (end > 12) Studio.Fill(g, new RectangleF(12, 18, end - 12, 5), Studio.Accent, 2);
        using var b = new SolidBrush(Studio.Accent); g.FillEllipse(b, end - 7, 13, 15, 15);
        Studio.Write(g, "1 GB", new(8, 40, 60, 24), 8, Studio.Muted); Studio.Write(g, "16 GB", new(Width - 48, 40, 48, 24), 8, Studio.Muted);
        if (Focused) Studio.Outline(g, new(1, 1, Width - 3, Height - 3), Studio.Accent, 8);
    }
}

internal sealed class StudioProgress : Control
{
    private readonly System.Windows.Forms.Timer timer = new() { Interval = 60 };
    private ProgressBarStyle style; private int value, phase;
    public int Value { get => value; set { this.value = Math.Clamp(value, 0, 100); Invalidate(); } }
    public ProgressBarStyle Style { get => style; set { style = value; UpdateTimer(); Invalidate(); } }
    public StudioProgress() { DoubleBuffered = true; timer.Tick += (_, _) => { phase = (phase + 9) % Math.Max(1, Width); Invalidate(); }; }
    protected override void OnVisibleChanged(EventArgs e) { UpdateTimer(); base.OnVisibleChanged(e); }
    private void UpdateTimer() { if (Visible && style == ProgressBarStyle.Marquee) timer.Start(); else timer.Stop(); }
    protected override void OnPaint(PaintEventArgs e) { Studio.Fill(e.Graphics, new(0, 0, Width, Height), Studio.Border, 2); if (style == ProgressBarStyle.Marquee) Studio.Fill(e.Graphics, new(phase, 0, Math.Min(100, Width - phase), Height), Studio.Accent, 2); else if (value > 0) Studio.Fill(e.Graphics, new(0, 0, Width * value / 100f, Height), Studio.Accent, 2); }
    protected override void Dispose(bool disposing) { if (disposing) timer.Dispose(); base.Dispose(disposing); }
}
