# US-3.2.3: Export Feature - Verification Checklist

## Visual Components

### Analysis Screen - Export Button
```
┌─────────────────────────────────────┐
│ TIEMPOS │ GOLEADORES                │ ← Tabs
├─────────────────────────────────────┤
│                                     │
│  [Bar Chart with Player Stats]     │
│                                     │
│                                     │
│                                     │
│                                     │
│                                  ┌──┐│
│                                  │📤││ ← Export FAB
│                                  └──┘│
└─────────────────────────────────────┘
```

### Export Flow
```
User taps FAB → Loading → PDF Generation → Share Dialog
     ↓             ↓            ↓              ↓
  Request      Collect       Create         Share
   Export       Data          PDF           File
```

## PDF Document Structure

### Page 1: Team Statistics
```
╔════════════════════════════════════════╗
║     Estadísticas del Equipo            ║
║           [Team Name]                  ║
║       Fecha: DD/MM/YYYY                ║
║                                        ║
║  Estadísticas de Jugadores             ║
║  ─────────────────────────────────     ║
║  Jugador  Conv Jug T.Tot T.Med Goles  ║
║  ────────────────────────────────────  ║
║  Juan P.   12   10  450m  45m    5    ║
║  María G.  10    8  380m  47m    3    ║
║  Carlos L. 11    9  420m  46m    2    ║
║  ...                                   ║
╚════════════════════════════════════════╝
```

### Page 2: Scorers & Results
```
╔════════════════════════════════════════╗
║  Goleadores                            ║
║  ───────────                           ║
║  1. Juan Pérez - 5 gol(es)            ║
║  2. María García - 3 gol(es)          ║
║  3. Carlos López - 2 gol(es)          ║
║  ...                                   ║
║                                        ║
║  Resultados de Partidos                ║
║  ───────────────────────                ║
║  15/01/2024 | Rival A (3-2) - Casa    ║
║  22/01/2024 | Rival B (1-1) - Fuera   ║
║  29/01/2024 | Rival C (4-0) - Casa    ║
║  ...                                   ║
╚════════════════════════════════════════╝
```

## Share Dialog (Android Native)
```
┌─────────────────────────────────────┐
│ Compartir estadísticas              │
├─────────────────────────────────────┤
│  📧 Email                            │
│  💬 WhatsApp                         │
│  📁 Google Drive                     │
│  📱 Bluetooth                        │
│  ...                                 │
└─────────────────────────────────────┘
```

## Code Flow Diagram

```
User Action
    │
    ↓
AnalysisScreen.FAB.onClick
    │
    ↓
ViewModel.requestExportData()
    │
    ↓
GetExportDataUseCase()
    │
    ├→ PlayerRepository.getAllPlayers()
    ├→ MatchRepository.getAllMatches()
    ├→ PlayerTimeHistoryRepository.getAllPlayerTimeHistory()
    └→ GoalRepository.getAllTeamGoals()
    │
    ↓
Calculate Statistics
    │
    ├→ matchesCalledUp per player
    ├→ matchesPlayed per player
    ├→ totalTime per player
    ├→ averageTime per player
    └→ goalsScored per player
    │
    ↓
ExportState.Ready(data, teamName)
    │
    ↓
LaunchedEffect triggers
    │
    ↓
PdfExporter.exportToPdf()
    │
    ├→ Create PdfDocument
    ├→ Draw title and headers
    ├→ Draw player stats table
    ├→ Draw top scorers list
    ├→ Draw match results
    └→ Save to cache and get Uri
    │
    ↓
Create Share Intent
    │
    ↓
System Share Dialog
    │
    ↓
User selects app to share
    │
    ↓
PDF Shared Successfully
```

## Data Mapping

### PlayerExportStats
```kotlin
matchesCalledUp    ← matches.filter { FINISHED }.count { squadCallUpIds.contains(playerId) }
matchesPlayed      ← timeHistory.filter { playerId }.distinctBy { matchId }.size
totalTimeMinutes   ← timeHistory.sumOf { elapsedTimeMillis } / (60 * 1000)
averageTimePerMatch ← totalTime / matchesPlayed
goalsScored        ← goals.filter { scorerId == playerId }.size
```

### MatchExportResult
```kotlin
date         ← match.dateTime
opponent     ← match.opponent
location     ← match.location
teamGoals    ← match.goals
opponentGoals ← match.opponentGoals
```

## Testing Scenarios

### Happy Path
1. ✅ User has players with statistics
2. ✅ User has finished matches
3. ✅ User has goals recorded
4. ✅ PDF generates successfully
5. ✅ Share dialog appears
6. ✅ PDF can be shared

### Edge Cases
1. ✅ No players → Should show empty data message
2. ✅ No finished matches → Should show no match data
3. ✅ No goals → Should show scorers as empty
4. ✅ Large dataset → Pagination handles multiple pages
5. ✅ No team name → Uses "Mi Equipo" as fallback

## Verification Points

### UI
- [ ] FAB appears in Analysis screen
- [ ] FAB has Share icon
- [ ] FAB is positioned bottom-right
- [ ] Tapping FAB triggers export

### PDF Content
- [ ] Team name appears correctly
- [ ] Current date is shown
- [ ] Player statistics table has all columns
- [ ] All players are listed
- [ ] Top scorers are ranked correctly
- [ ] Match results are chronologically ordered
- [ ] Spanish labels are used

### Sharing
- [ ] Share dialog appears
- [ ] Multiple sharing apps available
- [ ] PDF can be opened in PDF readers
- [ ] PDF formatting is professional
- [ ] Data is accurate

### Technical
- [ ] No compilation errors
- [ ] No runtime errors
- [ ] Proper state management
- [ ] Memory efficient
- [ ] FileProvider configured correctly

## File Locations

```
teamflowmanager/
├── domain/model/
│   ├── PlayerExportStats.kt          ← Player stats model
│   ├── MatchExportResult.kt          ← Match result model
│   └── ExportData.kt                 ← Aggregation model
├── usecase/
│   ├── GetExportDataUseCase.kt       ← Data collection use case
│   └── di/UseCaseModule.kt           ← DI registration
├── viewmodel/
│   ├── AnalysisViewModel.kt          ← Export state management
│   └── di/ViewModelModule.kt         ← DI registration
└── app/
    ├── ui/analysis/
    │   └── AnalysisScreen.kt         ← Export button UI
    ├── ui/util/
    │   └── PdfExporter.kt            ← PDF generation
    ├── res/
    │   ├── values/strings.xml        ← English strings
    │   ├── values-es/strings.xml     ← Spanish strings
    │   └── xml/file_paths.xml        ← FileProvider paths
    └── AndroidManifest.xml           ← FileProvider config
```

## Success Criteria ✅

All requirements from US-3.2.3 have been implemented:
- ✅ Export button in statistics report
- ✅ Shareable file generation (PDF)
- ✅ Player data: convocados, jugados, tiempo total, tiempo medio, goles
- ✅ Top scorers list
- ✅ Match results with opponent, date, and score
- ✅ Results ordered by date
- ✅ Easy to share
