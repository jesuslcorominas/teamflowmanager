# US-3.2.3: Export Feature - Visual Guide

## 📱 User Interface

### Analysis Screen with Export Button

The export button appears as a Floating Action Button (FAB) in the bottom-right corner of the Analysis screen:

```
╔═══════════════════════════════════════════╗
║  TeamFlow Manager                         ║
╠═══════════════════════════════════════════╣
║                                           ║
║  ┌─────────────┬─────────────────┐        ║
║  │  TIEMPOS    │  GOLEADORES     │        ║
║  └─────────────┴─────────────────┘        ║
║                                           ║
║  ┌─────────────────────────────────────┐  ║
║  │                                     │  ║
║  │   📊 [Bar Chart Display]           │  ║
║  │                                     │  ║
║  │   Juan Pérez    ████████████ 450m  │  ║
║  │   María García  ██████████   380m  │  ║
║  │   Carlos López  ███████████  420m  │  ║
║  │                                     │  ║
║  └─────────────────────────────────────┘  ║
║                                           ║
║                                  ┌────┐   ║
║                                  │ 📤 │   ║ ← Export Button
║                                  └────┘   ║
╚═══════════════════════════════════════════╝
```

## 📄 Generated PDF Document

### Document Structure

The exported PDF contains multiple sections across pages:

#### **Page 1: Title and Player Statistics**

```
╔════════════════════════════════════════════════════════╗
║                                                        ║
║          Estadísticas del Equipo                       ║
║              Los Dragones FC                           ║
║            Fecha: 30/10/2025                          ║
║                                                        ║
║  ─────────────────────────────────────────────────    ║
║  Estadísticas de Jugadores                            ║
║  ─────────────────────────────────────────────────    ║
║                                                        ║
║  Jugador        Conv  Jug  T.Tot  T.Med  Goles       ║
║  ───────────────────────────────────────────────      ║
║  Juan Pérez      12    10   450m   45m    5          ║
║  María García    10     8   380m   47m    3          ║
║  Carlos López    11     9   420m   46m    2          ║
║  Ana Martínez    12    11   480m   43m    1          ║
║  Luis Rodríguez  10     7   320m   45m    1          ║
║  Elena Torres    11     9   400m   44m    0          ║
║  Pedro Sánchez    9     6   280m   46m    0          ║
║  Laura González  12    10   440m   44m    0          ║
║  ...                                                  ║
║                                                        ║
╚════════════════════════════════════════════════════════╝
```

**Column Explanations:**
- **Jugador**: Player full name (First name + Last name)
- **Conv**: Number of matches the player was called up for (convocatoria)
- **Jug**: Number of matches the player actually played in
- **T.Tot**: Total time played across all matches (in minutes)
- **T.Med**: Average time per match (in minutes)
- **Goles**: Total goals scored by the player

#### **Page 2: Top Scorers and Match Results**

```
╔════════════════════════════════════════════════════════╗
║                                                        ║
║  ─────────────────────────────────────────────────    ║
║  Goleadores                                           ║
║  ─────────────────────────────────────────────────    ║
║                                                        ║
║  1. Juan Pérez - 5 gol(es)                           ║
║  2. María García - 3 gol(es)                         ║
║  3. Carlos López - 2 gol(es)                         ║
║  4. Ana Martínez - 1 gol(es)                         ║
║  5. Luis Rodríguez - 1 gol(es)                       ║
║                                                        ║
║  ─────────────────────────────────────────────────    ║
║  Resultados de Partidos                               ║
║  ─────────────────────────────────────────────────    ║
║                                                        ║
║  15/01/2025 | Tigres FC (3-2) - Estadio Municipal   ║
║  22/01/2025 | Leones FC (1-1) - Polideportivo Sur   ║
║  29/01/2025 | Águilas FC (4-0) - Estadio Municipal  ║
║  05/02/2025 | Panteras FC (2-3) - Campo Norte       ║
║  12/02/2025 | Lobos FC (3-1) - Estadio Municipal    ║
║  19/02/2025 | Halcones FC (2-2) - Complejo Este     ║
║  26/02/2025 | Búhos FC (5-1) - Estadio Municipal    ║
║  ...                                                  ║
║                                                        ║
╚════════════════════════════════════════════════════════╝
```

**Match Results Format:**
- Date in DD/MM/YYYY format
- Opponent team name
- Score (Team Goals - Opponent Goals)
- Location/Venue name
- Ordered chronologically to show team evolution

## 📤 Share Flow

### Step-by-Step User Experience

#### **Step 1: User taps Export button**
```
┌────────────────────────────┐
│  Analysis Screen           │
│                            │
│  [Statistics displayed]    │
│                            │
│                    ┌────┐  │
│  User taps →       │ 📤 │  │
│                    └────┘  │
└────────────────────────────┘
```

#### **Step 2: Loading State (brief)**
```
┌────────────────────────────┐
│                            │
│      ⏳ Generating PDF...  │
│                            │
└────────────────────────────┘
```

#### **Step 3: Android Share Sheet Appears**
```
╔═══════════════════════════════════════╗
║  Compartir estadísticas               ║
╠═══════════════════════════════════════╣
║                                       ║
║  📧  Email                            ║
║  ─────────────────────────────────    ║
║                                       ║
║  💬  WhatsApp                         ║
║  ─────────────────────────────────    ║
║                                       ║
║  📁  Google Drive                     ║
║  ─────────────────────────────────    ║
║                                       ║
║  📱  Bluetooth                        ║
║  ─────────────────────────────────    ║
║                                       ║
║  📋  Copy to clipboard                ║
║  ─────────────────────────────────    ║
║                                       ║
║  👥  More apps...                     ║
║                                       ║
╚═══════════════════════════════════════╝
```

#### **Step 4: User Selects Sharing Method**

For example, if user selects **WhatsApp**:

```
╔═══════════════════════════════════════╗
║  WhatsApp                             ║
╠═══════════════════════════════════════╣
║                                       ║
║  Search or select contact             ║
║  ┌───────────────────────────────┐    ║
║  │ 🔍 Search...                  │    ║
║  └───────────────────────────────┘    ║
║                                       ║
║  Recent Chats:                        ║
║  👥 Equipo Los Dragones               ║
║  👤 Entrenador Principal              ║
║  👤 Padre de Juan                     ║
║  ...                                  ║
║                                       ║
║  📎 estadisticas_1234567890.pdf       ║
║                                       ║
║              [ Send ]                 ║
╚═══════════════════════════════════════╝
```

## 💾 Technical Details

### File Naming
- Format: `estadisticas_[timestamp].pdf`
- Example: `estadisticas_1730288684123.pdf`

### File Location
- Stored in app's cache directory
- Automatically cleaned by system
- Shared via FileProvider with secure URI

### File Size
- Varies based on data volume
- Typical: 20-100 KB for average team
- Multiple pages as needed

## 📊 Data Examples

### Example Player Statistics

| Jugador | Conv | Jug | T.Tot | T.Med | Goles |
|---------|------|-----|-------|-------|-------|
| Juan Pérez | 12 | 10 | 450m | 45m | 5 |
| María García | 10 | 8 | 380m | 47m | 3 |
| Carlos López | 11 | 9 | 420m | 46m | 2 |

**Interpretation:**
- Juan was called up for 12 matches
- Actually played in 10 matches
- Total playing time: 450 minutes (7.5 hours)
- Average per match: 45 minutes
- Scored 5 goals

### Example Match Results

| Date | Opponent | Score | Location |
|------|----------|-------|----------|
| 15/01/2025 | Tigres FC | 3-2 | Estadio Municipal |
| 22/01/2025 | Leones FC | 1-1 | Polideportivo Sur |
| 29/01/2025 | Águilas FC | 4-0 | Estadio Municipal |

Shows the team's progression through the season.

## 🎯 Use Cases

### For Coaches
- Share statistics with team management
- Send reports to parents
- Keep records for season analysis
- Compare player development

### For Parents
- Track their child's participation
- See team performance
- Share with family members
- Keep as memorabilia

### For Team Management
- Season reports
- Budget justification
- Tournament applications
- Historical records

## ✨ Key Features

1. **One-Tap Export**: Single button press to generate and share
2. **Professional Format**: Clean, readable PDF document
3. **Comprehensive Data**: All key statistics in one file
4. **Easy Sharing**: Native Android share to any app
5. **Bilingual**: Spanish labels (primary) with English support
6. **Automatic Dating**: Current date included
7. **Team Branding**: Team name prominently displayed
8. **Chronological Order**: Matches show team evolution

## 🔒 Security & Privacy

- Files stored in app's private cache
- Shared via secure FileProvider
- Read-only permission granted
- No permanent storage unless user saves
- No cloud upload without user action

## ✅ Success Indicators

User will know the feature works when:
1. ✅ Export button appears and responds to tap
2. ✅ Brief loading indicator shows
3. ✅ Android share sheet appears
4. ✅ Can select any sharing app
5. ✅ PDF opens correctly in selected app
6. ✅ PDF contains accurate, formatted data

---

**Implementation Status**: ✅ Complete and Ready for Testing

All UI components, data flows, and sharing mechanisms have been implemented according to the user story requirements.
