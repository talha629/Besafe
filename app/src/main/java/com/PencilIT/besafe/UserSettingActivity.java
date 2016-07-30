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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.HashMap;

public class UserSettingActivity extends Activity implements View.OnClickListener {

    SharedPreferences sharedpreferences;
    Button updatepass, logout;
    EditText newpass, confnewpass;
    String sI, sP;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        sharedpreferences = getSharedPreferences("BeSafe", Context.MODE_PRIVATE);

        sI = sharedpreferences.getString("userid", "");
        sP = sharedpreferences.getString("password", "");

        newpass = (EditText) findViewById(R.id.newpass);
        confnewpass = (EditText) findViewById(R.id.confnewpass);


        updatepass = (Button) findViewById(R.id.updatepass);
        updatepass.setOnClickListener(this);

        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(this);
        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(this);


    }

    public boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.updatepass:

                if (isNetworkAvailable()) {
                    if (newpass.getText().toString().length() > 0 && newpass.getText().toString().equals(confnewpass.getText().toString())) {
                        new LoginAsync().execute(sI, confnewpass.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "Password Not same", Toast.LENGTH_LONG).show();

                    }
                } else
                    Toast.makeText(getApplicationContext(), "No Internet!", Toast.LENGTH_LONG).show();
                break;

            case R.id.logout:
                SharedPreferences preferences = getSharedPreferences("BeSafe", Context.MODE_PRIVATE);
                preferences.edit().clear().apply();

                Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
                intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentMain);

                break;
            case R.id.back:
                finish();

                break;
        }
    }

    class LoginAsync extends AsyncTask<String, String, String> {

        JSONParser jsonParser = new JSONParser();
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(UserSettingActivity.this);
            pDialog.setMessage("Updating...");
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
                        Config.PASSWORD_UPDATE_URL, "POST", params);

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
                editor.putString("password", confnewpass.getText().toString());
                editor.apply();
                Toast.makeText(getApplicationContext(), "SUccessful", Toast.LENGTH_LONG).show();
                newpass.getText().clear();
                confnewpass.getText().clear();
                //finish();
            } else
                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();

        }

    }

}
