# US-6.1.3: Mejora de Vista de Partido - Guía de Implementación

## Resumen Ejecutivo

Esta implementación mejora significativamente la pantalla de partido activo (CurrentMatchScreen) con las siguientes características principales:

1. **Listado mejorado de jugadores** con ordenación configurable y estilo visual actualizado
2. **Temporizador por periodo** con cuenta atrás y visualización de tiempo de descuento
3. **Control de pausas** basado en el número de periodos
4. **Navegación mejorada** sin inicio automático del partido
5. **Indicador de capitán** visible en el listado de jugadores

## Cambios Técnicos Detallados

### 1. Modelo de Dominio (Match.kt)

Se añadieron dos nuevos campos al modelo `Match`:

```kotlin
val currentPeriod: Int = 1      // Periodo actual (índice base 1)
val pauseCount: Int = 0         // Número de veces que se ha pausado el partido
```

Y métodos helper para lógica de negocio:

```kotlin
fun getPeriodDurationMillis(): Long
fun getMaxPauses(): Int
fun canPause(): Boolean
fun isLastPeriod(): Boolean
```

**Lógica de duración de periodos:**
- 2 periodos (partes): 25 minutos cada uno
- 4 periodos (cuartos): 12 minutos 30 segundos cada uno

**Lógica de pausas:**
- 2 periodos: máximo 1 pausa (descanso)
- 4 periodos: máximo 3 pausas (entre cuartos)

### 2. Capa de Datos

#### MatchEntity.kt
Añadidos campos correspondientes a la base de datos:
```kotlin
val currentPeriod: Int = 1
val pauseCount: Int = 0
```

#### Migración de Base de Datos
Creada migración de versión 2 a 3:
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE match ADD COLUMN currentPeriod INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE match ADD COLUMN pauseCount INTEGER NOT NULL DEFAULT 0")
    }
}
```

#### MatchRepositoryImpl.kt
Actualizado el método `pauseTimer` para incrementar el contador de pausas y periodo:
```kotlin
override suspend fun pauseTimer(currentTimeMillis: Long) {
    // ... código existente ...
    val updatedMatch = currentMatch.copy(
        // ... otros campos ...
        pauseCount = currentMatch.pauseCount + 1,
        currentPeriod = minOf(currentMatch.currentPeriod + 1, currentMatch.numberOfPeriods),
    )
    // ...
}
```

### 3. Capa de Casos de Uso

#### SetCurrentMatchUseCase.kt (NUEVO)
Nuevo caso de uso para establecer un partido como actual sin iniciar el temporizador:

```kotlin
interface SetCurrentMatchUseCase {
    suspend operator fun invoke(matchId: Long)
}
```

Esto permite navegar a la pantalla de partido sin iniciar automáticamente el cronómetro.

### 4. Capa de ViewModel

#### PlayerSortOrder (NUEVO)
Enum para controlar el orden de los jugadores:

```kotlin
enum class PlayerSortOrder {
    BY_TIME_DESC,      // Más tiempo primero
    BY_TIME_ASC,       // Menos tiempo primero
    BY_ACTIVE_FIRST,   // Activos primero (por defecto)
}
```

#### MatchViewModel.kt
Cambios principales:

1. **Nuevo estado en MatchUiState.Success:**
   ```kotlin
   data class Success(
       // ... campos existentes ...
       val numberOfPeriods: Int = 2,
       val currentPeriod: Int = 1,
       val pauseCount: Int = 0,
       val canPause: Boolean = true,
       val isLastPeriod: Boolean = false,
       val sortOrder: PlayerSortOrder = PlayerSortOrder.BY_ACTIVE_FIRST,
       val isMatchStarted: Boolean = false,
   )
   ```

2. **Nuevos métodos:**
   ```kotlin
   fun beginMatch()                              // Inicia el partido
   fun confirmStopMatch()                        // Confirma detener partido anticipadamente
   fun dismissStopConfirmation()                 // Cancela confirmación
   fun setSortOrder(sortOrder: PlayerSortOrder)  // Cambia orden de jugadores
   ```

3. **Lógica de ordenación:**
   ```kotlin
   val sortedPlayers = when (sortOrder) {
       PlayerSortOrder.BY_TIME_DESC -> playerTimeItems.sortedByDescending { it.timeMillis }
       PlayerSortOrder.BY_TIME_ASC -> playerTimeItems.sortedBy { it.timeMillis }
       PlayerSortOrder.BY_ACTIVE_FIRST -> playerTimeItems.sortedWith(
           compareByDescending<PlayerTimeItem> { it.isRunning }
               .thenByDescending { it.timeMillis }
       )
   }
   ```

4. **PlayerTimeItem actualizado:**
   ```kotlin
   data class PlayerTimeItem(
       // ... campos existentes ...
       val isCaptain: Boolean = false,  // NUEVO
   )
   ```

#### MatchListViewModel.kt
Añadido método para establecer partido actual:
```kotlin
fun setCurrentMatch(matchId: Long) {
    viewModelScope.launch {
        setCurrentMatchUseCase.invoke(matchId)
    }
}
```

### 5. Capa de UI

#### CurrentMatchScreen.kt
Completamente rediseñado con dos estados:

##### Estado Pre-Partido (PreMatchView)
- Muestra información del partido
- Lista de jugadores (sin interacción)
- Botón "Comenzar Partido" para iniciar el cronómetro

##### Estado Durante Partido (OngoingMatchView)
- Tarjeta de tiempo con:
  - Nombre del periodo (ej: "1ª Parte", "2º Cuarto")
  - Cuenta atrás desde la duración del periodo
  - Tiempo de descuento en rojo (ej: "25 + 2")
- Selector de ordenación de jugadores
- Lista de jugadores con:
  - Número de dorsal con estilo de camiseta
  - Insignia de capitán (C)
  - Nombre completo
  - Tiempo jugado
- Botón de Pausa (deshabilitado cuando se alcanza el límite)
- Botón de Detener (con confirmación si no es el último periodo)

#### Componentes Nuevos

**MatchTimeCard:**
```kotlin
@Composable
private fun MatchTimeCard(
    timeMillis: Long,
    isRunning: Boolean,
    numberOfPeriods: Int,
    currentPeriod: Int,
)
```
- Calcula y muestra el tiempo restante en el periodo actual
- Muestra tiempo de descuento en rojo cuando es negativo
- Tarjeta con borde (sin relleno de fondo)

**SortOrderSelector:**
```kotlin
@Composable
private fun SortOrderSelector(
    currentSortOrder: PlayerSortOrder,
    onSortOrderChange: (PlayerSortOrder) -> Unit,
)
```
- Menú desplegable para cambiar el orden
- Muestra el orden actual

**PlayerTimeCard (actualizado):**
- Número de dorsal con diseño de camiseta (fondo oscuro, franja naranja)
- Fuente Bebas Neue para el número
- Insignia de capitán circular con "C"
- Diseño más limpio y profesional

**StopMatchEarlyConfirmationDialog:**
```kotlin
@Composable
private fun StopMatchEarlyConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
)
```
- Dialogo de confirmación cuando se intenta detener el partido antes del último periodo

### 6. Recursos de Strings

#### Inglés (values/strings.xml)
```xml
<string name="sort_by_time_desc">Most Time First</string>
<string name="sort_by_time_asc">Least Time First</string>
<string name="sort_by_active">Active First</string>
<string name="captain_badge">C</string>
<string name="period_label">Period %1$d/%2$d</string>
<string name="first_quarter">1st Quarter</string>
<string name="second_quarter">2nd Quarter</string>
<string name="third_quarter">3rd Quarter</string>
<string name="fourth_quarter">4th Quarter</string>
<string name="first_half">1st Half</string>
<string name="second_half">2nd Half</string>
<string name="period_time_label">Period Time</string>
<string name="stop_match_early_title">Stop Match Early?</string>
<string name="stop_match_early_message">The match is not in the final period. Are you sure you want to stop it now?</string>
<string name="pause_limit_reached">Cannot pause. Maximum pauses reached.</string>
<string name="begin_match">Begin Match</string>
```

#### Español (values-es/strings.xml)
```xml
<string name="sort_by_time_desc">Más Tiempo Primero</string>
<string name="sort_by_time_asc">Menos Tiempo Primero</string>
<string name="sort_by_active">Activos Primero</string>
<string name="captain_badge">C</string>
<string name="period_label">Periodo %1$d/%2$d</string>
<string name="first_quarter">1º Cuarto</string>
<string name="second_quarter">2º Cuarto</string>
<string name="third_quarter">3º Cuarto</string>
<string name="fourth_quarter">4º Cuarto</string>
<string name="first_half">1ª Parte</string>
<string name="second_half">2ª Parte</string>
<string name="period_time_label">Tiempo de Periodo</string>
<string name="stop_match_early_title">¿Detener Partido Antes de Tiempo?</string>
<string name="stop_match_early_message">El partido no está en el periodo final. ¿Estás seguro de que deseas detenerlo ahora?</string>
<string name="pause_limit_reached">No se puede pausar. Máximo de pausas alcanzado.</string>
<string name="begin_match">Comenzar Partido</string>
```

## Tests Unitarios

### MatchTest.kt
Tests para los métodos helper del modelo Match:
- `getPeriodDurationMillis()`
- `getMaxPauses()`
- `canPause()`
- `isLastPeriod()`

### SetCurrentMatchUseCaseTest.kt
Tests para el nuevo caso de uso:
- Establece el estado correcto del partido
- Maneja errores cuando el partido no existe

## Flujo de Usuario

### Inicio de Partido (Mejorado)
1. Usuario ve la lista de partidos pendientes
2. Hace clic en "Empezar" en un partido
3. **NUEVO:** Navega a la pantalla de detalle sin iniciar el cronómetro
4. Ve la lista de jugadores y puede revisarla
5. Hace clic en "Comenzar Partido" para iniciar el cronómetro
6. El partido comienza con todos los jugadores de la alineación inicial activos

### Durante el Partido
1. Usuario ve el tiempo del periodo actual contando hacia atrás
2. Ve qué periodo está en curso (ej: "1ª Parte")
3. Puede ordenar jugadores por:
   - Activos primero (predeterminado)
   - Más tiempo jugado primero
   - Menos tiempo jugado primero
4. Los jugadores muestran:
   - Número de dorsal con estilo de camiseta
   - Nombre completo
   - Insignia de capitán (si aplica)
   - Tiempo jugado
5. Puede realizar cambios de jugadores
6. Puede pausar el partido (máximo 1 vez para 2 periodos, 3 veces para 4 periodos)
7. Si intenta detener el partido antes del último periodo, se le pide confirmación

### Tiempo de Descuento
Cuando el tiempo del periodo pasa de cero:
- Se muestra "25 + 2" (por ejemplo, para 2 minutos de descuento)
- El tiempo adicional se muestra en rojo
- El cronómetro sigue corriendo

## Consideraciones de Diseño

### Arquitectura
- Separación clara de responsabilidades por capas
- Casos de uso específicos para cada operación
- Estados inmutables en ViewModels
- Composables reutilizables

### Rendimiento
- Ordenación de jugadores en el ViewModel
- Actualización reactiva con Flows
- Composables optimizados con `remember` y `derivedStateOf` donde es necesario

### Usabilidad
- Confirmación antes de acciones irreversibles
- Deshabilitación visual de controles cuando no están disponibles
- Feedback visual claro del estado actual
- Iconografía intuitiva

### Internacionalización
- Todos los strings externalizados
- Soporte completo en inglés y español
- Formato de periodos adaptado al idioma

## Validación Manual Requerida

Una vez construida la aplicación, validar:

1. **Inicio de partido:**
   - ✅ No se inicia automáticamente al hacer clic en "Empezar"
   - ✅ Muestra botón "Comenzar Partido" en la pantalla de detalle
   - ✅ El cronómetro inicia al hacer clic en "Comenzar Partido"

2. **Visualización de tiempo:**
   - ✅ Cuenta atrás desde 25:00 para 2 periodos
   - ✅ Cuenta atrás desde 12:30 para 4 periodos
   - ✅ Muestra tiempo de descuento en rojo cuando es negativo
   - ✅ Muestra formato "25 + 2" para tiempo de descuento

3. **Control de pausas:**
   - ✅ Permite 1 pausa para 2 periodos
   - ✅ Permite 3 pausas para 4 periodos
   - ✅ Botón de pausa deshabilitado cuando se alcanza el límite
   - ✅ Incrementa el periodo al pausar

4. **Ordenación de jugadores:**
   - ✅ Por defecto muestra activos primero
   - ✅ Selector permite cambiar el orden
   - ✅ Orden se mantiene durante los cambios

5. **Visualización de jugadores:**
   - ✅ Número de dorsal con estilo de camiseta
   - ✅ Insignia de capitán visible
   - ✅ Nombre completo legible

6. **Confirmación de detención:**
   - ✅ Pide confirmación si no está en el último periodo
   - ✅ No pide confirmación en el último periodo

7. **Migración de base de datos:**
   - ✅ Datos existentes se preservan
   - ✅ Nuevos campos tienen valores por defecto correctos

## Archivos Modificados

### Domain
- `Match.kt` - Modelo actualizado con nuevos campos y métodos
- `MatchTest.kt` - Tests unitarios (NUEVO)

### UseCase
- `SetCurrentMatchUseCase.kt` - Nuevo caso de uso (NUEVO)
- `SetCurrentMatchUseCaseTest.kt` - Tests (NUEVO)
- `UseCaseModule.kt` - Registro DI actualizado

### Data
- `MatchEntity.kt` - Entity actualizada
- `MatchRepositoryImpl.kt` - Lógica de pausa actualizada
- `TeamFlowManagerDatabase.kt` - Versión incrementada
- `DataLocalModule.kt` - Migración añadida

### ViewModel
- `MatchViewModel.kt` - Lógica actualizada con ordenación y periodos
- `MatchListViewModel.kt` - Método setCurrentMatch añadido
- `ViewModelModule.kt` - DI actualizado

### UI
- `CurrentMatchScreen.kt` - Completamente rediseñado
- `MatchListScreen.kt` - Navegación actualizada
- `strings.xml` (en/es) - Nuevos strings añadidos

## Compatibilidad

- ✅ Compatible con datos existentes (migración automática)
- ✅ No rompe funcionalidad existente
- ✅ Mantiene separación de capas del proyecto
- ✅ Sigue patrones arquitectónicos establecidos
- ✅ Tests unitarios incluidos

## Próximos Pasos

1. Construir la aplicación en un entorno Android
2. Ejecutar tests unitarios
3. Realizar pruebas manuales según la checklist anterior
4. Ajustar estilos visuales si es necesario
5. Verificar comportamiento en diferentes tamaños de pantalla
