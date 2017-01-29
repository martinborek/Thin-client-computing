package com.mccG13;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.coboltforge.dontmind.multivnc.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppSelectionActivity extends MainActivity {

    public AppSelectionActivity source = this;
    private int heartFrequency = 10; // 10 minutes


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        SharedPreferences sharedPref = source.getSharedPreferences("sessionData", Context.MODE_PRIVATE);
        String token = sharedPref.getString("token", "");

        Bundle extras = getIntent().getExtras();
        final String appsJSONString = extras.getString("appsJSONString");

        try {
            JSONObject jsonObj = new JSONObject(appsJSONString);
            JSONArray jsonArray = jsonObj.getJSONArray("apps");
            int numOfApps = jsonArray.length();
            final String[] readableNames = new String[jsonArray.length()];
            final String[] instancesNames = new String[jsonArray.length()];

            //changing the order of applications;
            //if you are in CS-building then the order is openoff-inksc
            if (MainActivity.c.equals("Inside"))
                for (int i = 0; i < numOfApps; i++) {
                    readableNames[i] = jsonArray.getJSONObject(i).getString("readableName");
                    instancesNames[i] = jsonArray.getJSONObject(i).getString("instanceName");
                }

            else {
                {
                    for (int i = 0; i < numOfApps; i++) {
                        readableNames[i] = jsonArray.getJSONObject(i).getString("readableName");
                        instancesNames[i] = jsonArray.getJSONObject(i).getString("instanceName");
                    }
                    String tempR, tempI;
                    tempR=readableNames[1];
                    tempI=instancesNames[1];
                    readableNames[1]=readableNames[0];
                    instancesNames[1]=instancesNames[0];
                    readableNames[0]=tempR;
                    instancesNames[0]=tempI;
                }
            }
            ListView list = (ListView) findViewById(R.id.apps_listview);
            AppListAdapter adapter = new AppListAdapter(this, instancesNames, readableNames);
            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    SharedPreferences sharedPref = AppSelectionActivity.this.getSharedPreferences("sessionData", Context.MODE_PRIVATE);
                    String token = sharedPref.getString("token", "");

                    StartVMBW startVMBW = new StartVMBW(AppSelectionActivity.this, AppSelectionActivity.this);
                    startVMBW.execute(token, "", instancesNames[position]);

                }
            });

        } catch (JSONException e) {
            Log.e("Parsing error", e.toString());
        }

    }

    private Handler heartHandler = new Handler();
    private Runnable heartRunnable = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sharedPref = AppSelectionActivity.this.getSharedPreferences("sessionData", Context.MODE_PRIVATE);
            String token = sharedPref.getString("token", "");
            HeartbeatBW heartbeatBW = new HeartbeatBW(AppSelectionActivity.this, AppSelectionActivity.this);
            heartbeatBW.execute(token, "");
            startHeartBeat();
        }
    };

    private void startHeartBeat() {
        heartHandler.postDelayed(heartRunnable, heartFrequency * 60 * 1000);
    }

    private void stopHeartBeat() {
        heartHandler.removeCallbacks(heartRunnable);
    }

    public void startVirtualApp(String instanceIP) {

        startHeartBeat();

        String cloudPort = ":5901";
        String colorScheme = "/C24bit";
        String instancePwd = "/tReFre4r";

        Intent intent = new Intent(this, com.coboltforge.dontmind.multivnc.VncCanvasActivity.class);
        intent.setData(Uri.parse("vnc://" + instanceIP + cloudPort + colorScheme + instancePwd));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        stopHeartBeat();
    }

    @Override
    public void onRestart() {
        super.onRestart();

        stopHeartBeat();

        SharedPreferences sharedPref = source.getSharedPreferences("sessionData", Context.MODE_PRIVATE);
        String token = sharedPref.getString("token", "");

        StopVMBW stopVMBW = new StopVMBW(AppSelectionActivity.this, AppSelectionActivity.this);
        stopVMBW.execute(token, "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopHeartBeat();
    }

}
