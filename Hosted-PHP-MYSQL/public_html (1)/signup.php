<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Signup a user if the email isnt already used in our users table.

header('Content-Type: application/json');

$conn = new mysqli("localhost","u466992296_root","Amnda010","u466992296_pulsepalDB");
if ($conn->connect_error) {
    echo json_encode(["success"=>false,"message"=>"DB connection failed"]);
    exit;
}
$data = json_decode(file_get_contents("php://input"), true);

// sanitize & extract
$email    = $conn->real_escape_string($data["email"]    ?? "");
$fullName = $conn->real_escape_string($data["fullName"] ?? "");
$age      = intval($data["age"]    ?? 0);
$gender   = $conn->real_escape_string($data["gender"]   ?? "");
$height   = floatval($data["height"] ?? 0);
$weight   = floatval($data["weight"] ?? 0);
$password = $data["password"] ?? "";

// basic validation
if (!$email || !$fullName || !$password || $age<=0 || $height<=0 || $weight<=0) {
    echo json_encode(["success"=>false,"message"=>"Invalid input"]);
    exit;
}

$hash = password_hash($password, PASSWORD_DEFAULT);

// insert new user
$sql = "INSERT INTO users 
    (fullName,email,password,age,gender,height,weight)
    VALUES
    ('$fullName','$email','$hash',$age,'$gender',$height,$weight)";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["success"=>true,"message"=>"Account created successfully"]);
} else {
    echo json_encode(["success"=>false,"message"=>"DB error: ".$conn->error]);
}
$conn->close();
?>
