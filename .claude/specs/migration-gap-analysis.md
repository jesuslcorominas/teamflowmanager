# Migration Gap Analysis — epic/issue-320-kmp-migration-cleanup

**Fecha**: 2026-04-24
**Baseline original**: commit `ac743553` (v0.5.0, pre-cleanup)
**Target**: `shared-ui/src/commonMain/kotlin/.../ui/`

## Metodología

Comparación firma-por-firma de cada fichero eliminado de `app/ui/` contra su equivalente en `shared-ui/`. Se verificaron además los commits post-migración en `app/` (entre `ac743553` y `develop`) para detectar funcionalidades añadidas después de la migración KMP.

---

## Resultado: 4 gaps encontrados

### GAP-1 — ⚠️ ALTA: `GeneralDataStep` — falta `homeGround` + `SuggestionChip`

**Fichero afectado**: `shared-ui/src/commonMain/kotlin/.../ui/matches/wizard/GeneralDataStep.kt`

El original en `app/` tenía:
```kotlin
fun GeneralDataStep(
    ...
    homeGround: String? = null,   // ← FALTA en shared-ui
    ...
) {
    ...
    if (homeGround != null) {
        SuggestionChip(           // ← FALTA en shared-ui
            onClick = { location = homeGround },
            label = { Text(homeGround) },
        )
    }
}
```

El `SuggestionChip` permite al usuario pre-rellenar el campo "sede" con el campo de juego habitual del club con un solo tap.

**Causa**: La funcionalidad fue añadida en el commit `9dc44a4c` (feat: president match notifications) junto con `getHomeGround()` en el ViewModel, pero la migración de `GeneralDataStep` a shared-ui no la incluyó.

---

### GAP-2 — ⚠️ ALTA: `MatchCreationWizardScreen` — no pasa `homeGround`

**Fichero afectado**: `shared-ui/src/commonMain/kotlin/.../ui/matches/wizard/MatchCreationWizardScreen.kt`

El original en `app/` llamaba:
```kotlin
GeneralDataStep(
    ...
    homeGround = wizardViewModel.getHomeGround(),  // ← FALTA en shared-ui
    ...
)
```

`MatchCreationWizardViewModel.getHomeGround()` existe y carga el `homeGround` del club desde Firestore. Sin este parámetro, el SuggestionChip nunca aparece aunque se arregle GAP-1.

---

### GAP-3 — ⚠️ MEDIA: `MatchScreen` — `StatisticsTabContent` es placeholder

**Fichero afectado**: `shared-ui/src/commonMain/kotlin/.../ui/matches/MatchScreen.kt`

La pestaña "Statistics" en shared-ui muestra un `LazyColumn` vacío con comentario `// Charts deferred to KMP-28`. El issue KMP-28 se cerró migrando `AnalysisScreen` pero **sin completar esta pestaña en `MatchScreen`**.

El original en `app/` tenía:
- `PlayerActivityChart` — barras de tiempo jugado por jugador en el partido
- `ScoreEvolutionChart` — evolución del marcador a lo largo del tiempo

Estos composables están en `app/ui/matches/components/` y **no tienen equivalente en shared-ui**.

---

### GAP-4 — 🔵 BAJA (decisión consciente): Drag-and-drop en `MatchScreen`

**Fichero afectado**: `shared-ui/src/commonMain/kotlin/.../ui/matches/MatchScreen.kt`

Las sustituciones en shared-ui son click-based (comentario en línea ~355: `// Player list — click-based substitution (no drag-drop in KMP-23)`). Las clases de drag-drop solo existen en `app/components/dragdrop/` y están pendientes del issue KMP-27 (deferred como "comfort feature").

**No se considera un gap de regresión** porque está documentado como decisión consciente.

---

## Screens auditados: ✅ OK

| Screen | Estado |
|--------|--------|
| CaptainSelectionStep | ✅ |
| SquadCallUpStep | ✅ |
| StartingLineupStep | ✅ |
| MatchListScreen | ✅ |
| ArchivedMatchesScreen | ✅ |
| PendingMatchCard / PlayedMatchCard / PausedMatchCard | ✅ |
| PlayersScreen | ✅ |
| PlayerWizardScreen | ✅ |
| PlayerDataStep | ✅ |
| PlayerPositionsStep | ✅ |
| ClubSelectionScreen | ✅ |
| CreateClubScreen | ✅ |
| JoinClubScreen | ✅ |
| PresidentTeamDetailScreen | ✅ |
| ClubMembersScreen | ✅ (corregido en esta PR) |
| ClubSettingsScreen | ✅ (reescrito en esta PR) |
| PendingTeamAssignmentScreen | ✅ |
| PresidentNotificationsScreen | ✅ |
| TeamScreen | ✅ |
| TeamListScreen | ✅ (corregido en esta PR) |
| TeamForm | ✅ |
| TeamDetailContent | ✅ |
| SettingsScreen | ✅ (RoleSelectorSection añadida en esta PR) |
| SplashScreen | ✅ |
| LoginScreen | ✅ |
| AcceptTeamInvitationScreen | ✅ |
| AnalysisScreen | ✅ (corregido en esta PR) |

---

## Plan de implementación

### P1 — Arreglar GAP-1 y GAP-2 (homeGround en wizard de partido)

**`GeneralDataStep.kt`** — añadir parámetro `homeGround: String? = null` y el `SuggestionChip` condicional.

**`MatchCreationWizardScreen.kt`** — pasar `homeGround = wizardViewModel.getHomeGround()` a `GeneralDataStep`.

### P2 — Completar GAP-3 (StatisticsTabContent en MatchScreen)

Migrar `PlayerActivityChart` y `ScoreEvolutionChart` de `app/ui/matches/components/` a `shared-ui` y conectarlos en `MatchScreen.StatisticsTabContent`.

### P3 — GAP-4 (drag-and-drop) — Issue KMP-27 abierto, no bloquea esta PR

---

## Correcciones ya aplicadas en esta PR

- `LocalContentBottomPadding`: eliminado `ContentPadding.kt` de app/, resuelve el scroll de todas las listas
- `LocalSearchState`: eliminado `search/SearchState.kt` de app/, resuelve el crash en TeamListScreen
- `ClubSettingsScreen`: reescritura completa (view mode con InfoRow, edit mode con campos, FAB, InvitationCodeSection)
- `ClubMembersScreen`: corregido scroll con `contentPadding`
- `AnalysisScreen`: corregido FAB con `LocalContentBottomPadding`
- `SettingsScreen`: añadida `RoleSelectorSection` (switch Coach/President)
- Material3: bajado de 1.4.0 a 1.3.1 para coincidir con CMP 1.7.3 (fix crash en TeamForm)
- DEX collisions: renombrados AppTopBar, BottomNavItem, BottomNavigationBar, MainScreen en app/