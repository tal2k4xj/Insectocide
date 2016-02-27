package insectocide.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class MultiplayerMenu extends Activity implements View.OnClickListener{

    private ArrayAdapter<String> adapter;
    private ListView listView;
    private ImageButton createRoomButton;
    private ImageButton findRoomButton;
    private Handler buttonHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_menu);

        buttonHandler = new Handler();

        listView = (ListView) findViewById(R.id.listView);
        createRoomButton = (ImageButton)findViewById(R.id.CreateRoomButton);
        createRoomButton.setOnClickListener(this);
        findRoomButton = (ImageButton)findViewById(R.id.FindRoomButton);
        findRoomButton.setOnClickListener(this);

        loadRooms();
    }

    public void loadRooms(){
        List<String> roomsToString = new ArrayList<String>();
        for (int i=0 ; i<10 ; i++){
            roomsToString.add(i+"room");
        }
        adapter = new ArrayAdapter<>(this, R.layout.adapter_layout, R.id.textView, roomsToString);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.CreateRoomButton:
                createRoomButton.setBackgroundResource(R.drawable.createroombuttonpressed);
                //do something
                break;
            case R.id.FindRoomButton:
                findRoomButton.setBackgroundResource(R.drawable.findroombuttonpressed);
                //do something

                break;
        }
        buttonHandler.postDelayed(new Runnable() {
            public void run() {
                createRoomButton.setBackgroundResource(R.drawable.createroombutton);
                findRoomButton.setBackgroundResource(R.drawable.findroombutton);
            }
        }, 500);
    }
}
