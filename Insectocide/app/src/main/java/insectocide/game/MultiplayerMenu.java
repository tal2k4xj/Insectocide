package insectocide.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import insectocide.logic.ActionListenerHandler;
import insectocide.logic.WiFiDirectReceiver;

public class MultiplayerMenu extends Activity implements View.OnClickListener,WifiP2pManager.ChannelListener{

    private ArrayAdapter<String> adapter;
    private ListView listView;
    private ImageButton findOpponentButton;
    private Handler buttonHandler;

    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WiFiDirectReceiver wfdReceiver;
    private WifiP2pDevice[] wfdDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_menu);

        buttonHandler = new Handler();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), this);

        findOpponentButton = (ImageButton)findViewById(R.id.FindOpponentButton);
        findOpponentButton.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                onConnect(wfdDevices[position]);
            }
        });
    }

    public void loadRooms(){
        wfdDevices = wfdReceiver.getAvailableDevices();
        if (wfdDevices != null) {
            List<String> roomsToString = new ArrayList<>();
            for (int i = 0; i < wfdDevices.length; i++)
                roomsToString.add(wfdDevices[i].toString());
            adapter = new ArrayAdapter<>(this, R.layout.adapter_layout, R.id.textView, roomsToString);
            listView.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.FindOpponentButton:
                findOpponentButton.setBackgroundResource(R.drawable.findopponentbuttonpressed);
                registerWfdReceiver();
                onDiscover();
                loadRooms();
                break;
        }
        buttonHandler.postDelayed(new Runnable() {
            public void run() {
                findOpponentButton.setBackgroundResource(R.drawable.findopponentbutton);
            }
        }, 500);
    }

    public void onChannelDisconnected(){
        Toast.makeText(this, "WiFi Direct Disconnected - Reinitialize.", Toast.LENGTH_SHORT).show();
        reinitializeChannel();
    }

    private void reinitializeChannel(){
        mChannel = mManager.initialize(this, getMainLooper(), this);
        Toast.makeText(this, "WiFi Direct Channel Initialization: " + ((mChannel != null)? "SUCCESS" : "FAILED"), Toast.LENGTH_SHORT).show();
    }

    private void registerWfdReceiver(){
        wfdReceiver = new WiFiDirectReceiver(mManager, mChannel, this);
        wfdReceiver.registerReceiver();
    }

    private void unRegisterWfdReceiver(){
        if(wfdReceiver != null)
            wfdReceiver.unregisterReceiver();
        wfdReceiver = null;
    }

    public void onDiscover(){
        if(isWfdReceiverRegisteredAndFeatureEnabled()){
            mManager.discoverPeers(mChannel,
                    new ActionListenerHandler(this, "Discover Peers"));
        }
    }

    public void onConnect(WifiP2pDevice device){
        if(isWfdReceiverRegisteredAndFeatureEnabled()){
            if(device != null){
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                mManager.connect(mChannel, config, new ActionListenerHandler(this, "Connection"));
            }
        }
    }

    private boolean isWfdReceiverRegisteredAndFeatureEnabled(){
        return (wfdReceiver != null && wfdReceiver.isWifiDirectEnabled);
    }
}
