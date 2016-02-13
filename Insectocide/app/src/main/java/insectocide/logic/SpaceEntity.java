package insectocide.logic;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.util.DisplayMetrics;
import android.widget.ImageView;

/**
 * Created by Zukis87 on 06/02/2016.
 */
public abstract class SpaceEntity extends ImageView {
    protected int firePower;
    protected int fireSpeed;
    protected Shot shot;
    protected int health;
    protected int movementSpeed;
    protected double y;
    protected double x;
    protected double width;
    protected double height;
    protected String color;
    protected DisplayMetrics metrics;
    protected AnimationDrawable animation;

    public SpaceEntity(Context context , DisplayMetrics metrics) {
        super(context);
        this.metrics = metrics;
    }

    public Shot fire(){
        shot = new Shot(this.getContext(),this,metrics);
        return shot;
    }

    public void gotHit(int power){
        health = health-power;
    }

    public boolean isDead(){
        if (health == 0){
            return true;
        }
        return false;
    }

    public String getColor (){
        return this.color;
    }

    public abstract void move(String direction);

    public int getFirePower() {
        return firePower;
    }

    public void setFirePower(int firePower) {
        this.firePower = firePower;
    }

    public int getFireSpeed() {
        return fireSpeed;
    }

    public void setFireSpeed(int fireSpeed) {
        this.fireSpeed = fireSpeed;
    }
}
