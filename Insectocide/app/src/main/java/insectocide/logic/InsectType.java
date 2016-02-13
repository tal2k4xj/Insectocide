package insectocide.logic;

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
