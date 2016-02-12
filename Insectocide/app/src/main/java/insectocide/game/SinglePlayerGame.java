package insectocide.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import insectocide.logic.Insect;
import insectocide.logic.InsectType;
import insectocide.logic.Shot;
import insectocide.logic.SpaceShip;

public class SinglePlayerGame extends Activity implements SensorEventListener {
    private final String DEFAULT_COLOR = "red";
    private final int INSECTS_ROWS = 3;
    private final int INSECTS_COLS = 10;
    private final long SHOOT_DELAY = 500;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip ship;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private Handler handler;
    private List<Shot> shoots;
    private RelativeLayout rl;
    private long lastShootTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);
        shoots = new ArrayList<>();
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);

        initShip();
        initInsects();

        //delay the sensors for the start animations

        lastShootTime = 7000;

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                initAccelerometer();
                ;
            }
        }, 7000);

    }

    @Override
    protected void onStart(){
        super.onStart();
        Thread checkShots = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        for (Shot s : shoots) {
                            s.shoot();
                        }

                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        checkShots.start();
    }

    private void initShip(){
        ship = new SpaceShip(DEFAULT_COLOR, this , metrics);
        ship.bringToFront();
        rl.addView(ship);
    }

    private void initInsects(){
        insects = new Insect[INSECTS_COLS][INSECTS_ROWS];
        for (int i=0 ; i < INSECTS_COLS ; i++){
            for(int j=0 ; j < INSECTS_ROWS ; j++){
                insects[i][j] = new Insect(pickRandomType(),this,this.metrics);
                insects[i][j].setPositionAndDimensions(i,j);
                insects[i][j].bringToFront();
                rl.addView(insects[i][j]);
            }
        }
    }

    public InsectType pickRandomType(){
        Random rand = new Random();
        int n = rand.nextInt(5);
        return InsectType.values()[n];
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

    public boolean onTouchEvent(MotionEvent event) {
        long time = event.getDownTime();
        if(time-lastShootTime > SHOOT_DELAY ) {
            Shot s = ship.fire();
            shoots.add(s);
            s.bringToFront();
            rl.addView(s);
            lastShootTime = time;
        }
        return false;
    }
}
