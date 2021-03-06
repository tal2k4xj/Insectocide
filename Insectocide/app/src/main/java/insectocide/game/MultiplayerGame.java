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
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import insectocide.logic.Insect;
import insectocide.logic.InsectsProvider;
import insectocide.logic.Shot;
import insectocide.logic.SpaceShip;
import insectocide.logic.WiFiDirectReceiver;

public class MultiplayerGame extends Activity implements SensorEventListener{
    private final String DEFAULT_PLAYER_SHIP_COLOR = "red";
    private final String DEFAULT_OPPONENT_COLOR = "blue";
    private final int INSECTS_ROWS = 4;
    private final int INSECTS_COLS = 10;
    private final long SHOOT_DELAY = 800;
    private final long START_ANIMATION_DELAY = 7200;
    private WifiP2pInfo wifiP2pInfo;
    private MediaPlayer shipStartSound;
    private MediaPlayer shipShootSound;
    private MediaPlayer shipExplodeSound;
    private MediaPlayer bugDieSound;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip playerSpaceShip;
    private SpaceShip opponentSpaceShip;
    private DisplayMetrics metrics;
    private List<Shot> shipsShoots;
    private List<Shot> insectsShoots;
    private List<Insect> liveInsects;
    private List<ImageView> shipLives;
    private RelativeLayout rl;
    private long lastShootTime = 0;
    private boolean isConnectedToOpponent = false;
    private boolean isStartAnimationDone = false;
    private volatile boolean isClientStartAnimationDone = false;
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
    private String lastPlayerShipMovement = "";
    private String lastEnemyShipMovement = "middle";
    private String curInputMessage = "";
    private boolean isServerClosed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);
        initParameters();
        initMultiplayerConnection();
        chooseInsectsMovementStartDirection();
        initSpaceShips();
        updatePlayerLives();
        initInsects();
        startReadyGoAnimation();
        initTheRestProcessesWithStartDelay();
    }

    private void initParameters() {
        shipsShoots = new CopyOnWriteArrayList<>();
        insectsShoots = new CopyOnWriteArrayList<>();
        shipLives = new CopyOnWriteArrayList<>();
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        rl = (RelativeLayout)findViewById(R.id.MultiplayerLayout);
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        shipShootSound = MediaPlayer.create(this, R.raw.shoot);
        shipStartSound = MediaPlayer.create(this, R.raw.shipstart);
        shipExplodeSound = MediaPlayer.create(this, R.raw.shipexplode);
        bugDieSound = MediaPlayer.create(this, R.raw.bugdie);
        Bundle extras = getIntent().getExtras();
        wifiP2pInfo = (WifiP2pInfo)extras.get("WIFI_P2P_INFO");
    }

    private void chooseInsectsMovementStartDirection() {
        moveLeft = wifiP2pInfo.isGroupOwner ? false : true;
    }

    private void initTheRestProcessesWithStartDelay() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                isStartAnimationDone = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(wifiP2pInfo.isGroupOwner){
                            while (!isClientStartAnimationDone) {
                            }
                        }else{
                            sendWifiMessage("clientDone");
                            isClientStartAnimationDone = true;
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

    private void initMultiplayerConnection(){
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

    private void initSpaceShips() {
        playerSpaceShip = new SpaceShip(DEFAULT_PLAYER_SHIP_COLOR, this, metrics);
        playerSpaceShip.bringToFront();
        rl.addView(playerSpaceShip);
        opponentSpaceShip = new SpaceShip(DEFAULT_OPPONENT_COLOR, this , metrics);
        opponentSpaceShip.bringToFront();
        rl.addView(opponentSpaceShip);
        shipStartSound.start();
    }

    private void initInsects(){
        InsectsProvider insectsProvider = new InsectsProvider(INSECTS_ROWS, INSECTS_COLS, "multi", this, metrics);
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
                while (wifiP2pInfo.isGroupOwner && isConnectedToOpponent && !liveInsects.isEmpty()) {
                    try {
                        Random rand = new Random();
                        int insectNumber = rand.nextInt(liveInsects.size());
                        final String shotMessage="insect "+ getOutputInsectNumber(insectNumber) + " shoot";
                        sendWifiMessage(shotMessage);
                        insectShoot(insectNumber);
                        int timeToSleep = calculateInsectShootingTime();
                        Thread.sleep(timeToSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            private int getOutputInsectNumber(int insectNumber) {
                return liveInsects.size() - 1 - insectNumber +100;
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
                                checkIfShipGotHit(s);
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
                                checkIfShipGotHit(s);
                            }else{
                                removeShot(s);
                            }
                        }
                        updatePlayerLives();
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
                            bugDieSound.start();
                        }
                    });
                    if (shipColor.equals(DEFAULT_PLAYER_SHIP_COLOR)) {
                        playerSpaceShip.getPowerFromInsect(insect.getType());
                    } else {
                        opponentSpaceShip.getPowerFromInsect(insect.getType());
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
                opponentSpaceShip.die();
                shipExplodeSound.start();
                playerSpaceShip.win();
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
        isServerClosed = true;
        if(!curInputMessage.equals("enemyDie"))
            sendWifiMessage("connectionClosed");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        System.exit(0);
                    }
                }, 2000);
            }
        });
    }

    private void checkIfShipGotHit(Shot s){
        RectF r1 = s.getRect();
        RectF r2 = playerSpaceShip.getRect();
        RectF r3 = opponentSpaceShip.getRect();
        if (r1.intersect(r2)){
            playerSpaceShip.gotHit(s.getPower());
            if(s.getEntity() instanceof Insect) {
                playerSpaceShip.reducePowers();
            }
            vibrate.vibrate(150);
            if(playerSpaceShip.isDead()){
                loseGame();
            }
            removeShot(s);
        }
        if (r1.intersect(r3)){
            opponentSpaceShip.gotHit(s.getPower());
            if(s.getEntity() instanceof Insect) {
                opponentSpaceShip.reducePowers();
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
        sendWifiMessage("enemyDie");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerSpaceShip.die();
                shipExplodeSound.start();
                opponentSpaceShip.win();
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

    private void updatePlayerLives(){
        if (shipLives.size() > playerSpaceShip.getHealth()){
            removeShipLive();
        }else if (shipLives.size() < playerSpaceShip.getHealth()){
            drawShipLive();
        }
    }

    private void drawShipLive(){
        for (int i=shipLives.size(); i< playerSpaceShip.getHealth() ; i++){
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
        for (int i=(int) playerSpaceShip.getHealth(); i<shipLives.size() ; i++){
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
        float curSensorMovement = event.values[1];
        moveSpaceShips(curSensorMovement);
    }

    private void moveSpaceShips(float curSensorMovement) {
        String shipMovement = movePlayerSpaceShipBySensorInput(curSensorMovement);
        if(shipMovement != lastPlayerShipMovement){
            lastPlayerShipMovement = shipMovement;
            sendMovementToOpponent(shipMovement);
        }
        moveEnemyShip();
    }

    private void sendMovementToOpponent(String shipMovement) {
        String movementToSend = convertMovementToOppositeMovement(shipMovement);
        sendWifiMessage(movementToSend);
    }

    private void moveEnemyShip() {
        opponentSpaceShip.move(lastEnemyShipMovement);
    }

    private String movePlayerSpaceShipBySensorInput(float curMovement) {

        if (curMovement >1) {
            if(curMovement>2){
                playerSpaceShip.move("right3");
                return("right3");
            }else {
                playerSpaceShip.move("right2");
                return("right2");
            }
        }else if(curMovement<-1){
            if(curMovement<-2){
                playerSpaceShip.move("left3");
                return("left3");
            }else {
                playerSpaceShip.move("left2");
                return("left2");
            }
        }else{
            playerSpaceShip.move("middle");
            return("middle");
        }
    }
    public String convertMovementToOppositeMovement(String movement){
        String oppositeMovement = movement;
        switch(movement){
            case("right2"):
                oppositeMovement = "left2";
                break;
            case("right3"):
                oppositeMovement = "left3";
                break;
            case("left2"):
                oppositeMovement = "right2";
                break;
            case("left3"):
                oppositeMovement = "right3";
                break;
        }
        return oppositeMovement;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean onTouchEvent(MotionEvent event) {
        long time = event.getDownTime();
        if(isConnectedToOpponent && isStartAnimationDone && isClientStartAnimationDone && time-lastShootTime > SHOOT_DELAY ) {
            Shot s = playerSpaceShip.fire();
            sendWifiMessage("shipFire");
            shipsShoots.add(s);
            s.bringToFront();
            rl.addView(s);
            lastShootTime = time;
            shipShootSound.start();
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
                while (!isServerClosed) {
                    try {
                        connection = server.accept();
                        isConnectedToOpponent = true;
                        output = new ObjectOutputStream(connection.getOutputStream());
                        output.flush();
                        input = new ObjectInputStream(connection.getInputStream());
                        checkInputWhilePlay();
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
                checkInputWhilePlay();
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

    public void checkInputWhilePlay() throws IOException{

        do{
           curInputMessage = input.readUTF();
            if (!curInputMessage.equals("")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (curInputMessage.equals("clientDone")){
                            isClientStartAnimationDone = true;
                        } else if (curInputMessage.equals("shipFire")) {
                            opponentShipFire();
                        } else if (curInputMessage.startsWith("insect")) {
                            int insectNum = (Integer.parseInt(curInputMessage.substring(7,10))-100);
                            insectShoot(insectNum);
                        } else if (curInputMessage.equals("enemyDie")){
                            winGame();
                        }else if (!curInputMessage.equals("connectionClosed")){
                            handleOpponentShipMovement();
                        }
                    }
                });
            }
        }while((!curInputMessage.equals("connectionClosed") || !curInputMessage.equals("enemyDie") ) && isConnectedToOpponent);
    }

    private void opponentShipFire() {
        Shot s = opponentSpaceShip.fire();
        shipsShoots.add(s);
        s.bringToFront();
        rl.addView(s);
        shipShootSound.start();
    }

    private void handleOpponentShipMovement() {
        if (curInputMessage !=lastEnemyShipMovement)
            lastEnemyShipMovement = curInputMessage;
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
        System.out.println(message);
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
