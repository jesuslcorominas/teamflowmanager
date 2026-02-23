# C2-S6 Visual Guide - Auto-Asignación (Presidente a Coach)

## Overview
This guide illustrates the UI changes for the auto-assignment feature where Presidents can assign themselves as Coach to teams without a coach.

## Screen: Team List (President View)

### Before Implementation
```
┌─────────────────────────────────────┐
│  Team List                          │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Equipo Juvenil A        [↗]│   │ ← Share button (team has no coach)
│  │ Coach: (Sin asignar)        │   │
│  │ Delegado: Juan Pérez        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Equipo Infantil B           │   │ ← No share button (has coach)
│  │ Coach: María García         │   │
│  │ Delegado: Luis Martín       │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### After Implementation (President View)

```
┌─────────────────────────────────────┐
│  Team List                          │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Equipo Juvenil A        [↗]│   │ ← Share button still available
│  │ Coach: (Sin asignar)        │   │
│  │ Delegado: Juan Pérez        │   │
│  │                             │   │
│  │ ┌─────────────────────────┐ │   │
│  │ │ [👤] Asignarme como     │ │   │ ← NEW: Self-assign button
│  │ │      Coach               │ │   │
│  │ └─────────────────────────┘ │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Equipo Infantil B           │   │ ← No button (has coach)
│  │ Coach: María García         │   │
│  │ Delegado: Luis Martín       │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

### After Implementation (Coach/Staff View)

```
┌─────────────────────────────────────┐
│  Team List                          │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Equipo Juvenil A        [↗]│   │ ← Share button visible
│  │ Coach: (Sin asignar)        │   │
│  │ Delegado: Juan Pérez        │   │
│  └─────────────────────────────┘   │ ← NO self-assign button (not President)
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Equipo Infantil B           │   │
│  │ Coach: María García         │   │
│  │ Delegado: Luis Martín       │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

## User Flow

### Happy Path: President Assigns Self as Coach

```
┌──────────────────┐
│  1. View Teams   │
│  (President)     │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ 2. See Team      │
│ without Coach    │
│ + Button         │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ 3. Click Button  │
│ "Asignarme como  │
│ Coach"           │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ 4. Loading       │
│ Overlay Shows    │
│ [○○○ Loading]    │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ 5. Success!      │
│ Team Updates:    │
│ Coach: <Your     │
│ Name>            │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ 6. Button        │
│ Disappears       │
│ (has coach now)  │
└──────────────────┘
```

## UI Components

### Self-Assign Button

**Appearance:**
```
┌─────────────────────────────┐
│ [👤 icon] Asignarme como    │
│           Coach             │
└─────────────────────────────┘
```

**States:**

1. **Normal** (Enabled)
   - Background: Primary color (blue/green)
   - Text: White
   - Icon: PersonAdd (👤)
   - Cursor: Clickable

2. **Loading** (Disabled)
   - Background: Primary color with reduced opacity
   - Text: Gray/lighter
   - Icon: PersonAdd (👤) grayed out
   - Cursor: Not allowed
   - Full-screen overlay visible

3. **Hidden**
   - Not displayed when:
     - Team already has coach
     - User is not President
     - Team is being shared

### Loading Overlay

**Appearance:**
```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│                                     │
│           ⊚ Loading...             │ ← Circular progress
│                                     │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

**Properties:**
- Semi-transparent background (surface with 70% opacity)
- Blocks all user interaction
- Shows circular progress indicator
- Centered on screen

## Button Visibility Logic

```
Show button IF:
  ✓ User role is "Presidente"
  AND
  ✓ Team.coachId is null
  AND
  ✓ Not currently assigning/sharing this team

Hide button IF:
  ✗ User is Coach or Staff
  OR
  ✗ Team already has coach (coachId != null)
  OR
  ✗ Operation in progress
```

## Error Handling

### Scenario: Assignment Fails

```
┌──────────────────┐
│  Error occurs    │
│  during API call │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ Loading overlay  │
│ disappears       │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ Button returns   │
│ to normal state  │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ [Future] Show    │
│ error toast      │
│ (TODO)           │
└──────────────────┘
```

## Accessibility Features

### Screen Reader Support
- Button: "Asignarme como Coach"
- Icon: PersonAdd with null contentDescription (text is sufficient)
- Loading state: Progress indicator announced

### Visual Indicators
- ✓ Button disabled state shows reduced opacity
- ✓ Loading overlay prevents accidental clicks
- ✓ Clear visual feedback during operation

### Keyboard Support
- ✓ Button is focusable
- ✓ Can be activated with Enter/Space

## Material Design 3 Compliance

### Typography
- Button text: Material 3 Button component default
- Card title: titleMedium, Bold
- Card details: bodyMedium

### Colors
- Button: Primary color scheme
- Icon: Primary color
- Loading indicator: Primary color
- Disabled state: onSurface with alpha

### Spacing
- Button padding top: 12dp
- Button full width
- Icon size: 20dp
- Icon-text spacing: 8dp

## Animation (Future Enhancement)

Potential animations to add:
1. Button fade-in when team loads
2. Smooth transition to loading overlay
3. Success animation on completion
4. Team card update animation

## Responsive Design

### Phone (Portrait)
- Button full width within card
- Text wraps if needed
- Icon maintains size

### Tablet (Landscape)
- Same layout (button still full width of card)
- Cards may be wider but button adapts

## Comparison: Share vs Self-Assign

### Share Button (Existing)
- Location: Top-right of card (IconButton)
- Icon: Share (↗)
- Visible when: team.coachId == null
- Action: Generates invitation link

### Self-Assign Button (New)
- Location: Bottom of card (Full-width Button)
- Icon: PersonAdd (👤)
- Visible when: President AND team.coachId == null
- Action: Assigns current user as coach

Both can be visible simultaneously on same team card!

## Testing Checklist

Visual testing to perform:
- [ ] Button appears for President on teams without coach
- [ ] Button does NOT appear for Coach/Staff
- [ ] Button does NOT appear when team has coach
- [ ] Loading overlay appears on click
- [ ] Button is disabled during loading
- [ ] Team card updates after successful assignment
- [ ] Button disappears after successful assignment
- [ ] Share button remains visible during assignment
- [ ] Button styling matches Material Design 3
- [ ] Icon is properly aligned with text
- [ ] Works on different screen sizes

---

**Document Status**: Final  
**Last Updated**: 2026-02-02  
**Related Issue**: C2-S6 - Auto-Asignación (Presidente a Coach)
