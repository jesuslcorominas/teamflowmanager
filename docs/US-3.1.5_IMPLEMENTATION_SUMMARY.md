# US-3.1.5: Match Report Export - Implementation Summary

## Overview
This implementation adds the ability to export detailed match reports as shareable PDF files directly from finished matches in the TeamFlow Manager app.

## Features Implemented

### 1. Match Report Data Model
**Location:** `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/MatchReportData.kt`

Created data structures to represent a complete match report:
- `MatchReportData`: Contains match and player reports
- `PlayerMatchReport`: Individual player statistics including:
  - Dorsal number
  - Goalkeeper status (yes/no marked with X)
  - Captain status (yes/no marked with X)
  - Starter status (yes/no marked with X)
  - Total playing time
  - Goals with timestamps
  - Substitutions (in/out) with timestamps

### 2. Use Cases

#### GetMatchReportDataUseCase
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/GetMatchReportDataUseCase.kt`

Aggregates match data from multiple repositories:
- Match details (opponent, date, location, result)
- Player times from PlayerTimeHistoryRepository
- Goals from GoalRepository (excluding opponent goals)
- Substitutions from PlayerSubstitutionRepository
- Player information from PlayerRepository

#### ExportMatchReportToPdfUseCase
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/ExportMatchReportToPdfUseCase.kt`

Coordinates the PDF export process by delegating to the PDF exporter implementation.

### 3. PDF Exporter Implementation
**Location:** `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/util/MatchReportPdfExporterImpl.kt`

Creates professional PDF reports with:
- **Header Section:**
  - Match Report title
  - Match information (opponent, date, location, result)

- **Player Table:**
  - Sortable columns: Number, Player Name, GK, Captain, Starter, Time, Goals, Substitutions
  - Alternating row colors for readability
  - Goals shown with count and timestamps
  - Substitutions shown with direction arrows (↑ for in, ↓ for out) and timestamps

### 4. UI Integration
**Location:** `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/MatchScreen.kt`

Added Export functionality to FinishedMatchState:
- Floating Action Button (FAB) with Share icon in bottom-right corner
- Export triggers PDF generation
- Android share sheet appears automatically to share the generated PDF
- Export state management (Loading, Ready, Error)

### 5. ViewModel Updates
**Location:** `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchViewModel.kt`

Extended MatchViewModel with:
- Export state flow
- `requestExport()` function to trigger PDF generation
- `exportCompleted()` function to reset export state
- Integration with GetMatchReportDataUseCase and ExportMatchReportToPdfUseCase

### 6. Dependency Injection
Updated DI modules to register new components:
- `AppUseCaseModule.kt`: Registered MatchReportPdfExporterImpl
- `UseCaseModule.kt`: Registered GetMatchReportDataUseCase and ExportMatchReportToPdfUseCase
- `ViewModelModule.kt`: Added new use cases to MatchViewModel constructor

### 7. String Resources
**Location:** `app/src/main/res/values/strings.xml`

Added localized strings:
- `match_report_title`: "Match Report"
- `match_info_section`: "Match Information"
- `players_section`: "Players"
- Table headers: `number_short`, `player_name`, `goalkeeper_short`, `captain_short`, `starter_short`, `time_short`, `goals_short`, `substitutions_short`
- Export button strings

## Acceptance Criteria Met

✅ **Export button available in finished matches**
- FAB with Share icon appears in FinishedMatchState

✅ **PDF contains required match data:**
- ✅ Opponent
- ✅ Date
- ✅ Location
- ✅ Result

✅ **PDF contains required player data for each player:**
- ✅ Dorsal (number)
- ✅ Goalkeeper (marked with X)
- ✅ Captain (marked with X)
- ✅ Starter (marked with X)
- ✅ Total playing time
- ✅ Goals (total and timestamps)
- ✅ Substitutions (in/out with timestamps)

✅ **File is easy to share**
- PDF saved to cache directory
- Android share sheet integration
- Compatible with all PDF viewers

## Technical Architecture

The implementation follows the existing layered architecture:

```
UI Layer (MatchScreen)
    ↓
ViewModel (MatchViewModel + ExportState)
    ↓
Use Case Layer (GetMatchReportDataUseCase, ExportMatchReportToPdfUseCase)
    ↓
Repository Layer (MatchRepository, PlayerRepository, GoalRepository, etc.)
    ↓
PDF Exporter (MatchReportPdfExporterImpl)
```

## Code Quality

- ✅ Follows existing code patterns and conventions
- ✅ Reuses existing repositories and data sources
- ✅ Minimal changes to existing code
- ✅ Proper separation of concerns
- ✅ Clean dependency injection
- ✅ Consistent with existing export functionality (AnalysisViewModel pattern)

## User Flow

1. User finishes a match
2. FinishedMatchState screen displays with match summary
3. User taps the Export FAB (Share icon) in bottom-right
4. PDF is generated with all match and player data
5. Android share sheet appears
6. User can share PDF via email, messaging apps, cloud storage, etc.

## Files Modified/Created

### Created Files (5):
1. `domain/src/main/kotlin/.../domain/model/MatchReportData.kt`
2. `domain/src/main/kotlin/.../domain/utils/MatchReportPdfExporter.kt`
3. `usecase/src/main/kotlin/.../usecase/GetMatchReportDataUseCase.kt`
4. `usecase/src/main/kotlin/.../usecase/ExportMatchReportToPdfUseCase.kt`
5. `app/src/main/java/.../ui/util/MatchReportPdfExporterImpl.kt`

### Modified Files (6):
1. `usecase/src/main/kotlin/.../usecase/di/UseCaseModule.kt`
2. `app/src/main/java/.../di/AppUseCaseModule.kt`
3. `viewmodel/src/main/java/.../viewmodel/MatchViewModel.kt`
4. `viewmodel/src/main/java/.../viewmodel/di/ViewModelModule.kt`
5. `app/src/main/java/.../ui/matches/MatchScreen.kt`
6. `app/src/main/res/values/strings.xml`

## Notes

- The implementation reuses the existing ExportState pattern from AnalysisViewModel
- PDF files are saved to app cache directory and shared via FileProvider
- Player list is sorted by dorsal number in the report
- Goals and substitutions are sorted by match elapsed time
- Only team goals are included (opponent goals excluded from player reports)
- The export button only appears in finished matches (MatchStatus.FINISHED)
