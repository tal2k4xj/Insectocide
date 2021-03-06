package insectocide.game;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.util.Locale;

public class MainMenu extends Activity implements View.OnClickListener {

    private View singlePlayerButton;
    private View MultiPlayerButton;
    private View SettingsButton;
    private VideoView startAppVideo;
    private RelativeLayout rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        rl = (RelativeLayout)findViewById(R.id.mainMenuLayout);
        singlePlayerButton = findViewById(R.id.SinglePlayerButton);
        MultiPlayerButton = findViewById(R.id.MultiPlayerButton);
        SettingsButton = findViewById(R.id.SettingsButton);

        startAppVideo = (VideoView)findViewById(R.id.StartVideo);
        String UrlPath="android.resource://"+getPackageName()+"/"+R.raw.insectocideintro;
        startAppVideo.setVideoURI(Uri.parse(UrlPath));
        startAppVideo.start();
        startAppVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                rl.removeView(startAppVideo);
                activateListeners();
                return false;
            }
        });

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (startAppVideo != null) {
                    rl.removeView(startAppVideo);
                    activateListeners();
                }
            }
        }, 15000);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.SinglePlayerButton:
                singlePlayerButton.setBackgroundResource(R.drawable.singleplayerbuttonpressed);
                Intent singlePlayer = new Intent (this,SinglePlayerMenu.class);
                startActivity(singlePlayer);
                break;
            case R.id.MultiPlayerButton:
                MultiPlayerButton.setBackgroundResource(R.drawable.multiplayerbuttonpressed);
                Intent multiPlayer = new Intent (this,MultiplayerMenu.class);
                startActivity(multiPlayer);
                break;
            case R.id.SettingsButton:
                SettingsButton.setBackgroundResource(R.drawable.settingsbuttonpressed);
                Intent settings = new Intent (this,SettingsMenu.class);
                startActivity(settings);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        singlePlayerButton.setBackgroundResource(R.drawable.singleplayerbutton);
        MultiPlayerButton.setBackgroundResource(R.drawable.multiplayerbutton);
        SettingsButton.setBackgroundResource(R.drawable.settingsbutton);
    }

    private void activateListeners(){
        singlePlayerButton.setOnClickListener(this);
        MultiPlayerButton.setOnClickListener(this);
        SettingsButton.setOnClickListener(this);
    }
}
