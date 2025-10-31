# US-3.1.5: Match Report Export - Visual Guide

## User Interface Location

### Export Button in Finished Match Screen

The Export button appears as a **Floating Action Button (FAB)** with a **Share icon** in the **bottom-right corner** of the screen when viewing a finished match.

```
┌─────────────────────────────────────────┐
│  Match Summary                          │
│                                         │
│  Opponent: Rival Team                   │
│  Date: 31/10/2024 15:30                │
│  Location: Home Stadium                 │
│  Result: 3 - 1                         │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Sort by: Time (descending)        │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ 9 | John Doe          | 25:00     │ │
│  │   ⚽ Captain                       │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ 10 | Jane Smith       | 20:15     │ │
│  │   🥅 Goalkeeper                   │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ 11 | Mike Johnson     | 18:30     │ │
│  └───────────────────────────────────┘ │
│                                         │
│                                  ╔═══╗ │
│                                  ║ ↗ ║ │ <- Export FAB
│                                  ╚═══╝ │
└─────────────────────────────────────────┘
```

## PDF Report Layout

### Page Structure

The generated PDF contains:

#### 1. Header Section
```
═══════════════════════════════════════════
            MATCH REPORT
═══════════════════════════════════════════

Match Information
─────────────────────────────────────────
Opponent: Rival Team
Date: 31/10/2024 15:30
Location: Home Stadium
Result: 3 - 1
```

#### 2. Player Table

The player table includes all squad members with comprehensive statistics:

```
Players
════════════════════════════════════════════════════════════════════════════

┌────┬──────────────┬────┬───┬────┬────────┬───────┐
│No. │ Player       │ GK │ C │ ST │ Time   │ Goals │
├────┼──────────────┼────┼───┼────┼────────┼───────┤
│ 9  │ John Doe     │ -  │ X │ X  │ 25:00  │ 2     │
├────┼──────────────┼────┼───┼────┼────────┼───────┤
│ 10 │ Jane Smith   │ X  │ -  │ X  │ 25:00  │ -     │
├────┼──────────────┼────┼───┼────┼────────┼───────┤
│ 11 │ Mike Johnson │ -  │ -  │ X  │ 15:30  │ 1     │
├────┼──────────────┼────┼───┼────┼────────┼───────┤
│ 7  │ Emily Davis  │ -  │ -  │ -  │ 9:30   │ -     │
├────┼──────────────┼────┼───┼────┼────────┼───────┤
│ 8  │ David Wilson │ -  │ -  │ X  │ 25:00  │ -     │
└────┴──────────────┴────┴───┴────┴────────┴───────┘

Legend:
- GK = Goalkeeper (marked with X if player is a goalkeeper)
- C = Captain (marked with X if player is the team captain)
- ST = Starter (marked with X if player was in starting lineup)
- Time = Total playing time in minutes:seconds format
- Goals = Total number of goals scored
```

### Table Features

1. **Alternating Row Colors**: Improves readability with gray/white alternating backgrounds
2. **Bordered Cells**: Clear separation between data points
3. **Professional Headers**: Bold, centered column headers with gray background
4. **Compact Format**: Fits on A4 paper width
5. **Sorted by Number**: Players listed in ascending order by dorsal number
6. **Simplified Layout**: Fixed row height with clean, easy-to-read format

### Column Details

| Column | Width | Description |
|--------|-------|-------------|
| No. | 10% | Player's dorsal/jersey number |
| Player | 30% | Full name (First + Last) |
| GK | 10% | "X" if goalkeeper, "-" otherwise |
| C | 10% | "X" if captain, "-" otherwise |
| ST | 10% | "X" if starter, "-" otherwise |
| Time | 15% | Playing time in MM:SS format |
| Goals | 15% | Total number of goals scored |

## Export Flow

### Step-by-Step Process

1. **User Action**: Coach taps the Export FAB (Share icon button)

2. **PDF Generation**:
   - ViewModel calls `requestExport()`
   - GetMatchReportDataUseCase aggregates data from repositories
   - MatchReportPdfExporterImpl generates PDF file
   - PDF saved to app cache directory

3. **Share Sheet**:
   - Android system share sheet appears
   - User can choose sharing method:
     - Email
     - WhatsApp
     - Google Drive
     - Other installed apps

4. **Completion**:
   - File shared successfully
   - Export state resets
   - User returns to match screen

### Visual Flow Diagram

```
┌─────────────────┐
│ Finished Match  │
│     Screen      │
└────────┬────────┘
         │
         │ User taps Export FAB
         ▼
┌─────────────────┐
│  Loading...     │
└────────┬────────┘
         │
         │ PDF generated
         ▼
┌─────────────────┐
│  Share Sheet    │
│                 │
│  📧 Email       │
│  💬 WhatsApp    │
│  ☁️ Drive       │
│  📱 More...     │
└─────────────────┘
```

## Example PDF Content

For a match with the following data:
- **Opponent**: Athletic Bilbao
- **Date**: 31/10/2024 15:30
- **Location**: San Mamés
- **Result**: 3 - 1

The PDF would show:
- Match header with all details (internationalized in Spanish/English)
- 11-15 players in a table
- Each player with their statistics
- Goals shown as total count: "2" or "-"
- Clean, single-line rows for easy reading

## File Naming Convention

Generated PDF files use this naming pattern:
```
partido_[OpponentName]_[Timestamp].pdf

Example: partido_Athletic_Bilbao_1698762600000.pdf
```

This ensures:
- ✅ Unique filenames (timestamp)
- ✅ Identifiable matches (opponent name)
- ✅ No special characters issues (spaces replaced with underscores)

## Accessibility

- **Button Size**: FAB is large enough for easy tapping (56dp standard)
- **Icon**: Universal "Share" icon recognized by users
- **Color**: Uses app's primary color scheme for consistency
- **Position**: Bottom-right corner (standard FAB placement)
- **Content Description**: "Export match report" for screen readers

## Technical Notes

- PDF uses A4 page size (595 x 842 points)
- Margins: 40 points on all sides
- Font sizes: Title (18pt), Section (14pt), Body (10pt), Table (8pt)
- Colors: Black text, gray backgrounds, standard borders
- Format: Standard PDF compatible with all viewers
