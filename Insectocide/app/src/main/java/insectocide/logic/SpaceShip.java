package insectocide.logic;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

public class SpaceShip extends SpaceEntity {
    private final int MAX_ENTITY = 6;
    private final int MIN_ENTITY = 2;
    private final int DEFAULT_HEALTH = 3;
    private static String lastMovement;

    public SpaceShip(String color, Context context , DisplayMetrics metrics) {
        super(context,metrics);
        this.firePower = MIN_ENTITY;
        this.fireSpeed = MIN_ENTITY;
        this.health = DEFAULT_HEALTH;
        this.movementSpeed = MIN_ENTITY;
        this.color = color;
        lastMovement = "middle";
        setVisibility(View.VISIBLE);
        int startAnimationId = getResources().getIdentifier(color.toLowerCase()+"_ship_animation", "drawable", "insectocide.game");
        setBackgroundResource(startAnimationId);
        animation = (AnimationDrawable) this.getBackground();
        animation.start();
        setPositionAndDimensions();
    }

    private void setImage(String imageName) {
        int drawableId = getResources().getIdentifier(imageName, "drawable", "insectocide.game");
        this.setBackgroundResource(drawableId);
    }

    public void setPositionAndDimensions(){
        this.width = metrics.heightPixels*0.2/1.5;
        this.height = metrics.heightPixels*0.2;
        this.setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) height));
        this.x = metrics.widthPixels / 2 - metrics.heightPixels * 0.2 / 1.5 / 2;
        this.setX((float) x);
        if (color == "red") {
            this.y = metrics.heightPixels;
            this.setY((float) y);
            animate().y((float) (getY() - metrics.heightPixels * 0.2));
        }
        else if (color == "blue") {
            this.y = 0 - this.height;
            this.setY((float) y);
            animate().y((float) (getY() + metrics.heightPixels * 0.2));

        }
        animate().setDuration(7000);
        animate().start();
    }

    public String getColor (){
        return this.color;
    }

    @Override
    public void move(String direction) {
        if (!direction.equals(lastMovement)) {
            setImage(color + direction);
        }
        switch (direction) {
            case "left2":
                if (getX() > 0)
                    setX(getX()- (movementSpeed*2));
                break;
            case "left3":
                if (getX() > 0)
                    setX(getX()- (movementSpeed*3));
                break;
            case "right2":
                if (getX() < metrics.widthPixels-this.width)
                    setX(getX()+ (movementSpeed*2));
                break;
            case "right3":
                if (getX() < metrics.widthPixels-this.width)
                    setX(getX()+ (movementSpeed*3));
                break;
            }
        lastMovement=direction;
    }
}
