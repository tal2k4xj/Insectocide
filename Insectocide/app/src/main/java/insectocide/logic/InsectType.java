package insectocide.logic;

import java.util.Random;

/**
 * Created by Zukis87 on 06/02/2016.
 */
public enum InsectType {
    SpeedyShoot("pink"),PowerShoot("orange"),ExtraHealth("blue"),Normal("brown"),DoubleShoot("green");
    private final String color;
    InsectType(String color){
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
