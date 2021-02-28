package com.example.DiaShield;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;

public class SymptomsPage extends AppCompatActivity {
    public static final String[] Simlist = Dbhelp.columnToSymptomNameMap
            .values()
            .toArray(new String[0]);

    private HashMap<String, Float> SList;
    private Dbhelp myDB;
    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        myDB = new Dbhelp(this);
        if (savedInstanceState != null) {
            SList = (HashMap<String, Float>) savedInstanceState.getSerializable("spinnerRatingMap");
            timestamp = savedInstanceState.getLong("timestamp");
        } else {
            Bundle bundle = getIntent().getExtras();
            timestamp = bundle.getLong("timestamp");

            SList = myDB.readSymptoms(timestamp);
            for (String str : Simlist) {
                if (!SList.containsKey(str)) {
                    SList.put(str, 0.0f);
                }
            }
        }

        final Spinner s = (Spinner) findViewById(R.id.symptomsSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, Simlist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        final RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingBar);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ratingbar.setRating(SList.get(parentView.getItemAtPosition(position).toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                SList.put(s.getSelectedItem().toString(), rating);
            }

        });

    }

    public void uploadSym(View view) {
        if(myDB.updateRowSymptoms(timestamp, SList)) {
            Toast.makeText(this, "Symptoms Upload Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Symptoms Upload Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timestamp", timestamp);
        outState.putSerializable("spinnerRatingMap", SList);

    }
}