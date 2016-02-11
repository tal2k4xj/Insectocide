package insectocide.game;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;

import java.util.Random;

import insectocide.logic.Insect;
import insectocide.logic.InsectType;
import insectocide.logic.SpaceShip;

public class SinglePlayerGame extends Activity implements SensorEventListener {
    private final String DEFAULT_COLOR = "red";
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip ship;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private AnimationDrawable insectAnimations[][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //get maxX and maxY that are the max value of the screen. the min is 0.
        //link: http://stackoverflow.com/questions/11483345/how-do-android-screen-coordinates-work

        /* i think we dont need this part
        Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        */

        //int maxX = mdispSize.x;

        //add ship add insect + placement + adjust size

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);

        initShip(rl);
        initInsects(rl);

        initAccelerometer();
    }

    private void initShip(RelativeLayout rl){
        ship = new SpaceShip(DEFAULT_COLOR, this , metrics);
        ship.bringToFront();
        rl.addView(ship);
    }

    private void initInsects(RelativeLayout rl){
        insects = new Insect[10][3];
        insectAnimations = new AnimationDrawable[10][3];
        for (int i=0 ; i < 10 ; i++){
            for(int j=0 ; j < 3 ; j++){
                insects[i][j] = new Insect(pickRandomType(),this,this.metrics);
                insects[i][j].setPositionAndDimensions(i,j);
                insectAnimations[i][j] = (AnimationDrawable) insects[i][j].getBackground();
                insectAnimations[i][j].start();
                insects[i][j].bringToFront();
                rl.addView(insects[i][j]);
            }
        }
    }

    public InsectType pickRandomType(){
        Random rand = new Random();
        int n = rand.nextInt(5);
        switch (n){
            case 0:
                return InsectType.SpeedyShoot;
            case 1:
                return InsectType.PowerShoot;
            case 2:
                return InsectType.ExtraHealth;
            case 3:
                return InsectType.DoubleShoot;
        }
        return InsectType.Normal;
    }

    private void initAccelerometer() {
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float curMovement = event.values[1];
        moveShip(curMovement);
    }

    private void moveShip(float curMovement) {
        if (curMovement >0.5) {
            if(curMovement>1.5){
                ship.move("right3");
            }else {
                ship.move("right2");
            }
        }else if(curMovement<-0.5){
            if(curMovement<-1.5){
                ship.move("left3");
            }else {
                ship.move("left2");
            }
        }else{
            ship.move("middle");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
