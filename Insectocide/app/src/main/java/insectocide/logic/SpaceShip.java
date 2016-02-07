package insectocide.logic;

import android.content.Context;
import android.view.View;

public class SpaceShip extends SpaceEntity {
    private final int MAX_ENTITY = 6;
    private final int MIN_ENTITY = 2;
    private final int DEFAULT_HEALTH = 3;
    private String color;
    private static String lastMovement;

    public SpaceShip(String color, Context context) {
        super(context);
        this.firePower = MIN_ENTITY;
        this.fireSpeed = MIN_ENTITY;
        this.health = DEFAULT_HEALTH;
        this.movementSpeed = MIN_ENTITY;
        this.color = color;
        lastMovement = "middle";
        setVisibility(View.VISIBLE);
        setY(0);
        setImage(color + lastMovement);
    }

    private void setImage(String imageName) {
        int drawableId = getResources().getIdentifier(imageName, "drawable", "insectocide.game");
        this.setBackgroundResource(drawableId);
    }

    @Override
    public void fire() {

    }

    @Override
    public void move(String direction) {
        if (!direction.equals(lastMovement)) {
            setImage(color + direction);
        }
        switch (direction) {
            case "left2":
                setX(getX()- (movementSpeed*2));
                break;
            case "left3":
                setX(getX()- (movementSpeed*3));
                break;
            case "right2":
                setX(getX()+ (movementSpeed*2));
                break;
            case "right3":
                setX(getX()+ (movementSpeed*3));
                break;
            }
        lastMovement=direction;
    }
}
