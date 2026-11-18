<?php
ini_set('display_errors', 1);
$backend = $_POST['backend'];

$service = $_POST['service'];
$ip = $_POST['ip'];
$port = $_POST['port'];
$newbackend = $_POST['newbackend'] ?? '';
$FILE_PATH = '/etc/haproxy/haproxy.cfg';



if ($newbackend) {
    file_put_contents($FILE_PATH, "\n\nbackend $newbackend\n    balance roundrobin\n    server $service $ip:$port check\n", FILE_APPEND);
}
else{

    $contenu = file_get_contents($FILE_PATH);
    $pattern = "/(backend\s+$backend\s+balance\s+roundrobin\s+)(.*?)(?=backend|$)/s";
    $remplacement = "$1    server $service $ip:$port check\n$2";
    $nouveauContenu = preg_replace($pattern, $remplacement, $contenu);

    file_put_contents($FILE_PATH, $nouveauContenu);
}
    header('location: index.php');
