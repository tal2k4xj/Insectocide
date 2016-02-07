package insectocide.game;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
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
    private ImageView blueInsect;
    private AnimationDrawable blueInsectAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game);

        redShip = (ImageView) findViewById(R.id.redShip);
        blueInsect = (ImageView) findViewById(R.id.blueInsect);
        blueInsect.setBackgroundResource(R.drawable.blue_animation);
        blueInsectAnimation = (AnimationDrawable) blueInsect.getBackground();
        blueInsectAnimation.start();

        y = (TextView) findViewById(R.id.y);

        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float curMovement = event.values[1];
        int lastMovement = 0 ;
        y.setText("y = " + curMovement);
        if (curMovement != lastMovement){
            if (curMovement > 0.5 && curMovement < 1.5) {
                redShip.setX(redShip.getX() + 2);
                redShip.setBackgroundResource(R.drawable.red);
            }else if(curMovement > 1.5 && curMovement < 3){
                redShip.setX(redShip.getX() + 4);
                redShip.setBackgroundResource(R.drawable.redright2);
            }else if(curMovement > 3){
                redShip.setX(redShip.getX() + 6);
                redShip.setBackgroundResource(R.drawable.redright3);
            }else if(curMovement < -0.5 && curMovement > -1.5){
                redShip.setX(redShip.getX() - 2);
                redShip.setBackgroundResource(R.drawable.red);
            }else if(curMovement < -1.5 && curMovement > -3){
                redShip.setX(redShip.getX() - 4);
                redShip.setBackgroundResource(R.drawable.redleft2);
            }else if(curMovement < -3){
                redShip.setX(redShip.getX() - 6);
                redShip.setBackgroundResource(R.drawable.redleft3);
            }
        }else if (curMovement > 0.5 && curMovement < 1.5) {
            redShip.setX(redShip.getX() + 2);
        }else if(curMovement > 1.5 && curMovement < 3){
            redShip.setX(redShip.getX() + 4);
        }else if(curMovement > 3){
            redShip.setX(redShip.getX() + 6);
        }else if(curMovement < -0.5 && curMovement > -1.5){
            redShip.setX(redShip.getX() - 2);
        }else if(curMovement < -1.5 && curMovement > -3){
            redShip.setX(redShip.getX() - 4);
        }else if(curMovement < -3){
            redShip.setX(redShip.getX() - 6);
        }
        lastMovement = (int)curMovement;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
