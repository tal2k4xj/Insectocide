package insectocide.logic;
//credit to "Android 4.0 New Features - Demo Wi Fi Direct" on Youtube : https://www.youtube.com/watch?v=cgJ71IfZZw8
//credit to Moran Barzilay from our class that also gave us some information about this technology
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;
import insectocide.game.MultiplayerGame;

public class WiFiDirectReceiver extends BroadcastReceiver implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    public static final int PORT = 7890;
    public boolean isWifiDirectEnabled;
    private WifiP2pManager wfdManager;
    private WifiP2pManager.Channel wfdChannel;
    private Activity activity;
    private IntentFilter intentFilter;
    private WifiP2pDevice[] wfdDevices;


    public WiFiDirectReceiver(WifiP2pManager wfdManager, WifiP2pManager.Channel wfdChannel, Activity activity){
        this.wfdManager = wfdManager;
        this.wfdChannel = wfdChannel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            handleWifiP2pStateChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            handleWifiP2pPeersChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            handleWifiP2pConnectionChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            handleWifiP2pThisDeviceChanged(intent);
        }
    }

    private void handleWifiP2pStateChanged(Intent intent){
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        isWifiDirectEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED ? true : false;
        Toast.makeText(activity, "Enabled : " +isWifiDirectEnabled, Toast.LENGTH_SHORT).show();

    }

    private void handleWifiP2pThisDeviceChanged(Intent intent){
        WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
    }

    private void handleWifiP2pPeersChanged(Intent intent){
        // The list of available peers has changed
        //Request the current list of peers
        wfdManager.requestPeers(wfdChannel, this);
    }

    private void handleWifiP2pConnectionChanged(Intent intent){
        // A connection is in place
        NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if(info != null && info.isConnected()){
            wfdManager.requestConnectionInfo(wfdChannel, this);
        }
    }

    public void registerReceiver(){
        activity.registerReceiver(this, getIntentFilter());
    }

    public void unregisterReceiver(){
        activity.unregisterReceiver(this);
    }

    private IntentFilter getIntentFilter(){
        if(intentFilter == null){
            intentFilter = new IntentFilter();
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        }
        return intentFilter;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        if(wifiP2pInfo.groupFormed){
            Intent intent = new Intent(activity, MultiplayerGame.class);
            intent.putExtra("WIFI_P2P_INFO", wifiP2pInfo);
            activity.startActivity(intent);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        if(wifiP2pDeviceList != null && wifiP2pDeviceList.getDeviceList() != null && wifiP2pDeviceList.getDeviceList().size() > 0){
            wfdDevices = wifiP2pDeviceList.getDeviceList().toArray(new WifiP2pDevice[0]);
        }
        else {
            wfdDevices = null;
        }
    }

    public WifiP2pDevice[] getAvailableDevices(){
        if(wfdDevices != null) {
            return wfdDevices;
        }
        return null;
    }
}
