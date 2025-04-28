<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Get session goals based off session id(exercise session id).

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


$data = json_decode(file_get_contents("php://input"), true);
$session_id = intval($data['session_id'] ?? 0);
if ($session_id <= 0) {
    echo json_encode(["success"=>false,"message"=>"Invalid session_id"]);
    exit;
}

// Retrieve the session id that matches the selected session id
$sql = "SELECT id, metric, operator, target_value, duration_sec
        FROM session_goals
        WHERE session_id = $session_id";
$res = $conn->query($sql);
$goals = [];
if ($res) {
    while ($row = $res->fetch_assoc()) {
        $goals[] = [
          "id"           => (int)$row['id'],
          "metric"       => $row['metric'],
          "operator"     => $row['operator'],
          "target_value" => (float)$row['target_value'],
          "duration_sec" => (int)$row['duration_sec']
        ];
    }
    echo json_encode([
      "success" => true,
      "message" => "Fetched goals",
      "goals"   => $goals
    ]);
} else {
    echo json_encode([
      "success"=>false,
      "message"=>"DB error: ".$conn->error
    ]);
}
$conn->close();
