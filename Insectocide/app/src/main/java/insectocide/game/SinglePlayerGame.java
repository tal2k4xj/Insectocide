package insectocide.game;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;

import insectocide.logic.SpaceShip;

public class SinglePlayerGame extends Activity implements SensorEventListener {
    private final String DEFAULT_COLOR = "red";
    private Sensor accelerometer;
    private SensorManager sm;
    private ImageView blueInsect;
    private AnimationDrawable blueInsectAnimation;
    private SpaceShip ship;
    private DisplayMetrics metrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);

        //add insect + animation
        blueInsect = (ImageView) findViewById(R.id.blueInsect);
        blueInsect.setBackgroundResource(R.drawable.blue_animation);
        blueInsectAnimation = (AnimationDrawable) blueInsect.getBackground();
        blueInsectAnimation.start();

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //get maxX and maxY that are the max value of the screen. the min is 0.
        //link: http://stackoverflow.com/questions/11483345/how-do-android-screen-coordinates-work

        /* i think we dont need this part
        Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        */

        //int maxX = mdispSize.x;

        //add ship + placement + adjust size

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);
        ship = new SpaceShip(DEFAULT_COLOR, this);
        ship.bringToFront();
        double shipw = metrics.heightPixels*0.2/1.5;
        double shiph = metrics.heightPixels*0.2;
        ship.setLayoutParams(new LayoutParams((int) shipw, (int) shiph));
        double maxY = metrics.heightPixels - metrics.heightPixels*0.2;
        double maxX = metrics.widthPixels/2 - metrics.heightPixels*0.2/1.5/2;
        ship.setY((float)maxY);
        ship.setX((float)maxX);
        rl.addView(ship);

        initAccelerometer();
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
