package dev.alien.client.world;
import dev.alien.client.gui.Ui;
/** Shared custom component with click-to-type numeric values. */
final class StudioSlider extends Ui.Slider {
    interface Change {void set(float value);}
    StudioSlider(int id,int x,int y,int w,String label,float min,float max,float value,Change change){
        super(id,x,y,w,20,label,min,max,max<=2?(max-min)/1000f:1f,value,v->change.set(v));
    }
}
