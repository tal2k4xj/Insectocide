package insectocide.game;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import insectocide.logic.Insect;
import insectocide.logic.Shot;
import insectocide.logic.SpaceShip;

public abstract class Game implements SensorEventListener,View.OnClickListener{
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
    private ImageButton pauseButton;
}
