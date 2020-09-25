package com.example.cavoid;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);

        }
        ToggleButton alarmToggle = findViewById(R.id.alarmToggle);

        Intent notifyIntent = new Intent(this, AlarmReceiver.class);
        boolean alarmUp = (PendingIntent.getBroadcast(this, NOTIFICATION_ID, notifyIntent,
                PendingIntent.FLAG_NO_CREATE) != null);

        alarmToggle.setChecked(alarmUp);

        final PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        alarmToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                String toastMessage;
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar calendar = Calendar.getInstance();
                if (isChecked) {

                    long thirtySecondsFromNow = System.currentTimeMillis() + 10 * 1000;
                    long repeatInterval_1 = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                    long repeatInterval_2 =  System.currentTimeMillis() + 10 * 1000;
                    long triggerTime = SystemClock.elapsedRealtime()
                            + repeatInterval_2;

                    if (alarmManager != null) {
                        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() +
                                        5 * 1000, notifyPendingIntent);
                    }
                    toastMessage = "Covid alarm on";
                } else {
                    if (alarmManager != null) {
                        alarmManager.cancel(notifyPendingIntent);
                    }
                    mNotificationManager.cancelAll();
                    toastMessage = "Covid alarm off";
                }
                Toast.makeText(MapsActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

    }





    public void createNotificationChannel() {

        // Create a notification manager object.
        mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            "Stand up notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Notifies every 15 minutes to stand up and walk");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Polygon polygon = createCountyPolygon();
        polygon.setClickable(true);
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon){
                System.out.println("click");
                polygon.setFillColor(Color.RED);
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng ashland = new LatLng(37.75, -77.85);
        mMap.addMarker(new MarkerOptions().position(ashland).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ashland));


    }

    /*
      Gets the county line coordinates then creates a polygon object to return them
     */
    private Polygon createCountyPolygon(){
        Polygon polygon = null;
        polygon = mMap.addPolygon(new PolygonOptions()
                .addAll(getCountyLines("51760")));
        return polygon;
    }

    private ArrayList<LatLng> getCountyLines(String fips){
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(41, -109));
        coordinates.add(new LatLng(41, -102));
        coordinates.add(new LatLng(37, -102));
        coordinates.add(new LatLng(37, -109));
        return coordinates;
    }


}