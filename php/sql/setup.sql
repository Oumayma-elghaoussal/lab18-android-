-- =============================================
-- LAB 9 : Base de données school1
-- =============================================

CREATE DATABASE IF NOT EXISTS school1;
USE school1;

CREATE TABLE IF NOT EXISTS Etudiant (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(50),
  prenom VARCHAR(50),
  ville VARCHAR(50),
  sexe VARCHAR(10)
);

-- Données de test
INSERT INTO Etudiant (nom, prenom, ville, sexe)
VALUES ('Lachgar', 'Mohamed', 'Rabat', 'homme'),
       ('Safi', 'Amine', 'Marrakech', 'homme');
