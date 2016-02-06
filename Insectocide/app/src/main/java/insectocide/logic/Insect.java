package insectocide.logic;

import android.content.Context;

/**
 * Created by Zukis87 on 06/02/2016.
 */
public class Insect extends SpaceEntity {
    protected InsectType type;

    public Insect(InsectType type ,Context context) {
        super(context);
        this.type = type;
        setImageByType();
        initPowersByType();
    }

    @Override
    public void fire() {

    }

    @Override
    public void move(String direction) {

    }

    private void setImageByType(){
        String color = type.getColor();

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
