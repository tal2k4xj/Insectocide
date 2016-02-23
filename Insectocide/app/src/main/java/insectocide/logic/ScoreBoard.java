package insectocide.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class ScoreBoard{
    private final int MAX_PLAYERS = 10;
    private LinkedList<Player> players;

    public ScoreBoard() {
        players = new LinkedList<>();
    }

    public LinkedList<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        if(players.size() < MAX_PLAYERS)
            players.add(player);
        else if (player.getScore()>players.getLast().getScore()){
            players.pollLast();
            players.add(player);
        }
        Collections.sort(players);
    }

    public boolean isPlayersListFull(){
        return players.size()==MAX_PLAYERS;
    }

    public ArrayList<String> getScoreBoardAsStringList(){
        ArrayList<String> scoreBoardString = new ArrayList<>();
        int i=1;
        Collections.sort(players);
        for (Player p : players) {
            String playerString = String.format("%-2d)  %-10.10s |  Score: %4d",i++, p.getName(), p.getScore());
            scoreBoardString.add(playerString);
        }
        return scoreBoardString;
    }
}
