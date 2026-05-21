<?php
header('Content-Type: application/json; charset=utf-8');

include_once '../connexion/Connexion.php';

try {
    $conn = new Connexion();
    $pdo = $conn->getConnexion();

    $query = "SELECT * FROM position ORDER BY created_at DESC";
    $stmt = $pdo->query($query);
    $positions = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => "success",
        "count"  => count($positions),
        "data"   => $positions
    ]);

} catch (PDOException $e) {
    echo json_encode([
        "status"  => "error",
        "message" => "Erreur : " . $e->getMessage()
    ]);
}
?>
