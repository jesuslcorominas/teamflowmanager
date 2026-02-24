# US-3.2.3: Export Key Statistics - Implementation Summary

## Overview
Implemented functionality to export team statistics and match results to a shareable PDF file.

## Changes Made

### 1. Domain Models
Created three new domain models in the `domain` module:

- **PlayerExportStats**: Contains comprehensive player statistics
  - Player information
  - Matches called up (convocados)
  - Matches played (jugados)
  - Total time in minutes
  - Average time per match
  - Goals scored

- **MatchExportResult**: Contains match results with key information
  - Match reference
  - Date
  - Opponent
  - Location
  - Team goals
  - Opponent goals

- **ExportData**: Aggregates all export data
  - List of player statistics
  - List of top scorers
  - List of match results

### 2. Use Case
Created `GetExportDataUseCase` in the `usecase` module:
- Aggregates data from existing repositories (PlayerRepository, MatchRepository, PlayerTimeHistoryRepository, GoalRepository)
- Calculates comprehensive statistics for each player
- Filters matches by FINISHED status
- Sorts players by time played and scorers by goals
- Returns data as a Flow for reactive updates

### 3. PDF Export Utility
Created `PdfExporter` in the `app/ui/util` package:
- Uses Android's native PdfDocument API
- Generates professional multi-page PDF documents
- Includes:
  - Title page with team name and date
  - Player statistics table with headers
  - Top scorers list
  - Match results ordered by date
- Handles pagination automatically
- Returns shareable Uri via FileProvider

### 4. ViewModel Update
Updated `AnalysisViewModel`:
- Added `GetExportDataUseCase` dependency
- Added `GetTeamUseCase` to get team name
- Added `ExportState` sealed interface (Idle, Loading, Ready)
- Implemented `requestExportData()` function
- Implemented `exportCompleted()` function for state cleanup

### 5. UI Update
Updated `AnalysisScreen`:
- Added FloatingActionButton with Share icon in bottom-right corner
- Implemented export flow with LaunchedEffect
- Shows share intent when export is ready
- Uses PdfExporter to generate PDF
- Shares PDF using Android's native share dialog

### 6. Configuration
- Added FileProvider configuration to AndroidManifest.xml
- Created file_paths.xml for secure file sharing
- Added string resources in both English and Spanish

### 7. Dependency Injection
- Registered `GetExportDataUseCase` in UseCaseModule
- Updated AnalysisViewModel registration in ViewModelModule

## Data Exported

The PDF export includes:

### Page 1: Player Statistics Table
For each player:
- Full name
- Matches called up (convocados)
- Matches played (jugados)
- Total time (minutes)
- Average time per match (minutes)
- Goals scored

### Page 2: Additional Data
- **Top Scorers**: List of players with goals (up to 10)
  - Position, name, and goal count
  
- **Match Results**: Chronologically ordered list
  - Date, opponent, score, and location

## Technical Notes

1. **Reused Existing Components**: As instructed, the implementation reuses existing use cases and repositories without creating unnecessary new ones.

2. **Layered Architecture**: Follows the project's architecture:
   - Domain models in `domain` module
   - Use cases in `usecase` module
   - UI utilities in `app` module
   - ViewModels in `viewmodel` module

3. **Reactive Programming**: Uses Kotlin Flows for reactive data updates.

4. **Android Best Practices**:
   - Uses FileProvider for secure file sharing
   - Uses native Android PDF generation
   - Follows Material Design with FAB

5. **User Experience**:
   - Simple one-tap export with FAB
   - Native share dialog for easy distribution
   - Loading state during export
   - Automatic cleanup after sharing

## User Flow

1. User navigates to Analysis screen
2. User taps the Share/Export button (FAB)
3. System collects all statistics data
4. System generates PDF document
5. System shows native share dialog
6. User can share PDF via email, WhatsApp, Drive, etc.

## Testing Notes

Since tests are broken and not the focus per instructions, manual testing would verify:
- PDF generation with correct data
- Proper formatting and pagination
- Share intent functionality
- Different data scenarios (no data, partial data, full data)
