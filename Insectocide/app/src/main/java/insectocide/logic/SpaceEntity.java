package insectocide.logic;

import android.content.Context;
import android.media.Image;
import android.widget.ImageView;

/**
 * Created by Zukis87 on 06/02/2016.
 */
public abstract class SpaceEntity extends ImageView {
    protected int firePower;
    protected int fireSpeed;
    protected int health;
    protected int movementSpeed;
    Coordinate centerLocation;

    public SpaceEntity(Context context) {
        super(context);
    }

    public abstract void fire();
    public abstract void move(String direction);
}
