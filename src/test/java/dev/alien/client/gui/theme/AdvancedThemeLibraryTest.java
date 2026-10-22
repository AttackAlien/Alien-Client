package dev.alien.client.gui.theme;

import dev.alien.client.config.ClientConfig;
import dev.alien.client.gui.ThemeLibrary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class AdvancedThemeLibraryTest {
    @Rule public TemporaryFolder folder=new TemporaryFolder();
    @Test public void advancedTokensRoundTripWithoutReplacingHudOrBindings() throws IOException {
        ThemeLibrary library=new ThemeLibrary(folder.getRoot());ClientConfig source=new ClientConfig();
        source.ui.primary=source.accentColor=0x8066ccdd;source.ui.radius=12;source.ui.blur=.4f;source.ui.shadow=.33f;
        source.ui.font="Vanilla";source.ui.reducedMotion=true;source.ui.menuScale=.8f;source.ui.secondary=0xffffaa66;
        source.normalize(); library.save("advanced",source);
        ClientConfig target=new ClientConfig();target.menuKey=99;target.moduleKeys.put("sky",34);target.hud.widgets.get("fps").x=.6f;
        library.load("advanced",target);
        assertEquals(source.ui.primary,target.ui.primary);assertEquals(source.ui.primary,target.accentColor);
        assertEquals(12,target.ui.radius,0);assertEquals(.4f,target.ui.blur,0);assertEquals(.33f,target.ui.shadow,0);
        assertEquals("Vanilla",target.ui.font);assertTrue(target.ui.reducedMotion);assertEquals(.8f,target.ui.menuScale,0);
        assertEquals(99,target.menuKey);assertEquals(Integer.valueOf(34),target.moduleKeys.get("sky"));assertEquals(.6f,target.hud.widgets.get("fps").x,0);
        target.ui.radius=0;assertEquals(12,source.ui.radius,0);
    }
    @Test public void legacyThemeAdaptsTokensWithoutDiscardingAdvancedPreferences() throws IOException {
        File themes=new File(folder.getRoot(),"themes");assertTrue(themes.mkdirs());
        Files.write(new File(themes,"old.json").toPath(),"{\"version\":1,\"accentColor\":-13408615,\"opacity\":0.5,\"animationSpeed\":2}".getBytes(StandardCharsets.UTF_8));
        ClientConfig target=new ClientConfig();target.ui.radius=11;target.ui.font="Vanilla";
        new ThemeLibrary(folder.getRoot()).load("old",target);
        assertEquals(target.accentColor,target.ui.primary);assertEquals(.5f,target.ui.opacity,0);assertEquals(2,target.ui.animationSpeed,0);
        assertEquals(11,target.ui.radius,0);assertEquals("Vanilla",target.ui.font);assertEquals("Custom RGB",target.ui.preset);
    }
}
