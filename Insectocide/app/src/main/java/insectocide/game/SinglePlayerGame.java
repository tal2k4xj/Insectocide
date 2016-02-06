package insectocide.game;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SinglePlayerGame extends Activity implements SensorEventListener {

    private TextView y;
    private Sensor accelerometer;
    private SensorManager sm;
    private ImageView redShip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);

        redShip = (ImageView) findViewById(R.id.redShip);

        y = (TextView) findViewById(R.id.y);

        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float curMovement = event.values[1];
        y.setText("y = " + curMovement);
        if (curMovement >1) {
            redShip.setX(redShip.getX() + 2);
        }else if(curMovement<-1){
            redShip.setX(redShip.getX() - 2);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
