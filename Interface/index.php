<?php
$lignes = file('/etc/haproxy/haproxy.cfg', FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
$backends = [];
$backendActuel = 'default';

foreach ($lignes as $ligne) {
    $ligne = trim($ligne);

// Si la ligne commence par "backend", on change de contexte
    if (str_starts_with($ligne, 'backend')) {
        $parties = preg_split('/\s+/', $ligne);
        $backendActuel = $parties[1] ?? 'inconnu';
        continue;
    }

// Si la ligne commence par "server", on l'ajoute au backend en cours
    if (str_starts_with($ligne, 'server')) {
        $parties = preg_split('/\s+/', $ligne);
        if (isset($parties[1]) && isset($parties[2])) {
            list($ip, $port) = explode(':', $parties[2]);

            $backends[$backendActuel][] = [
                    'nom' => $parties[1],
                    'ip' => $ip,
                    'port' => $port
            ];
        }
    }
}
?>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Document</title>
</head>
<body>
<h2>Formulaire d'entrer de server</h2>
<form action="traitement.php" method="post">
    <select name="backend" id="">
        <option value="">Sélectionnez un backend</option>
        <?php foreach ($backends as $nomBackend => $serveurs): ?>
            <option value="<?= htmlspecialchars($nomBackend) ?>"><?= htmlspecialchars($nomBackend) ?></option>
        <?php endforeach; ?>
    </select>
    <label>
        <input type="checkbox" name="actif" value="1"> Créer un nouveau backend
    </label>
    <div id="backend-container"></div>

    <script>
        const checkbox = document.querySelector('input[name="actif"]');
        const container = document.getElementById('backend-container');

        checkbox.addEventListener('change', function() {
            if (this.checked) {
                // Création de l'input si la case est cochée
                const newInput = document.createElement('input');
                newInput.type = 'text';
                newInput.placeholder = 'Taper un nouveau backend';
                newInput.name = 'newbackend';
                newInput.id = 'dynamic-backend-input';

                // On l'ajoute dans le conteneur (et non dans document)
                container.appendChild(newInput);
            } else {
                // Optionnel : supprimer l'input si on décoche la case
                const existingInput = document.getElementById('dynamic-backend-input');
                if (existingInput) {
                    existingInput.remove();
                }
            }
        });
    </script>
    <p>Enter le nom du service: <input name="service" id="service" required></p>
    <p>Entrer l'ip et le port du server: <input type="text" name="ip" id="ip">:<input type="text" name="port" id="port" required></p>
    <input type="submit" value="Envoyer" required>
</form>
<h2>Liste des servers</h2>
<?php
foreach ($backends as $nomBackend => $serveurs) {
    echo "<h2>Backend : " . htmlspecialchars($nomBackend) . "</h2>";
    echo "<ul>";
    foreach ($serveurs as $srv) {
        echo "<li>{$srv['nom']} ({$srv['ip']}:{$srv['port']})</li>";
    }
    echo "</ul>";
}
?>
</body>
</html>
