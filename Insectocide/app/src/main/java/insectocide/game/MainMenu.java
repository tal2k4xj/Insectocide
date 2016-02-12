package insectocide.game;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.Locale;

public class MainMenu extends Activity implements View.OnClickListener {

    View singlePlayerButton;
    View MultiPlayerButton;
    View SettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        initEnglishDefaultLanguage();
        singlePlayerButton = findViewById(R.id.singlePlayerButton);
        singlePlayerButton.setOnClickListener(this);
        MultiPlayerButton = findViewById(R.id.MultiPlayerButton);
        MultiPlayerButton.setOnClickListener(this);
        SettingsButton = findViewById(R.id.SettingsButton);
        SettingsButton.setOnClickListener(this);
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
            case R.id.singlePlayerButton:
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
