# Application Hautlesbas

## Application backend Java pour la gestion de vente en ligne de chaussettes. Prérequis

    JDK 24 (obligatoire pour la compatibilité)

    IntelliJ IDEA (Community ou Ultimate)

    Maven 3.6+ (généralement inclus avec IntelliJ)

## Lancement avec IntelliJ IDEA
### Méthode 1 : Importation directe

    Ouvrir IntelliJ IDEA

    Fichier → Ouvrir (ou Open)

    Sélectionnez le dossier du projet hautlesbas

    Choisissez "Open as Project"

Méthode 2 : Importation Maven

    Fichier → Nouveau → Projet à partir de sources existantes (ou New → Project from Existing Sources)

    Sélectionnez le fichier pom.xml

    Suivez l'assistant d'importation Maven

    Choisissez JDK 24 dans la configuration du projet

Configuration du JDK dans IntelliJ

Si IntelliJ ne détecte pas automatiquement le JDK 24 :

    Fichier → Structure de projet (ou File → Project Structure)

    Onglet "Project"

    SDK du projet : Sélectionnez JDK 24

        Si non disponible : Add JDK → Sélectionnez le dossier d'installation du JDK 24

    Niveau du langage : 24 - Modules en cours de preview

    Cliquez sur "Apply" puis "OK"

Exécution de l'application.
Exécution directe

    Ouvrez la classe Main.java dans src/main/java/com/hautlesbas/Main.java

    Cliquez sur l'icône  verte dans la marge à côté de la méthode main

    Choisissez "Run 'Main.main()'"

