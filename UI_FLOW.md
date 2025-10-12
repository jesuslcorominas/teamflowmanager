# Match Pause/Resume UI Flow

## Screen States

### 1. Main Screen
```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│        Welcome to TeamFlow          │
│            Manager                  │
│                                     │
│     ┌─────────────────────┐        │
│     │ View Match Detail   │        │
│     │      (Demo)         │        │
│     └─────────────────────┘        │
│                                     │
└─────────────────────────────────────┘
```

### 2. Match Detail Screen - In Progress
```
┌─────────────────────────────────────┐
│ Match Detail                        │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Opponent: Opponent Team         │ │
│ │                                 │ │
│ │ Status: IN PROGRESS             │ │
│ │                                 │ │
│ │ First Half: 00:00               │ │
│ │ Second Half: 00:00              │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │         Descanso                │ │ ← Pause Button
│ └─────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

### 3. Match Detail Screen - Paused
```
┌─────────────────────────────────────┐
│ Match Detail                        │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Opponent: Opponent Team         │ │
│ │                                 │ │
│ │ Status: PAUSED                  │ │
│ │                                 │ │
│ │ First Half: 00:00               │ │
│ │ Second Half: 00:00              │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │         Resume                  │ │ ← Resume Button
│ └─────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

## User Flow

1. User launches app → Main Screen is shown
2. User clicks "View Match Detail (Demo)" → Match Detail Screen shown with match IN_PROGRESS
3. User clicks "Descanso" button → Match is paused
   - All timers stop
   - Status changes to PAUSED
   - Button changes to "Resume"
4. User clicks "Resume" button → Match resumes
   - Status changes to IN_PROGRESS
   - Button changes back to "Descanso"

## Key Features

✅ **Pause Button ("Descanso")**: Only shown when match is IN_PROGRESS
✅ **Resume Button**: Only shown when match is PAUSED  
✅ **Status Display**: Shows current match status
✅ **Time Tracking**: Shows first and second half durations
✅ **Player Timers**: All active player timers are paused when match is paused

## Technical Implementation

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture (Domain, Use Case, Data, Presentation)
- **DI**: Koin
- **State Management**: ViewModel + LiveData
- **Async**: Kotlin Coroutines
