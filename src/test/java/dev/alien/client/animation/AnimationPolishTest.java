package dev.alien.client.animation;
import dev.alien.client.cosmetic.mesh.CapeDynamics;
import dev.alien.client.cosmetic.pet.*;
import dev.alien.client.emote.*;
import dev.alien.client.world.ParticleAppearance;
import org.junit.Test;
import static org.junit.Assert.*;

public class AnimationPolishTest {
 @Test public void springResponseMatchesAcrossFramePartitions(){Spring a=new Spring(-2),b=new Spring(-2);a.advance(3,8,1);for(int i=0;i<144;i++)b.advance(3,8,1.0/144);assertEquals(a.value(),b.value(),1e-12);assertEquals(a.velocity(),b.velocity(),1e-12);}
 @Test public void reversingSpringPreservesItsVelocityAndSettlesWithoutNaNs(){Spring s=new Spring();s.advance(1,14,.07);double v=s.velocity(),p=s.value();s.advance(0,14,.000001);assertEquals(p,s.value(),.00001);assertEquals(v,s.velocity(),.001);for(int i=0;i<100;i++)s.advance(0,14,.05);assertEquals(0,s.value(),1e-7);s.advance(Double.NaN,14,.1);assertEquals(0,s.value(),0);}
 @Test public void savedOnSwitchDoesNotSlideFromTheWrongStateOnFirstPaint(){ControlMotion c=new ControlMotion();c.update(false,false,true,0,true,1);assertEquals(1,c.toggle(),0);c.update(true,false,false,.03,true,1);assertTrue(c.toggle()>0&&c.toggle()<1);c.update(false,false,true,.01,false,1);assertEquals(1,c.toggle(),0);assertEquals(0,c.hover(),0);}
 @Test public void capeInertiaIsIndependentOfRenderSampling(){CapeDynamics a=new CapeDynamics(),b=new CapeDynamics();a.update(0,0,0,0,0);b.update(0,0,0,0,0);for(int i=1;i<=20;i++)a.update(i,2,1,.2f,2);for(int i=1;i<=144;i++)b.update(i*20f/144,2,1,.2f,2);assertEquals(a.bend(),b.bend(),.000001);assertEquals(a.sway(),b.sway(),.000001);}
 @Test public void petMovingTargetStepIsStableAcrossTickPartitions(){PetMotion a=new PetMotion(),b=new PetMotion();PetDesign c=new PetDesign();a.update(c,.05,0,0,0,0,0,false,true);b.update(c,.05,0,0,0,0,0,false,true);for(int i=0;i<20;i++)a.update(c,.05,2,1,1,90,2,false,true);for(int i=0;i<40;i++)b.update(c,.025,2,1,1,90,2,false,true);assertEquals(a.x(1),b.x(1),1e-10);assertEquals(a.y(1),b.y(1),1e-10);assertEquals(a.yaw(1),b.yaw(1),.0001);}
 @Test public void hermiteParticlePathKeepsPositionAndVelocityAtEndpoints(){assertEquals(1,ParticleAppearance.curve(1,3,.5,.2,0),0);assertEquals(3,ParticleAppearance.curve(1,3,.5,.2,1),0);double h=.0001;assertEquals(.5,(ParticleAppearance.curve(1,3,.5,.2,(float)h)-1)/h,.002);assertEquals(.2,(3-ParticleAppearance.curve(1,3,.5,.2,(float)(1-h)))/h,.002);}
 @Test public void everyLoopIncludingRobotHasNoSnappedJointBetweenFrames(){EmotePose a=new EmotePose(),b=new EmotePose();for(String id:EmoteLibrary.IDS){if(!EmoteLibrary.loopable(id))continue;for(int i=1;i<=4000;i++){EmoteLibrary.sample(id,(i-1)*.001,a);EmoteLibrary.sample(id,i*.001,b);for(int j=0;j<18;j++)assertTrue(id+" joint "+j,Math.abs(a.angles[j]-b.angles[j])<.03);}}}
 @Test public void emoteSwitchAndStopRetainOutgoingVelocity(){for(boolean stop:new boolean[]{false,true}){EmotePlayback p=new EmotePlayback();p.play("orange_justice",1,true);p.update(.25);p.update(.25);EmotePose before=new EmotePose(),ahead=new EmotePose(),after=new EmotePose();p.sample(0,before);p.sample(.001,ahead);if(stop)p.stop();else p.play("floss",1,true);p.sample(.001,after);for(int i=0;i<18;i++)assertEquals((ahead.angles[i]-before.angles[i])/.001,(after.angles[i]-before.angles[i])/.001,.025);}}
 @Test public void poseBlendUsesShortArcAcrossPi(){EmotePose a=new EmotePose(),b=new EmotePose(),p=new EmotePose();a.angles[1]=(float)Math.toRadians(179);b.angles[1]=(float)Math.toRadians(-179);p.mix(a,b,.5f);assertEquals(Math.PI,p.angles[1],.00001);}
 @Test public void clothHasNoArtificialPhaseJumpAfterSixteenMinutes(){dev.alien.client.cosmetic.mesh.CapeCloth cloth=new dev.alien.client.cosmetic.mesh.CapeCloth();cloth.update(19999.999f,1,1,1,.3f);float[] before=new float[cloth.vertexCount()];for(int i=0;i<before.length;i++)before[i]=cloth.z(i);cloth.update(20000.001f,1,1,1,.3f);for(int i=0;i<before.length;i++)assertEquals(before[i],cloth.z(i),.002);}
}
