# US-2.1.6: Navigation System Visual Guide

## Navigation Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        App Launch                                │
│                                                                   │
│                     ┌─────────────┐                              │
│                     │   Splash    │                              │
│                     │   Screen    │                              │
│                     └──────┬──────┘                              │
│                            │                                      │
│              ┌─────────────┴─────────────┐                       │
│              │                           │                       │
│       ┌──────▼──────┐           ┌───────▼────────┐             │
│       │  No Team?   │           │  Team Exists?  │             │
│       │ Create Team │           │    Players     │             │
│       │   Screen    │           │    Screen      │             │
│       └──────┬──────┘           └───────┬────────┘             │
│              │                           │                       │
│              └───────────┬───────────────┘                       │
│                          │                                       │
│                   ┌──────▼──────┐                               │
│                   │   Players   │                               │
│                   │   Screen    │                               │
│                   └──────┬──────┘                               │
│                          │                                       │
└──────────────────────────┼───────────────────────────────────────┘
                           │
                           │
┌──────────────────────────┼───────────────────────────────────────┐
│            Bottom Navigation Bar (Always Visible)                 │
│  ┌──────────────┬──────────────────┬──────────────────────────┐  │
│  │   Players    │   Team Detail    │       Matches            │  │
│  │  (Group Icon)│  (Groups Icon)   │  (Soccer Ball Icon)      │  │
│  └──────┬───────┴─────────┬────────┴──────────┬───────────────┘  │
│         │                 │                    │                   │
│  ┌──────▼──────┐   ┌──────▼───────┐   ┌───────▼────────────┐    │
│  │  Players    │   │Team Detail   │   │   Match List       │    │
│  │  Screen     │   │   Screen     │   │   Screen           │    │
│  │             │   │              │   │ ┌────────────────┐ │    │
│  │ • List of   │   │ • Team Name  │   │ │Pending Matches │ │    │
│  │   players   │   │ • Coach      │   │ │ • Edit         │ │    │
│  │ • Add FAB   │   │ • Delegate   │   │ │ • Delete       │ │    │
│  │             │   │              │   │ │ • Start ►      │ │    │
│  │             │   │              │   │ └────────────────┘ │    │
│  │             │   │              │   │ ┌────────────────┐ │    │
│  │             │   │              │   │ │Played Matches  │ │    │
│  │             │   │              │   │ │ • Score 0-0    │ │    │
│  │             │   │              │   │ │ • Read Only    │ │    │
│  │             │   │              │   │ └────────────────┘ │    │
│  │             │   │              │   │ • Add Match FAB    │    │
│  └─────────────┘   └──────────────┘   └────────┬───────────┘    │
│                                                 │                 │
└─────────────────────────────────────────────────┼─────────────────┘
                                                  │
                                                  │
                            ┌─────────────────────┴──────────┐
                            │                                │
                     ┌──────▼───────┐              ┌─────────▼────────┐
                     │ Match Detail │              │  Current Match   │
                     │   Screen     │              │     Screen       │
                     │              │              │                  │
                     │ • Add/Edit   │              │ • Active Match   │
                     │ • Opponent   │              │ • Timer          │
                     │ • Location   │              │ • Player Times   │
                     │ • Date/Time  │              │ • Substitutions  │
                     │ • Lineup     │              │                  │
                     │              │              │                  │
                     │ [← Back]     │              │ [← Back]         │
                     └──────────────┘              └──────────────────┘
```

## Screen Breakdown

### 1. Splash Screen
**Purpose**: Initial screen that determines navigation flow
- **No TopBar**: ✓
- **No BottomBar**: ✓
- **Can Go Back**: ✗
- **Duration**: Instant (as soon as team state is determined)
- **Navigation**:
  - If no team exists → CreateTeam (Splash removed from stack)
  - If team exists → Players (Splash removed from stack)

### 2. Create Team Screen
**Purpose**: First-time team setup
- **No TopBar**: ✓
- **No BottomBar**: ✓
- **Can Go Back**: ✗ (Back button closes app via BackHandler)
- **Fields**:
  - Team Name (required)
  - Coach Name (required)
  - Delegate Name (required)
- **Navigation**:
  - On Save → Players (CreateTeam removed from stack)

### 3. Players Screen (Plantilla)
**Purpose**: Team roster management
- **TopBar**: Shows team name
- **BottomBar**: ✓ (Selected: Players)
- **Can Go Back**: ✗ (One of the root screens)
- **Features**:
  - List of all players
  - Add player button (FAB)
  - Edit/Delete player actions
- **Previous Navigation Removed**: No longer has FABs for Matches or Current Match

### 4. Team Detail Screen (Equipo)
**Purpose**: View and edit team information
- **TopBar**: Shows team name
- **BottomBar**: ✓ (Selected: Team)
- **Can Go Back**: ✗ (One of the root screens)
- **Features**:
  - Displays team name
  - Displays coach name
  - Displays delegate name
  - Can edit team info (via dialog)
- **Replaces**: Previous TeamInfoDialog that was accessed via info button

### 5. Match List Screen (Partidos)
**Purpose**: View and manage all matches
- **TopBar**: Shows team name
- **BottomBar**: ✓ (Selected: Matches)
- **Can Go Back**: ✗ (One of the root screens)
- **Features**:
  - **Pending Matches Section**:
    - Shows matches not yet played (elapsedTimeMillis == 0)
    - Edit button (navigates to Match Detail)
    - Delete button (shows confirmation dialog)
    - Start button (navigates to Current Match)
    - Start button disabled if another match is active
    - Warning message shown when match is active
  - **Played Matches Section**:
    - Shows completed matches (elapsedTimeMillis > 0)
    - Displays score (0-0 placeholder for now)
    - Read-only (no edit/delete)
  - Add match button (FAB)

### 6. Match Detail Screen
**Purpose**: Create or edit match details
- **TopBar**: Shows team name with back button
- **BottomBar**: ✗
- **Can Go Back**: ✓
- **Features**:
  - Opponent name input
  - Location input
  - Date and time picker
  - Starting lineup selection
  - Substitutes selection
  - Save button
- **Navigation**:
  - Back button → Returns to Match List

### 7. Current Match Screen
**Purpose**: Active match management
- **TopBar**: Shows team name with back button
- **BottomBar**: ✗
- **Can Go Back**: ✓
- **Features**:
  - Match timer
  - Player time tracking
  - Substitution management
  - Finish match button
- **Navigation**:
  - Back button → Returns to Match List

## Bottom Navigation Bar Details

```
┌────────────────────────────────────────────────────────┐
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │   [👥]      │  │   [👥👥]    │  │   [⚽]       │    │
│  │  Plantilla  │  │   Equipo    │  │  Partidos   │    │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
└────────────────────────────────────────────────────────┘
```

**Icons Used** (Material Icons):
- Players: `Icons.Default.Group` (Single person group)
- Team: `Icons.Default.Groups` (Multiple people)
- Matches: `Icons.Default.SportsSoccer` (Soccer ball)

**Behavior**:
- Highlights current selected tab
- Preserves state when switching tabs
- Single-top launch mode (no duplicates)
- Proper back stack management

## Key Navigation Patterns

### Pattern 1: First Time User
```
App Launch → Splash → (No Team) → Create Team → [Save] → Players
              ↓                                             ↓
           (removed)                                (Create Team removed)
```

### Pattern 2: Returning User
```
App Launch → Splash → (Team Exists) → Players
              ↓
           (removed)
```

### Pattern 3: Bottom Navigation
```
Players ←→ Team Detail ←→ Matches
  ↑            ↑              ↑
  └────────────┴──────────────┘
   (State preserved, back stack managed)
```

### Pattern 4: Match Management
```
Matches → [Start Button] → Current Match → [Back] → Matches
Matches → [Edit Button]  → Match Detail → [Back] → Matches
Matches → [Add FAB]      → Match Detail → [Back] → Matches
```

## Back Button Behavior Summary

| Screen          | Back Button Action                      |
|-----------------|-----------------------------------------|
| Splash          | N/A (not accessible after navigation)   |
| Create Team     | Closes app (BackHandler)               |
| Players         | System default (exits app)             |
| Team Detail     | System default (exits app)             |
| Matches         | System default (exits app)             |
| Current Match   | Returns to Matches                     |
| Match Detail    | Returns to Matches                     |

## State Management

### Navigation State
- **Preserved**: When switching between bottom navigation tabs
- **Cleared**: When navigating to detail screens (Current Match, Match Detail)
- **Removed**: Splash and Create Team screens (after initial navigation)

### UI State
- **TeamViewModel**: Shared across app, provides team information
- **PlayerViewModel**: Used in Players screen
- **MatchListViewModel**: Used in Match List screen
- **MatchViewModel**: Used in Current Match screen
- **MatchDetailViewModel**: Used in Match Detail screen

## Material Design Compliance

✅ Material Design 3 Components Used:
- NavigationBar (Bottom Navigation)
- NavigationBarItem
- FloatingActionButton
- TopAppBar (CenterAligned)
- Card (for match items)
- Button (for actions)
- Material Icons (for navigation and actions)

✅ Material Design Principles Applied:
- Clear visual hierarchy
- Consistent iconography
- Proper touch targets
- Accessible navigation
- State-based feedback
