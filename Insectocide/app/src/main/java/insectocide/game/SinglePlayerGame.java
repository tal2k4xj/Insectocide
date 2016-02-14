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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    private final long START_ANIMATION_DELAY = 7200;
    private int timeOfGame;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip spaceShip;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private Handler handler;
    private List<Shot> shipShoots;
    private List<Shot> insectsShoots;
    private List<Insect> liveInsects;
    private List<ImageView> shipLives;
    private RelativeLayout rl;
    private long lastShootTime = 0;
    private boolean isActivityPaused = false;
    private boolean isStartAnimationDone = false;
    private Thread insectShots;
    private Thread moveShots;
    private Thread timer;
    private TextView scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);
        shipShoots = new CopyOnWriteArrayList<>();
        insectsShoots = new CopyOnWriteArrayList<>();
        liveInsects = new CopyOnWriteArrayList<>();
        shipLives = new CopyOnWriteArrayList<>();
        metrics = new DisplayMetrics();
        timeOfGame = 0;
        scoreText = (TextView) findViewById(R.id.ScoreText);
        scoreText.setVisibility(View.INVISIBLE);

        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);

        initShip();
        upDateLives();
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
                startTimer();
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
                liveInsects.add(insects[i][j]);
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
                                int i, j;
                                do {
                                    i = rand.nextInt(INSECTS_COLS);
                                    j = rand.nextInt(INSECTS_ROWS);
                                } while (insects[i][j].isDead());
                                Shot s = insects[i][j].fire();
                                insectsShoots.add(s);
                                s.bringToFront();
                                rl.addView(s);
                            }
                        });
                        int timeToSleep = calculateInsectShootingTime();
                        Thread.sleep(timeToSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            private int calculateInsectShootingTime() {
                int numOfInsects = liveInsects.size();
                if (numOfInsects>=25){
                    return 500;
                }else if(numOfInsects>=20){
                    return 850;
                }else if(numOfInsects>=15){
                    return 1300;
                }else if(numOfInsects>=10){
                    return 1750;
                }else {
                    return 2200;
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
                                upDateLives();;
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
    private void checkIfInsectHit(Shot shot){
        RectF r1 = shot.getRect();
        for(Insect insect:liveInsects){
            RectF r2 = insect.getRect();
            if (r1.intersect(r2)) {
                insect.gotHit(shot.getPower());
                if (insect.isDead()) {
                    insect.die();
                    spaceShip.getPowerFromInsect(insect.getType());
                    showInsectBonus(insect);
                    liveInsects.remove(insect);
                    //removeView(insect);
                }
                if (liveInsects.size()==0){
                    winGame();
                }
                removeShot(shot);
            }
        }
    }

    private void showInsectBonus(Insect insect){
        final ImageView bonus = new ImageView(this);
        int drawableId = getResources().getIdentifier(insect.getType().getColor().toLowerCase()+"_bonus" , "drawable", "insectocide.game");
        bonus.setBackgroundResource(drawableId);
        double length = metrics.heightPixels*0.1;
        bonus.setLayoutParams(new ViewGroup.LayoutParams((int) length, (int) length));
        bonus.setY(insect.getY());
        bonus.setX(insect.getX());
        bonus.bringToFront();
        bonus.setVisibility(View.VISIBLE);
        rl.addView(bonus);
        bonus.animate().y(bonus.getY() - 100);
        bonus.animate().setDuration(3000);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                bonus.setVisibility(View.INVISIBLE);
                ;
            }
        }, 3000);
    }

    private void winGame() {
        endGame();
    }

    private void endGame() {
        int score = calcScore();
        scoreText.setText("Score: " + score);
        scoreText.setVisibility(View.VISIBLE);
    }

    private int calcScore() {
        return (getNumOfKilledInsects()*10000)/(timeOfGame);
    }

    private int getNumOfKilledInsects() {
        return (INSECTS_COLS*INSECTS_ROWS)-liveInsects.size();
    }

    private void startTimer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (!spaceShip.isDead()&& liveInsects.size()>0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            timeOfGame++;
                        }
                    });
                }
            }
        };
        timer = new Thread(runnable);
        timer.start();
    }
    private void checkIfShipHit(Shot s){
        RectF r1 = s.getRect();
        RectF r2 = spaceShip.getRect();
        if (r1.intersect(r2)){
            spaceShip.gotHit(s.getPower());
            spaceShip.reducePowers();
            if(spaceShip.isDead()){
                loseGame();
            }
            removeShot(s);
        } // need to add blue ship for multiplayer
    }

    private void loseGame() {
        endGame();
        removeView(spaceShip);
    }

    private void removeShot(Shot s) {
        removeView(s);
        s.destroy();
        if (s.getEntity() instanceof SpaceShip){
            shipShoots.remove(s);
        }else
            insectsShoots.remove(s);
    }

    private void upDateLives(){
        if (shipLives.size() > spaceShip.getHealth()){
            removeShipLive();
        }else if (shipLives.size() < spaceShip.getHealth()){
            drawShipLive();
        }
    }

    private void drawShipLive(){
        for (int i=shipLives.size(); i<spaceShip.getHealth() ; i++){
            ImageView live = new ImageView(this);
            live.setBackgroundResource(R.drawable.live);
            double length = metrics.heightPixels*0.1;
            live.setLayoutParams(new ViewGroup.LayoutParams((int) length, (int) length));
            live.setY(0);
            double x = i*length;
            live.setX((float) x);
            live.bringToFront();
            live.setVisibility(View.VISIBLE);
            rl.addView(live);
            shipLives.add(live);
        }
    }

    private void removeShipLive(){
        for (int i=(int)spaceShip.getHealth(); i<shipLives.size() ; i++){
            ImageView live = new ImageView(this);
            live = shipLives.get(i);
            live.setVisibility(View.VISIBLE);
            rl.removeView(live);
            shipLives.remove(live);
        }
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
            if(curMovement>2){
                spaceShip.move("right3");
            }else {
                spaceShip.move("right2");
            }
        }else if(curMovement<-1){
            if(curMovement<-2){
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
        if(isStartAnimationDone){
            insectShots.interrupt();
            moveShots.interrupt();
            insectShots = null;
            moveShots = null;
            unRegisterAccelerometer();
        }
        isActivityPaused = true;
    }
}
