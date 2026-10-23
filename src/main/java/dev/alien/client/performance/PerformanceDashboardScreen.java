package dev.alien.client.performance;

import dev.alien.client.AlienClient;
import dev.alien.client.gui.PremiumScreen;
import dev.alien.client.gui.Ui;
import java.io.IOException;
import java.util.Locale;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

/**
 * Real values from game render events. The graph does not sample this menu
 * itself; a benchmark must be recorded while playing an actual local world.
 */
public final class PerformanceDashboardScreen extends PremiumScreen {
    private final GuiScreen parent;
    private int left, top, panelWidth, panelHeight;
    private boolean compactGraph;
    private String notice = "Graph shows the last 240 gameplay frames, not menu FPS.";

    public PerformanceDashboardScreen(GuiScreen parent) { this.parent = parent; }
    private PerformanceEngine engine() { return AlienClient.get().performance(); }
    private PerformanceConfig config() { return AlienClient.get().config().performance; }

    @Override public void initGui() {
        fontRendererObj = Ui.font();
        panelWidth = Math.min(690, width - 18);
        panelHeight = Math.min(425, height - 18);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        buttonList.clear();
        int bw = Math.max(65, (panelWidth - 30) / 4);
        int y = top + panelHeight - 70;
        buttonList.add(new Ui.Button(0, left + panelWidth - 68, top + 8, 58, 20, "Back"));
        if (panelHeight < 335) buttonList.add(new Ui.Button(9, left + panelWidth - 136,
                top + 8, 58, 20, compactGraph ? "Stats" : "Graph"));
        buttonList.add(new Ui.Button(1, left + 10, top + 37, bw, 21, "Adaptive " + (config().adaptiveEnabled?"ON":"OFF")));
        buttonList.add(new Ui.Button(2, left + 14 + bw, top + 37, bw, 21, "Metrics " + (config().diagnostics?"ON":"OFF")));
        buttonList.add(new Ui.Button(3, left + 18 + 2*bw, top + 37, bw, 21, "FPS -10"));
        buttonList.add(new Ui.Button(4, left + 22 + 3*bw, top + 37, bw, 21, "FPS +10"));
        buttonList.add(new Ui.Button(5, left + 10, y, bw, 21, "Sample BASE"));
        buttonList.add(new Ui.Button(6, left + 14 + bw, y, bw, 21, "Sample EFFECTS"));
        buttonList.add(new Ui.Button(7, left + 18 + 2*bw, y, bw, 21, "Cancel sample"));
        buttonList.add(new Ui.Button(8, left + 22 + 3*bw, y, bw, 21, "Reset scale"));
    }

    @Override public void drawContents(int mx, int my, float partialTicks) {
        Ui.background(width, height);
        Ui.panel(left, top, panelWidth, panelHeight, Ui.theme().surface);
        Ui.text(fontRendererObj.trimStringToWidth("ALIEN / PERFORMANCE ENGINE 2.0",
                panelWidth - (panelHeight < 335 ? 153 : 85)), left + 12, top + 12, Ui.theme().text);
        Ui.text("Target: " + config().targetFps + " FPS   |   Quality: "
                        + Math.round(engine().qualityScale()*100) + "%  |  Alien-only budgets",
                left+12,top+66,Ui.theme().muted);
        int x=left+12, y=top+88;
        if (panelHeight >= 335 || !compactGraph) {
            Ui.text(String.format(Locale.ROOT,"Frame %.1fms     P95 %.1fms     P99 %.1fms",
                    engine().frameTimeMs(),engine().percentile95Ms(),engine().percentile99Ms()),x,y,Ui.theme().text);
            Ui.text("Frames " + engine().frameCount() + "   |   Spikes " + engine().spikeCount()
                    + "   |   Heap " + engine().usedMemoryMb() + "MB",x,y+15,Ui.theme().muted);
            Ui.text("Effective caps: " + engine().particleBudget() + " particles, "
                    + engine().cosmeticBudget() + " cosmetics, " + Math.round(engine().effectDistance()) + " blocks",
                    x,y+29,Ui.theme().muted);
        }

        // Explicitly separate chart, legend, A/B results and footer on compact 4:3 screens.
        int graphX=x, graphY=top+(panelHeight < 335 ? 86 : 151);
        int graphW=panelWidth-24, graphH=panelHeight < 335 ? 43 : Math.max(25,panelHeight-316);
        if (panelHeight >= 335 || compactGraph) {
            Ui.rounded(graphX,graphY,graphW,graphH,5,Ui.theme().surfaceRaised);
            double[] samples=engine().frameHistory();
            double maxMs=Math.max(33.3,Math.max(config().spikeThresholdMs*1.5,
                    engine().percentile99Ms()*1.5));
            int bars=Math.min(graphW,150);
            for (int col=0;col<bars && samples.length>0;col++) {
                int start=(int)(col*(long)samples.length/bars);
                int end=(int)((col+1L)*samples.length/bars);
                if(end<=start)end=start+1;
                double slowest=0;
                for(int i=start;i<end && i<samples.length;i++)slowest=Math.max(slowest,samples[i]);
                if(slowest<=0)continue;
                int bh=Math.min(graphH-4,Math.max(1,(int)Math.round(slowest/maxMs*(graphH-5))));
                int color=slowest>=config().spikeThresholdMs?0xfff16e84:Ui.accent();
                int bx=graphX+(int)(col*(long)graphW/bars);
                int bw=Math.max(1,(int)((col+1L)*graphW/bars)-(int)(col*(long)graphW/bars));
                drawRect(bx,graphY+graphH-2-bh,bx+bw,graphY+graphH-2,color);
            }
            Ui.text("Last gameplay frames  |  Red: slow frame (>"
                    + Math.round(config().spikeThresholdMs) + " ms)",
                    x,graphY+graphH+6,Ui.theme().muted);
        }
        int benchY=top+panelHeight-122;
        if (panelHeight >= 335) {
            Ui.text("Manual 15s comparison: BASE / EFFECTS (no automatic switches)",x,benchY,Ui.theme().muted);
            Ui.text(fontRendererObj.trimStringToWidth(resultLine(0,"BASE"),panelWidth-24),
                    x,benchY+14,Ui.theme().text);
            Ui.text(fontRendererObj.trimStringToWidth(resultLine(1,"EFFECTS"),panelWidth-24),
                    x,benchY+28,Ui.theme().text);
        } else if (!compactGraph) {
            Ui.text(fontRendererObj.trimStringToWidth(
                    "Tap Graph above for frame history / smaller GUI Scale for full view",
                    panelWidth-24),x,top+135,Ui.theme().muted);
        }
        Ui.text(fontRendererObj.trimStringToWidth(notice,panelWidth-24),x,top+panelHeight-38,Ui.theme().muted);
        for(GuiButton button:buttonList)button.drawButton(mc,mx,my);
    }

    private String resultLine(int slot,String name) {
        BenchmarkCapture.Result result=engine().benchmarkResult(slot);
        if(result==null)return name+": no sample yet";
        return String.format(Locale.ROOT,"%s: %d frames  /  mean %.1f ms  /  P95 %.1f  /  P99 %.1f",
                name,result.samples,result.meanMs,result.p95Ms,result.p99Ms);
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        switch(button.id) {
            case 0: mc.displayGuiScreen(parent); return;
            case 1: config().adaptiveEnabled=!config().adaptiveEnabled; break;
            case 2: config().diagnostics=!config().diagnostics; break;
            case 3: config().targetFps=Math.max(30,config().targetFps-10); break;
            case 4: config().targetFps=Math.min(240,config().targetFps+10); break;
            case 5: case 6:
                if(engine().startBenchmark(button.id-5)) {
                    notice="Sampling gameplay for 15 seconds. Stay in-world; reopen Performance to see results.";
                    mc.displayGuiScreen(null); return;
                }
                notice="Join a world before starting a sample.";break;
            case 7: engine().cancelBenchmark();notice="Sample cancelled.";break;
            case 9: compactGraph = !compactGraph; initGui(); return;
            case 8:
                config().adaptiveEnabled=false;
                notice="Adaptive OFF; effective quality is now 100%. Saved budgets unchanged.";break;
            default:return;
        }
        AlienClient.get().save();
        initGui();
    }

    @Override protected void keyTyped(char character,int key) throws IOException {
        if(key==Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(character,key);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
