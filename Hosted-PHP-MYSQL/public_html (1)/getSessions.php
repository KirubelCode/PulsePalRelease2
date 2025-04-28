<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: To retrieve all user exercise sessions based off user ID in exercise sessions.

header('Content-Type: application/json');

$servername = "localhost";
$username   = "u466992296_root";
$password   = "Amnda010";
$dbname     = "u466992296_pulsepalDB";


$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"));
$email = $data->email;

if (!$email) {
    echo json_encode(["success" => false, "message" => "Missing email"]);
    exit;
}

// Get the user's ID
$result = $conn->query("SELECT id FROM users WHERE email = '$email'");
if ($result->num_rows == 0) {
    echo json_encode(["success" => false, "message" => "User not found"]);
    exit;
}
$user_id = $result->fetch_assoc()['id'];

// Get the user's session list
$sessions = [];
$res = $conn->query("SELECT id, date, duration_minutes FROM exercise_sessions WHERE user_id = '$user_id' ORDER BY date DESC");
while ($row = $res->fetch_assoc()) {
    $sessions[] = $row;
}

echo json_encode(["success" => true, "sessions" => $sessions]);
?>
