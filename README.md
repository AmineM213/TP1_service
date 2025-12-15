Installation et Exécution
Prérequis

    JDK 24 installé.

    Maven 3.6+ installé.

Qu'est-ce que Maven ?

Maven est un outil de gestion et d'automatisation de production de projets logiciels Java. Il gère :

    Les dépendances : Télécharge automatiquement les bibliothèques (H2, Gson, HikariCP) définies dans le pom.xml.

    Le build : Compile le code source, exécute les tests et package le projet.

Commandes

    Cloner le dépôt :
    Bash

git clone https://github.com/votre-repo/hautlesbas.git
cd hautlesbas

Compiler le projet :
Bash

mvn clean package

Lancer l'application : Vous pouvez lancer l'application directement via Maven ou via le JAR généré.
Bash

    # Via Maven
    mvn exec:java -Dexec.mainClass="com.hautlesbas.Main"

    # OU via le JAR (après compilation)
    java -jar target/hautlesbas-1.0-SNAPSHOT.jar --enable-preview

L'API sera accessible sur http://localhost:8080. La base de données hautlesbas_db sera créée automatiquement à la racine du projet au premier lancement.
Documentation de l'API
Gestion des Chaussettes
1. Ajouter une chaussette

    URL : /chaussettes

    Méthode : POST

    Corps (JSON) :
    JSON

    {
      "couleur": "Rouge",
      "taille": "42",
      "typeTissu": "Laine",
      "prix": 12.50
    }

    Réponse (201) : Retourne l'objet créé avec son identifiant.

2. Lister les chaussettes

    URL : /chaussettes

    Méthode : GET

    Réponse (200) : Liste JSON de toutes les chaussettes en stock.

3. Modifier une chaussette

    URL : /chaussettes/{id}

    Méthode : PUT

    Corps (JSON) : Mêmes champs que le POST.

4. Supprimer une chaussette

    URL : /chaussettes/{id}

    Méthode : DELETE

5. Rechercher

    URL : /chaussettes/recherche?couleur=Rouge&taille=42

    Méthode : GET

Gestion des Ventes
1. Créer une vente

    URL : /ventes

    Méthode : POST

    Description : Vend des chaussettes existantes. Les chaussettes vendues sont retirées de l'inventaire.

    Corps (JSON) :
    JSON

    {
      "chaussettes": [
        { "identifiant": 1 },
        { "identifiant": 5 }
      ]
    }

    Réponse (201) : Succès.

    Erreur (409) : Si une des chaussettes n'existe pas ou a déjà été vendue.

2. Lister toutes les ventes

    URL : /ventes

    Méthode : GET

3. Rechercher des ventes par date

    URL : /ventes/recherche/date?start=yyyy-MM-dd&end=yyyy-MM-dd

    Méthode : GET

    Exemple : /ventes/recherche/date?start=2023-01-01&end=2023-12-31

4. Annuler une vente

    URL : /ventes/{id}

    Méthode : DELETE

    Effet : Supprime la vente et remet les chaussettes associées dans l'inventaire (Restockage automatique).