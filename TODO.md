# TODO LIST — HAProxy Manager (Java sans base de données)

## Stack retenue
- Java 17 + Spring Boot pour l’interface et l’API.
- Fichier JSON pour conserver les backends; aucune base SQL.
- Docker Compose pour tester HAProxy et deux serveurs web localement.
- [x] Remplacer le choix PHP/Java par Java.

## Phase 1 — Infrastructure réseau
- [ ] Créer 3 machines virtuelles Ubuntu.
- [ ] Configurer les adresses IP statiques.
- [ ] Vérifier la communication entre les machines.
- [ ] Installer SSH.
- [ ] Tester les connexions entre les serveurs.

## Phase 2 — Installation HAProxy
- [ ] Installer HAProxy.
- [ ] Activer le service.
- [ ] Vérifier son fonctionnement.
- [ ] Comprendre `/etc/haproxy/haproxy.cfg`.
- [ ] Configurer le frontend.
- [ ] Configurer le backend.
- [ ] Déclarer les deux serveurs.
- [ ] Configurer Round Robin.
- [ ] Configurer les Health Checks.
- [ ] Tester le Load Balancing.

## Phase 3 — Installation des serveurs Web
Sur chaque serveur :
- [ ] Installer Apache2.
- [ ] Configurer le port 8080.
- [ ] Créer une page de test PHP ou HTML.
- [ ] Afficher le nom du serveur.
- [ ] Afficher son adresse IP.
- [ ] Afficher son port.
- [ ] Tester individuellement chaque serveur.

## Phase 4 — Développement de l'interface Web

### Dashboard
- [x] Créer une interface d'administration.
- [x] Afficher le résumé du service et les compteurs des serveurs.
- [x] Afficher le nombre de serveurs.
- [x] Afficher les serveurs actifs.
- [x] Afficher les serveurs indisponibles.
- [ ] Afficher les statistiques HAProxy détaillées (connexions et requêtes).

### Gestion des serveurs
- [x] Ajouter un serveur.
- [x] Modifier un serveur.
- [x] Supprimer un serveur.
- [x] Activer un serveur.
- [x] Désactiver un serveur.
- [x] Configurer son adresse IP.
- [x] Configurer son port.
- [x] Vérifier sa disponibilité par TCP.

### Configuration HAProxy
- [x] Générer automatiquement `haproxy/generated.cfg`.
- [x] Générer le frontend et le backend.
- [x] Choisir l’algorithme (`roundrobin`, `leastconn`, `source`).
- [x] Prendre en charge la validation et le rechargement par commandes configurées.
- [ ] Raccorder la génération au HAProxy de la démo Compose.

## Phase 5 — Gestion des configurations sans SQL
Les informations des serveurs sont conservées dans des fichiers JSON.

Exemple, `config/servers.json` :

```json
{
  "servers": [
    {
      "id": 1,
      "name": "Serveur 1",
      "ip": "192.168.1.11",
      "port": 8080,
      "enabled": true
    },
    {
      "id": 2,
      "name": "Serveur 2",
      "ip": "192.168.1.12",
      "port": 8080,
      "enabled": true
    }
  ]
}
```

Avantages :
- Pas besoin de MySQL ou MariaDB.
- Configuration facile à lire.
- Modification possible depuis PHP.
- Conservation des serveurs après redémarrage.

## Phase 6 — Démonstration du Load Balancing
- [ ] Accéder à l'adresse IP de HAProxy.
- [ ] Afficher le serveur qui répond.
- [ ] Actualiser la page.
- [ ] Afficher le deuxième serveur.
- [ ] Vérifier l'alternance.
- [ ] Tester avec un serveur arrêté.
- [ ] Vérifier que HAProxy utilise le serveur restant.

## Phase 7 — Monitoring
- [ ] Afficher les statuts ONLINE/OFFLINE.
- [ ] Afficher les ports.
- [ ] Afficher les adresses IP.
- [ ] Afficher le nombre de requêtes.
- [ ] Afficher les connexions actives.
- [x] Actualiser automatiquement les statuts toutes les 10 secondes.
- [ ] Afficher l'historique dans un fichier JSON ou un fichier journal.

## Phase 8 — Sécurité
- [ ] Créer une authentification administrateur.
- [ ] Sécuriser les sessions PHP.
- [ ] Protéger les fichiers JSON.
- [ ] Sécuriser les commandes système.
- [ ] Limiter les privilèges d'administration.
- [ ] Protéger les modifications de configuration.

## Architecture finale

```
             ADMINISTRATEUR
                    |
              INTERFACE WEB
               Java + HTML
                    |
             Fichiers JSON
                    |
                 HAProxy
              192.168.1.10
                    |
              ROUND ROBIN
                /     \
               /       \
        SERVEUR 1     SERVEUR 2
        Apache2       Apache2
        Port 8080     Port 8080
        .11           .12
```

## Structure du projet

```
haproxy-manager/
├── index.php
├── dashboard.php
├── config/
│   └── servers.json
├── servers/
│   ├── index.php
│   ├── add.php
│   ├── edit.php
│   └── delete.php
├── haproxy/
│   ├── generator.php
│   ├── validator.php
│   └── manager.php
├── monitoring/
│   ├── status.php
│   ├── statistics.php
│   └── healthcheck.php
└── assets/
    ├── css/
    ├── js/
    └── images/
```

## Décision technique
Java 17 + Spring Boot + JSON + HAProxy + Apache2, sans base de données.
L’authentification, les statistiques HAProxy détaillées et le raccordement de la génération à HAProxy restent à faire avant un déploiement réseau.
