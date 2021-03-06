package insectocide.logic;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;


public class Insect extends SpaceEntity {

    private final int MAX_INSECTS = 10;
    private final int SINGLE_PLAYER_ROWS = 3;
    protected InsectType type;
    private String shootDirection;

    public Insect(InsectType type,String shootDirection, Context context, DisplayMetrics metrics) {
        super(context,metrics);
        this.type = type;
        this.shootDirection = shootDirection;
        setImageByType();
        initPowersByType();
        setVisibility(View.VISIBLE);
        animation = (AnimationDrawable)this.getBackground();
        animation.start();
    }

    @Override
    public void move(String direction) {
        switch (direction) {
            case "left":
                setX(getX()- (float)(movementSpeed*(metrics.widthPixels*0.0007)));
                break;
            case "right":
                setX(getX()+ (float)(movementSpeed*(metrics.widthPixels*0.0007)));
                break;
        }
    }

    private void setImageByType(){
        int drawableId = getResources().getIdentifier(type.getColor()+"_animation", "drawable", "insectocide.game");
        this.setBackgroundResource(drawableId);
    }

    public void setPositionAndDimensions(int i , int j , int rows){
        width = metrics.heightPixels*0.09*1.3;
        height = metrics.heightPixels*0.09;
        setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) height));
        x = metrics.widthPixels + (metrics.widthPixels - (MAX_INSECTS * width + (MAX_INSECTS - 1) * metrics.widthPixels * 0.02)) / 2 + (i * width) + (i * metrics.widthPixels * 0.02);
        if (rows == SINGLE_PLAYER_ROWS) {
            y = metrics.heightPixels * 0.35 + j * height;
        }else{
            y = metrics.heightPixels * 0.32 + j * height;
        }
        setY((float) y);
        setX((float)x);
        animate().x(getX() - this.metrics.widthPixels);
        animate().setDuration(7000);
        animate().start();
    }

    private void initPowersByType(){
        setDefaults();
        switch (type){
            case SpeedyShoot:
                this.fireSpeed=4;
                break;
            case PowerShoot:
                this.firePower=2;
                break;
            case ExtraHealth:
                this.health=4;
            case Normal:
                break;
            case DoubleShoot:
                break;
        }
    }

    private void setDefaults() {
        health=2;
        movementSpeed=4;
        firePower=1;
        fireSpeed=2;
    }

    public String getShootDirection() {
        return shootDirection;
    }

    public void setShootDirection(String shootDirection) {
        this.shootDirection = shootDirection;
    }

    public InsectType getType() {
        return type;
    }

    public void die(){
        int drawableId = getResources().getIdentifier("bug_die_animation" , "drawable", "insectocide.game");
        this.setBackgroundResource(drawableId);
        animation = (AnimationDrawable)this.getBackground();
        animation.start();
    }
}
