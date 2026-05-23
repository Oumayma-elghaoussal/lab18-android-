package com.example.hellotoast;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour la table etudiant.
 *
 * Méthodes :
 *   create(Etudiant)       — INSERT
 *   getById(int id)        — SELECT … WHERE id = ?
 *   getAll()               — SELECT *
 *   update(Etudiant)       — UPDATE … WHERE id = ?
 *   delete(int id)         — DELETE … WHERE id = ?
 */
public class EtudiantService {

    private static final String TAG = "EtudiantService";
    private final MySQLiteHelper helper;

    public EtudiantService(Context context) {
        this.helper = new MySQLiteHelper(context);
    }

    // ════════════════════════════════════════════════
    //  CREATE
    // ════════════════════════════════════════════════
    public long create(Etudiant e) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COL_NOM, e.getNom());
        values.put(MySQLiteHelper.COL_PRENOM, e.getPrenom());

        long id = db.insert(MySQLiteHelper.TABLE_ETUDIANT, null, values);
        Log.i(TAG, "create → id=" + id + " | " + e);

        db.close();
        return id;
    }

    // ════════════════════════════════════════════════
    //  READ — par ID
    // ════════════════════════════════════════════════
    public Etudiant getById(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(
                MySQLiteHelper.TABLE_ETUDIANT,
                new String[]{MySQLiteHelper.COL_ID, MySQLiteHelper.COL_NOM, MySQLiteHelper.COL_PRENOM},
                MySQLiteHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Etudiant etudiant = null;
        if (cursor.moveToFirst()) {
            etudiant = cursorToEtudiant(cursor);
            Log.i(TAG, "getById(" + id + ") → " + etudiant);
        } else {
            Log.w(TAG, "getById(" + id + ") → introuvable");
        }

        cursor.close();
        db.close();
        return etudiant;
    }

    // ════════════════════════════════════════════════
    //  READ — tous
    // ════════════════════════════════════════════════
    public List<Etudiant> getAll() {
        List<Etudiant> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_ETUDIANT, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToEtudiant(cursor));
            } while (cursor.moveToNext());
        }

        Log.i(TAG, "getAll → " + list.size() + " étudiant(s)");
        cursor.close();
        db.close();
        return list;
    }

    // ════════════════════════════════════════════════
    //  UPDATE
    // ════════════════════════════════════════════════
    public int update(Etudiant e) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COL_NOM, e.getNom());
        values.put(MySQLiteHelper.COL_PRENOM, e.getPrenom());

        int rows = db.update(
                MySQLiteHelper.TABLE_ETUDIANT,
                values,
                MySQLiteHelper.COL_ID + " = ?",
                new String[]{String.valueOf(e.getId())}
        );

        Log.i(TAG, "update(" + e.getId() + ") → " + rows + " ligne(s)");
        db.close();
        return rows;
    }

    // ════════════════════════════════════════════════
    //  DELETE
    // ════════════════════════════════════════════════
    public int delete(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();

        int rows = db.delete(
                MySQLiteHelper.TABLE_ETUDIANT,
                MySQLiteHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        Log.i(TAG, "delete(" + id + ") → " + rows + " ligne(s)");
        db.close();
        return rows;
    }

    // ════════════════════════════════════════════════
    //  Utilitaire : Cursor → Etudiant
    // ════════════════════════════════════════════════
    private Etudiant cursorToEtudiant(Cursor c) {
        return new Etudiant(
                c.getInt(c.getColumnIndexOrThrow(MySQLiteHelper.COL_ID)),
                c.getString(c.getColumnIndexOrThrow(MySQLiteHelper.COL_NOM)),
                c.getString(c.getColumnIndexOrThrow(MySQLiteHelper.COL_PRENOM))
        );
    }
}
