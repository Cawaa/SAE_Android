# eq_1_04_b


# Projet SAÉ 4 - R4.11 Dév. mobile

## Arborescence du projet : 

## 📁 Architecture du projet

```plaintext
app/src/main/java/dev/mobile/tpsae/
│
├── data/
│   ├── MovieRepository.kt      # Gestion des appels API
│   └── TmdbApi.kt              # Configuration du client Ktor
│
├── model/
│   └── Movie.kt                # Classes de données (Movie, MovieResponse)
│
├── viewmodel/
│   └── MainViewModel.kt        # Logique métier et état de l’UI
│
├── ui/
│   ├── components/
│   │   └── MovieComponents.kt  # Composants UI réutilisables (cards, search bar)
│   │
│   ├── screens/
│   │   └── MovieListScreen.kt  # Écran principal (liste des films)
│   │
│   ├── MainActivity.kt         # Activité principale
│   └── DetailActivity.kt       # Écran de détail
│
└── ui/theme/                   # Thème Material (couleurs, typographie, shapes)
```


Le but de cette SAÉ est de développer une application Android native (*déployable sur une machine de l'iut*) par sous-équipe de projet SAÉ (2-3 étudiants ; normalement 2 sous-équipes par équipe de SAÉ).
Cette application devra :

- récupérer des données sur `https://www.themoviedb.org/` ;
- proposer (à minima) trois vues différentes (une page de recherche/paramétrage, une page d'affichage dans une `LazyList`, une page "vue détaillée", ...) ;
- compter au moins deux activités avec passage de données entre elles ;
- comporter des classes de données (`Film`, ...) correspondant à la partie modèle de votre application ;
- afficher des images téléchargées dynamiquement ;
- mettre en oeuvre les bonnes pratiques vues en TP : UDF, séparation des couches : Ui et ViewModel, données, ...

De plus, votre application pourra démontrer l'usage d'*aspects* du développement android non-abordés dans les tutoriels. Quelques exemples : 

- tri, filtrage, pagination des résultats ;
- basculement portait/paysage ;
- gestion des locales (EN, FR) ; 
- persistance des données ;
- etc.

Votre application s’appuiera sur l'api `https://developer.themoviedb.org/reference/intro/getting-started` (documentation `https://developer.themoviedb.org/docs/getting-started`). Pour utiliser cette API, il vous faudra préalablement obtenir une clé.

Voici une proposition de maquette mais vous pouvez en concevoir une autre.
![](img/movies.png)

Les bibliothèques autorisées, sont celles utilisées en TD ou citées en cours : `ktor`, `kotlinx-serialization`, `kotlin-parcelize` et `coil`.

Un projet vierge incluant déjà toutes ces librairies est fourni.


## Rendu - Recette finale

L'évaluation, qui se fera sous la forme d'une revue de code avec une présentation du projet, aura lieu en fin de semaine 15 (9-10 avril).

Quelques précisions :
- votre code sera récupéré via *votre* dépôt Git *uniquement* ;
- ce dépôt git est, lui aussi, fourni ;
- celui-ci contiendra un `Readme.md` ainsi que votre application ;
- votre code sera re-compilé, puis déployé sur la machine de l'enseignant en charge de votre recette ;
- assurez-vous également que votre code puisse également être compilé/déployé sur une machine de l'IUT.
