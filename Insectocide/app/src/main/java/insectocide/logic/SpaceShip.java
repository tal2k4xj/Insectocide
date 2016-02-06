package insectocide.logic;

import android.content.Context;

/**
 * Created by Zukis87 on 06/02/2016.
 */
public class SpaceShip extends SpaceEntity {
    private final int MAX_ENTITY = 6;
    private final int MIN_ENTITY = 1;
    private final int DEFAULT_HEALTH = 3;

    public SpaceShip(Context context) {
        super(context);
        this.firePower = MIN_ENTITY;
        this.fireSpeed = MIN_ENTITY;
        this.health = DEFAULT_HEALTH;
        this.movementSpeed = MIN_ENTITY;
    }

    @Override
    public void fire() {

    }

    @Override
    public void move(String direction) {

    }
}
