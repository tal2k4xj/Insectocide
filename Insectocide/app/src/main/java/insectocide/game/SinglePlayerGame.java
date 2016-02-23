package insectocide.game;

import android.app.Activity;
import android.content.res.TypedArray;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import insectocide.logic.Insect;
import insectocide.logic.InsectType;
import insectocide.logic.InsectsProvider;
import insectocide.logic.Shot;
import insectocide.logic.SpaceShip;

public class SinglePlayerGame extends Activity implements SensorEventListener,View.OnClickListener {
    private final String DEFAULT_COLOR = "red";
    private final int INSECTS_ROWS = 3;
    private final int INSECTS_COLS = 10;
    private final long SHOOT_DELAY = 800;
    private final long START_ANIMATION_DELAY = 7200;
    private int timeOfGame;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip spaceShip;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private Handler startDelayHandler;
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
    private ImageButton pauseButton;
    private List<InsectType> insectTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);
        shipShoots = new CopyOnWriteArrayList<>();
        insectsShoots = new CopyOnWriteArrayList<>();
        shipLives = new CopyOnWriteArrayList<>();

        startDelayHandler = new Handler();
        timeOfGame = 0;

        scoreText = (TextView) findViewById(R.id.ScoreText);
        scoreText.setVisibility(View.INVISIBLE);
        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);

        initShip();
        updateLives();
        initInsects();
        initHandler();
        initReadyGo();
    }

    private void initHandler() {
        startDelayHandler.postDelayed(new Runnable() {
            public void run() {
                isStartAnimationDone = true;
                initAccelerometer();
                initMoveShotsThread();
                startInsectsShotsThread();
                startTimer();
            }
        }, START_ANIMATION_DELAY);
    }

    @Override
    protected void onStart(){
        super.onStart();
        resumePauseGame();
    }

    private void initShip(){
        spaceShip = new SpaceShip(DEFAULT_COLOR, this , metrics);
        spaceShip.bringToFront();
        rl.addView(spaceShip);
    }

    private void initInsects(){
        InsectsProvider insectsProvider = new InsectsProvider(INSECTS_ROWS, INSECTS_COLS, this, metrics);
        liveInsects = insectsProvider.getLiveInsectsList();
        insects = insectsProvider.getInsectMatrix();
        for (Insect insect: liveInsects) {
            rl.addView(insect);
        }
    }


    private void initAccelerometer() {
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void unRegisterAccelerometer(){
        sm.unregisterListener(this, accelerometer);
    }

    public void initReadyGo(){
        readyGo("ready");
        startDelayHandler.postDelayed(new Runnable() {
            public void run() {
                readyGo("go");
            }
        }, 4500);
    }

    public void readyGo(String image){
        final ImageView readyGo = new ImageView(this);
        int drawableId = getResources().getIdentifier(image, "drawable", "insectocide.game");
        readyGo.setBackgroundResource(drawableId);
        final double width = metrics.heightPixels*0.3*2;
        double height = metrics.heightPixels*0.3;
        readyGo.setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) height));
        double y = metrics.heightPixels*0.35;
        readyGo.setY((float) y);
        readyGo.setX((float) (-width));
        readyGo.bringToFront();
        readyGo.setVisibility(View.VISIBLE);
        rl.addView(readyGo);
        readyGo.animate().x((float) (readyGo.getX() + metrics.widthPixels * 0.5 + width / 2));
        readyGo.animate().setDuration(500);
        startDelayHandler.postDelayed(new Runnable() {
            public void run() {
                readyGo.animate().x((float) (readyGo.getX() + metrics.widthPixels * 0.5 + width / 2));
                readyGo.animate().setDuration(500);
            }
        }, 2500);
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
                    try{
                        for (final Shot s : insectsShoots) {
                            if(!s.isOutOfScreen()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        s.shoot();
                                    }
                                });
                                checkIfShipHit(s);
                            }else{
                                removeShot(s);
                            }
                        }
                        for (final Shot s : shipShoots) {
                            if(!s.isOutOfScreen()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        s.shoot();
                                    }
                                });
                                checkIfInsectHit(s);
                            }else{
                                removeShot(s);
                            }
                        }
                        updateLives();
                        Thread.sleep(50);
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
        for(final Insect insect:liveInsects){
            RectF r2 = insect.getRect();
            if (r1.intersect(r2)) {
                insect.gotHit(shot.getPower());
                if (insect.isDead()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            insect.die();
                            showInsectBonus(insect);
                        }
                    });
                    spaceShip.getPowerFromInsect(insect.getType());
                    liveInsects.remove(insect);
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
        startDelayHandler.postDelayed(new Runnable() {
            public void run() {
                bonus.setVisibility(View.INVISIBLE);
            }
        }, 3000);
    }

    private void winGame() {
        resumePauseGame();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startDelayHandler.postDelayed(new Runnable() {
                    public void run() {
                        spaceShip.win();
                    }
                }, 300);
                endGame();
            }
        });
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

                    startDelayHandler.post(new Runnable() {
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
            spaceShip.resetPowers();
            if(spaceShip.isDead()){
                loseGame();
            }
            removeShot(s);
        } // need to add blue ship for multiplayer
    }

    private void loseGame() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spaceShip.die();
                endGame();
            }
        });
        resumePauseGame();
    }

    private void removeShot(Shot s) {
        removeView(s);
        if (s.getEntity() instanceof SpaceShip){
            shipShoots.remove(s);
        }else
            insectsShoots.remove(s);
    }

    private void updateLives(){
        if (shipLives.size() > spaceShip.getHealth()){
            removeShipLive();
        }else if (shipLives.size() < spaceShip.getHealth()){
            drawShipLive();
        }
    }

    private void drawShipLive(){
        for (int i=shipLives.size(); i<spaceShip.getHealth() ; i++){
            final ImageView live = new ImageView(this);
            live.setBackgroundResource(R.drawable.live);
            double length = metrics.heightPixels*0.1;
            live.setLayoutParams(new ViewGroup.LayoutParams((int) length, (int) length));
            live.setY(0);
            double x = i*length;
            live.setX((float) x);
            live.bringToFront();
            live.setVisibility(View.VISIBLE);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rl.addView(live);
                }
            });
            shipLives.add(live);
        }
    }

    private void removeShipLive(){
        for (int i=spaceShip.getHealth(); i<shipLives.size() ; i++){
            final ImageView live = shipLives.get(i);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    live.setVisibility(View.VISIBLE);
                    rl.removeView(live);
                }
            });
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
        if(!isActivityPaused && isStartAnimationDone && time-lastShootTime > SHOOT_DELAY ) {
            Shot s = spaceShip.fire();
            shipShoots.add(s);
            s.bringToFront();
            rl.addView(s);
            lastShootTime = time;
        }
        return false;
    }

    public void resumePauseGame (){
        if(isStartAnimationDone && !isActivityPaused){
            unRegisterAccelerometer();
            isActivityPaused = true;
        }else if (isStartAnimationDone && isActivityPaused) {
            isActivityPaused = false;
            initAccelerometer();
            initMoveShotsThread();
            startInsectsShotsThread();
            startTimer();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pauseButton:
                resumePauseGame();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        resumePauseGame();
    }
}
