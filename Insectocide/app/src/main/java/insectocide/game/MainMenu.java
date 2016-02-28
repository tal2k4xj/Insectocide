package insectocide.game;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
        initEnglishDefaultLanguage();
        rl = (RelativeLayout)findViewById(R.id.mainMenuLayout);
        singlePlayerButton = findViewById(R.id.SinglePlayerButton);
        singlePlayerButton.setOnClickListener(this);
        MultiPlayerButton = findViewById(R.id.MultiPlayerButton);
        MultiPlayerButton.setOnClickListener(this);
        SettingsButton = findViewById(R.id.SettingsButton);
        SettingsButton.setOnClickListener(this);

        startAppVideo = (VideoView)findViewById(R.id.StartVideo);
        String UrlPath="android.resource://"+getPackageName()+"/"+R.raw.zoocoyote;
        startAppVideo.setVideoURI(Uri.parse(UrlPath));
        startAppVideo.start();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                rl.removeView(startAppVideo);
            }
        }, 6000);
    }



    private void initEnglishDefaultLanguage() {
        Locale locale = new Locale("en_US");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getApplicationContext().getResources().updateConfiguration(config, null);
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
}
