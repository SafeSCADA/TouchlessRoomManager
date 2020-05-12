package com.safescada.touchlessroommanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textStatusView;
    private TextView textOccupancyView;
    private TextView textSensorView;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private Boolean isProximitySensorAvailable;
    private Integer personCount;
    private Integer personLimit;
    private Float sensorValue;
    private Integer longWaveTime;
    private ProgressBar pbarWaveProgress;
    public Boolean riseTrigger;
    public Boolean fallTrigger;
    public long waveProgress;
    public long riseTime;
    public long fallTime;
    public long waveTime;
    ConstraintLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.mainPage);
        textStatusView = findViewById(R.id.textStat);
        textOccupancyView = findViewById(R.id.textStat2);
        textSensorView = findViewById(R.id.textSensorValue);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pbarWaveProgress = (ProgressBar) findViewById(R.id.progressWave);


        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!=null)
        {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            isProximitySensorAvailable = true;
        }else {
            textStatusView.setText("Proximity sensor not available");
            isProximitySensorAvailable = false;
        }

        personCount = 0; // assume no people in on startup
        personLimit = 2; // TODO change this to user configurable
        longWaveTime = 1;
        riseTrigger = false;
        fallTrigger = false;

        roomEmpty(); // vacant to start with

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        sensorValue = sensorEvent.values[0];
        if (sensorValue == 0 && !fallTrigger) {
            if (!riseTrigger)
            {
                riseTime = sensorEvent.timestamp;
            }
            riseTrigger = true;
        }
        if (sensorValue == 0) {
            startProgressBar(riseTime);
        }

        textSensorView.setText("sensor value " + sensorValue + " cm held for " + waveProgress + " falltime: " + fallTime + " risetime: " + riseTime + " sensortime: " + sensorEvent.timestamp);
        if (sensorValue > 0 && riseTrigger) {
            fallTime = sensorEvent.timestamp;
            fallTrigger = true;
        }

        if (sensorValue > 0 && riseTrigger && fallTrigger) {
            //pbarWaveProgress.setProgress(0); // Disabled due to OnSensor change difference in VM and device
            pbarWaveProgress.setVisibility(View.INVISIBLE);
            fallTrigger = false;
            riseTrigger = false;
            waveTime = fallTime - riseTime;
            if (waveTime < longWaveTime * 1000000000)
            {
                fallTime = 0;
                riseTime = 0;
                waveTime = 0;
                addPerson();
            } else {
                fallTime = 0;
                riseTime = 0;
                waveTime = 0;
                removePerson();
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isProximitySensorAvailable)
        {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isProximitySensorAvailable)
        {
            sensorManager.unregisterListener(this);
        }
    }

    public void roomEmpty() {
        textStatusView.setText("VACANT");
        if (personLimit == 1) {
            textOccupancyView.setText("maximum " + personLimit + " person");
        } else {
            textOccupancyView.setText("maximum " + personLimit + " persons");
        }
        layout.setBackgroundColor(Color.parseColor("#00FF00"));

    }

    public void roomFull() {
        textStatusView.setText("ENGAGED");
        if (personLimit == 1) {
            textOccupancyView.setText("by " + personCount + " of " + personLimit + " person");
        } else {
            textOccupancyView.setText("by " + personCount + " of " + personLimit + " persons");
        }
        layout.setBackgroundColor(Color.parseColor("#FF0000"));
    }

    public void roomOccupied() {
        textStatusView.setText("OCCUPIED");
        if (personLimit == 1) {
            textOccupancyView.setText("by " + personCount + " of " + personLimit + " person");
        } else {
            textOccupancyView.setText("by " + personCount + " of " + personLimit + " persons");
        }
        layout.setBackgroundColor(Color.parseColor("#FFFF00"));
    }

    public void addPerson() {
        if (personCount < personLimit) {
            personCount++;
        }
        if (personCount >= personLimit) {
            roomFull();
        } else {
            roomOccupied();
        }
    }

    public void removePerson() {
        if (personCount > 0) {
            personCount--;
        }
        if (personCount == 0) {
            roomEmpty();
        } else {
                roomOccupied();
        }
    }
    public void startProgressBar(long riseTime) {
        // TODO: Change back to horizontal type and use the riseTime in a thread to set progress.
        pbarWaveProgress.setVisibility(View.VISIBLE);

    }

    public void textStat2_onClick(View view) {
        Intent intent=new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


}
