package insectocide.game;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import insectocide.logic.SpaceShip;

public class SinglePlayerGame extends Activity implements SensorEventListener {
    private final String DEFAULT_COLOR = "red";
    private TextView y;
    private Sensor accelerometer;
    private SensorManager sm;
    private SpaceShip ship;
    private DisplayMetrics metrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);

        metrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //get maxX and maxY that are the max value of the screen. the min is 0.
        //link: http://stackoverflow.com/questions/11483345/how-do-android-screen-coordinates-work

        Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        //int maxX = mdispSize.x;
        int maxY = mdispSize.y - 300;

        //add ship - need to check what is the best values to add in the LayoutParams.

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.singlePlayerLayout);
        ship = new SpaceShip(DEFAULT_COLOR, this);
        ship.bringToFront();
        ship.setLayoutParams(new LayoutParams(metrics.widthPixels / 8, metrics.widthPixels / 8));
        ship.setY(maxY);
        rl.addView(ship);

        y = (TextView) findViewById(R.id.y);

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
        y.setText("y = " + curMovement);
        moveShip(curMovement);

    }

    private void moveShip(float curMovement) {
        if (curMovement >1) {
            if(curMovement>2){
                ship.move("right3");
            }else {
                ship.move("right2");
            }
        }else if(curMovement<-1){
            if(curMovement<-2){
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
