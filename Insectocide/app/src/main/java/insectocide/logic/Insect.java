package insectocide.logic;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import insectocide.game.R;

/**
 * Created by Zukis87 on 06/02/2016.
 */

public class Insect extends SpaceEntity {

    private final int MAX_INSECTS = 10;
    protected InsectType type;
    private String shootDirection;

    public Insect(InsectType type ,Context context,DisplayMetrics metrics) {
        super(context,metrics);
        this.type = type;
        this.shootDirection = "down"; // need to be changed for multiplayer
        setImageByType();
        initPowersByType();
        setVisibility(View.VISIBLE);
        animation = (AnimationDrawable)this.getBackground();
        animation.start();
    }

    @Override
    public void move(String direction) {

    }

    private void setImageByType(){
        int drawableId = getResources().getIdentifier(type.getColor()+"_animation", "drawable", "insectocide.game");
        this.setBackgroundResource(drawableId);
    }

    public void setPositionAndDimensions(int i , int j){
        this.width = this.metrics.heightPixels*0.09*1.3;
        this.height = this.metrics.heightPixels*0.09;
        this.setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) height));
        this.x = this.metrics.widthPixels + (this.metrics.widthPixels - (MAX_INSECTS*width + (MAX_INSECTS-1)*this.metrics.widthPixels*0.02))/2 + (i*width) + (i*this.metrics.widthPixels*0.02);
        this.y = this.metrics.heightPixels*0.35 + j*height;
        this.setY((float)y);
        this.setX((float)x);
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
        this.health=2;
        this.movementSpeed=2;
        this.firePower=1;
        this.fireSpeed=2;
    }

    public String getShootDirection() {
        return shootDirection;
    }

    public void setShootDirection(String shootDirection) {
        this.shootDirection = shootDirection;
    }
}
