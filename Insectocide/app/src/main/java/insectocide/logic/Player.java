package insectocide.logic;

public class Player {
    private String name;
    private int score;
    private SpaceShip ship;

    public Player(SpaceShip ship) {
        this.ship = ship;
        name = "";
        score = 0;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public SpaceShip getShip() {
        return ship;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        score = score;
    }

    public void setShip(SpaceShip ship) {
        this.ship = ship;
    }
}
