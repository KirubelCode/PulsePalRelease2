<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Retrieve a user by uninue identifier ~ email.

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
$user_email = $data->user_email;

// Look up user id from users table using email.
$sql_user = "SELECT id FROM users WHERE email = '$user_email'";
$result = mysqli_query($conn, $sql_user);
if ($result && mysqli_num_rows($result) > 0) {
    $row = mysqli_fetch_assoc($result);
    $user_id = $row['id'];
} else {
    echo json_encode(["success" => false, "message" => "User not found"]);
    exit;
}

$sessionStart = date("Y-m-d H:i:s");
$sql = "INSERT INTO exercise_sessions (user_id, date, duration_minutes, total_steps, total_distance, avg_pace, avg_heart_rate, total_calories) VALUES ('$user_id', '$sessionStart', 0, 0, 0, 0, 0, 0)";
if (mysqli_query($conn, $sql)) {
    $session_id = mysqli_insert_id($conn);
    echo json_encode(["success" => true, "message" => "Session created successfully", "session_id" => $session_id]);
} else {
    echo json_encode(["success" => false, "message" => "Error creating session: " . mysqli_error($conn)]);
}
?>
