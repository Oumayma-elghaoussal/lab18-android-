<?php
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER["REQUEST_METHOD"] == "POST") {

    include_once '../connexion/Connexion.php';

    // Récupérer les paramètres POST
    $latitude  = isset($_POST['latitude'])  ? $_POST['latitude']  : null;
    $longitude = isset($_POST['longitude']) ? $_POST['longitude'] : null;
    $date      = isset($_POST['date'])      ? $_POST['date']      : null;
    $deviceId  = isset($_POST['deviceId'])  ? $_POST['deviceId']  : null;

    // Validation
    if (!$latitude || !$longitude || !$date || !$deviceId) {
        echo json_encode([
            "status"  => "error",
            "message" => "Paramètres manquants (latitude, longitude, date, deviceId)"
        ]);
        exit;
    }

    try {
        $conn = new Connexion();
        $pdo = $conn->getConnexion();

        $query = "INSERT INTO position (latitude, longitude, date_heure, device_id)
                  VALUES (:lat, :lng, :date, :deviceId)";
        $stmt = $pdo->prepare($query);
        $stmt->execute([
            ':lat'      => $latitude,
            ':lng'      => $longitude,
            ':date'     => $date,
            ':deviceId' => $deviceId
        ]);

        echo json_encode([
            "status"  => "success",
            "message" => "Position enregistrée avec succès",
            "data"    => [
                "id"        => $pdo->lastInsertId(),
                "latitude"  => $latitude,
                "longitude" => $longitude,
                "date"      => $date,
                "deviceId"  => $deviceId
            ]
        ]);

    } catch (PDOException $e) {
        echo json_encode([
            "status"  => "error",
            "message" => "Erreur d'insertion : " . $e->getMessage()
        ]);
    }

} else {
    echo json_encode([
        "status"  => "error",
        "message" => "Méthode non autorisée. Utilisez POST."
    ]);
}
?>
