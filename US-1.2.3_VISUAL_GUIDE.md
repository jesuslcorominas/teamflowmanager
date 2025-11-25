# US-1.2.3: Visualización en Tiempo Real - Guía Visual

## 🎯 Descripción de la Funcionalidad

Esta funcionalidad permite al entrenador visualizar en tiempo real:
- El tiempo total del partido
- El tiempo acumulado de cada jugador
- Qué jugadores tienen sus cronómetros activos
- Todo se actualiza automáticamente cada segundo

## 📱 Flujo de Navegación

```
┌──────────────────┐
│   TeamScreen     │  (Crear/Ver Equipo)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  PlayersScreen   │  (Gestionar Jugadores)
│                  │
│  [+] [▶]        │  ← Nuevo botón de play
└────────┬─────────┘
         │
         ▼ (Tap en ▶)
┌──────────────────┐
│  SessionScreen   │  (Visualizar Tiempos)
│                  │
│  ⏱️ Tiempo Match │
│  👥 Jugadores    │
└──────────────────┘
```

## 🎨 Diseño de la Pantalla SessionScreen

### Layout Completo

```
┌─────────────────────────────────────────┐
│         Team Name             [ℹ️]      │  ← TopBar (existente)
├─────────────────────────────────────────┤
│                                         │
│  ┌───────────────────────────────────┐ │
│  │      🏆 Tiempo de Partido         │ │
│  │                                   │ │
│  │          15:30 [ACTIVO]          │ │  ← Match Time Card
│  │                                   │ │
│  └───────────────────────────────────┘ │
│                                         │
│  Tiempos de Jugadores                  │  ← Título
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ John Doe         07:45 [ACTIVO]  │ │  ← Player 1 (activo)
│  │ #10                               │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Jane Smith       08:00           │ │  ← Player 2 (pausado)
│  │ #8                                │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Mike Johnson     00:00           │ │  ← Player 3 (sin tiempo)
│  │ #5                                │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ...más jugadores...                   │
│                                         │
└─────────────────────────────────────────┘
```

### Componentes Detallados

#### 1. Match Time Card (Tarjeta Grande Superior)

**Aspecto Visual:**
```
┌─────────────────────────────────────────┐
│      🏆 Tiempo de Partido               │
│                                         │
│          15:30 [ACTIVO]                │
│                                         │
└─────────────────────────────────────────┘
```

**Características:**
- **Color de fondo**: Primary Container (azul claro en tema claro)
- **Tiempo**: Display Medium font, Bold
- **Badge "ACTIVO"**: Fondo rojo (error color) cuando el cronómetro está corriendo
- **Elevación**: 4dp shadow
- **Padding**: 16dp interno

**Estados:**
- **Running**: Muestra badge "ACTIVO", tiempo se actualiza cada segundo
- **Paused**: Sin badge, tiempo estático

#### 2. Player Time Cards (Tarjetas de Jugadores)

**Jugador con Cronómetro Activo:**
```
┌─────────────────────────────────────────┐
│ John Doe                 07:45 [ACTIVO] │
│ #10                                     │
└─────────────────────────────────────────┘
```

**Características:**
- **Color de fondo**: Secondary Container (verde/morado claro) cuando activo
- **Color de fondo**: Surface (blanco/gris) cuando pausado
- **Nombre**: Body Large, Medium weight
- **Número**: Body Small, Surface Variant color
- **Tiempo**: Title Large, Bold
- **Badge "ACTIVO"**: Igual que match card
- **Elevación**: 2dp shadow
- **Padding**: 16dp interno

**Jugador sin Cronómetro:**
```
┌─────────────────────────────────────────┐
│ Mike Johnson             00:00          │
│ #5                                      │
└─────────────────────────────────────────┘
```

- Muestra 00:00
- No tiene badge
- Fondo Surface normal

## 🎨 Paleta de Colores

### Tema Claro
- **Match Card Fondo**: Primary Container (#EAF4FF o similar)
- **Active Player Card**: Secondary Container (#E8F5E9 o similar)
- **Inactive Player Card**: Surface (#FFFFFF)
- **Badge Activo**: Error (#B3261E rojo)
- **Texto Principal**: OnSurface (#1C1B1F negro)

### Tema Oscuro
- **Match Card Fondo**: Primary Container (azul oscuro)
- **Active Player Card**: Secondary Container (verde oscuro)
- **Inactive Player Card**: Surface (gris oscuro)
- **Badge Activo**: Error (rojo claro)
- **Texto Principal**: OnSurface (blanco)

## ⚡ Comportamiento en Tiempo Real

### Actualización Automática

```
Segundo 0:  15:30 [ACTIVO]
            ↓
Segundo 1:  15:31 [ACTIVO]  ← Automático
            ↓
Segundo 2:  15:32 [ACTIVO]  ← Automático
            ↓
Segundo 3:  15:33 [ACTIVO]  ← Automático
```

**Cómo funciona:**
1. ViewModel tiene un coroutine que se ejecuta cada segundo
2. Actualiza `_currentTime.value = System.currentTimeMillis()`
3. Esto dispara recálculo en el `combine` operator
4. UI se actualiza automáticamente vía `collectAsState()`

**Sin intervención del usuario**: Todo es automático y reactivo

## 🔄 Estados de la Pantalla

### Estado 1: Loading (Cargando)

```
┌─────────────────────────────────────────┐
│                                         │
│              🔄 Loading...              │
│                                         │
└─────────────────────────────────────────┘
```

### Estado 2: No Match (Sin Partido)

```
┌─────────────────────────────────────────┐
│                                         │
│       No hay partido activo            │
│                                         │
└─────────────────────────────────────────┘
```

### Estado 3: Success (Con Datos)

Ver "Layout Completo" arriba

## 📊 Ejemplo Completo con Datos Reales

```
═════════════════════════════════════════
   Barcelona Juvenil               [ℹ️]
═════════════════════════════════════════

┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃      🏆 Tiempo de Partido          ┃
┃                                    ┃
┃          45:23 [ACTIVO]           ┃
┃                                    ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛

Tiempos de Jugadores

┌────────────────────────────────────┐
│ Carlos Pérez      22:45 [ACTIVO]  │  ← Jugando ahora
│ #10                                │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│ María García      22:38           │  ← Jugó, ahora en banca
│ #7                                 │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│ Juan Martínez     15:20 [ACTIVO]  │  ← Jugando ahora
│ #5                                 │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│ Ana López         07:15           │  ← Jugó poco tiempo
│ #3                                 │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│ Pedro Sánchez     00:00           │  ← No ha jugado
│ #2                                 │
└────────────────────────────────────┘

... 6 jugadores más ...
```

## 🎯 Casos de Uso Específicos

### Caso 1: Inicio de Partido

**Entrenador inicia el partido:**
1. Match timer empieza: `00:00 → 00:01 → 00:02...`
2. Todos los jugadores muestran `00:00`
3. Entrenador selecciona jugadores iniciales y les inicia el cronómetro
4. Esos jugadores ahora muestran badge `[ACTIVO]`

### Caso 2: Sustitución

**Entrenador hace cambio (Min 20):**
1. Jugador A: `20:00 [ACTIVO]` → Pausa → `20:00` (sin badge)
2. Jugador B: `00:00` → Inicia → `00:00 [ACTIVO]`
3. Ambos tiempos siguen visible en la lista

### Caso 3: Fin de Primera Parte

**Entrenador pausa el partido (Min 45):**
1. Match: `45:00 [ACTIVO]` → Pausa → `45:00` (sin badge)
2. Todos los jugadores activos se pausan
3. Tiempos quedan congelados hasta segundo tiempo

### Caso 4: Revisión Durante Partido

**Entrenador revisa tiempos a mitad de partido:**
1. Entra a SessionScreen
2. Ve Match: `23:15 [ACTIVO]`
3. Ve que Juan tiene `23:15 [ACTIVO]` (ha jugado todo)
4. Ve que Pedro tiene `08:30` (jugó al inicio, ahora en banca)
5. Ve que Ana tiene `00:00` (aún no ha jugado)
6. **Decisión**: Pone a Ana y saca a Juan para equilibrar tiempos

## 🎓 Tips para el Entrenador

### Interpretación de los Datos

**Objetivo común**: Todos los jugadores deben jugar tiempo similar

```
Ideal en partido de 60 min con 11 jugadores:
- Jugadores: ~30 min cada uno
- Rotaciones frecuentes para equilibrar

Señales de alerta:
❌ Diferencia > 15 min entre jugadores
❌ Jugadores con 00:00 después de 30 min de partido
✅ Tiempos entre 20-40 min = Bien distribuido
```

### Colores como Ayuda Visual

- **Verde/Azul claro** (Secondary Container) = Jugador activo EN CAMPO
- **Blanco/Gris** (Surface) = Jugador en BANCA
- **Badge Rojo "ACTIVO"** = Cronómetro CORRIENDO

**Vista rápida**: Count de badges rojos = jugadores en campo

## 🔧 Aspectos Técnicos (Para Desarrolladores)

### Formato de Tiempo

**Función**: `formatTime(timeMillis: Long): String`

```kotlin
Examples:
- 0 ms       → "00:00"
- 30000 ms   → "00:30"
- 60000 ms   → "01:00"
- 90000 ms   → "01:30"
- 3600000 ms → "60:00"
```

**Formato**: Siempre MM:SS con padding de ceros

### Cálculo en Tiempo Real

**Para timers ACTIVOS**:
```kotlin
currentTime = elapsedTime + (System.currentTimeMillis() - lastStartTime)
```

**Para timers PAUSADOS**:
```kotlin
currentTime = elapsedTime
```

**Ventaja**: No se escribe en BD cada segundo, solo cuando se pausa/inicia

## 📝 Strings Utilizados

### Español (values-es/)
- `session_title` = "Sesión de Partido"
- `no_match_message` = "No hay partido activo"
- `match_time_label` = "Tiempo de Partido"
- `player_times_title` = "Tiempos de Jugadores"
- `running_indicator` = "ACTIVO"
- `player_number_format` = "#%d"

### Inglés (values/)
- `session_title` = "Match Session"
- `no_match_message` = "No active match"
- `match_time_label` = "Match Time"
- `player_times_title` = "Player Times"
- `running_indicator` = "ACTIVE"
- `player_number_format` = "#%d"

## ✨ Mejoras Futuras (No Implementadas)

1. **Filtros**: Mostrar solo jugadores activos
2. **Ordenamiento**: Por tiempo (más/menos), nombre, número
3. **Estadísticas**: Promedio, min, max
4. **Alertas**: Notificar cuando diferencia > X minutos
5. **Gráficos**: Visualización de distribución de tiempo
6. **Historial**: Ver sesiones anteriores
7. **Export**: Guardar datos en CSV/PDF

## 🎬 Flujo Completo de Usuario

```
1. Entrenador abre app
   ↓
2. Ve lista de jugadores
   ↓
3. Toca botón [▶] (play)
   ↓
4. Ve SessionScreen con todos los tiempos
   ↓
5. Observa en tiempo real mientras partido transcurre
   ↓
6. Identifica quién necesita más/menos tiempo
   ↓
7. Toma decisiones de sustitución basadas en datos
   ↓
8. Resultado: Distribución equitativa de minutos
```

## 📱 Responsive Design

- **LazyColumn**: Scroll eficiente para muchos jugadores
- **Cards**: Tamaño fijo, contenido adaptable
- **Texto**: Escala según configuración del sistema
- **Spacing**: Usa tokens de diseño consistentes (TFMSpacing)

---

**Estado de Implementación**: ✅ COMPLETO

La funcionalidad está lista para usar. El entrenador puede navegar a SessionScreen y ver todos los tiempos actualizándose en tiempo real cada segundo.
