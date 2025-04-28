<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Retrieve session data points in order of timestamp.

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

// Read input JSON
$data = json_decode(file_get_contents("php://input"));
$session_id = isset($data->session_id) ? intval($data->session_id) : 0;
if ($session_id <= 0) {
    echo json_encode(["success" => false, "message" => "Invalid session ID"]);
    exit;
}

// Fetch data points ordered by timestamp
$sql = "SELECT timestamp, heart_rate, steps, distance, pace, calories FROM session_data_points WHERE session_id = '$session_id' ORDER BY timestamp";
$result = mysqli_query($conn, $sql);
if (!$result) {
    echo json_encode(["success" => false, "message" => "Query error: " . mysqli_error($conn)]);
    exit;
}

// Data points array
$data_points = [];
while ($row = mysqli_fetch_assoc($result)) {
    $ts = strtotime($row['timestamp']) * 1000; // milliseconds
    $data_points[] = [
        'timestamp'   => $ts,
        'heart_rate'  => floatval($row['heart_rate']),
        'steps'       => intval($row['steps']),
        'distance'    => floatval($row['distance']),
        'pace'        => floatval($row['pace']),
	'calories'    => floatval($row['calories'])
    ];
}

echo json_encode([
    "success"    => true,
    "message"    => "Data points fetched successfully",
    "data_points"=> $data_points
]);
?>
