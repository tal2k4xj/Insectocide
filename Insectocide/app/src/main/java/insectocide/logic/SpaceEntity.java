package insectocide.logic;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public abstract class SpaceEntity extends ImageView {
    protected double firePower;
    protected double fireSpeed;
    protected Shot shot;
    protected double health;
    protected double movementSpeed;
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

    public void gotHit(double power){
        if (health > 0) {
            if (health - power < 0)
                health = health - 1;
            else
                health = health - power;
        }
    }

    public boolean isDead(){
        return health <= 0;
    }

    public String getColor (){
        return this.color;
    }

    public abstract void move(String direction);

    public double getFirePower() {
        return firePower;
    }

    public void setFirePower(double firePower) {
        this.firePower = firePower;
    }

    public double getFireSpeed() {
        return fireSpeed;
    }

    public void setFireSpeed(double fireSpeed) {
        this.fireSpeed = fireSpeed;
    }
    public RectF getRect(){
        RectF r = new RectF(getX(),getY(),(float)(getX()+width),(float)(getY()+height*0.7));
        return r;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }
}
