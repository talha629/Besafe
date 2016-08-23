package com.PencilIT.besafe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class MainActivity extends Activity {

    EditText userid, password;
    Button button;


    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences("BeSafe", Context.MODE_PRIVATE);

        userid = (EditText) findViewById(R.id.userId);
        password = (EditText) findViewById(R.id.password);
        String sI = sharedpreferences.getString("userid", null);
        String sP = sharedpreferences.getString("password", null);
        Boolean sL = sharedpreferences.getBoolean("logedin", false);

        if (sI != null)
            userid.setText(sI);
        if (sP != null)
            password.setText(sP);
        if (sL) {
            Intent intent = new Intent(getApplicationContext(), CautionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }

        button = (Button) findViewById(R.id.buttonSubmit);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (isNetworkAvailable())
                    new LoginAsync().execute(userid.getText().toString(), password.getText().toString());
                else
                    Toast.makeText(getApplicationContext(), "No Internet!", Toast.LENGTH_LONG).show();

            }
        });


    }


    public boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    class LoginAsync extends AsyncTask<String, String, String> {

        JSONParser jsonParser = new JSONParser();
        private ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Attempting login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("userid", args[0]);
                params.put("password", args[1]);

                Log.d("request", "starting");

                String result = jsonParser.makeHttpRequest(
                        Config.LOGIN_URL, "POST", params);

                if (result != null) {
                    Log.d("JSON result", result);

                    return result;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            if (result != null && result.equals("Success!")) {

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("userid", userid.getText().toString());
                editor.putString("password", password.getText().toString());
                editor.putBoolean("logedin", true);

                editor.apply();
                Intent intent = new Intent(getApplicationContext(), CautionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else
                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();

        }

    }
}
