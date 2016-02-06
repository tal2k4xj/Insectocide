package insectocide.game;

import android.app.Activity;
import android.app.UiAutomation;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SinglePlayerMenu extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_menu);

        View newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.newGameButton:
                Intent newGame = new Intent (this,SinglePlayerGame.class);
                startActivity(newGame);
                break;
        }

    }
}
