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

public class MainActivity extends AppCompatActivity {
    
    StringBuilder stringBuilder = new StringBuilder();
    TextView textViewWifiResults;

    WifiManager wifiManager;
    //stores the wifi scan result in a list
    List<ScanResult> scanList;
    BroadcastReceiver receiver;

    // intent filter for broadcast receiver
    IntentFilter filter;
    // dummy value for auto-wifi connection test

    String myNetworkName = "";
    String dbSSID = "Bibek";
    final String dbPassword = "Bib9841445379";


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void initView(){
        Log.d(TAG, "initView: ");
        textViewWifiResults = (TextView)findViewById(R.id.tvWifiNetworks);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity: Thread id :"+ Thread.currentThread().getId());
        initView();
        getWifiNetworksList();
    }


    private void getWifiNetworksList(){

        final WifiConfiguration conf = new WifiConfiguration();
        Log.d(TAG, "getWifiNetworksList: ");
        // dynamic registration of broadcast receiver for WifiManager
        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        // getting instance of wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                // enable wifi ,if wifi is disabled by user
                if (!wifiManager.isWifiEnabled()) {
                    Toast.makeText(getApplicationContext(),
                            "wifi is disabled ! enabling it", Toast.LENGTH_LONG).show();
                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(getApplicationContext(),
                            "wifi enabled", Toast.LENGTH_LONG).show();
                }
                stringBuilder = new StringBuilder();
                // get the list of available wifi networks
                scanList = wifiManager.getScanResults();
                Log.d(TAG, "onReceive: scanList : " + scanList);

                stringBuilder.append("\n  Number Of Wifi connections :" + " " + scanList.size() + "\n\n");
                
                for (int i = 0; i < scanList.size(); i++) {
                    
                    Log.d(TAG, "onReceive: wifi details : " + scanList.get(i).SSID.toString());

                    stringBuilder.append(new Integer(i + 1).toString() + ". ");
                    stringBuilder.append((scanList.get(i).SSID).toString());
                    stringBuilder.append("\n\n");

                    // if networkId = Amage ,connect to it
                    if (scanList.get(i).SSID.toString().equals(dbSSID)) {

                        Log.d(TAG, "onCreate: check networkID :" + scanList.get(i).SSID.toString());
                        Log.d(TAG, "onCreate: password : " + dbPassword);
//                        dbSSID = scanList.get(i).SSID.toString();
                        myNetworkName = scanList.get(i).SSID.toString();
                        Log.d(TAG, "onReceive: "+ dbSSID);

                    }

                }
                // add the network ID's to textview
                textViewWifiResults.setText(stringBuilder);


                conf.SSID = "\"" + myNetworkName + "\"";
                Log.d(TAG, "getWifiNetworksList: conf" + conf.SSID.toString());
                conf.preSharedKey = "\""+ dbPassword +"\"";
                wifiManager.addNetwork(conf);

                Log.d(TAG, "connectToWifi: conf.ssid"+ conf.SSID.toString());
                // disconnect previously connected wifi ,and connect to amage
                if(conf.SSID != null && conf.SSID.equals("\"" + dbSSID + "\"")) {

                    Boolean disconnect = wifiManager.disconnect();
                    Log.d(TAG, "getWifiNetworksList: disconnect :"+ disconnect );

                    Boolean enableNetwork = wifiManager.enableNetwork(conf.networkId, true);
                    Log.d(TAG, "getWifiNetworksList: enableNetwork :"+enableNetwork);

                    Boolean reconnect = wifiManager.reconnect();
                    Log.d(TAG, "getWifiNetworksList: "+ reconnect);

                }else{
                    Log.d(TAG, "getWifiNetworksList: conf null or not equal to dbSSID");
                    Toast.makeText(getApplicationContext(),
                            "required network not found", Toast.LENGTH_LONG).show();
                }



            }
        };
        registerReceiver(receiver,filter);
        wifiManager.startScan();



    }

}

