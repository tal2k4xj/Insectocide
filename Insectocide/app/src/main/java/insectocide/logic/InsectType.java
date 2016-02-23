package insectocide.logic;

public enum InsectType {
    Normal("brown"), SpeedyShoot("pink"), PowerShoot("orange"), ExtraHealth("blue"), DoubleShoot("green");
    private final String color;
    InsectType(String color){
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
