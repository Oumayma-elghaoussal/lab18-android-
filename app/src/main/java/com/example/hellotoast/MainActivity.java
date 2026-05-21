package com.example.hellotoast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String TAG = "LOCALISATION";
    private static final String SERVER_URL = "http://10.0.2.2/localisation/ws/savePosition.php";

    // UI
    private TextView tvStatus, tvLatitude, tvLongitude, tvAltitude, tvAccuracy;
    private TextView tvDeviceId, tvDate, tvServerResponse;
    private MaterialButton btnLocate, btnSend;
    private MaterialCardView cardResult;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double currentLat = 0, currentLng = 0;
    private String currentDate = "";
    private String deviceId = "";
    private boolean locationObtained = false;

    // Network
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Localisation TP");

        // Init UI
        tvStatus = findViewById(R.id.tvStatus);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvDeviceId = findViewById(R.id.tvDeviceId);
        tvDate = findViewById(R.id.tvDate);
        tvServerResponse = findViewById(R.id.tvServerResponse);
        btnLocate = findViewById(R.id.btnLocate);
        btnSend = findViewById(R.id.btnSend);
        cardResult = findViewById(R.id.cardResult);

        // Device ID (Android ID — no special permission needed)
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        tvDeviceId.setText(deviceId);

        // Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);

        // Location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocationUI(location);
                    // Stop updates after first fix
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        // Button listeners
        btnLocate.setOnClickListener(v -> startLocationRequest());
        btnSend.setOnClickListener(v -> sendToServer());
    }

    /**
     * Check permissions then request location updates
     */
    private void startLocationRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        tvStatus.setText("Recherche de position en cours...");
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdates(5)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    /**
     * Update all UI fields with the obtained location
     */
    private void updateLocationUI(Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();
        currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        tvLatitude.setText(String.valueOf(currentLat));
        tvLongitude.setText(String.valueOf(currentLng));
        tvAltitude.setText(String.format(Locale.getDefault(), "%.1f m", location.getAltitude()));
        tvAccuracy.setText(String.format(Locale.getDefault(), "%.1f m", location.getAccuracy()));
        tvDate.setText(currentDate);
        tvStatus.setText("✅ Position détectée avec succès !");
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.success));

        locationObtained = true;
        btnSend.setEnabled(true);

        Log.d(TAG, "Lat=" + currentLat + " Lng=" + currentLng);
    }

    /**
     * Send position data to PHP web service via Volley POST
     */
    private void sendToServer() {
        if (!locationObtained) {
            Toast.makeText(this, "Veuillez d'abord obtenir la position", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);
        btnSend.setText("Envoi en cours...");

        StringRequest request = new StringRequest(Request.Method.POST, SERVER_URL,
                response -> {
                    Log.d(TAG, "Réponse serveur : " + response);
                    cardResult.setVisibility(View.VISIBLE);
                    tvServerResponse.setText("✅ Données envoyées avec succès !\n\n" +
                            "Latitude : " + currentLat + "\n" +
                            "Longitude : " + currentLng + "\n" +
                            "Date : " + currentDate + "\n" +
                            "ID : " + deviceId + "\n\n" +
                            "Réponse : " + response);
                    tvServerResponse.setTextColor(ContextCompat.getColor(this, R.color.success));
                    btnSend.setText("☁️ Envoyer au serveur");
                    btnSend.setEnabled(true);
                    Toast.makeText(this, "Envoyé avec succès !", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e(TAG, "Erreur Volley : " + error.getMessage());
                    cardResult.setVisibility(View.VISIBLE);
                    tvServerResponse.setText("❌ Erreur de connexion au serveur.\n" +
                            "Vérifiez que XAMPP (Apache + MySQL) est démarré.\n\n" +
                            "Erreur : " + (error.getMessage() != null ? error.getMessage() : "Timeout"));
                    tvServerResponse.setTextColor(ContextCompat.getColor(this, R.color.error));
                    btnSend.setText("☁️ Envoyer au serveur");
                    btnSend.setEnabled(true);
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("latitude", String.valueOf(currentLat));
                params.put("longitude", String.valueOf(currentLng));
                params.put("date", currentDate);
                params.put("deviceId", deviceId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationRequest();
            } else {
                tvStatus.setText("❌ Permission de localisation refusée");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates when activity is paused
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
