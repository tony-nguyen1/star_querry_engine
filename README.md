
# HAI914I - Un moteur d’évaluation de requêtes en étoile

## Auteurs
- <federico.ulliana@inria.fr>
- <guillaume.perution-kihli@inria.fr>

---

## Objectif
L’objectif du TD est d’implémenter l’approche hexastore pour l’interrogation des données RDF vue en cours, ainsi que les procédures nécessaires à l’évaluation de requêtes en étoile (exprimées en syntaxe SPARQL) qui seront introduites par la suite.

Le TD suivant sera dédié à l’évaluation des performances du système réalisé. Votre prototype sera comparé avec l’évaluateur de requêtes du logiciel libre InteGraal. De plus, InteGraal peut être considéré comme un système “oracle” pour vérifier la correction et la complétude de votre moteur, c’est-à-dire, vérifier que votre moteur donne toutes et seules les bonnes réponses à une requête. Celle-ci est évidemment une condition nécessaire à la conduite d’expériences.

---

## Consignes
- Le TD se fait en groupes de 1 ou 2 personnes.
- Lorsque vous avez décidé quel sera votre groupe, inscrivez-vous dans le [tableau suivant](https://docs.google.com/spreadsheets/d/1bQY-Xba11DNcbCC5gujvPnidhajN-Kcb306PjXveF7M/edit?usp=sharing)
- Le langage de programmation est imposé : **Java**.

---

## Projet : Dates de rendu
1. **Dictionnaire, index (Hexastore)** : rendu du code (pas de rapport) - **15 Novembre**.  
2. **Évaluation des requêtes en étoile** : code + rapport (3 pages) - **29 Novembre**.  
3. **Analyse des performances** : code + rapport (5 pages) - **13 Décembre**.

**Note :** Il n’y aura pas de soutenance, mais il sera demandé aux groupes d’expliquer le travail réalisé lors des séances de TD. La présence en TD est obligatoire.

---

## Évaluation
Les points suivants seront évalués :
- Travail réalisé (code fonctionnel, implémentation des fonctionnalités).
- Qualité du code produit (taux de couverture du code par des tests unitaires).
- Clarté et concision du rapport.

---

## Point de départ du projet logiciel
Un squelette du projet est disponible dans ce dépôt git.

Vous pouvez réutiliser ce projet (ce qui est conseillé), ou juste l’étudier comme exemple. Pour s’assurer que tout fonctionne bien, exécutez le programme dans la classe **Example.java**.

### Description des principales classes fournies
- **Example.java** : montre comment lire les données et les requêtes depuis des fichiers via des parseurs dédiés.
- **RDFAtom.java** : représente des triplets RDF.
- **StarQuery.java** : représente des requêtes en étoile.
- **RDFStorage.java** : une interface décrivant le contrat (ensemble des méthodes supportées) d’un objet Hexastore.

### IDE recommandé
1. Installez **Eclipse IDE for Java Developers** :  
   [Lien Moodle](https://moodle.umontpellier.fr/mod/page/view.php?id=131074)
2. Vous pouvez aussi utiliser **IntelliJ IDEA** (version communautaire ou Ultimate avec une licence étudiante) :  
   [Télécharger IntelliJ](https://www.jetbrains.com/idea/download/?section=linux)

---

## Version de Java
Le projet utilise **Java 21**. Vous êtes libres d’utiliser une version plus récente si vous êtes à l’aise avec l’environnement Java, mais ne perdez pas de temps sur des considérations techniques.

---

## Dépendances Java avec Maven
Le projet utilise **Maven** pour la gestion des dépendances. Importez le projet dans votre IDE avec "Import Maven Project". Le fichier `pom.xml` contient toutes les bibliothèques nécessaires.

Si Eclipse ne charge pas correctement les dépendances Maven :
1. Dans **Package Explorer**, clic droit sur le projet.
2. Allez dans **Configure > Convert to Maven Project**.

### Bibliothèques externes utilisées
- **rdf4j 3.7.3** : pour lire les données RDF et les requêtes SPARQL.  
  [Documentation RDF4J](https://rdf4j.org/documentation)  
  [Javadoc RDF4J](https://rdf4j.org/javadoc/latest/)  
- **InteGraal 1.6.0** : utilisé comme base pour les implémentations et pour les comparaisons.  
  [Site InteGraal](https://rules.gitlabpages.inria.fr/integraal-website/)

---

## Tests unitaires
Des tests unitaires sont fournis dans `src/test/java`. Ils permettent de mieux comprendre le comportement attendu des objets.

### Jeu de données et requêtes
Dans le répertoire `data/`, des fichiers sont fournis pour les tests :  
- **sample_data.nt** : données de micro-tests.  
- **sample_query.queryset** : requêtes pour micro-tests.  
- **100K.nt** et **STAR_ALL_workload.queryset** : plus de données et de requêtes.

---

## Requêtes en étoile
Les requêtes en étoile sont des requêtes SPARQL composées de `n` patrons de triplets partageant une même variable.  
Exemple :  
```sparql
SELECT ?x WHERE { ?x p1 o1 . ?x p2 o2 . ?x p3 o3 }
```
Représentation graphique :
```
  -- p2 --- o2 
 ?x --- p1 --- o1 
  -- p3 --- o3 
```

### Exemples de requêtes
1. **Quels écrivains ayant reçu un prix Nobel sont aussi des peintres ?**
   ```sparql
   SELECT ?x WHERE { ?x type peintre . ?x won_prize Nobel . ?x type écrivain }
   ```
2. **Quels artistes nés en 1904 ont vécu à Paris ?**  
   Paramètres :  
   - `p1 = type, o1 = artiste`  
   - `p2 = lives_in, o2 = Paris`  
   - `p3 = birth_year, o3 = 1904`  
3. **Qui sont les amis d’Alice qui aiment le cinéma ?**
   ```sparql
   SELECT ?x WHERE { ?x friendOf Alice . ?x likes Movies }
   ```

---

## Évaluation des requêtes
L’évaluation des requêtes en étoile comprend 4 étapes :

1. **Chargement des triplets** (déjà implémenté) et encodage dans un dictionnaire.
2. **Création de l’hexastore** avec ses indexes.
3. **Lecture des requêtes** (parser fourni).
4. **Accès aux données et visualisation des résultats**.

---
