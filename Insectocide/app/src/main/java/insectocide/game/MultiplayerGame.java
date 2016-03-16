package insectocide.game;

import android.app.Activity;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.SynchronousQueue;

import insectocide.logic.Insect;
import insectocide.logic.InsectsProvider;
import insectocide.logic.Shot;
import insectocide.logic.SpaceShip;
import insectocide.logic.WiFiDirectReceiver;

public class MultiplayerGame extends Activity implements SensorEventListener{
    private final String DEFAULT_PLAYER_SHIP_COLOR = "red";
    private final String DEFAULT_OPPONENT_COLOR = "blue";
    private final int SOCKET_TIMEOUT = 5000;
    private final int INSECTS_ROWS = 4;
    private final int INSECTS_COLS = 10;
    private final long SHOOT_DELAY = 800;
    private final long START_ANIMATION_DELAY = 7200;
    private WifiP2pInfo wifiP2pInfo;
    private MediaPlayer shipStartSound;
    private MediaPlayer shootSound;
    private MediaPlayer shipExplode;
    private MediaPlayer bugDie;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip playerShip;
    private SpaceShip opponentShip;
    private DisplayMetrics metrics;
    private Insect insects[][];
    private List<Shot> shipsShoots;
    private List<Shot> insectsShoots;
    private List<Insect> liveInsects;
    private List<ImageView> shipLives;
    private RelativeLayout rl;
    private long lastShootTime = 0;
    private boolean isConnectedToOpponent = false;
    private boolean isStartAnimationDone = false;
    private volatile boolean isStartClientAnimationDone = false;
    private boolean moveLeft;
    private Thread insectShots;
    private Thread moveShots;
    private Thread moveInsects;
    private Vibrator vibrate;
    private ServerSocket server;
    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocketThread serverThread;
    private ClientSocketThread clientThread;
    private String lastMovement = "";
    private String curInputMessage = "";
    private Queue<String> messeageQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);
        messeageQueue = new LinkedTransferQueue<>();
        shipsShoots = new CopyOnWriteArrayList<>();
        insectsShoots = new CopyOnWriteArrayList<>();
        shipLives = new CopyOnWriteArrayList<>();
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        rl = (RelativeLayout)findViewById(R.id.MultiplayerLayout);

        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        shootSound = MediaPlayer.create(this, R.raw.shoot);
        shipStartSound = MediaPlayer.create(this, R.raw.shipstart);
        shipExplode = MediaPlayer.create(this, R.raw.shipexplode);
        bugDie = MediaPlayer.create(this, R.raw.bugdie);

        Bundle extras = getIntent().getExtras();
        wifiP2pInfo = (WifiP2pInfo)extras.get("WIFI_P2P_INFO");
        initMultiplayer();
        moveLeft = wifiP2pInfo.isGroupOwner ? false : true;
        initShips();
        updateLives();
        initInsects();
        startReadyGoAnimation();
        initWithRestWithStartDelay();
    }

    private void initWithRestWithStartDelay() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                isStartAnimationDone = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(wifiP2pInfo.isGroupOwner){
                            while (!isStartClientAnimationDone) {
                            }
                        }else{
                            sendWifiMessage("clientDone");
                            isStartClientAnimationDone = true;
                        }
                        initAccelerometer();
                        initMoveInsectsThread();
                        initMoveShotsThread();
                        startInsectsShotsThread();
                    }
                }).start();
            }
        }, START_ANIMATION_DELAY);
    }

    private void initMultiplayer(){
        connection = new Socket();
        if (wifiP2pInfo != null && wifiP2pInfo.isGroupOwner) {
            serverThread = new ServerSocketThread();
            serverThread.start();
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    clientThread = new ClientSocketThread();
                    clientThread.start();
                }
            }, 100);
        }
    }

    private void initShips() {
        playerShip = new SpaceShip(DEFAULT_PLAYER_SHIP_COLOR, this, metrics);
        playerShip.bringToFront();
        rl.addView(playerShip);
        opponentShip = new SpaceShip(DEFAULT_OPPONENT_COLOR, this , metrics);
        opponentShip.bringToFront();
        rl.addView(opponentShip);
        shipStartSound.start();
    }

    private void initInsects(){
        InsectsProvider insectsProvider = new InsectsProvider(INSECTS_ROWS, INSECTS_COLS, "multi", this, metrics);
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
                while (wifiP2pInfo.isGroupOwner && isConnectedToOpponent && !liveInsects.isEmpty()) {
                    try {
                        Random rand = new Random();
                        int i = rand.nextInt(liveInsects.size());
                        i = (liveInsects.size() - 1) - i;
                        final String shotMessage="insect "+ (i+100) + " shoot";
                        sendWifiMessage(shotMessage);
                        insectShoot(i);
                        int timeToSleep = calculateInsectShootingTime();
                        Thread.sleep(timeToSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            private int calculateInsectShootingTime() {
                int numOfInsects = liveInsects.size();
                if (numOfInsects>=numOfInsects*0.8){
                    return 450;
                }else if(numOfInsects>=numOfInsects*0.6){
                    return 800;
                }else if(numOfInsects>=numOfInsects*0.4){
                    return 1300;
                }else if(numOfInsects>=numOfInsects*0.2){
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
                while (isConnectedToOpponent) {
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
                        for (final Shot s : shipsShoots) {
                            if(!s.isOutOfScreen()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        s.shoot();
                                    }
                                });
                                checkIfInsectHit(s);
                                checkIfShipHit(s);
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
                final String shipColor = shot.getEntity().getColor();
                insect.gotHit(shot.getPower());
                if (insect.isDead()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            insect.die();
                            if (shipColor.equals(DEFAULT_PLAYER_SHIP_COLOR)) {
                                showInsectBonus(insect);
                            }
                            bugDie.start();
                        }
                    });
                    if (shipColor.equals(DEFAULT_PLAYER_SHIP_COLOR)) {
                        playerShip.getPowerFromInsect(insect.getType());
                    } else {
                        opponentShip.getPowerFromInsect(insect.getType());
                    }
                    liveInsects.remove(insect);
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
                opponentShip.die();
                shipExplode.start();
                playerShip.win();
                endGame();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void endGame() {
        unRegisterAccelerometer();
        isConnectedToOpponent = false;
        if(!curInputMessage.equals("END")) {
            sendWifiMessage("END");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        finish();
                    }
                }, 2000);
            }
        });
    }

    private void checkIfShipHit(Shot s){
        RectF r1 = s.getRect();
        RectF r2 = playerShip.getRect();
        RectF r3 = opponentShip.getRect();
        if (r1.intersect(r2)){
            playerShip.gotHit(s.getPower());
            if(s.getEntity() instanceof Insect) {
                playerShip.reducePowers();
            }
            vibrate.vibrate(150);
            if(playerShip.isDead()){
                loseGame();
            }
            removeShot(s);
        }
        if (r1.intersect(r3)){
            opponentShip.gotHit(s.getPower());
            if(s.getEntity() instanceof Insect) {
                opponentShip.reducePowers();
            }
            removeShot(s);
        }
    }
    private synchronized void initMoveInsectsThread(){
        moveInsects = new Thread(new Runnable() {
            @Override
            public void run() {
                while(isConnectedToOpponent && !liveInsects.isEmpty()) {
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
                playerShip.die();
                shipExplode.start();
                opponentShip.win();
                endGame();
            }
        });
    }

    private void removeShot(Shot s) {
        removeView(s);
        if (s.getEntity() instanceof SpaceShip){
            shipsShoots.remove(s);
        }else
            insectsShoots.remove(s);
    }

    private void updateLives(){
        if (shipLives.size() > playerShip.getHealth()){
            removeShipLive();
        }else if (shipLives.size() < playerShip.getHealth()){
            drawShipLive();
        }
    }

    private void drawShipLive(){
        for (int i=shipLives.size(); i< playerShip.getHealth() ; i++){
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
        for (int i=(int) playerShip.getHealth(); i<shipLives.size() ; i++){
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
            lastMovement = "right";
            if(curMovement>2){
                playerShip.move("right3");
                sendWifiMessage("left3");
            }else {
                playerShip.move("right2");
                sendWifiMessage("left2");
            }
        }else if(curMovement<-1){
            lastMovement = "left";
            if(curMovement<-2){
                playerShip.move("left3");
                sendWifiMessage("right3");
            }else {
                playerShip.move("left2");
                sendWifiMessage("right2");
            }
        }else{
            if(!lastMovement.equals("middle")) {
                playerShip.move("middle");
                sendWifiMessage("middle");
            }
            lastMovement = "middle";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean onTouchEvent(MotionEvent event) {
        long time = event.getDownTime();
        if(isConnectedToOpponent && isStartAnimationDone && isStartClientAnimationDone && time-lastShootTime > SHOOT_DELAY ) {
            Shot s = playerShip.fire();
            sendWifiMessage("shipFire");
            shipsShoots.add(s);
            s.bringToFront();
            rl.addView(s);
            lastShootTime = time;
            shootSound.start();
        }
        return false;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            endGame();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class ServerSocketThread  extends Thread {
        @Override
        public void run() {
            try {
                server = new ServerSocket(WiFiDirectReceiver.PORT,1);
                while (true) {
                    try {
                        connection = server.accept();
                        isConnectedToOpponent = true;
                        output = new ObjectOutputStream(connection.getOutputStream());
                        output.flush();
                        input = new ObjectInputStream(connection.getInputStream());
                        messeageQueue.add(input.readUTF());
                        playByInput();
                    } catch (EOFException e) {
                        e.printStackTrace();
                        endGame();
                    }
                    finally {
                        output.close();
                        input.close();
                        connection.close();
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
                endGame();
            }
        }
    }

    public class ClientSocketThread extends Thread{
        @Override
        public void run() {
            try {
                connection = new Socket(wifiP2pInfo.groupOwnerAddress, WiFiDirectReceiver.PORT);
                isConnectedToOpponent = true;
                output = new ObjectOutputStream(connection.getOutputStream());
                output.flush();
                input = new ObjectInputStream(connection.getInputStream());
                messeageQueue.add(input.readUTF());
                playByInput();
            } catch (EOFException e) {
                e.printStackTrace();
                endGame();
            } catch (IOException e) {
                e.printStackTrace();
                endGame();
            } finally {
                try {
                    output.close();
                    input.close();
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void playByInput(){
        do{
           curInputMessage = messeageQueue.poll();
            if (curInputMessage != null && !curInputMessage.equals("") ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (curInputMessage.equals("clientDone")){
                            isStartClientAnimationDone = true;
                        } else if (curInputMessage.equals("enemy is dead")) {
                            winGame();
                        } else if (curInputMessage.equals("shipFire")) {
                            Shot s = opponentShip.fire();
                            shipsShoots.add(s);
                            s.bringToFront();
                            rl.addView(s);
                            shootSound.start();
                        } else if (curInputMessage.startsWith("insect")) {
                            int insectNum = (Integer.parseInt(curInputMessage.substring(7,10))-100);
                            insectShoot(insectNum);
                        } else if (!curInputMessage.equals("END")){
                            opponentShip.move(curInputMessage);
                        }else{
                            winGame();
                        }
                    }
                });
            }
        }while(!curInputMessage.equals("END") && isConnectedToOpponent);
    }

    private synchronized void insectShoot(int insectNum) {
        if (liveInsects.size() > insectNum) {
            final Insect insect = liveInsects.get(insectNum);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Shot s = insect.fire();
                    s.bringToFront();
                    rl.addView(s);
                    insectsShoots.add(s);
                }
            });
        }
    }

    public synchronized void sendWifiMessage(String message){
        try{
            if(isConnectedToOpponent) {
                output.writeUTF(message);
                output.flush();
            }
        }
        catch (IOException e){
            e.printStackTrace();
            endGame();
        }
    }
}
