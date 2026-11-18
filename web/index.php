<?php
// Page de test : affiche l'identité du serveur qui répond.
// Le cache est désactivé pour que chaque actualisation montre le bon serveur.
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');
header('Expires: 0');

$name   = getenv('SERVER_NAME') ?: 'Serveur inconnu';
$ip     = $_SERVER['SERVER_ADDR'] ?? 'inconnue';
$port   = $_SERVER['SERVER_PORT'] ?? 'inconnu';
$hostNm = gethostname();
?>
<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Load Balancing Test</title>
    <style>
        body { font-family: monospace; background: #f4f6f8; color: #222; padding: 2rem; }
        pre  { background: #fff; border: 1px solid #ccc; padding: 1.5rem; border-radius: 6px; }
        .ok  { color: #0a7d32; font-weight: bold; }
    </style>
</head>
<body>
<pre>
================================
       LOAD BALANCING TEST
================================

Serveur actuellement utilisé :

<?= htmlspecialchars(strtoupper($name)) ?>

IP       : <?= htmlspecialchars($ip) ?>

PORT     : <?= htmlspecialchars((string)$port) ?>

Hôte     : <?= htmlspecialchars($hostNm) ?>

Statut   : <span class="ok">ONLINE</span>

================================
</pre>
</body>
</html>
