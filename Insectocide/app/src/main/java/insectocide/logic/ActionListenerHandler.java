package insectocide.logic;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class ActionListenerHandler implements WifiP2pManager.ActionListener {
    private Activity activity;
    private String actionDisplayText;

    public ActionListenerHandler(Activity activity, String actionDisplayText){
        this.activity = activity;
        this.actionDisplayText = actionDisplayText;
    }

    @Override
    public void onSuccess() {
        Toast.makeText(activity.getApplicationContext(), actionDisplayText + " Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(activity.getApplicationContext(), actionDisplayText + " Failed", Toast.LENGTH_SHORT).show();
    }
}
