package com.example.chitooowifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity  {

    StringBuilder stringBuilder = new StringBuilder();
    // for displaying available wifi SSID results
    TextView textViewWifiResults;
    // This class provides the primary API for managing all aspects of Wi-Fi connectivity
    WifiManager wifiManager;
    //stores the wifi scan result in a list
    List<ScanResult> scanList;

    // intent filter for broadcast receiver
    IntentFilter filter;
    // dummy value for auto-wifi connection test
    String myNetworkName = "" ;
    final String dbSSID = "Amage";
    final String dbPassword = "amagenepal9";

//    final String dbSSID = "ideaAction";
//    final String dbPassword = "idea1234";

    // flags to disconnect and reconnect required wifi network only on the 1st receiver trigger
    private boolean firstTimeDisconnect = true;
    private boolean firstTimeReconnect = true;

    // extends broadcast receiver (inner class)
    WifiReceiver wifiReceiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        // unregister the wifiReceiver if app is closed
        unregisterReceiver(wifiReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initView() {

        Log.d(TAG, "initView: ");
        textViewWifiResults = (TextView)findViewById(R.id.tvWifiNetworks);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        // getting instance of wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // enable wifi ,if wifi is disabled by user
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(),
                    "wifi is disabled ! enabling it", Toast.LENGTH_LONG).show();

            wifiManager.setWifiEnabled(true);

            Toast.makeText(getApplicationContext(),
                    "wifi enabled", Toast.LENGTH_LONG).show();
        }

        // new instance of wifiReceiver
        wifiReceiver = new WifiReceiver();
        // define filter and action for dynamic registration of broadcast receicer i.e. wifiReceiver
        registerReceiverIntentFilter();
        // call to register the wifireceiver. Register it on app launch ,
        // and it gets triggered whenever wifi available list is updated
        registerReceiver(wifiReceiver,filter);
        // request a scan for access points
        wifiManager.startScan();

    }
    // dynamic registration of broadcast receiver for WifiManager
    private void registerReceiverIntentFilter(){
        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }


    public class WifiReceiver extends BroadcastReceiver{

        // to save the configuration of wifi. i.e. SSID and password
        final WifiConfiguration conf = new WifiConfiguration();

        @Override
        public void onReceive(Context context, Intent intent) {

            // to append the values to textview
            stringBuilder = new StringBuilder();
            // get the list of available wifi networks
            scanList = wifiManager.getScanResults();

            Log.d(TAG, "onReceive: scanList : " +"\n"+ scanList );
            stringBuilder.append("\n  Number Of Wifi connections :" + " " + scanList.size() + "\n\n");

            for (int i = 0; i < scanList.size(); i++) {
                Log.d(TAG, "onReceive: wifi details : " + scanList.get(i).SSID.toString()+"\n");

                stringBuilder.append(new Integer(i + 1).toString() + ". ");
                stringBuilder.append((scanList.get(i).SSID).toString());
                stringBuilder.append("\n\n");

                // if networkId = Amage(required SSID) ,connect to it
                if (scanList.get(i).SSID.toString().equals(dbSSID)) {

                    Log.d(TAG, "onCreate: check networkID :" + scanList.get(i).SSID.toString());
                    Toast.makeText(getApplicationContext(),
                            "required network found", Toast.LENGTH_LONG).show();
                    // get the SSID which matches the required SSID
                    myNetworkName = scanList.get(i).SSID.toString();

                    Log.d(TAG, "onReceive: "+ dbSSID);

                }

            }
            // add the network ID's to textview
            textViewWifiResults.setText(stringBuilder);

            // add required network name to wifi configuration
            conf.SSID = "\"" + myNetworkName + "\"";
            Log.d(TAG, "getWifiNetworksList: conf" + conf.SSID.toString());
            conf.status = WifiConfiguration.Status.ENABLED;
            //Open System authentication (required for WPA/WPA2)
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//              RSN -> WPA2/IEEE 802.11i
//              WPA -> WPA/IEEE 802.11i/D3.0
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//               WPA_EAP
//               WPA using EAP authentication.
//               int   WPA_PSK
//               WPA pre-shared key (requires preSharedKey to be specified).
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
//                int	CCMP
//                AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
//                int	NONE
//                Use only Group keys (deprecated)
//                int	TKIP
//                Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                int	CCMP
//                AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
//                int	TKIP
//                Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            // add preshared key to wifi configuration
            conf.preSharedKey = "\""+ dbPassword +"\"";
            // pass the saved configuration to wifimanager network.
            wifiManager.addNetwork(conf);
            // no need to call wifimanager.saveConfiguration. it is deprecated. config is automatically saved
            Log.d(TAG, "connectToWifi: conf.ssid"+ conf.SSID);

            // disconnect previously connected wifi ,and connect to amage(required)
            if(conf.SSID != null && conf.SSID.equals("\"" + dbSSID + "\"") ) {
                // disconnect from currently connected SSID
                if(firstTimeDisconnect) {
                    // false because disconnect only during the app launch ,not after that
                    firstTimeDisconnect = false;
                    //disconnects from current network
                    Boolean disconnect = wifiManager.disconnect();
                    Log.d(TAG, "getWifiNetworksList: disconnect :"+ disconnect );

                }
                // enables and connects to the wifi with the conf.networkId (required networkId)
                Boolean enableNetwork = wifiManager.enableNetwork(conf.networkId, true);
                Log.d(TAG, "onReceive: enable network"+enableNetwork);

//                     if networkID != -1 , it is success .it will return true

                // reconnect now to the required network
                if(firstTimeReconnect) {
                    // false because disconnect and reconnect only during the app launch ,not after that
                    firstTimeReconnect = false;
                    //reconnects to new enabled network
                    Boolean reconnect = wifiManager.reconnect();
                    Log.d(TAG, "getWifiNetworksList: " + reconnect);
                }

            }


        }
    }

}



