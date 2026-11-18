# HAProxy Manager — Java sans base de données

Application d’administration écrite en Java 17 / Spring Boot. Les serveurs sont conservés dans `data/servers.json`; aucune base SQL n’est utilisée. HAProxy et deux serveurs web Apache/PHP peuvent être lancés localement avec Docker Compose pour la démonstration.

## Démarrer avec Docker

Prérequis : Docker avec Compose.

```bash
docker compose up --build
```

- Interface d’administration : http://127.0.0.1:8090
- HAProxy / démonstration round robin : http://127.0.0.1:8080
- Serveur 1 : http://127.0.0.1:8081
- Serveur 2 : http://127.0.0.1:8082

Arrêter avec `Ctrl+C`, ou lancer `docker compose down`. Les modifications sont persistées dans `data/servers.json`.

## Démarrer sans Docker

Prérequis : JDK 17 et Maven 3.9+.

```bash
mvn spring-boot:run
```

L’interface est alors disponible sur http://localhost:8080. Le fichier JSON est créé dans `data/servers.json` au premier lancement. Les adresses initiales `127.0.0.1:8081` et `127.0.0.1:8082` correspondent à la configuration de test native.

## Fonctions

- Tableau de bord avec le nombre de backends disponibles et indisponibles.
- Ajout, modification, suppression et activation/désactivation des serveurs.
- Vérification TCP du port configuré, actualisée toutes les dix secondes.
- Génération de `haproxy/generated.cfg` à partir des backends activés.
- Choix de l’algorithme par `HAPROXY_ALGORITHM` : `roundrobin`, `leastconn` ou `source`.
- JSON remplacé de façon atomique pour limiter les fichiers partiellement écrits.

La génération ne recharge pas automatiquement HAProxy. Pour activer le bouton **Appliquer**, configurer des commandes d’administration dédiées dans l’environnement :

```bash
HAPROXY_VALIDATE_COMMAND="haproxy -c -f {config}"
HAPROXY_RELOAD_COMMAND="systemctl reload haproxy"
```

`{config}` est remplacé par le chemin du fichier. Le processus Java doit alors disposer des droits système minimaux nécessaires, et les commandes doivent être configurées par l’administrateur du serveur, jamais saisies depuis l’interface. Dans la démo Docker, le HAProxy de façade lit `haproxy/haproxy.cfg`; la configuration générée est un artefact distinct et son rechargement doit être raccordé à l’installation HAProxy de déploiement.

## API

- `GET /api/dashboard` — résumé et états des serveurs.
- `GET /api/servers` — liste avec statut de santé.
- `POST /api/servers` — ajouter (`name`, `ip`, `port`, `enabled`).
- `PUT /api/servers/{id}` — modifier.
- `PATCH /api/servers/{id}/enabled` — activer/désactiver.
- `DELETE /api/servers/{id}` — supprimer.
- `POST /api/haproxy/generate` — générer; corps `{ "apply": true }` pour demander validation et rechargement configurés.

## Sécurité et déploiement

Cette version est un prototype pédagogique : l’API n’a pas encore d’authentification. Compose publie l’interface et les services de démonstration uniquement sur `127.0.0.1`. Ne l’exposez pas sur un réseau partagé avant d’ajouter une authentification, une protection CSRF et une politique d’accès adaptée. En production, restreignez l’écriture des JSON, les commandes de service et le fichier HAProxy à un compte de service à privilèges limités.

L’application cible Java 17. L’environnement de développement qui a servi à créer ces fichiers avait Java 8 et aucun Maven; la compilation n’a donc pas pu être vérifiée ici.
