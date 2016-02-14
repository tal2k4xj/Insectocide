package insectocide.logic;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

public class SpaceShip extends SpaceEntity {
    private final int MAX_HEALTH = 5;
    private static String lastMovement;

    public SpaceShip(String color, Context context , DisplayMetrics metrics) {
        super(context, metrics);
        this.color = color;
        setDefaults();
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
        width = metrics.heightPixels*0.2/1.5;
        height = metrics.heightPixels*0.2;
        setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) height));
        x = metrics.widthPixels / 2 - metrics.heightPixels * 0.2 / 1.5 / 2;
        setX((float) x);
        if (color == "red") {
            y = metrics.heightPixels;
            setY((float) y);
            animate().y((float) (getY() - metrics.heightPixels * 0.2));
        }
        else if (color == "blue") {
            y = 0 - height;
            setY((float) y);
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
                if (getX() < metrics.widthPixels-width)
                    setX(getX()+ (movementSpeed*2));
                break;
            case "right3":
                if (getX() < metrics.widthPixels-width)
                    setX(getX()+ (movementSpeed*3));
                break;
            }
        lastMovement=direction;
    }

    private void setDefaults() {
        health=3;
        movementSpeed=2;
        firePower=1;
        fireSpeed=2;
    }

    public void getPowerFromInsect(InsectType type) {
        switch(type){
            case ExtraHealth:
                if (health <MAX_HEALTH)
                    health+=1;
                break;
            case PowerShoot:
                firePower+=1;
                break;
            case SpeedyShoot:
                fireSpeed+=1;
                break;
            case DoubleShoot:
                movementSpeed+=1;
                break;
        }
    }

    public void resetPowers() {
        movementSpeed-=1;
        firePower-=1;
        fireSpeed-=1;
    }
}
