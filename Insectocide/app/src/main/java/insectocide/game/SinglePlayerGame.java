package insectocide.game;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;
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
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip redShip;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private Handler handler;
    private List<Shot> shoots;
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
        shoots = new ArrayList<>();
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);

        initShip();
        initInsects();

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                isStartAnimationDone = true;
                initAccelerometer();
                initMoveShotsThread();
                startInsectsShotsThread();
                checkIfShotHit();
                ;
            }
        }, 7000);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if (isActivityPaused){
            isActivityPaused = false;
            initAccelerometer();
            initMoveShotsThread();
            startInsectsShotsThread();
            checkIfShotHit();
        }

    }

    private void initShip(){
        redShip = new SpaceShip(DEFAULT_COLOR, this , metrics);
        redShip.bringToFront();
        rl.addView(redShip);
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

    private void startInsectsShotsThread(){
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
                                    shoots.add(s);
                                    s.bringToFront();
                                    rl.addView(s);
                                }
                            }
                        });
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        insectShots.start();
    }

    private void initMoveShotsThread() {
        moveShots = new Thread(new Runnable() {
            public void run() {
                while (!isActivityPaused) {
                    try {
                        for (final Shot s : shoots) {
                          if(!s.isOutOfScreen()) {
                              runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      s.shoot();
                                  }
                              });
                           }else{
                              removeView(s);
                              shoots.remove(s);
                           }
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        moveShots.start();
    }

    private void checkIfShotHit() {
        checkHitShots = new Thread(new Runnable() {
            public void run() {
                try{
                    while (!isActivityPaused) {
                       for (Shot s : shoots) {
                           RectF r1 = new RectF(s.getLeft(),s.getTop(),s.getRight(),s.getBottom());
                           if(s.getEntity() instanceof SpaceShip){
                               checkIfInsectHit(r1,s);
                            }else if (s.getEntity() instanceof Insect){
                               checkIfShipHit(r1,s);
                            }
                        }
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        checkHitShots.start();
    }

    private void checkIfInsectHit(RectF r1,Shot s){
        for (int i=0; i<INSECTS_COLS ; i++){
            for (int j=0; j<INSECTS_ROWS ; j++){
                if(insects[i][j]!=null) {
                    RectF r2 = new RectF(insects[i][j].getLeft(),insects[i][j].getTop(),insects[i][j].getRight(),insects[i][j].getBottom());
                    if (r1.intersect(r2)) {
                        insects[i][j].gotHit(s.getPower());
                        if (insects[i][j].isDead()) {
                            removeView(insects[i][j]);
                            insects[i][j] = null;
                        }
                        removeView(s);
                        shoots.remove(s);
                    }
                }
            }
        }
    }

    private void checkIfShipHit(RectF r1,Shot s){
        RectF r2 = new RectF(redShip.getLeft(),redShip.getTop(),redShip.getRight(),redShip.getBottom());
        if (r1.intersect(r2)){
            redShip.gotHit(s.getPower());
            if(redShip.isDead()){
                removeView(redShip);
            }
            removeView(s);
            shoots.remove(s);
        } // need to add blue ship for multiplayer
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
        if (curMovement >0.5) {
            if(curMovement>1.5){
                redShip.move("right3");
            }else {
                redShip.move("right2");
            }
        }else if(curMovement<-0.5){
            if(curMovement<-1.5){
                redShip.move("left3");
            }else {
                redShip.move("left2");
            }
        }else{
            redShip.move("middle");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean onTouchEvent(MotionEvent event) {
        long time = event.getDownTime();
        if(time-lastShootTime > SHOOT_DELAY && isStartAnimationDone) {
            Shot s = redShip.fire();
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
        checkHitShots.interrupt();
        checkHitShots = null;
        insectShots = null;
        moveShots = null;
        unRegisterAccelerometer();
    }
}
