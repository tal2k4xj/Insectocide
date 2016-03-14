package insectocide.game;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import insectocide.logic.ActionListenerHandler;
import insectocide.logic.WiFiDirectReceiver;

public class MultiplayerMenu extends Activity implements View.OnClickListener,WifiP2pManager.ChannelListener{

    private ArrayAdapter<String> adapter;
    private ListView listView;
    private ImageButton findOpponentButton;
    private ImageButton enableWifi;
    private ImageButton disableWifi;
    private Handler buttonHandler;
    private ProgressBar loading;

    private WifiP2pManager.Channel wfChannel;
    private WifiP2pManager wfManager;
    private WiFiDirectReceiver wfReceiver;
    private WifiP2pDevice[] wfDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_menu);

        buttonHandler = new Handler();
        
        if(isWfdReceiverRegisteredAndFeatureEnabled()) {
            unRegisterWfdReceiver();
            wfManager = null;
        }

        wfManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wfChannel = wfManager.initialize(this, getMainLooper(), this);

        enableWifi = (ImageButton)findViewById(R.id.EnableWifiButton);
        enableWifi.setOnClickListener(this);
        findOpponentButton = (ImageButton)findViewById(R.id.FindOpponentButton);
        findOpponentButton.setOnClickListener(this);
        disableWifi = (ImageButton)findViewById(R.id.DisableWifiButton);
        disableWifi.setOnClickListener(this);

        loading = (ProgressBar)findViewById(R.id.progressBar);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                onConnect(wfDevices[position]);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.EnableWifiButton:
                enableWifi.setBackgroundResource(R.drawable.enablewifibuttonpressed);
                startProgressBar();
                registerWfdReceiver();
                onDiscover();
                break;
            case R.id.FindOpponentButton:
                findOpponentButton.setBackgroundResource(R.drawable.findopponentbuttonpressed);
                startProgressBar();
                onDiscover();
                loadRooms();
                break;
            case R.id.DisableWifiButton:
                disableWifi.setBackgroundResource(R.drawable.disablewifibuttonpressed);
                startProgressBar();
                unRegisterWfdReceiver();
                break;
        }
        buttonHandler.postDelayed(new Runnable() {
            public void run() {
                findOpponentButton.setBackgroundResource(R.drawable.findopponentbutton);
                enableWifi.setBackgroundResource(R.drawable.enablewifibutton);
                disableWifi.setBackgroundResource(R.drawable.disablewifibutton);
            }
        }, 500);
    }

    private void startProgressBar() {
        loading.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                loading.setVisibility(View.INVISIBLE);
            }
        }, 3000);
    }

    private void registerWfdReceiver(){
        wfReceiver = new WiFiDirectReceiver(wfManager, wfChannel, this);
        wfReceiver.registerReceiver();
    }

    public void onDiscover(){
        if(isWfdReceiverRegisteredAndFeatureEnabled()){
            wfManager.discoverPeers(wfChannel,
                    new ActionListenerHandler(this, "Discover Peers"));
        }
    }

    private boolean isWfdReceiverRegisteredAndFeatureEnabled(){
        return (wfReceiver != null && wfReceiver.isWifiDirectEnabled);
    }

    public void loadRooms(){
        if(isWfdReceiverRegisteredAndFeatureEnabled()) {
            wfDevices = wfReceiver.getAvailableDevices();
        }else{
            Toast.makeText(this.getApplicationContext(), "Please Enable Wifi First", Toast.LENGTH_SHORT).show();
        }

        if (wfDevices != null) {
            List<String> roomsToString = new ArrayList<>();
            for (int i = 0; i < wfDevices.length; i++)
                roomsToString.add(wfDevices[i].toString());
            adapter = new ArrayAdapter<>(this, R.layout.adapter_layout, R.id.textView, roomsToString);
            listView.setAdapter(adapter);
        }
    }

    private void unRegisterWfdReceiver(){
        if(wfReceiver != null) {
            wfReceiver.unregisterReceiver();
            List<String> roomsToString = new ArrayList<>();
            adapter = new ArrayAdapter<>(this, R.layout.adapter_layout, R.id.textView, roomsToString);
            listView.setAdapter(adapter);
        }
        wfReceiver = null;
    }

    public void onConnect(WifiP2pDevice device){
        if(isWfdReceiverRegisteredAndFeatureEnabled()){
            if(device != null){
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                wfManager.connect(wfChannel, config, new ActionListenerHandler(this, "Connection"));
            }
        }
    }

    public void onChannelDisconnected(){
        Toast.makeText(this, "WiFi Direct Disconnected - Reinitialize.", Toast.LENGTH_SHORT).show();
        reinitializeChannel();
    }

    private void reinitializeChannel(){
        wfChannel = wfManager.initialize(this, getMainLooper(), this);
        Toast.makeText(this, "WiFi Direct Channel Initialization: " + ((wfChannel != null)? "SUCCESS" : "FAILED"), Toast.LENGTH_SHORT).show();
    }
}
