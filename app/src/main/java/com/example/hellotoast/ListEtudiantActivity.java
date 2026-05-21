package com.example.hellotoast;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hellotoast.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListEtudiantActivity extends AppCompatActivity {

    private ListView listView;
    private RequestQueue requestQueue;
    private List<Etudiant> etudiantList = new ArrayList<>();
    private EtudiantAdapter adapter;

    private static final String loadUrl = "http://10.0.2.2/projet/ws/loadEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiant);

        setTitle("Liste des Étudiants");

        listView = findViewById(R.id.listView);
        requestQueue = Volley.newRequestQueue(this);
        adapter = new EtudiantAdapter();
        listView.setAdapter(adapter);

        // Click on an item to show details
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Etudiant e = etudiantList.get(position);
            new AlertDialog.Builder(ListEtudiantActivity.this)
                    .setTitle(e.getNom() + " " + e.getPrenom())
                    .setMessage("Ville : " + e.getVille() + "\nSexe : " + e.getSexe())
                    .setPositiveButton("OK", null)
                    .show();
        });

        chargerEtudiants();
    }

    private void chargerEtudiants() {
        StringRequest request = new StringRequest(Request.Method.GET, loadUrl,
                response -> {
                    Log.d("RESPONSE", response);
                    Type type = new TypeToken<List<Etudiant>>(){}.getType();
                    etudiantList = new Gson().fromJson(response, type);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("VOLLEY", "Erreur : " + error.getMessage());
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
    }

    // Custom adapter for the ListView
    private class EtudiantAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return etudiantList.size();
        }

        @Override
        public Etudiant getItem(int position) {
            return etudiantList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ListEtudiantActivity.this)
                        .inflate(R.layout.item_etudiant, parent, false);
            }

            Etudiant e = getItem(position);

            TextView tvNomPrenom = convertView.findViewById(R.id.itemNomPrenom);
            TextView tvVilleSexe = convertView.findViewById(R.id.itemVilleSexe);

            tvNomPrenom.setText(e.getNom() + " " + e.getPrenom());
            tvVilleSexe.setText(e.getVille() + " — " + e.getSexe());

            return convertView;
        }
    }
}
