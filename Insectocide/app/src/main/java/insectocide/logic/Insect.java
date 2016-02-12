package insectocide.logic;

import android.content.Context;
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
    private double y;
    private double x;
    private double width;
    private double height;
    private DisplayMetrics metrics;

    public Insect(InsectType type ,Context context,DisplayMetrics metrics) {
        super(context);
        this.type = type;
        setImageByType();
        initPowersByType();
        setVisibility(View.VISIBLE);
        this.metrics = metrics;
    }

    @Override
    public void fire() {

    }

    @Override
    public void move(String direction) {

    }

    private void setImageByType(){
        switch (type){
            case SpeedyShoot:
                setBackgroundResource(R.drawable.pink_animation);
                break;
            case PowerShoot:
                setBackgroundResource(R.drawable.orange_animation);
                break;
            case ExtraHealth:
                setBackgroundResource(R.drawable.blue_animation);
                break;
            case Normal:
                setBackgroundResource(R.drawable.brown_animation);
                break;
            case DoubleShoot:
                setBackgroundResource(R.drawable.green_animation);
                break;
        }

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

}
