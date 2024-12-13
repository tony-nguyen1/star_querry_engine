# COMPTE-RENDU

13/12/24 14h
2 étudiants présent

## Combien de requêtes doivent être satifiable ?

Idéalement, entre 0% et 5% des requêtes peuvent être non satisfiable: une réponse sans Substitution.

## Est-ce que le nombres de réponses "#reponses" pour chaque requêtes doit être variés ?

Nous devons créer intervales pour #reponses. Puis pour chaque requêtes, en inclure N au maximum dans notre jeu de données pour chaque intervales. Les intervalles peuvent être de taille variés.

### Idée pour l'implémentation

Créez une HashMap partant du #reponse vers les requêtes qui match vers une liste de #requete Substitution.