package insectocide.logic;

import android.content.Context;
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
    private DisplayMetrics metrics;

    public SpaceEntity(Context context) {
        super(context);
    }

    public void fire(){
        shot = new Shot(firePower,fireSpeed,this);
        shot.activate();
    }

    public String getColor (){
        return this.color;
    }

    public abstract void move(String direction);
}
