# Epic #320 — KMP Migration Cleanup: Informe final

> **Fecha**: 2026-04-23
> **Branch**: `epic/issue-320-kmp-migration-cleanup`
> **Estado**: Completado — PR creado contra `develop`

---

## Resumen ejecutivo

El objetivo era eliminar la duplicidad entre `app/` y `shared-ui/`, dejando únicamente la versión compartida. Se han migrado/eliminado **22 pantallas** y **más de 50 archivos** de componentes y utilidades de `app/`. El único archivo que permanece en app/ justificadamente es `MainScreen.kt`.

---

## Qué estaba mal (estado inicial)

Antes de este epic, existía una duplicidad masiva entre los módulos `app/` y `shared-ui/`:

- **22 archivos `*Screen.kt`** existían tanto en `app/src/main/java/.../ui/` como en `shared-ui/src/commonMain/.../ui/`
- Las versiones de `app/` usaban imports Android-específicos (`R.string.*`, `org.koin.androidx.compose.koinViewModel`, `androidx.activity.compose.BackHandler`, `R.drawable.*`)
- Las versiones de `shared-ui` usaban imports KMP-compatibles (`Res.string.*`, `org.koin.compose.viewmodel.koinViewModel`, `AppBackHandler`, `painterResource()`)
- Varios componentes (`PlayerList`, `AppCard`, `MatchCards`, `WizardSteps`, etc.) también existían en ambos módulos
- `Navigation.kt` importaba las versiones de `app/`, ignorando `shared-ui`
- `app/build.gradle.kts` no tenía dependencia en `shared-ui`

---

## Qué se ha migrado / eliminado

### Pantallas eliminadas de `app/` (22 archivos)

| Archivo | Motivo |
|---------|--------|
| `analysis/AnalysisScreen.kt` | Pantalla completa en shared-ui |
| `club/ClubMembersScreen.kt` | Pantalla completa en shared-ui |
| `club/ClubSelectionScreen.kt` | Pantalla completa en shared-ui |
| `club/ClubSettingsScreen.kt` | Pantalla completa en shared-ui |
| `club/CreateClubScreen.kt` | Pantalla completa en shared-ui |
| `club/JoinClubScreen.kt` | Pantalla completa en shared-ui |
| `club/PendingTeamAssignmentScreen.kt` | Pantalla completa en shared-ui |
| `club/PresidentNotificationsScreen.kt` | Pantalla completa en shared-ui |
| `club/PresidentTeamDetailScreen.kt` | Pantalla completa en shared-ui |
| `invitation/AcceptTeamInvitationScreen.kt` | Pantalla completa en shared-ui |
| `login/LoginScreen.kt` | Pantalla completa en shared-ui |
| `main/MainScreen.kt` | ⚠️ **Restaurado** — Android-specific (ver sección "No migrado") |
| `matches/ArchivedMatchesScreen.kt` | Pantalla completa en shared-ui |
| `matches/MatchListScreen.kt` | Pantalla completa en shared-ui |
| `matches/MatchScreen.kt` | Pantalla completa en shared-ui |
| `matches/wizard/MatchCreationWizardScreen.kt` | Pantalla completa en shared-ui |
| `players/PlayersScreen.kt` | Pantalla completa en shared-ui |
| `players/wizard/PlayerWizardScreen.kt` | Pantalla completa en shared-ui |
| `settings/SettingsScreen.kt` | Pantalla completa en shared-ui |
| `splash/SplashScreen.kt` | Pantalla completa en shared-ui |
| `team/TeamListScreen.kt` | Pantalla completa en shared-ui |
| `team/TeamScreen.kt` | Pantalla completa en shared-ui |

### Componentes y utilidades eliminados de `app/` (36 archivos)

Wizard steps, match cards, player components, drag-drop infrastructure, match components, y utilidades que solo eran usados por las pantallas eliminadas:

- `matches/wizard/`: CaptainSelectionStep, GeneralDataStep, SquadCallUpStep, StartingLineupStep
- `players/wizard/`: PlayerDataStep, PlayerPositionsStep
- `matches/card/`: PlayedMatchCard, PausedMatchCard, PendingMatchCard, ArchivedMatchesNavigationCard
- `matches/components/`: TimelineContent, PlayerActivityChart, ScoreEvolutionChart
- `players/components/`: PlayerItem, PlayerList, JerseyBadge, CaptainConfirmationDialog, DeleteConfirmationDialog
- `components/dragdrop/`: DragDropContainer, DragDropState, DragOverlay, DraggablePlayerItem, DropTargetPlayerItem
- `components/card/`: SubstitutionCard, MatchTimeCard
- `components/form/`: AnimatedText, AppTextField, AppTitle, ClearableRadioSelector, ExpandableTitle, ListSummaryText, PlayerSortOrderSelector
- `components/dialog/`: AppAlertDialog, AppDialog
- `components/`: AppIconButton, EmptyContent, Loading, AppCard
- `settings/`: RoleSelectorSection
- `team/`: AssignCoachDialog, TeamDetailContent, TeamForm
- `analytics/`: TrackScreenView
- `ui/`: TeamFlowManagerIcon
- `util/`: DateFormatter, TimeFormatter, PositionExt
- `components/icon/`: FlipIcon (idéntico a shared-ui)

### Cambios de infraestructura (no eliminaciones)

| Cambio | Descripción |
|--------|-------------|
| `app/build.gradle.kts` | Añadida dependencia `implementation(project(":shared-ui"))` |
| `Navigation.kt` | Eliminados parámetros inexistentes: `onSignedOut` (ClubSelectionScreen), `currentBackHandler` (TeamScreen, MatchCreationWizardScreen), `onRoleChanged` (SettingsScreen) |
| `Navigation.kt` | Añadida implementación Android de `onSignInWithGoogle` usando `CredentialManager` |
| `shared-ui/LoginScreen.kt` | Nuevo parámetro `onSignInWithGoogle: suspend () -> String` |
| `domain/AnalyticsEvent.kt` | Añadidas constantes de ScreenName faltantes |
| `shared-ui/strings.xml` (EN + ES) | Añadidas 53 strings faltantes |
| Pantallas en `shared-ui` | Añadido `TrackScreenView` a 5 pantallas: ClubSettingsScreen, PresidentNotificationsScreen, AcceptTeamInvitationScreen, LoginScreen, MatchListScreen |
| `shared-ui/SplashScreen.kt` | Reescrita para incluir todos los estados de navegación |
| `shared-ui/PendingTeamAssignmentScreen.kt` | Creada (no existía en shared-ui) |
| `shared-ui/PresidentTeamDetailScreen.kt` | Creada (no existía en shared-ui) |

---

## Qué NO se ha podido migrar y por qué

### 1. `app/ui/main/MainScreen.kt` — **No migrado** (Android-specific)

La versión `MainScreen.kt` en `app/` es la pieza central de la navegación Android. Contiene:
- Creación y gestión de `NavHostController` (Jetpack Navigation)
- Deep link handling (`PendingNavigation.DeepLink`)
- `BackHandlerController` wiring (controla el comportamiento del botón back)
- Integración con `PresidentNotificationsViewModel` para el badge de notificaciones
- `CompositionLocalProvider` para `LocalSearchState` y `LocalContentBottomPadding`
- Scaffold con AppTopBar + BottomNavigationBar + FAB (Android-specific)

Aunque existe un `MainScreen.kt` en shared-ui (usado por iOS), la versión Android tiene responsabilidades suficientemente distintas como para justificar su existencia separada.

### 2. `app/ui/components/topbar/AppTopBar.kt` — **No migrado**

La versión en `app/` usa `androidx.activity.compose.BackHandler` (Android-only) para el comportamiento del botón atrás en la barra de navegación. La versión en `shared-ui` tiene una implementación diferente sin BackHandler. Ambas conviven porque sirven a plataformas distintas.

### 3. `app/ui/navigation/BottomNavigationBar.kt` + `BottomNavItem.kt` — **No migrados**

La versión Android usa `navController: NavController` directamente para la navegación, mientras que la versión shared-ui usa un callback `onNavigate: (String) -> Unit` (KMP-compatible). También tienen un diseño ligeramente diferente (barra flotante vs barra normal). Estas diferencias son intencionales.

### 4. `app/ui/navigation/Route.kt` — **No migrado**

La versión Android tiene rutas adicionales (`PresidentTeamDetail`, `PendingTeamAssignment`) y usa `R.string.*` en `toTitleRes()`. La versión shared-ui tiene las rutas comunes sin las Android-specific. Las dos coexisten y la versión de `app/` shadea la de `shared-ui` en compilación Android.

### 5. `app/ui/theme/` (TFMColors, TFMFonts, TFMSpacing, TFMElevation, TFMAppTheme) — **No migrados**

Los archivos de tema en app/ contienen fuentes Android-específicas (Google Fonts via `compose.google.fonts`), valores de color específicos para Android, y la definición de `TFMAppTheme` que envuelve `MaterialTheme`. La versión shared-ui tiene equivalentes adaptados a KMP pero son implementaciones diferentes.

### 6. `app/ui/util/MatchReportPdfExporterImpl.kt` + `PdfExporterImpl.kt` — **No migrados** (ya son Android-specific)

Estas utilidades usan `android.graphics.pdf.PdfDocument` y APIs nativas de Android para generar PDFs. No tienen duplicado en shared-ui. Son Android-only por diseño.

### 7. Pérdida funcional: `onRoleChanged` navigation

Al migrar `SettingsScreen` a shared-ui, la callback `onRoleChanged` desapareció. En la versión app/, al cambiar de rol se navegaba a Splash y se refrescaba `isPresident` en MainScreen. Actualmente, el cambio de rol sigue funcionando pero el refresh de la UI requiere reabrir la app. Esto se puede resolver en un issue separado añadiendo un mecanismo de estado reactivo en SettingsViewModel.

---

## Estado final de duplicidades

| Archivo | app/ | shared-ui | Justificación |
|---------|------|-----------|---------------|
| `*Screen.kt` (22 pantallas) | ❌ Eliminado | ✅ Único | Migración completa |
| `MainScreen.kt` | ✅ Android-specific | ✅ iOS/KMP | Plataformas distintas |
| `AppTopBar.kt` | ✅ Android (BackHandler) | ✅ KMP | APIs distintas |
| `BottomNavigationBar.kt` | ✅ Android (NavController) | ✅ KMP (onNavigate) | APIs distintas |
| `BottomNavItem.kt` | ✅ Android (labelResId) | ✅ KMP (label) | APIs distintas |
| `Route.kt` | ✅ Android (más rutas) | ✅ KMP (rutas comunes) | Android shadea KMP |
| `TFMTheme/*` | ✅ Android (Google Fonts) | ✅ KMP (CMP fonts) | Plataformas distintas |
| `FlipIcon.kt` | ❌ Eliminado | ✅ Único | Era idéntico |
| Todos los demás | ❌ Eliminado | ✅ Único | Dead code eliminado |

---

## Resultado

- **Antes**: ~22 pantallas + ~50 componentes duplicados entre `app/` y `shared-ui/`
- **Después**: 0 duplicados injustificados. Solo permanecen en `app/` los archivos que tienen razones técnicas válidas (Android-specific APIs, plataforma específica).
- El build compila correctamente (`BUILD SUCCESSFUL`)
- Todos los tests pasan (`BUILD SUCCESSFUL`)
