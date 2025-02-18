<?php
$servername = "localhost";  // MySQL runs on localhost
$username = "root";         // Default MySQL user
$password = "root";             // Leave empty if no password set during installation
$dbname = "pulsepalDB";     // Your database name



$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$data = json_decode(file_get_contents("php://input"));

if (isset($data->fullName) && isset($data->email) && isset($data->password)) {
    $fullName = $data->fullName;
    $email = $data->email;
    $password = password_hash($data->password, PASSWORD_DEFAULT); // Hashing for security
    $age = $data->age;
    $gender = $data->gender;
    $height = $data->height;
    $weight = $data->weight;

    $sql = "INSERT INTO users (fullName, email, password, age, gender, height, weight)
            VALUES ('$fullName', '$email', '$password', '$age', '$gender', '$height', '$weight')";

    if ($conn->query($sql) === TRUE) {
        echo json_encode(["success" => true, "message" => "Account created successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Invalid input"]);
}

$conn->close();
?>
