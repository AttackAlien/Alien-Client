package dev.alien.client.hud;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
public class HudLayoutsTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void hudOnlyExchangePreservesStyleAndTransforms() throws Exception {
        File root=temp.newFolder();HudConfig source=new HudConfig();source.opacity=.4f;source.modernFont=false;source.widgets.get("fps").scale=1.7f;
        HudLayouts.exportLayout(root,source);File input=new File(root,"hud/imports/layout.json");input.getParentFile().mkdirs();
        Files.copy(new File(root,"hud/exports/layout.json").toPath(),input.toPath());
        HudConfig read=HudLayouts.importLayout(root);assertEquals(.4f,read.opacity,0);assertFalse(read.modernFont);assertEquals(1.7f,read.widgets.get("fps").scale,0);
        assertFalse(new File(root,"config.json").exists());
    }
    @Test public void maliciousNestingIsRejectedBeforeDeserialization() throws Exception {
        File root=temp.newFolder(),input=new File(root,"hud/imports/layout.json");input.getParentFile().mkdirs();
        StringBuilder s=new StringBuilder();for(int i=0;i<40;i++)s.append('[');for(int i=0;i<40;i++)s.append(']');
        Files.write(input.toPath(),s.toString().getBytes(StandardCharsets.UTF_8));
        try{HudLayouts.importLayout(root);fail("Must reject nesting");}catch(java.io.IOException expected){assertTrue(expected.getMessage().contains("nesting"));}
    }
}
