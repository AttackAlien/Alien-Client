package dev.alien.client.cosmetic;

import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

public class CosmeticConfigTest {
    @Test public void importedProfilesCannotInjectPathsOrUnboundedTransforms() {
        CosmeticConfig config = new CosmeticConfig();
        config.maxDistance = Float.POSITIVE_INFINITY;
        config.self.scale = Float.NaN;
        config.self.rotationX = 900;
        config.self.offsetZ = -900;
        config.self.hat = "UNKNOWN";
        config.self.modelFile = "../../outside.json";
        config.self.textureFile = "https://example.org/skin.png";
        config.normalize();
        assertEquals(4, config.maxDistance, 0);
        assertEquals(.25, config.self.scale, 0);
        assertEquals(180, config.self.rotationX, 0);
        assertEquals(-16, config.self.offsetZ, 0);
        assertEquals("NONE", config.self.hat);
        assertEquals("", config.self.modelFile);
        assertEquals("", config.self.textureFile);
    }

    @Test public void uuidOverridesAreBoundedAndInvalidEntriesRemoved() {
        CosmeticConfig config = new CosmeticConfig();
        config.players.put("not-a-uuid", new CosmeticConfig.Accessory());
        config.players.put(new UUID(1, 1).toString(), null);
        for (int i = 0; i < 150; i++) config.players.put(new UUID(0, i).toString(), new CosmeticConfig.Accessory());
        config.normalize();
        assertEquals(128, config.players.size());
        assertFalse(config.players.containsKey("not-a-uuid"));
        assertFalse(config.players.containsValue(null));
    }

    @Test public void copyingPresetDoesNotMutateSelf() {
        CosmeticConfig.Accessory self = new CosmeticConfig.Accessory();
        self.back = "WINGS"; self.aura = true; self.rotationY = 17; self.modelFile = "alien.json";
        CosmeticConfig.Accessory other = self.copy();
        assertEquals("WINGS", other.back); assertTrue(other.aura);
        assertEquals(17, other.rotationY, 0); assertEquals("alien.json", other.modelFile);
        other.color = 0; other.scale = 2;
        assertNotEquals(self.color, other.color);
        assertEquals(1, self.scale, 0);
    }

    @Test public void clothAndHatSettingsAreBoundedAndCopied() {
        CosmeticConfig.Accessory a = new CosmeticConfig.Accessory();
        a.capeFlutter = Float.NaN; a.capeLength = 100; a.hatSize = -3;
        a.detailLevel = "Unbounded"; a.animations = false;
        a.normalize();
        assertEquals(0, a.capeFlutter, 0); assertEquals(1.35f, a.capeLength, 0);
        assertEquals(.75f, a.hatSize, 0); assertEquals("Low", a.detailLevel);
        CosmeticConfig.Accessory copy = a.copy();
        assertEquals(a.capeLength, copy.capeLength, 0); assertEquals(a.capeFlutter, copy.capeFlutter, 0);
        assertEquals(a.hatSize, copy.hatSize, 0); assertEquals(a.detailLevel, copy.detailLevel);
        assertFalse(copy.animations);
        copy.capeLength = .7f; assertEquals(1.35f, a.capeLength, 0);
    }

    @Test public void oldProfilesReceiveNewClothDefaults() {
        CosmeticConfig.Accessory a = new com.google.gson.Gson().fromJson("{\"hat\":\"JAPANESE\",\"back\":\"CAPE\"}", CosmeticConfig.Accessory.class);
        a.normalize();
        assertEquals(1f, a.hatSize, 0); assertEquals(1f, a.capeLength, 0);
        assertEquals(1f, a.capeFlutter, 0); assertTrue(a.animations);
        assertEquals("Balanced", a.detailLevel);
    }

    @Test public void importerAllowsOnlyBoundedBasenamesWithExpectedExtension() {
        assertEquals("hat-model_1.json", CosmeticConfig.safeName("hat-model_1.json", ".json"));
        assertEquals("", CosmeticConfig.safeName("../hat.json", ".json"));
        assertEquals("", CosmeticConfig.safeName("a..json", ".json"));
        assertEquals("", CosmeticConfig.safeName("a.JSON", ".json"));
        assertEquals("", CosmeticConfig.safeName("/tmp/model.json", ".json"));
        assertEquals("", CosmeticConfig.safeName("model.json\\other.json", ".json"));
    }

    @Test public void validBoxModelPreservesDimensions() throws IOException {
        CosmeticAssets.Box box = CosmeticAssets.parseModel(new StringReader(
                "{\"boxes\":[{\"x\":-2.5,\"y\":-11,\"z\":-2,\"width\":4,\"height\":3,\"depth\":4}]}")).get(0);
        assertEquals(-2.5, box.x, 0); assertEquals(4, box.width); assertEquals(3, box.height);
    }

    @Test public void malformedOrDangerousBoxModelsAreRejected() {
        rejects("null"); rejects("{}"); rejects("{\"boxes\":[]}"); rejects("{\"boxes\":[null]}"); rejects("{nope");
        rejects("{\"boxes\":[{\"x\":65,\"width\":1,\"height\":1,\"depth\":1}]}");
        rejects("{\"boxes\":[{\"x\":\"NaN\",\"width\":1,\"height\":1,\"depth\":1}]}");
        rejects("{\"boxes\":[{\"width\":0,\"height\":1,\"depth\":1}]}");
        rejects("{\"boxes\":[{\"width\":33,\"height\":1,\"depth\":1}]}");
        StringBuilder model = new StringBuilder("{\"boxes\":[");
        for (int i = 0; i < 129; i++) {
            if (i > 0) model.append(',');
            model.append("{\"width\":1,\"height\":1,\"depth\":1}");
        }
        rejects(model.append("]}").toString());
    }

    private static void rejects(String json) {
        try { CosmeticAssets.parseModel(new StringReader(json)); fail("Expected rejected model"); }
        catch (IOException expected) { }
    }
}
