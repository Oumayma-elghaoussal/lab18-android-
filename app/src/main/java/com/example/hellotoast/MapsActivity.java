package com.example.hellotoast;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapsActivity extends AppCompatActivity {

    private MapView mapView;
    private RequestQueue requestQueue;

    // Remplacer par l'IP du PC serveur (même Wi-Fi)
    private final String showUrl = "http://192.168.43.228/localisation/showPositions.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration OSMDroid (user agent, cache, etc.)
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        setContentView(R.layout.activity_maps);

        // Initialisation de la MapView
        mapView = findViewById(R.id.osmMap);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Contrôleur de carte (zoom, centre)
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        // Position par défaut (Fès, Maroc)
        GeoPoint startPoint = new GeoPoint(34.0331, -5.0003);
        mapController.setCenter(startPoint);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Charger les positions depuis le serveur
        setUpMap();
    }

    private void setUpMap() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                showUrl,
                null,
                response -> {
                    try {
                        // showPositions.php renvoie {"positions":[...]}
                        JSONArray positions = response.getJSONArray("positions");

                        for (int i = 0; i < positions.length(); i++) {
                            JSONObject position = positions.getJSONObject(i);

                            double lat = position.getDouble("latitude");
                            double lon = position.getDouble("longitude");

                            // Créer un marqueur OSMDroid
                            Marker marker = new Marker(mapView);
                            marker.setPosition(new GeoPoint(lat, lon));
                            marker.setTitle("Position " + (i + 1));
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                            mapView.getOverlays().add(marker);
                        }

                        // Centrer sur la dernière position si disponible
                        if (positions.length() > 0) {
                            JSONObject last = positions.getJSONObject(positions.length() - 1);
                            double lat = last.getDouble("latitude");
                            double lon = last.getDouble("longitude");
                            mapView.getController().setCenter(new GeoPoint(lat, lon));
                        }

                        mapView.invalidate();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getApplicationContext(),
                        "Erreur chargement positions: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
