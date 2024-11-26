# Mini-Projet RDF Partie 2 : Évaluation et Analyse des Performances

## Objectif

L'objectif de cette deuxième partie du projet est d'évaluer et analyser les performances de votre prototype.  
Précisément, les performances de votre système doivent être comparées en utilisant le benchmark WatDiv avec :

1. La dernière version de Jena.
2. Une implémentation réalisée par vos collègues (à vous de choisir laquelle).  

Les réponses aux questions suivantes doivent être incluses dans le rapport final du projet, que vous pouvez organiser librement.

## Travail à rendre

1. Analyse des bancs d'essai et plan des tests à réaliser : **5 décembre**.
2. Réalisation des tests et document final : **15 décembre**.

---

## Benchmarks

1. Quels **types** de benchmark avez-vous utilisés pour vos tests pendant le développement ?  
2. Décrivez un test possible pour chaque **type** de benchmark (micro, standard, réel).
3. Vous allez utiliser le benchmark [WatDiv](http://dsg.uwaterloo.ca/watdiv/) pour vos tests.  
   Présentez brièvement le benchmark.

---

## Préparation des bancs d'essais

1. Utiliser la version de WatDiv mise à disposition sur Moodle pour générer des bases de données de tailles différentes, ainsi que des jeux de requêtes pour vos tests.
    - **Génération de données :** Compiler WatDiv (C++) selon les instructions disponibles sur [Moodle](https://moodle.umontpellier.fr/course/view.php?id=1126#section-5) ou le [site de WatDiv](https://dsg.uwaterloo.ca/watdiv/#installation). Vérifiez que la bibliothèque BOOST est installée.  
    - **Format des données :** WatDiv génère des fichiers au format N3. Utilisez [rdf2rdf](http://www.l3s.de/~minack/rdf2rdf/) pour les convertir en RDF/XML si nécessaire.
    - **Génération de requêtes :** Modifier le script `regenerate_queryset.sh` pour ajuster le nombre de requêtes par template (par défaut : 100).  
      Des patrons de requêtes sont disponibles dans les répertoires `testsuite` (requêtes en étoile) et `more query templates` (requêtes générales).

2. Créer un jeu de tests pour :
    - Les requêtes **en étoile**.
    - Les requêtes **générales**.  

    ⚠️ La validité des expériences dépend de ce passage clé. Vous devrez générer plusieurs requêtes avec WatDiv.

3. Une fois le jeu de requêtes de test créé, représenter avec un histogramme :
    - Le nombre de réponses aux requêtes sur une instance de **500K** et de **2M** triples.
4. Combien de requêtes ont zéro réponses ?  
5. Combien de requêtes générales sont en étoile ?

---

## Hardware et Software

1. Quel type de hardware et software avez-vous utilisé pour vos tests précédents ?  
2. Est-ce adapté à l’analyse des performances du système ? Justifiez.

---

## Métriques, Facteurs, et Niveaux

1. Donnez une liste de **métriques** permettant d’évaluer les moteurs de requêtes RDF.
2. Listez les **facteurs** qui interviennent dans l'évaluation du système, et définissez-en les niveaux.
3. Ordonnez les facteurs par importance et identifiez les facteurs principaux et secondaires.

---

## Évaluation des performances

1. Pour quelles métriques est-il préférable d’effectuer des mesures “cold” ou “warm” (ou les deux) ?  
2. Comment allez-vous réaliser ces mesures en pratique ?  
3. Proposez une procédure pour vérifier la correction et la complétude de votre système.  
   - Tester les systèmes sur des requêtes en étoile et générales.  
4. Réaliser une expérience **2²** en faisant varier la taille des données et de la mémoire. Que pouvez-vous conclure ?  
5. Planifiez une expérience **2²** (ou plusieurs) pour analyser l'impact des optimisations. Les optimisations sont-elles effectives ? Expliquez vos résultats.  

   Pour chaque test $2^2$* :
   - Choisissez deux facteurs et deux niveaux.  
   - Calculez l'importance des facteurs via un modèle de régression et interprétez les résultats.

6. Vérifiez si l'ordre d'évaluation des requêtes du workload joue un rôle dans l’analyse des performances.
7. Comparez votre système avec Jena et avec une implémentation concurrente sur des requêtes en étoile et générales.  
   - Quelles conclusions pouvez-vous en tirer ? Expliquez les résultats.  
8. Les mesures dans le système concurrent sont-elles réalisées de manière comparable (vérifiez le code source) ?  
9. Le système concurrent est-il correct et complet ?  
10. Toute expérience permettant de mieux comprendre votre système est bienvenue.

---

## Représentation graphique des résultats

1. Présentez les résultats de vos tests dans des histogrammes et analysez-les.  
2. Expliquez les différences de performances entre votre prototype et les systèmes concurrents.
