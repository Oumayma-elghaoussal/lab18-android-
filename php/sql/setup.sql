-- =============================================
-- TP 11 : Base de données pour la localisation
-- =============================================

CREATE DATABASE IF NOT EXISTS localisation_db;
USE localisation_db;

CREATE TABLE IF NOT EXISTS position (
    id INT AUTO_INCREMENT PRIMARY KEY,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    date_heure DATETIME NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Donnée de test
INSERT INTO position (latitude, longitude, date_heure, device_id)
VALUES (33.9716, -6.8498, '2025-05-21 14:30:00', 'test_device_001');
