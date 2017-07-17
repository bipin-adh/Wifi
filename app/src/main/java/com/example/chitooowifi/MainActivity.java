package com.example.chitooowifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.content.ContentValues.TAG;
import static android.net.wifi.WifiConfiguration.KeyMgmt.WPA_EAP;
import static android.net.wifi.WifiConfiguration.Protocol.WPA;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    
    StringBuilder stringBuilder = new StringBuilder();
    TextView textViewWifiResults;
    Button btnScan,btnStopScan;

    WifiManager wifiManager;
    //stores the wifi scan result in a list
    List<ScanResult> scanList;
    BroadcastReceiver receiver;

    // intent filter for broadcast receiver
    IntentFilter filter;
    // dummy value for auto-wifi connection test


    String myNetworkName = "" ;
    final String dbSSID = "Amage";
    final String dbPassword = "amagenepal9";
//    final String dbSSID = "ideaAction";
//    final String dbPassword = "idea1234";

    private boolean firstTimeDisconnect = true;
    private boolean firstTimeReconnect = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unregisterReceiver(receiver);
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
        btnScan = (Button)findViewById(R.id.btn_scan);
        btnStopScan = (Button)findViewById(R.id.btn_stopscan);
        textViewWifiResults = (TextView)findViewById(R.id.tvWifiNetworks);

        btnScan.setOnClickListener(this);
        btnStopScan.setOnClickListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity: Thread id :"+ Thread.currentThread().getId());
        initView();

    }
    private void registerReceiver(){
        // dynamic registration of broadcast receiver for WifiManager
        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }


    private void getWifiNetworksList(){

        final WifiConfiguration conf = new WifiConfiguration();

        registerReceiver();
        Log.d(TAG, "getWifiNetworksList: ");
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
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


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

                    // if networkId = Amage ,connect to it
                    if (scanList.get(i).SSID.toString().equals(dbSSID)) {

                        Log.d(TAG, "onCreate: check networkID :" + scanList.get(i).SSID.toString());
                        Toast.makeText(getApplicationContext(),
                                "required network found", Toast.LENGTH_LONG).show();
                        myNetworkName = scanList.get(i).SSID.toString();
                        Log.d(TAG, "onReceive: "+ dbSSID);

                    }

                }
                // add the network ID's to textview

                textViewWifiResults.setText(stringBuilder);

                conf.SSID = "\"" + myNetworkName + "\"";
                Log.d(TAG, "getWifiNetworksList: conf" + conf.SSID.toString());

                //Open System authentication (required for WPA/WPA2)
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                // RSN -> WPA2/IEEE 802.11i
                // WPA -> WPA/IEEE 802.11i/D3.0
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WPA);
//                WPA_EAP
//                WPA using EAP authentication.
//                int	WPA_PSK
//                WPA pre-shared key (requires preSharedKey to be specified).
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedKeyManagement.set(WPA_EAP);

//                int	CCMP
//                AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
//                int	NONE
//                Use only Group keys (deprecated)
//                int	TKIP
//                Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

//                int	CCMP
//                AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
//                int	TKIP
//                Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                conf.preSharedKey = "\""+ dbPassword +"\"";
                wifiManager.addNetwork(conf);

                Log.d(TAG, "connectToWifi: conf.ssid"+ conf.SSID);
                // disconnect previously connected wifi ,and connect to amage

                if(conf.SSID != null && conf.SSID.equals("\"" + dbSSID + "\"") ) {

                    if(firstTimeDisconnect) {
                        firstTimeDisconnect = false;
                        Boolean disconnect = wifiManager.disconnect();
                        Log.d(TAG, "getWifiNetworksList: disconnect :"+ disconnect );

                    }
                    Log.d(TAG, "onReceive: network id : " + conf.networkId);
                    Boolean enableNetwork = wifiManager.enableNetwork(conf.networkId, true);
                    Log.d(TAG, "getWifiNetworksList: enableNetwork :"+enableNetwork);

                    Log.d(TAG, "onReceive: wifi state : " + wifiManager.getWifiState());
//                     if networkID != -1 , it is success .it will return true

                   if(firstTimeReconnect) {
                       firstTimeReconnect = false;
                       Boolean reconnect = wifiManager.reconnect();
                       Log.d(TAG, "getWifiNetworksList: " + reconnect);
                   }

                }

            }
        };
        registerReceiver(receiver,filter);
        wifiManager.startScan();
    }

    private void stopScan(){

        if(receiver!=null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            Log.d(TAG, "getWifiNetworksList: wifi info network id" + wifiInfo.getNetworkId());
            Log.d(TAG, "getWifiNetworksList: wifi state : " + wifiManager.getWifiState());
            Log.d(TAG, "getWifiNetworksList: wifi ssid :" + wifiInfo.getSSID());

            // unregister receiver after a connection is established
            if (wifiInfo != null && wifiInfo.getSSID().equals("\"" + dbSSID + "\"")) {
                Log.d(TAG, "getWifiNetworksList: wifi Info ssid equals to Amage");
                unregisterReceiver(receiver);
            } else {
                Log.d(TAG, "getWifiNetworksList: condition not met");
                Log.d(TAG, "getWifiNetworksList: ssid : " + wifiInfo.getSSID());
                Log.d(TAG, "getWifiNetworksList: dbssid : " + dbSSID);
            }
        }else{
                Log.d(TAG, "stopScan: cannot unregister,receiver is not registered");
                Toast.makeText(getApplicationContext(),
                    "scan not started", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){

            case    R.id.btn_scan :
                    getWifiNetworksList();
                    break;

            case    R.id.btn_stopscan :
                    Log.d(TAG, "onClick: stop scan ,unregister receiver ");
                    stopScan();
                    break;
        }

    }
}

