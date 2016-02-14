package insectocide.logic;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public abstract class SpaceEntity extends ImageView {
    protected float firePower;
    protected float fireSpeed;
    protected Shot shot;
    protected float health;
    protected float movementSpeed;
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
        health =0;
        setDrawingCacheEnabled(true);
    }

    public Shot fire(){
        shot = new Shot(this.getContext(),this,metrics);
        return shot;
    }

    public void gotHit(float power){
        health = health-power;
    }

    public boolean isDead(){
        return health <= 0;
    }

    public String getColor (){
        return this.color;
    }

    public abstract void move(String direction);

    public float getFirePower() {
        return firePower;
    }

    public void setFirePower(float firePower) {
        this.firePower = firePower;
    }

    public float getFireSpeed() {
        return fireSpeed;
    }

    public void setFireSpeed(float fireSpeed) {
        this.fireSpeed = fireSpeed;
    }
    public RectF getRect(){
        RectF r = new RectF(getX(),getY(),(float)(getX()+width),(float)(getY()+height*0.7));
        return r;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }
}
