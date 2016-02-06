package insectocide.logic;

/**
 * Created by Zukis87 on 06/02/2016.
 */
public enum InsectType {
    SpeedyShoot("Pink"),PowerShoot("Orange"),ExtraHealth("Blue"),Normal("Brown"),DoubleShoot("Green");
    private final String color;
    InsectType(String color){
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
