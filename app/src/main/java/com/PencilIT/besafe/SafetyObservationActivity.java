package com.PencilIT.besafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class SafetyObservationActivity extends Activity implements View.OnClickListener {

    Button SO1, SO2, SO3, SO4, draft;
    ImageButton back, user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_observation);

        SO1 = (Button) findViewById(R.id.SO1);
        SO1.setOnClickListener(this);
        SO2 = (Button) findViewById(R.id.SO2);
        SO2.setOnClickListener(this);
        SO3 = (Button) findViewById(R.id.SO3);
        SO3.setOnClickListener(this);
        SO4 = (Button) findViewById(R.id.SO4);
        SO4.setOnClickListener(this);
        draft = (Button) findViewById(R.id.draft);
        draft.setOnClickListener(this);

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(this);

        user = (ImageButton) findViewById(R.id.user);
        user.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.safety_observation, menu);
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

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), SafetyObservationFormActivity.class);
        switch (v.getId()) {
            case R.id.SO1:
                intent.putExtra("SafetyObservation", "SAFE OBSERVATION");
                break;
            case R.id.SO2:
                intent.putExtra("SafetyObservation", "UNSAFE OBSERVATION");
                break;
            case R.id.SO3:
                intent.putExtra("SafetyObservation", "UNSAFE CONDITION");
                break;
            case R.id.SO4:
                intent.putExtra("SafetyObservation", "NEAR MISS");
                break;
            case R.id.draft:
                Intent intentDraft = new Intent(getApplicationContext(), DraftActivity.class);
                startActivity(intentDraft);

                break;
            case R.id.back:
                finish();

                break;
            case R.id.user:
                Intent intentUser = new Intent(getApplicationContext(), UserSettingActivity.class);
                startActivity(intentUser);

                break;


        }
        if (v.getId() != R.id.back && v.getId() != R.id.user && v.getId() != R.id.draft)
            startActivity(intent);

    }
}
