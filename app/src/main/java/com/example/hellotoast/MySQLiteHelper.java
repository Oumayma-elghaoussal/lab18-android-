package com.example.hellotoast;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper SQLite — crée et met à jour la base de données.
 *
 * Base   : etudiants.db
 * Table  : etudiant (id INTEGER PK AUTOINCREMENT, nom TEXT, prenom TEXT)
 * Version: 1
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "MySQLiteHelper";

    // Constantes de la base
    private static final String DATABASE_NAME = "etudiants.db";
    private static final int DATABASE_VERSION = 1;

    // Constantes de la table
    public static final String TABLE_ETUDIANT = "etudiant";
    public static final String COL_ID = "id";
    public static final String COL_NOM = "nom";
    public static final String COL_PRENOM = "prenom";

    // Script de création
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_ETUDIANT + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NOM + " TEXT NOT NULL, "
                    + COL_PRENOM + " TEXT NOT NULL"
                    + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate : création de la table " + TABLE_ETUDIANT);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade : suppression table (v" + oldVersion + " → v" + newVersion + ")");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ETUDIANT);
        onCreate(db);
    }
}
