# US-1.1.7 Visual Guide: Edit Team Information

## User Flow

This document describes the visual flow and user interactions for editing team information.

## Step 1: Team Roster Screen with Info Button

```
┌─────────────────────────────────────┐
│  Team Name Here              ⓘ     │  ← Top bar with info button
├─────────────────────────────────────┤
│                                     │
│   [Player List Content]             │
│                                     │
│   • Player 1 - #10                  │
│   • Player 2 - #7                   │
│   • Player 3 - #9                   │
│   ...                               │
│                                     │
└─────────────────────────────────────┘
```

**Action**: User taps the info button (ⓘ) in the top bar

## Step 2: Team Info Dialog (View Mode)

```
┌─────────────────────────────────────┐
│                                     │
│  ┌───────────────────────────────┐ │
│  │                               │ │
│  │  Team Info                    │ │
│  │                               │ │
│  │  Team Name                    │ │
│  │  My Soccer Team               │ │
│  │                               │ │
│  │  Coach Name                   │ │
│  │  John Doe                     │ │
│  │                               │ │
│  │  Delegate Name                │ │
│  │  Jane Smith                   │ │
│  │                               │ │
│  │         ┌──────┐  ┌──────┐   │ │
│  │         │ Edit │  │ Close│   │ │  ← Two buttons
│  │         └──────┘  └──────┘   │ │
│  │                               │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**Action**: User taps the "Edit" button

## Step 3: Edit Team Dialog (Edit Mode)

```
┌─────────────────────────────────────┐
│                                     │
│  ┌───────────────────────────────┐ │
│  │                               │ │
│  │  Edit Team                    │ │
│  │                               │ │
│  │  Team Name                    │ │
│  │  ┌─────────────────────────┐ │ │
│  │  │ My Soccer Team          │ │ │  ← Editable field
│  │  └─────────────────────────┘ │ │
│  │                               │ │
│  │  Coach Name                   │ │
│  │  ┌─────────────────────────┐ │ │
│  │  │ John Doe                │ │ │  ← Editable field
│  │  └─────────────────────────┘ │ │
│  │                               │ │
│  │  Delegate Name                │ │
│  │  ┌─────────────────────────┐ │ │
│  │  │ Jane Smith              │ │ │  ← Editable field
│  │  └─────────────────────────┘ │ │
│  │                               │ │
│  │      ┌────────┐  ┌──────┐   │ │
│  │      │ Cancel │  │ Save │   │ │  ← Action buttons
│  │      └────────┘  └──────┘   │ │
│  │                               │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**User Actions**:
- Edit any of the three fields
- Tap "Save" to confirm changes
- Tap "Cancel" to discard changes

## Step 4a: Validation Error State

If user tries to save with empty required fields:

```
┌─────────────────────────────────────┐
│                                     │
│  ┌───────────────────────────────┐ │
│  │                               │ │
│  │  Edit Team                    │ │
│  │                               │ │
│  │  Team Name                    │ │
│  │  ┌─────────────────────────┐ │ │
│  │  │                         │ │ │  ← Empty field
│  │  └─────────────────────────┘ │ │
│  │  ⚠ Team name is required     │ │  ← Error message
│  │                               │ │
│  │  Coach Name                   │ │
│  │  ┌─────────────────────────┐ │ │
│  │  │ John Doe                │ │ │
│  │  └─────────────────────────┘ │ │
│  │                               │ │
│  │  Delegate Name                │ │
│  │  ┌─────────────────────────┐ │ │
│  │  │ Jane Smith              │ │ │
│  │  └─────────────────────────┘ │ │
│  │                               │ │
│  │      ┌────────┐  ┌──────┐   │ │
│  │      │ Cancel │  │ Save │   │ │  ← Save enabled only when valid
│  │      └────────┘  └──────┘   │ │
│  │                               │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**Validation Rules**:
- Team Name: Required (cannot be blank)
- Coach Name: Required (cannot be blank)
- Delegate Name: Required (cannot be blank)

## Step 4b: Successful Save

When user saves valid data:

```
┌─────────────────────────────────────┐
│  Updated Team Name           ⓘ     │  ← Top bar updates with new name
├─────────────────────────────────────┤
│                                     │
│   [Player List Content]             │
│                                     │
│   • Player 1 - #10                  │
│   • Player 2 - #7                   │
│   • Player 3 - #9                   │
│   ...                               │
│                                     │
└─────────────────────────────────────┘
```

- Dialog closes automatically
- Top bar title updates if team name changed
- Changes persist to database

## Interaction Details

### Dialog Behavior

**TeamInfoDialog (View Mode)**:
- Shows current team information (read-only)
- Two buttons at bottom:
  - **Edit**: Opens edit dialog
  - **Close**: Dismisses dialog
- Can also be dismissed by tapping outside

**EditTeamDialog (Edit Mode)**:
- Three editable text fields
- Real-time validation
- Two buttons at bottom:
  - **Cancel**: Closes dialog without saving
  - **Save**: Updates team and closes dialog (only enabled when valid)
- Can be dismissed by tapping outside (acts like Cancel)

### Keyboard Navigation

- Tab/Next key moves between fields
- Last field (Delegate Name) has "Done" action
- Pressing "Done" closes keyboard but doesn't submit form
- Must explicitly tap "Save" button

### Field States

**Normal State**:
```
┌─────────────────────────┐
│ Field value here        │
└─────────────────────────┘
```

**Error State**:
```
┌─────────────────────────┐ ← Red border
│                         │
└─────────────────────────┘
⚠ Field name is required    ← Red error text
```

**Focused State**:
```
┌═════════════════════════┐ ← Thicker border
│ Field value here▊       │ ← Cursor visible
└═════════════════════════┘
```

## Localization

### English Strings
- Dialog Title: "Edit Team"
- Button: "Edit"
- Button: "Save"
- Button: "Cancel"
- Error: "Team name is required"
- Error: "Coach name is required"
- Error: "Delegate name is required"

### Spanish Strings
- Dialog Title: "Editar Equipo"
- Button: "Editar"
- Button: "Guardar"
- Button: "Cancelar"
- Error: "El nombre del equipo es obligatorio"
- Error: "El nombre del entrenador es obligatorio"
- Error: "El nombre del delegado es obligatorio"

## Design Tokens Used

### Spacing
- `TFMSpacing.spacing01` (4dp): Small gaps in form items
- `TFMSpacing.spacing02` (8dp): Gap between buttons, field spacing
- `TFMSpacing.spacing04` (16dp): Vertical arrangement in dialog
- `TFMSpacing.spacing06` (24dp): Dialog padding

### Elevation
- `TFMElevation.level3`: Dialog elevation above content

### Typography
- `headlineSmall`: Dialog title
- `labelMedium`: Field labels
- `bodyLarge`: Field values (in TeamInfoDialog)
- `bodyMedium`: Text input content

### Colors
- `onSurface`: Primary text color
- `onSurfaceVariant`: Secondary text color (labels)
- `error`: Error text and borders
- `surface`: Dialog background

## Material Design Compliance

This implementation follows Material Design 3 guidelines:

1. **Dialogs**: Uses proper Material 3 dialog styling
2. **Text Fields**: Outlined text fields with floating labels
3. **Buttons**: Primary button for Save, Text button for Cancel
4. **Elevation**: Proper shadow/elevation for dialogs
5. **Colors**: Uses theme colors for consistency
6. **Typography**: Follows Material 3 type scale
7. **Spacing**: Consistent spacing using design tokens

## Accessibility Considerations

1. **Content Descriptions**: Info button has proper description
2. **Field Labels**: All fields have clear labels
3. **Error Messages**: Clear, descriptive error messages
4. **Focus Order**: Logical tab order through fields
5. **Touch Targets**: All buttons meet minimum size requirements
6. **Contrast**: Text meets WCAG contrast requirements

## Future Enhancements

Potential improvements for future iterations:

1. **Undo/Redo**: Allow reverting changes
2. **Confirmation**: Show confirmation before saving
3. **Change Tracking**: Highlight what changed
4. **Keyboard Shortcuts**: Save with Ctrl+Enter, Cancel with Escape
5. **Auto-save**: Save automatically when field loses focus
6. **History**: Show edit history
7. **More Fields**: Add team logo, colors, season, etc.
