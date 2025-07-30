# Guide Complet de Création de Templates PDF

Ce répertoire contient les templates PDF AcroForm utilisés par l'application Badasign pour le remplissage automatique de formulaires et la signature électronique.

## Template Principal Requis

**Fichier :** `contract-template.pdf`

Ce template doit être un formulaire PDF AcroForm avec les champs nommés suivants :

### Champs Obligatoires

| Nom du Champ | Type | Propriétés | Description |
|--------------|------|------------|-------------|
| `firstName` | Texte | Requis, 50 caractères max | Prénom du signataire |
| `lastName` | Texte | Requis, 50 caractères max | Nom de famille du signataire |
| `email` | Texte | Requis, format email | Adresse email du signataire |
| `date` | Texte | Format DD/MM/YYYY | Date de signature |
| `signature` | Signature | Zone cliquable | Champ de signature électronique |

### Champs Optionnels Supportés

| Nom du Champ | Type | Description |
|--------------|------|-------------|
| `contractNumber` | Texte | Numéro de contrat généré automatiquement |
| `amount` | Texte | Montant du contrat avec devise |
| `company` | Texte | Nom de l'entreprise |
| `position` | Texte | Poste du signataire |
| `startDate` | Texte | Date de début de contrat |

## Création de Templates PDF

### Méthode 1 : Adobe Acrobat Pro (Recommandée)

1. **Créer le document de base**
   - Ouvrez votre document PDF existant ou créez-en un nouveau
   - Assurez-vous que le contenu textuel et la mise en page sont finalisés

2. **Activer l'outil de formulaire**
   - Allez dans `Outils` > `Préparer le formulaire`
   - Acrobat détectera automatiquement les zones de texte potentielles

3. **Configurer les champs**
   - **Champs texte** : Clic droit > Propriétés
     - Onglet `Général` : Définir le nom exact (ex: `firstName`)
     - Onglet `Apparence` : Police, taille, couleur
     - Onglet `Options` : Alignement, multiligne si nécessaire
     - Onglet `Format` : Type de données (texte, nombre, date)
   
   - **Champ signature** : 
     - Sélectionner l'outil `Signature numérique`
     - Dessiner la zone de signature
     - Nommer le champ `signature`

4. **Validation et test**
   - Utilisez `Aperçu` pour tester le formulaire
   - Vérifiez que tous les champs sont accessibles au clavier
   - Testez le remplissage avec des données d'exemple

### Méthode 2 : LibreOffice Writer

1. **Créer le document**
   - Rédigez votre contrat dans LibreOffice Writer
   - Laissez des espaces pour les champs de formulaire

2. **Insérer les champs de formulaire**
   - `Insertion` > `Champ de contrôle de formulaire`
   - Sélectionnez `Zone de texte` pour les champs texte
   - Configurez les propriétés dans le panneau de droite

3. **Configurer les propriétés**
   - **Nom** : Utilisez les noms exacts requis (`firstName`, `lastName`, etc.)
   - **Longueur max** : Définir selon les besoins
   - **Requis** : Cocher pour les champs obligatoires

4. **Exporter en PDF**
   - `Fichier` > `Exporter au format PDF`
   - Cocher `Créer un formulaire PDF`
   - Sélectionner `Envoyer les champs de formulaire au format FDF`

### Méthode 3 : PDFtk (Ligne de commande)

```bash
# Créer un template à partir d'un PDF existant
pdftk input.pdf generate_fdf output template.fdf

# Modifier le fichier FDF pour ajouter les champs
# Puis recréer le PDF avec les champs
pdftk input.pdf fill_form template.fdf output template_with_fields.pdf

# Lister tous les champs d'un PDF
pdftk contract-template.pdf dump_data_fields
```

### Méthode 4 : Outils en ligne

**PDF24** (Gratuit)
1. Uploadez votre PDF sur https://tools.pdf24.org/fr/
2. Utilisez l'outil "Créer un formulaire PDF"
3. Ajoutez les champs interactivement
4. Téléchargez le résultat

**JotForm PDF Editor**
1. Importez votre PDF
2. Glissez-déposez les champs de formulaire
3. Configurez les noms et propriétés
4. Exportez le formulaire

## Spécifications Techniques

### Format de Fichier
- **Type** : PDF/A-1b ou PDF 1.4+ recommandé
- **Taille** : Maximum 10 MB
- **Résolution** : 300 DPI pour l'impression
- **Couleurs** : RGB ou CMYK

### Contraintes des Champs

```javascript
// Exemple de validation JavaScript pour un champ email
if (event.value != "") {
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(event.value)) {
        app.alert("Format d'email invalide");
        event.rc = false;
    }
}
```

### Positionnement des Champs
- **Marges** : Minimum 10mm de tous les côtés
- **Espacement** : 5mm minimum entre les champs
- **Taille** : Hauteur minimum 8mm pour la lisibilité
- **Alignement** : Cohérent sur toute la page

## Validation et Tests

### Script de Test Automatique

```bash
#!/bin/bash
# test_template.sh - Script de validation du template

echo "Validation du template PDF..."

# Vérifier l'existence du fichier
if [ ! -f "contract-template.pdf" ]; then
    echo "❌ Fichier contract-template.pdf manquant"
    exit 1
fi

# Lister les champs et vérifier leur présence
FIELDS=$(pdftk contract-template.pdf dump_data_fields | grep "FieldName:" | cut -d' ' -f2)
REQUIRED_FIELDS=("firstName" "lastName" "email" "date" "signature")

for field in "${REQUIRED_FIELDS[@]}"; do
    if echo "$FIELDS" | grep -q "^$field$"; then
        echo "✅ Champ '$field' trouvé"
    else
        echo "❌ Champ '$field' manquant"
    fi
done

echo "Validation terminée."
```

### Test avec l'API

```bash
# Tester le remplissage du template
curl -X POST http://localhost:8080/api/pdf/fill \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jean",
    "lastName": "Dupont", 
    "email": "jean.dupont@test.com",
    "date": "30/07/2025",
    "contractNumber": "CTR-001",
    "amount": "1500.00 €",
    "company": "Test Corp",
    "position": "Développeur",
    "startDate": "01/08/2025"
  }' \
  --output test-filled.pdf

echo "PDF de test généré : test-filled.pdf"
```

## Dépannage

### Problèmes Courants

1. **Champs non détectés par PDFBox**
   ```
   Cause : Champs créés comme annotations au lieu de champs AcroForm
   Solution : Recréer les champs avec l'outil formulaire approprié
   ```

2. **Texte tronqué dans les champs**
   ```
   Cause : Taille de police trop grande ou champ trop petit
   Solution : Ajuster la taille du champ ou utiliser l'auto-dimensionnement
   ```

3. **Caractères spéciaux non affichés**
   ```
   Cause : Encodage de police incompatible
   Solution : Utiliser des polices standard (Arial, Times, Helvetica)
   ```

### Outils de Diagnostic

```bash
# Analyser la structure d'un PDF
pdftk contract-template.pdf dump_data

# Extraire les métadonnées
exiftool contract-template.pdf

# Vérifier la conformité PDF/A
verapdf contract-template.pdf
```

## Bonnes Pratiques

### Sécurité
- Ne pas inclure de JavaScript malveillant
- Limiter les permissions d'édition
- Utiliser des champs typés pour la validation

### Performance
- Optimiser la taille du fichier (compression)
- Éviter les images haute résolution inutiles
- Utiliser des polices système quand possible

### Accessibilité
- Ajouter des étiquettes aux champs (tooltips)
- Respecter l'ordre de tabulation logique
- Utiliser des contrastes suffisants

### Maintenance
- Versionner les templates
- Documenter les changements
- Tester après chaque modification

## Ressources Supplémentaires

- [Documentation PDFBox](https://pdfbox.apache.org/docs/)
- [Spécification PDF Adobe](https://www.adobe.com/devnet/pdf/pdf_reference.html)
- [Guide AcroForm](https://www.adobe.com/devnet/acrobat/pdfs/AcroFormsTutorial.pdf)
- [Validation PDF/A](https://www.pdfa.org/resource/pdf-a-validation/)

## Support

Pour des problèmes spécifiques aux templates :
1. Vérifiez les logs de l'application
2. Utilisez les outils de diagnostic fournis
3. Consultez la documentation de l'API
4. Créez une issue avec le template problématique