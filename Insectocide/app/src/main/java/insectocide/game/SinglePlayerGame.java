package insectocide.game;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import java.util.Random;

import insectocide.logic.Insect;
import insectocide.logic.InsectType;
import insectocide.logic.SpaceShip;

public class SinglePlayerGame extends Activity implements SensorEventListener {
    private final String DEFAULT_COLOR = "red";
    private final int INSECTS_ROWS = 3;
    private final int INSECTS_COLS = 10;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip ship;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private AnimationDrawable insectAnimations[][];
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //add ship add insect + placement + adjust size

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);

        initShip(rl);
        initInsects(rl);

        //delay the sensors for the start animations

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                initAccelerometer();
                ;
            }
        }, 7000);

    }

    private void initShip(RelativeLayout rl){
        ship = new SpaceShip(DEFAULT_COLOR, this , metrics);
        ship.setBackgroundResource(R.drawable.red_ship_animation);
        AnimationDrawable redShipAnimation = (AnimationDrawable) ship.getBackground();
        redShipAnimation.start();
        ship.bringToFront();
        rl.addView(ship);
    }

    private void initInsects(RelativeLayout rl){
        insects = new Insect[INSECTS_COLS][INSECTS_ROWS];
        insectAnimations = new AnimationDrawable[INSECTS_COLS][INSECTS_ROWS];
        for (int i=0 ; i < INSECTS_COLS ; i++){
            for(int j=0 ; j < INSECTS_ROWS ; j++){
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
