<?php
class Connexion {
    private $connexion;

    public function __construct() {
        try {
            $this->connexion = new PDO(
                "mysql:host=localhost;dbname=localisation_db;charset=utf8",
                "root",
                ""
            );
            $this->connexion->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        } catch (PDOException $e) {
            die(json_encode(["error" => "Connexion échouée : " . $e->getMessage()]));
        }
    }

    public function getConnexion() {
        return $this->connexion;
    }
}
?>
