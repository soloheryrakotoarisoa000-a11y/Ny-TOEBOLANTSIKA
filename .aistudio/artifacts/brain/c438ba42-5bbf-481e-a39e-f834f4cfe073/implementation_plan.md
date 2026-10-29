# NY TOEBOLANTSIKA — Gestion des Dépenses Personnelles

Application Android native complète, haut de gamme et 100% hors ligne, dédiée au suivi des dépenses, des revenus et du budget personnel avec devise malgache (Ariary - MGA) par défaut, persistance locale robuste avec Room Database et design fintech vert émeraude.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Décisions confirmées avec l'utilisateur :**
> - **Technologie & Hors-ligne** : Application Android native avec Jetpack Compose et base de données Room SQLite locale, assurant un fonctionnement 100% autonome sans connexion, sans compte et résistant aux redémarrages.
> - **Sécurité & Verrouillage** : Code PIN à 4 chiffres optionnel, activable et modifiable à tout moment dans les Paramètres (avec hachage SHA-256 local sécurisé).
> - **Initialisation & Démo** : Démarrage propre avec solde initial et état vide accueillant, accompagné d'un bouton facultatif « Charger des données de démonstration » dans les Paramètres pour explorer immédiatement l'application sans impacter les données utilisateur.

---

### 1. Overview & Core Concept

- **Ce que fait l'application** : Permet d'enregistrer instantanément les dépenses et revenus, de suivre en temps réel le solde net et les budgets mensuels avec alertes (75%, 90%, 100%), de visualiser des statistiques interactives (camemberts, graphiques d'évolution sur 7 jours) et d'exporter/importer des sauvegardes JSON/CSV.
- **Public cible & Contexte** : Particuliers, entrepreneurs, familles et indépendants à Madagascar et partout ailleurs, souhaitant maîtriser leurs finances personnelles en toute confidentialité, sans dépendre du réseau Internet ni de serveurs tiers.
- **Valeur clé** : Indépendance totale vis-à-vis d'Internet, zéro fuite de données privées, rapidité d'exécution instantanée, ergonomie à une main et calculs arithmétiques précis (en centimes ou unités monétaires entières pour l'Ariary).

---

### 2. User Experience & Visual Design

#### Parcours Utilisateur Clés
1. **Accueil & Synthèse** :
   - Cartouche d'en-tête premium affichant le solde disponible total, les revenus et les dépenses du mois courant.
   - Jauge circulaire ou barre de progression du budget mensuel avec indicateurs visuels (Vert < 75%, Orange 75-90%, Rouge >= 100%).
   - Graphique de répartition des dépenses par catégorie et historique d'activité sur 7 jours.
   - Liste des 5 opérations les plus récentes avec accès rapide aux détails.
   - Bouton d'action flottant (+ FAB) pour ajouter une opération en 3 clics.
2. **Saisie & Modification d'Opération** :
   - Bascule intuitive Dépense / Revenu.
   - Clavier numérique optimisé avec suggestions de montants.
   - Grille de sélection des catégories avec icônes distinctives (Alimentation, Transport, Logement, Mobile Money, Salaire, etc.).
   - Sélecteur de date/heure et méthode de paiement (Espèces, Mobile Money, Banque, Autre).
3. **Historique & Filtrage** :
   - Raccourcis temporels (Aujourd'hui, Cette semaine, Ce mois-ci, Mois précédent, Période libre).
   - Recherche textuelle instantanée et filtres combinés par type et catégorie.
   - Totaux réactifs pour la période sélectionnée.
4. **Gestion du Budget** :
   - Définition du budget global et de plafonds spécifiques par catégorie.
   - Suivi visuel clair : Budget alloué, Montant consommé, Reste à dépenser et pourcentage.
5. **Rapports & Statistiques** :
   - Analyse comparative mensuelle, catégories les plus dépensières, taux d'épargne.
   - Exportation instantanée en CSV et sauvegarde JSON intégrale.
6. **Paramètres & Confidentialité** :
   - Choix de la devise (Ariary Ar par défaut, EUR, USD, etc.).
   - Bascule Thème Clair / Thème Sombre / Système.
   - Verrouillage PIN optionnel avec délai d'inactivité.
   - Sauvegarde / Restauration locale JSON sécurisée avec validation de schéma.

#### Identité Visuelle & Thème M3
- **Ambiance** : Fintech haut de gamme, moderne, épurée et rassurante.
- **Couleurs principales** :
  - Primaire : Vert Émeraude profond (`#0B6646` en clair, `#2ED894` en sombre).
  - Fond clair : Blanc cassé soyeux (`#F8FAF8`).
  - Fond sombre : Gris charbon profond (`#111614`).
  - Revenus : Vert vivace (`#1B8755`).
  - Dépenses : Rouge discret (`#C93B3B`).
  - Épargne & Budget : Bleu glacier / canard (`#1E6F8F`).
- **Typographie** : Hiérarchie nette avec montants chiffrés en gras et lisibles (police géométrique sans-serif M3).
- **Composants** : Cartes aux angles arrondis (16-24dp), ombrages subtils, retours haptiques et animations fluides.

---

### 3. Key Product Decisions & Trade-Offs

- **Stockage Local Room (SQLite) vs Serveur Cloud** :
  - *Choix* : Base de données Room 100% locale, sans aucun serveur ni API distante.
  - *Pourquoi* : Garantie absolue de fonctionnement hors-ligne, zéro latence, confidentialité totale et respect strict du besoin utilisateur.
- **Précision Numérique des Montants** :
  - *Choix* : Stockage sous forme de `Long` (en sous-unités monétaires entières) pour éliminer tout risque d'erreur d'arrondi à virgule flottante (`Double`).
- **Sauvegarde & Restauration JSON/CSV** :
  - *Choix* : Utilisation des Storage Access Framework Android (`Intent.ACTION_CREATE_DOCUMENT` et `ACTION_OPEN_DOCUMENT`) permettant à l'utilisateur de sauvegarder où il le souhaite (mémoire interne, carte SD, Google Drive s'il le décide manuellement).
- **Navigation Type-Safe & Ergonomie** :
  - *Choix* : Barre de navigation inférieure M3 (5 onglets) avec respect rigoureux des insets système (`WindowInsets.navigationBars`).

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├──────────────┬──────────────┬──────────────┬──────────────┬────────────┤
│   Accueil    │  Historique  │   Budget     │   Rapports   │ Paramètres │
│  (Dashboard) │  (History)   │  (Budgets)   │  (Analytics) │ (Settings) │
└───────┬──────┴───────┬──────┴───────┬──────┴───────┬──────┴─────┬──────┘
        │              │              │              │            │
        ▼              ▼              ▼              ▼            ▼
┌────────────────────────────────────────────────────────────────────────┐
│              FinancialViewModel (StateFlow & M3 Events)                │
├────────────────────────────────────────────────────────────────────────┤
│ - uiState: Solde, Totaux mensuels, Transactions, Budgets, Alertes      │
│ - Actions: Ajouter/Éditer/Supprimer, Filtrer, Exporter, Configurer     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        FinancialRepository                             │
├───────────────────────────────────┬────────────────────────────────────┤
│ TransactionDao                    │ CategoryDao & BudgetDao            │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│               Room SQLite Database (ny_toebolantsika.db)               │
├─────────────────┬───────────────────────┬──────────────────────────────┤
│ transactions    │ categories            │ budgets & settings           │
└─────────────────┴───────────────────────┴──────────────────────────────┘
```

#### Modèle de Données Room :
1. **TransactionEntity** : `id`, `type` (EXPENSE/INCOME), `amountInCents` (Long), `categoryId`, `timestamp` (Long), `note`, `paymentMethod` (CASH, MOBILE_MONEY, BANK, OTHER).
2. **CategoryEntity** : `id`, `name`, `type`, `iconName`, `colorHex`, `isCustom`.
3. **BudgetEntity** : `id`, `monthYear` (String "YYYY-MM"), `categoryId` (null pour global), `amountInCents` (Long).
4. **PreferenceManager** (DataStore / Room KeyValue) : Devise, Code PIN (haché SHA-256), État du verrouillage, Mode Thème.

#### Couverture des Scénarios de Test :
- Calcul exact du solde (Revenus - Dépenses).
- Persistance lors du redémarrage de l'application sans réseau.
- Alertes de dépassement budgétaire (75%, 90%, 100%).
- Exportation et réimportation sans perte de données.
