package insectocide.logic;

import android.content.Context;

/**
 * Created by DELL1 on 2/12/2016.
 */
public class Shot {

    private int power;
    private int speed;
    private double width;
    private double height;

    public Shot (int power , int speed , SpaceEntity entity){
        if (entity instanceof SpaceShip){
            if(entity.getColor() == "red"){

            }else if(entity.getColor() == "blue"){

            }
        }else{

        }
    }

    public void activate(){

    }
}
