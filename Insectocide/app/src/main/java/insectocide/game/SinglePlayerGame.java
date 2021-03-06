package insectocide.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import insectocide.logic.Insect;
import insectocide.logic.InsectsProvider;
import insectocide.logic.Player;
import insectocide.logic.Shot;
import insectocide.logic.SpaceShip;

public class SinglePlayerGame extends Activity implements SensorEventListener,View.OnClickListener {
    private final String DEFAULT_COLOR = "red";
    private final int INSECTS_ROWS = 3;
    private final int INSECTS_COLS = 10;
    private final long SHOOT_DELAY = 800;
    private final long START_ANIMATION_DELAY = 7200;
    private MediaPlayer shipStartSound;
    private MediaPlayer shipShootSound;
    private MediaPlayer shipExplodeSound;
    private MediaPlayer insectDieSound;
    private MediaPlayer insectsSound;
    private int timeOfGame;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip spaceShip;
    private DisplayMetrics metrics;
    private Handler timerHandler;
    private List<Shot> shipShoots;
    private List<Shot> insectsShoots;
    private List<Insect> liveInsects;
    private List<ImageView> shipLives;
    private RelativeLayout rl;
    private long lastShootTime = 0;
    private boolean isActivityPaused = false;
    private boolean isStartAnimationDone = false;
    private boolean moveLeft = true;
    private Thread insectShots;
    private Thread moveShots;
    private Thread moveInsects;
    private Thread timer;
    private TextView scoreText;
    private ImageButton pauseButton;
    private String playerName;
    private Vibrator vibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);
        initParameters();
        initShip();
        updateLives();
        initInsects();
        startReadyGoAnimation();
        initWithRestWithStartDelay();
    }

    private void initParameters() {
        shipShoots = new CopyOnWriteArrayList<>();
        insectsShoots = new CopyOnWriteArrayList<>();
        shipLives = new CopyOnWriteArrayList<>();
        timerHandler = new Handler();
        timeOfGame = 0;
        playerName ="";
        scoreText = (TextView) findViewById(R.id.ScoreText);
        scoreText.setVisibility(View.INVISIBLE);
        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);
        shipShootSound = MediaPlayer.create(this, R.raw.shoot);
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        shipStartSound = MediaPlayer.create(this, R.raw.shipstart);
        insectsSound = MediaPlayer.create(this, R.raw.insects_start);
        insectsSound.setLooping(true);
        shipExplodeSound = MediaPlayer.create(this, R.raw.shipexplode);
        insectDieSound = MediaPlayer.create(this, R.raw.bugdie);
        insectsSound.start();
    }

    private void initWithRestWithStartDelay() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                isStartAnimationDone = true;
                initAccelerometer();
                initMoveInsectsThread();
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
        shipStartSound.start();
    }

    private void initInsects(){
        InsectsProvider insectsProvider = new InsectsProvider(INSECTS_ROWS, INSECTS_COLS,"single", this, metrics);
        liveInsects = insectsProvider.getLiveInsectsList();
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

    public void startReadyGoAnimation(){
        readyGo("ready");
        new Handler().postDelayed(new Runnable() {
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                readyGo.animate().x((float) (readyGo.getX() + metrics.widthPixels * 0.5 + width / 2));
                readyGo.animate().setDuration(500);
            }
        }, 2500);
    }

    private synchronized void startInsectsShotsThread(){
        insectShots = new Thread(new Runnable() {
            public void run() {
                while (!isActivityPaused && !liveInsects.isEmpty()) {
                    try {
                        Random rand = new Random();
                        int i = rand.nextInt(liveInsects.size());
                        final Insect insect = liveInsects.get(i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Shot s = insect.fire();
                                s.bringToFront();
                                rl.addView(s);
                                insectsShoots.add(s);
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
                    return 450;
                }else if(numOfInsects>=20){
                    return 800;
                }else if(numOfInsects>=15){
                    return 1300;
                }else if(numOfInsects>=10){
                    return 1800;
                }else {
                    return 2500;
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
                            insectDieSound.start();
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
        new Handler().postDelayed(new Runnable() {
            public void run() {
                rl.removeView(bonus);
            }
        }, 3000);
    }

    private void winGame() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        insectsSound.stop();
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
        rl.removeView(pauseButton);
        resumePauseGame();
        updateScoreBoard();
    }

    private int calcScore() {
        int score = 0;
        if (liveInsects.isEmpty()){
            score+=1000; // if killed all insects + 1000;
            score+= spaceShip.getHealth()*200; //+200 for every life;
        }
        score += (getNumOfKilledInsects()*400) - (timeOfGame)*100;
        return (score > 0) ? score : 0;
    }

    private int getNumOfKilledInsects() {
        return (INSECTS_COLS*INSECTS_ROWS)-liveInsects.size();
    }

    private void startTimer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (!isActivityPaused && !spaceShip.isDead()&& liveInsects.size()>0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    timerHandler.post(new Runnable() {
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
            vibrate.vibrate(150);
            if(spaceShip.isDead()){
                loseGame();
            }
            removeShot(s);
        }
    }
    private synchronized void initMoveInsectsThread(){
        moveInsects = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!isActivityPaused && !liveInsects.isEmpty()) {
                    Insect first,last;
                    if (moveLeft) {
                        for (final Insect i : liveInsects) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    i.move("left");
                                }
                            });
                        }
                        first = liveInsects.get(0);
                        if(first.getX() > 0){
                            moveLeft= true;
                        }else{
                            moveLeft=false;
                        }
                    } else{ //move right
                        for (final Insect i : liveInsects) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    i.move("right");
                                }
                            });
                        }
                        last = liveInsects.get(liveInsects.size() - 1);
                        if (last.getX() + last.getWidth() < metrics.widthPixels){
                            moveLeft = false;
                        }else{
                            moveLeft = true;
                        }
                    }
                    int insectSpeed = calcInsectSpeed();
                    try {
                        Thread.sleep(insectSpeed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            private int calcInsectSpeed() {
                int numbOfStartInsects = INSECTS_COLS*INSECTS_ROWS;
                int numOfInsects = liveInsects.size();
                if (numOfInsects>=numbOfStartInsects*0.8){
                    return 200;
                }else if(numOfInsects>=numbOfStartInsects*0.6){
                    return 180;
                }else if(numOfInsects>=numbOfStartInsects*0.4){
                    return 150;
                }else if(numOfInsects>=numbOfStartInsects*0.2){
                    return 120;
                }else {
                    return 100;
                }
            }
        });

        moveInsects.start();
    }

    private void loseGame() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spaceShip.die();
                shipExplodeSound.start();
                endGame();
            }
        });
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
        for (int i=shipLives.size(); i< spaceShip.getHealth() ; i++){
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
        for (int i=(int) spaceShip.getHealth(); i<shipLives.size() ; i++){
            final ImageView live = shipLives.get(i);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
            shipShootSound.start();
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
            initMoveInsectsThread();
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
    private void updateScoreBoard() {
        popUpPlayerNameInput();
    }
    public void saveScoreBoard(Player p){
        SharedPreferences settings = getSharedPreferences("ScoreBoard", Context.MODE_APPEND);
        String board = settings.getString("score_board", "");
        board = board.concat(p.toSaveString());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("score_board", board);
        editor.commit();
    }
    public void popUpPlayerNameInput(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("You Set a New Record!\nPlease Enter Your Name:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playerName = input.getText().toString();
                if (playerName.trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Name Can Not Be Empty. Score Will Not Save", Toast.LENGTH_SHORT).show();
                }else{
                    Player p = new Player(playerName, calcScore());
                    saveScoreBoard(p);
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        System.exit(0);
                    }
                }, 1000);
            }
        });
        if (!this.isFinishing())
            builder.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
