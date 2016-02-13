package insectocide.game;

import android.app.Activity;
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
    private final long SHOOT_DELAY = 1000;
    private final long START_ANIMATION_DELAY = 8000;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip ship;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private Handler accelerometerHandler;
    private List<Shot> shoots;
    private RelativeLayout rl;
    private long lastShootTime = 0;
    private boolean isActivityPaused = false;
    private boolean isStartAnimationDone = false;
    private Thread insectShots;
    private Thread moveShots;

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

        initAccelerometerWithDelay();
    }

    private void initAccelerometerWithDelay() {
        accelerometerHandler = new Handler();
        accelerometerHandler.postDelayed(new Runnable() {
            public void run() {
                isStartAnimationDone = true;
                initAccelerometer();
                initCheckShotsThread();
                startInsectsShots();
                ;
            }
        }, START_ANIMATION_DELAY);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if (isActivityPaused){
            isActivityPaused = false;
            initAccelerometer();
            initCheckShotsThread();
            startInsectsShots();
        }

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

    private void unRegisterAccelerometer(){
        sm.unregisterListener(this, accelerometer);
    }

    private void startInsectsShots(){
        insectShots = new Thread(new Runnable() {
            public void run() {
                while (!isActivityPaused) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Random rand = new Random();
                                int i = rand.nextInt(INSECTS_COLS);
                                int j = rand.nextInt(INSECTS_ROWS);
                                Shot s = insects[i][j].fire();
                                shoots.add(s);
                                s.bringToFront();
                                rl.addView(s);
                            }
                        });
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        insectShots.start();
    }

    private void initCheckShotsThread() {
        moveShots = new Thread(new Runnable() {
            public void run() {
                while (!isActivityPaused) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (Shot s : shoots) {
                                    if(!s.isOutOfScreen()) {
                                        s.shoot();
                                    }else{
                                        s.destrtoy();
                                        rl.removeView(s);
                                        shoots.remove(s);
                                    }
                                }
                            }
                        });
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        moveShots.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float curMovement = event.values[1];
        moveShip(curMovement);
    }

    private void moveShip(float curMovement) {
        if (curMovement >1) {
            if(curMovement>2.5){
                ship.move("right3");
            }else {
                ship.move("right2");
            }
        }else if(curMovement<-1){
            if(curMovement<-2.5){
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
        if(time-lastShootTime > SHOOT_DELAY && isStartAnimationDone) {
            Shot s = ship.fire();
            shoots.add(s);
            s.bringToFront();
            rl.addView(s);
            lastShootTime = time;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityPaused = true;
        insectShots.interrupt();
        moveShots.interrupt();
        insectShots = null;
        moveShots = null;
        unRegisterAccelerometer();
    }
}
