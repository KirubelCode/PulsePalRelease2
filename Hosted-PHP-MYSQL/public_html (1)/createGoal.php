
<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: To insert goals into the database.

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
$session_id   = intval($data->session_id);
$metric       = $conn->real_escape_string($data->metric);
$operator     = $conn->real_escape_string($data->operator);
$target_value = floatval($data->target_value);
$duration_sec = intval($data->duration_sec);  // <-- renamed

$sql = "INSERT INTO session_goals
    (session_id, metric, operator, target_value, duration_sec)
  VALUES
    ($session_id,'$metric','$operator',$target_value,$duration_sec)";
if ($conn->query($sql)===TRUE) {
    echo json_encode([
      "success"=>true,
      "message"=>"Goal created",
      "goal_id"=>$conn->insert_id
    ]);
} else {
    echo json_encode([
      "success"=>false,
      "message"=>"DB error: ".$conn->error
    ]);
}
$conn->close();
