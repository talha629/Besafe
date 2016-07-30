package com.PencilIT.besafe;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DraftActivity extends Activity implements OnItemClickListener {

    ArrayList<String> darftList = new ArrayList<>();
    ListView daraftListView;
    ArrayAdapter arrayAdapter;
    ImageButton back;

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    int draft_i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);

        sharedpreferences = getSharedPreferences("BeSafe", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        daraftListView = (ListView) findViewById(R.id.draftlist);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, darftList);
        daraftListView.setAdapter(arrayAdapter);
        daraftListView.setOnItemClickListener(this);

        refreshDrafts();
    }


    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        Toast.makeText(this, pos + " " + id, Toast.LENGTH_LONG).show();
    }

    public void refreshDrafts() {

        draft_i = sharedpreferences.getInt("draft_i", 0);

        while (draft_i > 0) {
            String title = sharedpreferences.getString("title" + (draft_i - 1), " ");
            String date = sharedpreferences.getString("datetime" + (draft_i - 1), " ");
            String description = sharedpreferences.getString("description" + (draft_i - 1), " ");

            String str = draft_i + " " + "Title: " + title + "\n" +
                    "Date: " + date + "\n" +
                    "Description: " + description + "\n";

            draft_i--;

            arrayAdapter.add(str);
        }


    }
}
