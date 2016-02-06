package insectocide.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        View singlePlayerButton = findViewById(R.id.singlePlayerButton);
        singlePlayerButton.setOnClickListener(this);
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.singlePlayerButton:
                Intent singlePlayer = new Intent (this,SinglePlayerMenu.class);
                startActivity(singlePlayer);
                break;
        }

    }
}
