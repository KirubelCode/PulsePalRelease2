<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Send final session data to exercise session table by updating.

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
$session_id = $data->session_id;
$duration = $data->duration_minutes;
$total_steps = $data->total_steps;
$total_distance = $data->total_distance;
$avg_pace = $data->avg_pace;
$avg_heart_rate = $data->avg_heart_rate;
$total_calories = $data->total_calories;

// Update the exercise session row
$sql = "UPDATE exercise_sessions SET duration_minutes = '$duration', total_steps = '$total_steps', total_distance = '$total_distance', avg_pace = '$avg_pace', avg_heart_rate = '$avg_heart_rate', total_calories = '$total_calories' WHERE id = '$session_id'";
if (mysqli_query($conn, $sql)) {
    echo json_encode(["success" => true, "message" => "Session finalized successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error finalizing session: " . mysqli_error($conn)]);
}
?>
