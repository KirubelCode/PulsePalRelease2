<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "root";
$dbname = "pulsepalDB";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data["email"], $data["password"])) {
    echo json_encode(["success" => false, "message" => "Invalid input"]);
    exit;
}

$email = $conn->real_escape_string($data["email"]);
$password = $data["password"]; // Do NOT hash here, check against hashed password in DB

$sql = "SELECT password FROM users WHERE email='$email'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    if (password_verify($password, $row["password"])) {
        echo json_encode(["success" => true, "message" => "Login successful"]);
    } else {
        echo json_encode(["success" => false, "message" => "Invalid password"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Email not found"]);
}

$conn->close();
?>
