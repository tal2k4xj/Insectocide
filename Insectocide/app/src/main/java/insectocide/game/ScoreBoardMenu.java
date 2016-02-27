package insectocide.game;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import insectocide.logic.Player;
import insectocide.logic.ScoreBoard;

public class ScoreBoardMenu extends Activity {

    private ArrayAdapter<String> adapter;
    private ListView listView;
    private ScoreBoard scoreBoard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board_menu);
        listView = (ListView) findViewById(R.id.listView);
        loadScoreBoard();
        loadScores();
    }
    public void loadScores(){
        List<String> playersToString = scoreBoard.getScoreBoardAsStringList();
        adapter = new ArrayAdapter<>(this, R.layout.adapter_layout, R.id.textView, playersToString);
        listView.setAdapter(adapter);
    }
    public void loadScoreBoard() {
        scoreBoard = new ScoreBoard();
        SharedPreferences preferences = getSharedPreferences("ScoreBoard", Context.MODE_APPEND);
        String board = preferences.getString("score_board", "");
        if(!board.equals("")) {
            String[] playersData = board.split("!@#@!");
            for (String s : playersData) {
                String[] playerData = s.split("-@-");
                Player p = new Player(playerData[0], Integer.parseInt(playerData[1]));
                scoreBoard.addPlayer(p);
            }
        }
    }
}
