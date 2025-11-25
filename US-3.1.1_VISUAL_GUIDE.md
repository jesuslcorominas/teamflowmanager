# US-3.1.1: Guía Visual - Visualización del Tiempo de Juego

## Navegación - Bottom Bar

La nueva pestaña "Análisis" se encuentra en la barra de navegación inferior:

```
┌────────────────────────────────────────────────────────────┐
│                     Top Bar                                │
│                    "Análisis"                              │
└────────────────────────────────────────────────────────────┘
│                                                            │
│                     (Contenido)                            │
│                                                            │
│                                                            │
│                                                            │
│                                                            │
┌────────────────────────────────────────────────────────────┐
│  ⚽ Partidos  │  👥 Jugadores  │  📊 Análisis  │  👥 Equipo │
│              │                │   [SELECTED]  │            │
└────────────────────────────────────────────────────────────┘
```

## Pantalla de Análisis - Layout Completo

```
┌────────────────────────────────────────────────────────────┐
│  ← Análisis                                                │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  TIEMPOS                                                   │
│                                                            │
│  Distribución del Tiempo de Juego                         │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Juan Pérez                             145 min       │ │
│  │ ████████████████████████████████████░░░  12 partidos │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ María González                         132 min       │ │
│  │ ███████████████████████████████░░░░░░░   11 partidos │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Carlos Rodríguez                       118 min       │ │
│  │ ██████████████████████████░░░░░░░░░░░    10 partidos │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Ana Martínez                            95 min       │ │
│  │ ███████████████████░░░░░░░░░░░░░░░░░░     8 partidos │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  (más jugadores...)                                        │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

## Detalle de un elemento de la lista

### Componente individual (Player Time Bar)

```
┌────────────────────────────────────────────────────────────┐
│ Juan Pérez                                      145 min    │  ← Nombre y tiempo total
│ ████████████████████████████████████░░░░░   12 partidos   │  ← Barra + partidos jugados
└────────────────────────────────────────────────────────────┘
   ↑                                             ↑
   Barra azul proporcional                    Info adicional
   al tiempo total (Primary color)
```

### Estructura del componente:
```
Row (horizontal)
├─ Text: "Juan Pérez" (weight=1f, expandible)
└─ Text: "145 min" (bold, color Primary)

Row (horizontal)  
├─ Box (weight=1f, expandible)
│  └─ Canvas
│     └─ RoundRect (width proporcional, color Primary)
└─ Text: "12 partidos" (small, gray, width=80dp)
```

## Estados de la pantalla

### Estado: Loading
```
┌────────────────────────────────────────────────────────────┐
│  ← Análisis                                                │
├────────────────────────────────────────────────────────────┤
│                                                            │
│                                                            │
│                                                            │
│                         ⏳                                 │
│                    Cargando...                             │
│                                                            │
│                                                            │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Estado: Empty (Sin datos)
```
┌────────────────────────────────────────────────────────────┐
│  ← Análisis                                                │
├────────────────────────────────────────────────────────────┤
│                                                            │
│                                                            │
│                         📊                                 │
│                                                            │
│         No hay datos de tiempo de juego disponibles        │
│                                                            │
│                                                            │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Estado: Success (Con datos)
```
┌────────────────────────────────────────────────────────────┐
│  ← Análisis                                                │
├────────────────────────────────────────────────────────────┤
│  TIEMPOS                                     ↑             │
│                                              │             │
│  Distribución del Tiempo de Juego           │             │
│                                              │   Scroll    │
│  ┌────────────────────────────────────┐     │   vertical  │
│  │ Jugador 1     ...min               │     │             │
│  │ ████████████████░░░░  X partidos   │     │             │
│  └────────────────────────────────────┘     │             │
│  ┌────────────────────────────────────┐     │             │
│  │ Jugador 2     ...min               │     │             │
│  │ ███████████░░░░░░░░░  X partidos   │     │             │
│  └────────────────────────────────────┘     │             │
│  ... más jugadores ...                      ↓             │
└────────────────────────────────────────────────────────────┘
```

## Flujo de interacción del usuario

### Caso de uso principal:

```
1. Usuario abre la app → Pantalla de Partidos
   ┌─────────────┐
   │ ⚽ PARTIDOS  │
   └─────────────┘
        ↓
2. Usuario toca tab "Análisis" en bottom bar
   ┌─────────────┐
   │ 📊 ANÁLISIS │
   └─────────────┘
        ↓
3. App carga datos de tiempo de juego
   [Loading State]
        ↓
4. App muestra gráfico con jugadores ordenados
   [Success State]
   - Jugador con más minutos arriba
   - Jugador con menos minutos abajo
        ↓
5. Usuario puede scroll para ver todos los jugadores
        ↓
6. Usuario puede volver a otras pestañas desde bottom bar
```

## Comparación visual de barras

Las barras son proporcionales al tiempo máximo:

```
Jugador con MÁS tiempo (145 min):
████████████████████████████████████████████  100% del ancho

Jugador con 75% del tiempo (109 min):
█████████████████████████████████░░░░░░░░░░░   75% del ancho

Jugador con 50% del tiempo (72 min):
██████████████████████░░░░░░░░░░░░░░░░░░░░░░   50% del ancho

Jugador con 25% del tiempo (36 min):
██████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   25% del ancho
```

## Colores y estilos

### Paleta de colores utilizada:

```
Barra de tiempo:          #003366 (Primary - Azul marino)
Texto de minutos:         #003366 (Primary - Azul marino, Bold)
Nombre del jugador:       #001933 (ContentMain - Casi negro)
Info de partidos:         onSurfaceVariant (Gris)
Fondo:                    #FFFFFF (White)
```

### Tipografía:

```
"TIEMPOS":                    headlineSmall, Bold
"Distribución...":            titleMedium, SemiBold
Nombre del jugador:           bodyMedium, Medium
Tiempo (min):                 bodyMedium, Bold
Partidos jugados:             bodySmall, 11sp
```

## Diseño responsive

### En dispositivos con pantallas pequeñas:
- Scroll vertical habilitado
- Barras mantienen proporciones
- Texto truncado si es necesario
- Espaciado consistente

### En dispositivos con pantallas grandes:
- Más jugadores visibles sin scroll
- Barras más largas y fáciles de comparar
- Mejor visualización de diferencias

## Datos mostrados

### Por cada jugador:
```
┌─────────────────────────────────────────────────┐
│ Información mostrada:                           │
│                                                 │
│ • Nombre completo: [firstName] [lastName]       │
│ • Tiempo total: [totalTimeMillis / 60000] min   │
│ • Partidos jugados: [matchesPlayed]             │
│ • Barra visual: proporcional al máximo          │
└─────────────────────────────────────────────────┘
```

### Cálculos:
```
Tiempo en minutos = totalTimeMillis ÷ 1000 ÷ 60
Porcentaje de barra = totalTimeMillis ÷ maxTimeMillis × 100%
Partidos jugados = COUNT(DISTINCT matchId)
```

## Integración con el sistema existente

### Antes (3 pestañas):
```
┌─────────────────────────────────────────────────┐
│  ⚽ Partidos  │  👥 Jugadores  │  👥 Equipo      │
└─────────────────────────────────────────────────┘
```

### Después (4 pestañas):
```
┌──────────────────────────────────────────────────────────┐
│  ⚽ Partidos  │  👥 Jugadores  │  📊 Análisis  │  👥 Equipo│
└──────────────────────────────────────────────────────────┘
```

## Comportamiento del back button

```
Desde Análisis:
[Análisis] → Press Back → [Partidos]

Desde otras pestañas:
[Partidos] → Press Back → Cierra app
[Jugadores] → Press Back → [Partidos]
[Equipo] → Press Back → [Partidos]
```

## Escenarios de prueba visual

### ✅ Escenario 1: Jugadores con tiempo similar
```
Juan     (120 min): ████████████████████████████████████████
Pedro    (118 min): ███████████████████████████████████████░
María    (115 min): ██████████████████████████████████████░░
```
→ Barras muy similares, fácil de comparar

### ✅ Escenario 2: Gran diferencia de tiempos
```
Juan     (200 min): ████████████████████████████████████████
Pedro     (50 min): ██████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
María     (25 min): █████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
```
→ Diferencia claramente visible

### ✅ Escenario 3: Un solo jugador
```
Juan     (100 min): ████████████████████████████████████████
```
→ Barra al 100% del ancho disponible

### ✅ Escenario 4: Sin jugadores
```
                    No hay datos disponibles
```
→ Mensaje de estado vacío

## Accesibilidad

- ✅ Contraste de colores adecuado (azul oscuro sobre blanco)
- ✅ Texto legible (tamaños apropiados)
- ✅ Información redundante (barra visual + texto numérico)
- ✅ Scroll suave para navegación
- ✅ Nombres completos de jugadores visibles

## Mejoras futuras potenciales

- Filtro por rango de fechas (mes, temporada)
- Gráfico de pastel alternativo
- Estadísticas adicionales (promedio por partido)
- Comparación entre jugadores
- Exportar datos

---

**Nota**: Los caracteres Unicode y ASCII art son aproximaciones. 
La implementación real usa Compose Canvas con renderizado vectorial.
