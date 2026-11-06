package dev.alien.client.cosmetic;
import com.google.gson.Gson;
import dev.alien.client.config.*;
import dev.alien.client.cosmetic.mesh.*;
import org.junit.*;import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
public class CharacterTest {
 @Rule public TemporaryFolder temp=new TemporaryFolder();
 @Test public void existingProfilesKeepVanillaBodyAndTheirAccessories(){CosmeticConfig.Accessory a=new Gson().fromJson("{\"hat\":\"SAMURAI\",\"back\":\"CAPE\",\"character\":null}",CosmeticConfig.Accessory.class);a.normalize();assertEquals("Vanilla",a.character.model);assertEquals("SAMURAI",a.hat);assertEquals("CAPE",a.back);}
 @Test public void malformedModelsFallBackAndVisualScaleStaysFinite(){CharacterDesign d=new CharacterDesign();d.model="invalid";d.size=Float.NaN;d.normalize();assertFalse(d.custom());assertEquals(1,d.size,0);d.size=99;d.model="Kenny";d.normalize();assertTrue(d.custom());assertEquals(1.25,d.size,0);}
 @Test public void copyKeepsIndependentBodyAndNeonPreferences(){CosmeticConfig.Accessory a=new CosmeticConfig.Accessory();a.character.model="Kenny";CosmeticConfig.Accessory b=a.copy();a.character.model="Alien";a.neon.color=0;assertEquals("Kenny",b.character.model);assertNotEquals(0,b.neon.color);}
 @Test public void bodyRoundTripPreservesMusicWidgetsAndOldPets()throws Exception{ConfigStore s=new ConfigStore(temp.newFolder());s.load();s.config().cosmetics.self.character.model="Kenny";s.config().cosmetics.self.character.size=1.1f;s.config().cosmetics.self.pet.setModel("Tung Tung Sahur");s.config().hud.music.style="Transparent";s.saveProfile("body");s.resetDefaults();assertTrue(s.loadProfile("body"));assertEquals("Kenny",s.config().cosmetics.self.character.model);assertEquals(1.1,s.config().cosmetics.self.character.size,.000001);assertEquals("Tung Tung Sahur",s.config().cosmetics.self.pet.model);assertEquals("Transparent",s.config().hud.music.style);assertEquals(16,s.config().hud.widgets.size());}
 @Test public void originalParkaRigHasRoundedFiniteGeometryAndIndependentSegments(){int vertices=0;for(Mesh m:new Mesh[]{CharacterMeshes.BODY,CharacterMeshes.HOOD,CharacterMeshes.FACE,CharacterMeshes.EYES,CharacterMeshes.UPPER,CharacterMeshes.LOWER,CharacterMeshes.MITTEN,CharacterMeshes.LEG,CharacterMeshes.BOOT}){assertTrue(m.vertexCount()>500);assertEquals(0,m.vertexCount()%3);vertices+=m.vertexCount();for(int i=0;i<m.vertexCount();i++){assertTrue(Float.isFinite(m.x(i))&&Float.isFinite(m.y(i))&&Float.isFinite(m.z(i)));assertEquals(1,m.nx(i)*m.nx(i)+m.ny(i)*m.ny(i)+m.nz(i)*m.nz(i),.00001);}}assertTrue(vertices<60000);assertNotSame(CharacterMeshes.UPPER,CharacterMeshes.LOWER);}
}
