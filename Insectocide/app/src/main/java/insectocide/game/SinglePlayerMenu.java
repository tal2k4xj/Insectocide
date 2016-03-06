package insectocide.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SinglePlayerMenu extends Activity implements View.OnClickListener {

    private View newGameButton;
    private View scoreBoardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_menu);

        newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(this);
        scoreBoardButton = findViewById(R.id.scoreBoardButton);
        scoreBoardButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.newGameButton:
                newGameButton.setBackgroundResource(R.drawable.newgamebuttonpressed);
                Intent newGame = new Intent (this,SinglePlayerGame.class);
                newGame.putExtra("MULTIPLAYER", false);
                startActivity(newGame);
                break;
            case R.id.scoreBoardButton:
                scoreBoardButton.setBackgroundResource(R.drawable.scoreboardbuttonpressed);
                Intent scoreBoard = new Intent (this,ScoreBoardMenu.class);
                startActivity(scoreBoard);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        newGameButton.setBackgroundResource(R.drawable.newgamebutton);
        scoreBoardButton.setBackgroundResource(R.drawable.scoreboardbutton);
    }
}
