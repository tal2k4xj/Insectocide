package insectocide.game;

import android.app.Activity;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private SpaceShip spaceShip;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private Handler handler;
    private List<Shot> shipShoots;
    private List<Shot> insectsShoots;
    private RelativeLayout rl;
    private long lastShootTime = 0;
    private boolean isActivityPaused = false;
    private boolean isStartAnimationDone = false;
    private Thread insectShots;
    private Thread moveShots;
    private Thread checkHitShots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);
        shipShoots = new CopyOnWriteArrayList<>();
        insectsShoots = new CopyOnWriteArrayList<>();
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);

        initShip();
        initInsects();
        initHandler();
    }

    private void initHandler() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                isStartAnimationDone = true;
                initAccelerometer();
                initMoveShotsThread();
                startInsectsShotsThread();
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
            initMoveShotsThread();
            startInsectsShotsThread();
        }

    }

    private void initShip(){
        spaceShip = new SpaceShip(DEFAULT_COLOR, this , metrics);
        spaceShip.bringToFront();
        rl.addView(spaceShip);
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

    private synchronized void startInsectsShotsThread(){
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
                                if(insects[i][j] != null) {
                                    Shot s = insects[i][j].fire();
                                    insectsShoots.add(s);
                                    s.bringToFront();
                                    rl.addView(s);
                                }
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        insectShots.start();
    }

    private synchronized void initMoveShotsThread() {
        moveShots = new Thread(new Runnable() {
            public void run() {
                while (!isActivityPaused) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (Shot s : insectsShoots) {
                                    if(!s.isOutOfScreen()) {
                                        s.shoot();
                                        checkIfShipHit(s);
                                    }else{
                                        removeShot(s);
                                    }
                                }
                                for (Shot s : shipShoots) {
                                    if(!s.isOutOfScreen()) {
                                        s.shoot();
                                        checkIfInsectHit(s);
                                    }else{
                                        removeShot(s);
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
//
//    private void checkIfShotHit() {
//        checkHitShots = new Thread(new Runnable() {
//            public void run() {
//                try{
//                    while (!isActivityPaused) {
//                       for (Shot s : shipShoots) {
//
//                        }
//                        for (Shot s : insectsShoots) {
//
//                        }
//                    }
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        checkHitShots.start();
//    }

    private void checkIfInsectHit(Shot s){
        RectF r1 = s.getRect();
        for (int i=0; i<INSECTS_COLS ; i++){
            for (int j=0; j<INSECTS_ROWS ; j++){
                if(insects[i][j]!=null) {
                    RectF r2 = insects[i][j].getRect();
                    if (r1.intersect(r2)) {
                        insects[i][j].gotHit(s.getPower());
                        if (insects[i][j].isDead()) {
                            removeView(insects[i][j]);
                            spaceShip.getPowerFromInsect(insects[i][j].getType());
                            insects[i][j] = null;
                        }
                        removeShot(s);
                    }
                }
            }
        }
    }

    private void checkIfShipHit(Shot s){
        RectF r1 = s.getRect();
        RectF r2 = spaceShip.getRect();
        if (r1.intersect(r2)){
            spaceShip.gotHit(s.getPower());
            if(spaceShip.isDead()){
                removeView(spaceShip);
            }
            removeShot(s);
        } // need to add blue ship for multiplayer
    }

    private void removeShot(Shot s) {
        removeView(s);
        s.destroy();
        if (s.getEntity() instanceof SpaceShip){
            shipShoots.remove(s);
        }else
            insectsShoots.remove(s);

    }

    private void removeView(final ImageView v){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rl.removeView(v);
            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float curMovement = event.values[1];
        moveShip(curMovement);
    }

    private void moveShip(float curMovement) {

        if (curMovement >1) {
            if(curMovement>2.5){
                spaceShip.move("right3");
            }else {
                spaceShip.move("right2");
            }
        }else if(curMovement<-1){
            if(curMovement<-2.5){
                spaceShip.move("left3");
            }else {
                spaceShip.move("left2");
            }
        }else{
            spaceShip.move("middle");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean onTouchEvent(MotionEvent event) {
        long time = event.getDownTime();
        if(isStartAnimationDone && time-lastShootTime > SHOOT_DELAY ) {
            Shot s = spaceShip.fire();
            shipShoots.add(s);
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
        checkHitShots.interrupt();
        checkHitShots = null;
        insectShots = null;
        moveShots = null;
        unRegisterAccelerometer();
    }
}
