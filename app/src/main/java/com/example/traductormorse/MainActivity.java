package com.example.traductormorse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView codigoMorse;
    EditText editTextPhone;
    private SensorManager sensorManager;
    private Sensor light;
    private boolean esTextEnMorse;
    private float ultimaMedicion;
    final int PUNTO_DE_TRADUCCION = 5;
    private SoundMaker soundMaker;
    private MorseCode morseCode;
    private AuthenticationResult authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        codigoMorse = findViewById(R.id.codigoMorse);
        authentication = new AuthenticationResult(getApplicationContext());
        codigoMorse.setText("");
        soundMaker = new SoundMaker(getApplication().getApplicationContext());
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        light = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) : null;
        esTextEnMorse = true;
        ultimaMedicion = 0;
        editTextPhone = findViewById(R.id.editTextPhone);
        morseCode = new MorseCode();

    }

    public void agregarRaya(View view) {
        codigoMorse.append("-");
        soundMaker.playDash();
    }

    public void agregarPunto(View view) {
        codigoMorse.append(".");
        soundMaker.playDot();
    }

    public void agregarEspacio(View view) {
        codigoMorse.append("|");
    }

    public void enviar(View view) {

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
            }, 1000);

            try {
                SmsManager smgr = SmsManager.getDefault();
                smgr.sendTextMessage(editTextPhone.getText().toString(), null, this.morseCode.morseToAlpha(codigoMorse.getText().toString()), null, null);
                Toast.makeText(MainActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void borrar(View view) {
        String str = codigoMorse.getText().toString();
        int length = str.length();
        if (0 < length) {
            str = str.substring(0, length - 1);
            codigoMorse.setText(str);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if(ultimaMedicion != event.values[0] && codigoMorse.getText().toString().length() > 0) {
                if (event.values[0] < PUNTO_DE_TRADUCCION && esTextEnMorse) {
                    codigoMorse.setText(this.morseCode.morseToAlpha(codigoMorse.getText().toString().trim()));
                    esTextEnMorse = false;
                } else if (event.values[0] >= PUNTO_DE_TRADUCCION && !esTextEnMorse) {
                    codigoMorse.setText(this.morseCode.alphaToMorse(codigoMorse.getText().toString()));
                    esTextEnMorse = true;
                }
            }
            ultimaMedicion = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}