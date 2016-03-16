package insectocide.logic;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Shot extends ImageView {

    private double power;
    private double speed;
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
        setDrawingCacheEnabled(true);
        setupShot();
        setVisibility(VISIBLE);
    }

    public void setupShot() {
        setSizeAndXPosition();
        int startAnimationId = 0;
        if (entity instanceof SpaceShip){
            startAnimationId = setSpaceShipShot();
        }else if(entity instanceof Insect){
            startAnimationId = setInsectShot();
        }
        setY((float) y);
        startAnimation(startAnimationId);
    }

    private void setSizeAndXPosition() {
        width = metrics.heightPixels * 0.06 / 2.5;
        height = metrics.heightPixels * 0.06;
        setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) height));
        x = entity.getX() + entity.getWidth()/2 - width/2;
        setX((float) x);
    }

    private int setSpaceShipShot() {
        int startAnimationId;
        startAnimationId = getResources().getIdentifier(entity.getColor()+"_ship_shot_animation", "drawable", "insectocide.game");
        if(entity.getColor().equals("red")){
            y = entity.getY() - height;
        }else if(entity.getColor().equals("blue")){
            y = entity.getY() + entity.getHeight();
        }
        return startAnimationId;
    }

    private int setInsectShot() {
        int startAnimationId;
        startAnimationId = getResources().getIdentifier("bug_"+((Insect) entity).getShootDirection()+"_shot_animation", "drawable", "insectocide.game");
        if(((Insect) entity).getShootDirection().equals("up")){
            y = entity.getY() - height;
        }else if(((Insect) entity).getShootDirection().equals("down")){
            y = entity.getY() + entity.getHeight();
        }
        return startAnimationId;
    }

    private void startAnimation(int startAnimationId) {
        setBackgroundResource(startAnimationId);
        animation = (AnimationDrawable) this.getBackground();
        animation.start();
    }

    public boolean isOutOfScreen(){
        if(getY()+width == 0){
            return true;
        }
        if(getY()-width == metrics.heightPixels){
            return true;
        }
        return false;
    }

    public void shoot(){
        if (entity instanceof SpaceShip){
            if(entity.getColor().equals("red")){
                setY(getY() - (float)(speed * metrics.heightPixels*0.009));
            }else if(entity.getColor().equals("blue")){
                setY(getY() + (float)(speed * metrics.heightPixels*0.009));
            }
        }else if (entity instanceof Insect){
            if(((Insect) entity).getShootDirection().equals("up")){
                setY(getY() - (float)(speed * metrics.heightPixels*0.005));
            }else if (((Insect) entity).getShootDirection().equals("down")){
                setY(getY() + (float)(speed * metrics.heightPixels*0.005));
            }
        }
    }

    public double getPower() {
        return power;
    }

    public SpaceEntity getEntity() {
        return entity;
    }

    public void destroy(){
        animation.stop();
        setVisibility(INVISIBLE);
    }
    public RectF getRect(){
        RectF r = new RectF(getX(),getY(),(float)(getX()+width),(float)(getY()+height*0.8));
        return r;
    }
}
