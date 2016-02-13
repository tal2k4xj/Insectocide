package insectocide.logic;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by DELL1 on 2/12/2016.
 */
public class Shot extends ImageView {

    private int power;
    private int speed;
    private double width;
    private double height;
    protected double y;
    protected double x;
    private SpaceEntity entity;
    private DisplayMetrics metrics;
    protected AnimationDrawable animation;

    public Shot (Context context , SpaceEntity entity , DisplayMetrics metrics){
        super(context);
        power = entity.getFirePower();
        speed = entity.getFireSpeed();
        this.entity = entity;
        this.metrics = metrics;
        setPositionAndDimensions();
        setVisibility(View.VISIBLE);
    }

    public void setPositionAndDimensions() {
        int startAnimationId = 0;
        width = metrics.heightPixels * 0.06 / 2.5;
        height = metrics.heightPixels * 0.06;
        setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) height));
        x = entity.getX() + entity.getWidth()/2 - width/2;
        if (entity instanceof SpaceShip){
            startAnimationId = getResources().getIdentifier(entity.getColor()+"_ship_shot_animation", "drawable", "insectocide.game");
            if(entity.getColor().equals("red")){
                y = entity.getY() - height;
            }else if(entity.getColor().equals("blue")){
                y = entity.getY() + height;
            }
        }
        setX((float)x);
        setY((float) y);
        startAnimation(startAnimationId);

    }

    private void startAnimation(int startAnimationId) {
        setBackgroundResource(startAnimationId);
        animation = (AnimationDrawable) this.getBackground();
        animation.start();
    }

    public boolean isOutOfScreen(){
        if (entity instanceof SpaceShip){
            if(entity.getColor().equals("red")){
                if(getY()+width == 0){
                    return true;
                }
            }else if(entity.getColor().equals("blue")){
                if(getY()-width == metrics.heightPixels){
                    return true;
                }
            }
        }
        return false;
    }

    public void shoot(){
        if (entity instanceof SpaceShip){
            if(entity.getColor().equals("red")){
                setY(getY() - (speed * 10));
            }else if(entity.getColor().equals("blue")){
                setY(getY() + (speed * 10));
            }
        }
    }
}
