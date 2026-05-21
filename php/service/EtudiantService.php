<?php
include_once '../classes/Etudiant.php';
include_once '../connexion/Connexion.php';
include_once '../dao/IDao.php';

class EtudiantService implements IDao {
    private $connexion;

    function __construct() {
        $this->connexion = new Connexion();
    }

    public function create($o) {
        $query = "INSERT INTO Etudiant (nom, prenom, ville, sexe)
                  VALUES (:nom, :prenom, :ville, :sexe)";
        $stmt = $this->connexion->getConnexion()->prepare($query);
        $stmt->execute([
            ':nom' => $o->getNom(),
            ':prenom' => $o->getPrenom(),
            ':ville' => $o->getVille(),
            ':sexe' => $o->getSexe()
        ]);
    }

    public function delete($o) {
        // TODO: implement
    }

    public function update($o) {
        // TODO: implement
    }

    public function findAll() {
        $req = $this->connexion->getConnexion()->query("SELECT * FROM Etudiant");
        return $req->fetchAll(PDO::FETCH_ASSOC);
    }

    public function findById($id) {
        // TODO: implement
        return null;
    }

    public function findAllApi() {
        return $this->findAll();
    }
}
?>
