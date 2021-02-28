package com.example.covid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;

public class SymptomsActivity extends AppCompatActivity {
    public static final String[] ARRAY_SPINNER = DatabaseHelper.columnToSymptomNameMap
            .values()
            .toArray(new String[0]);

    private HashMap<String, Float> spinnerRater;
    private DatabaseHelper myDB;
    private long TimeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        myDB = new DatabaseHelper(this);
        if (savedInstanceState != null) {
            spinnerRater = (HashMap<String, Float>) savedInstanceState.getSerializable("spinnerRatingMap");
            TimeStamp = savedInstanceState.getLong("timestamp");
        } else {
            Bundle bundle = getIntent().getExtras();
            TimeStamp = bundle.getLong("timestamp");

            spinnerRater = myDB.readSymptoms(TimeStamp);
            for (String str : ARRAY_SPINNER) {
                if (!spinnerRater.containsKey(str)) {
                    spinnerRater.put(str, 0.0f);
                }
            }
        }

        final Spinner s = (Spinner) findViewById(R.id.symptomsSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ARRAY_SPINNER);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        final RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingBar);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ratingbar.setRating(spinnerRater.get(parentView.getItemAtPosition(position).toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                spinnerRater.put(s.getSelectedItem().toString(), rating);
            }

        });

    }

    public void uploadSymptoms(View view) {
        if(myDB.updateRowSymptoms(TimeStamp, spinnerRater)) {
            Toast.makeText(this, "Symptoms Upload Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Symptoms Upload Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timestamp", TimeStamp);
        outState.putSerializable("spinnerRatingMap", spinnerRater);
        //Toast.makeText(this, "Saving State", Toast.LENGTH_SHORT).show();
    }
}