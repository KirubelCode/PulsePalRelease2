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

if (!$data) {
    echo json_encode(["success" => false, "message" => "No JSON received"]);
    exit;
}

$fullName = $conn->real_escape_string($data["fullName"] ?? "");
$email = $conn->real_escape_string($data["email"] ?? "");
$password = password_hash($data["password"] ?? "", PASSWORD_DEFAULT);
$age = isset($data["age"]) ? (int)$data["age"] : 0;
$gender = $conn->real_escape_string($data["gender"] ?? "");
$height = isset($data["height"]) ? (float)$data["height"] : 0;
$weight = isset($data["weight"]) ? (float)$data["weight"] : 0;

if (empty($fullName) || empty($email) || empty($password) || $age <= 0 || $height <= 0 || $weight <= 0) {
    echo json_encode(["success" => false, "message" => "Invalid input values"]);
    exit;
}

$sql = "INSERT INTO users (fullName, email, password, age, gender, height, weight)
        VALUES ('$fullName', '$email', '$password', '$age', '$gender', '$height', '$weight')";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true, "message" => "Account created successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Database error: " . $conn->error]);
}

$conn->close();
?>
