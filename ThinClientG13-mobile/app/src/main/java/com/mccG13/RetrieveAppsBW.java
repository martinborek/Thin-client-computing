package com.mccG13;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.coboltforge.dontmind.multivnc.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by fabiano.brito on 19/10/16.
 */

public class RetrieveAppsBW extends AsyncTask<String,Void,String> {
    public MainActivity source = null;
    Context context;
    ProgressDialog loading;

    public RetrieveAppsBW(MainActivity fl, Context ctx) {
        source = fl;
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        String server_ip = SP.getString("server_ip", context.getResources().getString(R.string.server_default_ip));

        String get_apps_url = "http://"+ server_ip +"/getapps/";

        try {
            String token = params[0];
            String blank = params[1];
            URL url = new URL(get_apps_url);

            // creating an http connection to communicate with url
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            String credentials = token + ":" + blank;
            String credBase64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT).replace("\n", "");
            httpURLConnection.setRequestProperty("Authorization", "Basic " + credBase64);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.connect();

            // reading answer from server
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String result = "";
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();

            httpURLConnection.disconnect();
            return result;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        loading = ProgressDialog.show(context, source.getResources().getString(R.string.get_apps_dialog), null, true, true);
    }

    @Override
    protected void onPostExecute(final String result) {
        if(result != null) {
            int numOfApps = 0;
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray jsonArray = jsonObj.getJSONArray("apps");
                numOfApps = jsonArray.length();
                if(numOfApps > 0) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loading.dismiss();
                            Intent intent = new Intent(context, AppSelectionActivity.class);
                            intent.putExtra("appsJSONString", result);
                            source.startActivity(intent);
                        }
                    }, 1000);
                } else {
                    loading.dismiss();
                    Toast.makeText(context, R.string.get_apps_error, Toast.LENGTH_SHORT).show();
                }


            } catch (JSONException e) {
                Log.e("Parsing error", e.toString());
            }
        } else {
            loading.dismiss();
            Toast.makeText(context, R.string.get_apps_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
