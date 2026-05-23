package com.example.hellotoast;

/**
 * Modèle métier — représente un étudiant.
 *
 * Champs :
 *   id      — clé primaire (AUTO_INCREMENT côté SQLite)
 *   nom     — nom de famille
 *   prenom  — prénom
 */
public class Etudiant {

    private int id;
    private String nom;
    private String prenom;

    // ── Constructeurs ──────────────────────────────

    /** Constructeur complet (utilisé à la lecture depuis la BDD). */
    public Etudiant(int id, String nom, String prenom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }

    /** Constructeur sans id (utilisé à l'insertion, id = AUTO_INCREMENT). */
    public Etudiant(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }

    // ── Getters / Setters ──────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    // ── toString (utile pour Logcat) ───────────────

    @Override
    public String toString() {
        return "Etudiant{id=" + id + ", nom='" + nom + "', prenom='" + prenom + "'}";
    }
}
