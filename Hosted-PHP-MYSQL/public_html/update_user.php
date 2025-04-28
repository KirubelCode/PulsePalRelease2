
<?php
// Prepared By: Kirubel Temesgen
// Student ID: C00260396
// Institution: SETU Carlow
// Supervisor: Joeseph Kehoe
// Purpose: Allow user to update their account and personal information.

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
$name=$conn->real_escape_string($data['fullName']??'');
$email=$conn->real_escape_string($data['email']??'');
$pass=password_hash($data['password']??'',PASSWORD_DEFAULT);
$age=(int)$data['age'];
$gender=$conn->real_escape_string($data['gender']??'');
$h=(float)$data['height'];
$w=(float)$data['weight'];
if(!$name||!$email||$age<1||$h<=0||$w<=0){
  echo json_encode(['success'=>false,'message'=>'Invalid']); exit;
}
$sql="UPDATE users SET fullName='$name', password='$pass', age=$age,
      gender='$gender', height=$h, weight=$w WHERE email='$email'";
$res=$conn->query($sql);
echo json_encode(['success'=>$res,'message'=>$res?'Updated':'Failed']);
$conn->close();
?>
