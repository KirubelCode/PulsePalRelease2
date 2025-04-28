<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Insert session data points based off the latest updated values.

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
$timestamp = date("Y-m-d H:i:s", $data->timestamp / 1000);
$heart_rate = $data->heart_rate;
$steps = $data->steps;
$distance = $data->distance;
$pace = $data->pace;
$calories = $data->calories;


$sql = "INSERT INTO session_data_points
   (session_id,timestamp,heart_rate,steps,distance,pace,calories)
 VALUES
   ('$session_id','$timestamp','$heart_rate','$steps','$distance','$pace','$calories')";
if (mysqli_query($conn, $sql)) {
    echo json_encode(["success" => true, "message" => "Data point inserted successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error inserting data point: " . mysqli_error($conn)]);
}
?>
