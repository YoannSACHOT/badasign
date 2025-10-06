# Badasign - Application de Signature Électronique PDF

Badasign est une application Spring Boot qui permet de remplir des templates PDF et de gérer des signatures électroniques via l'API Yousign.

## Fonctionnalités

- **Remplissage de templates PDF** : Remplissage automatique de formulaires PDF AcroForm avec des données personnalisées
- **Intégration Yousign** : Upload de documents et création de procédures de signature électronique
- **API REST** : Endpoints pour gérer les PDFs et les signatures
- **Documentation API** : Interface Swagger UI pour tester les endpoints

## Technologies Utilisées

- **Java 21** - Langage de programmation
- **Spring Boot 3.5.5** - Framework principal
- **Apache PDFBox 2.0.35** - Manipulation des fichiers PDF
- **Yousign API** - Service de signature électronique
- **SpringDoc OpenAPI** - Documentation API automatique
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestionnaire de dépendances

## Prérequis

- Java 21 ou supérieur
- Maven 3.6 ou supérieur
- Compte Yousign avec clé API

## Configuration

### 1. Configuration Yousign

Créez un fichier `application.yml` dans `src/main/resources/` avec votre configuration Yousign :

```yaml
yousign:
  api:
    base-url: https://api.yousign.app/v3  # URL par défaut (production). Utilisez https://api-sandbox.yousign.app/v3 pour le sandbox.
    api-key: YOUR_YOUSIGN_API_KEY
```

### 2. Templates PDF

Placez vos templates PDF dans le répertoire `src/main/resources/templates/`. Le template principal doit être nommé `contract-template.pdf`.

## Installation et Démarrage

1. **Cloner le projet**
```bash
git clone <repository-url>
cd badasign
```

2. **Configurer les variables d'environnement**
```bash
export YOUSIGN_API_KEY=your_api_key_here
```

3. **Compiler et démarrer l'application**
```bash
mvn clean install
mvn spring-boot:run
```

4. **Accéder à l'application**
- Application : http://localhost:58082
- Documentation API : http://localhost:58082/swagger-ui/index.html

## Exécution avec Docker

1. Construire le JAR
```bash
mvn clean package -DskipTests
```

2. Construire l'image Docker
```bash
docker build -t badasign:latest .
```

3. Démarrer le conteneur
```bash
docker run --rm \
  -e YOUSIGN_API_KEY=your_api_key_here \
  -p 58082:58082 \
  --name badasign \
  badasign:latest
```

4. Accéder à l'application
- Application : http://localhost:58082
- Documentation API : http://localhost:58082/swagger-ui/index.html

Note: Le Dockerfile inclut un healthcheck sur l'URL `/actuator/health`. Pour qu'il fonctionne, ajoutez la dépendance `spring-boot-starter-actuator` au projet et exposez l'endpoint de santé, ou bien modifiez/supprimez le healthcheck du Dockerfile selon votre besoin.

## Utilisation

### API Endpoints

#### PDF Management
- `POST /api/pdf/fill` - Remplit un template PDF avec des données
- `GET /api/pdf/sample-data` - Récupère des données d'exemple

#### Signature Management
- `POST /api/signature/upload` - Upload d'un document vers Yousign
  - Paramètres (multipart/form-data): `file` (PDF), `fileName` (nom du fichier), `email` (email du signataire), `name` (nom du signataire)

### Exemple d'utilisation

```bash
# Remplir un template PDF
curl -X POST http://localhost:58082/api/pdf/fill \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jean",
    "lastName": "Dupont",
    "email": "jean.dupont@example.com",
    "date": "30/07/2025"
  }'

# Upload d'un document pour signature (Yousign)
curl -X POST http://localhost:58082/api/signature/upload \
  -H "Content-Type: multipart/form-data" \
  -F "file=@document.pdf" \
  -F "fileName=document.pdf" \
  -F "email=signer@example.com" \
  -F "name=Jean Dupont"
```

## Création de Templates PDF

### Outils Recommandés

1. **Adobe Acrobat Pro** (Recommandé)
   - Créez votre document PDF
   - Utilisez l'outil "Préparer le formulaire"
   - Ajoutez des champs de formulaire avec des noms spécifiques

2. **LibreOffice Writer**
   - Créez votre document
   - Insérez des champs de formulaire via Insertion > Champ de contrôle de formulaire
   - Exportez en PDF en cochant "Créer un formulaire PDF"

3. **PDFtk** (Ligne de commande)
   ```bash
   pdftk input.pdf generate_fdf output data.fdf
   pdftk input.pdf fill_form data.fdf output filled.pdf
   ```

### Structure des Champs Requis

Votre template PDF doit contenir les champs suivants :

| Nom du Champ | Type | Description |
|--------------|------|-------------|
| `firstName` | Texte | Prénom du signataire |
| `lastName` | Texte | Nom du signataire |
| `email` | Texte | Email du signataire |
| `date` | Texte | Date de signature |
| `contractNumber` | Texte | Numéro de contrat |
| `amount` | Texte | Montant du contrat |
| `company` | Texte | Nom de l'entreprise |
| `position` | Texte | Poste du signataire |
| `startDate` | Texte | Date de début |
| `signature` | Signature | Zone de signature électronique |

### Bonnes Pratiques

- **Nommage des champs** : Utilisez des noms descriptifs et cohérents
- **Types de champs** : Respectez les types appropriés (texte, signature, date)
- **Taille des champs** : Prévoyez suffisamment d'espace pour le contenu
- **Validation** : Testez vos templates avec des données réelles
- **Accessibilité** : Assurez-vous que les champs sont accessibles

### Validation des Templates

Pour vérifier que votre template est correctement configuré :

1. Utilisez l'endpoint `/api/pdf/sample-data` pour obtenir des données de test
2. Appelez `/api/pdf/fill` avec ces données
3. Vérifiez que tous les champs sont correctement remplis

## Architecture

```
src/
├── main/
│   ├── java/fr/jixter/badasign/
│   │   ├── config/          # Configuration (Yousign, PDF)
│   │   ├── controller/      # Contrôleurs REST
│   │   ├── service/         # Services métier
│   │   ├── util/           # Utilitaires
│   │   └── BadasignApplication.java
│   └── resources/
│       ├── templates/       # Templates PDF
│       └── application.yml  # Configuration
└── test/                   # Tests unitaires
```

## Développement

### Lombok

Le projet utilise Lombok pour réduire le code boilerplate :
- `@Slf4j` pour les logs
- `@RequiredArgsConstructor` pour l'injection de dépendances
- `@Data`, `@Builder` pour les classes de données

### Tests

```bash
mvn test
```

### Packaging

```bash
mvn clean package
java -jar target/badasign-0.0.1-SNAPSHOT.jar
```

## Dépannage

### Problèmes Courants

1. **Template PDF non trouvé**
   - Vérifiez que le fichier `contract-template.pdf` est dans `src/main/resources/templates/`

2. **Champs non remplis**
   - Vérifiez les noms des champs dans votre template PDF
   - Utilisez un outil comme PDFtk pour lister les champs : `pdftk template.pdf dump_data_fields`

3. **Erreur API Yousign**
   - Vérifiez votre clé API
   - Consultez les logs pour plus de détails

### Logs

Les logs sont configurés avec SLF4J. Pour activer les logs de debug :

```yaml
logging:
  level:
    fr.jixter.badasign: DEBUG
```

## Contribution

1. Fork le projet
2. Créez une branche feature (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Committez vos changements (`git commit -am 'Ajout nouvelle fonctionnalité'`)
4. Push vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Créez une Pull Request

## Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## Support

Pour toute question ou problème, créez une issue sur le repository GitHub.