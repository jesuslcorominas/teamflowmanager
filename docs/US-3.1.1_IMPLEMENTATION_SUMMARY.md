# US-3.1.1: Visualizar la distribución del tiempo de juego - Resumen de Implementación

## Descripción de la funcionalidad

Se ha implementado una nueva pestaña "Análisis" en la barra de navegación inferior de la aplicación que permite visualizar la distribución del tiempo de juego acumulado de cada jugador durante la temporada.

## Características implementadas

### 1. Nueva pestaña de navegación "Análisis"
- Ubicada en la barra de navegación inferior (bottom bar)
- Icono: Gráfico de barras (BarChart)
- Posición: Entre "Jugadores" y "Equipo"

### 2. Pantalla de Análisis con gráfico de tiempos
La pantalla muestra:
- **Título**: "TIEMPOS" (en español) / "TIMES" (en inglés)
- **Subtítulo**: "Distribución del Tiempo de Juego" / "Playing Time Distribution"
- **Gráfico de barras horizontales** que incluye para cada jugador:
  - Nombre completo del jugador
  - Tiempo total jugado en minutos
  - Barra visual proporcional al tiempo (más tiempo = barra más larga)
  - Número de partidos jugados

### 3. Ordenamiento y visualización
- Los jugadores se ordenan por tiempo total descendente (mayor tiempo primero)
- Las barras son proporcionales al máximo tiempo jugado
- Color principal de la aplicación (azul marino #003366) para las barras

## Arquitectura de la solución

### Capa de Dominio
```
domain/model/PlayerTimeStats.kt
```
- Modelo que combina datos del jugador con estadísticas agregadas
- Campos: player, totalTimeMillis, matchesPlayed

### Capa de UseCase
```
usecase/GetPlayerTimeStatsUseCase.kt
```
- Combina datos de jugadores y tiempo histórico (PlayerTimeHistory)
- Calcula tiempo total y partidos únicos por jugador
- Retorna Flow<List<PlayerTimeStats>> ordenado por tiempo descendente

### Capa de ViewModel
```
viewmodel/AnalysisViewModel.kt
```
- Estados: Loading, Empty, Success
- Obtiene datos del UseCase y los expone al UI

### Capa de Presentación
```
app/ui/analysis/AnalysisScreen.kt
```
- Pantalla scrollable con lista de barras horizontales
- Cada barra muestra: nombre, tiempo, gráfico visual, y partidos jugados
- Responsive y adaptable a diferentes tamaños de pantalla

## Flujo de datos

1. **AnalysisScreen** solicita datos al **AnalysisViewModel**
2. **AnalysisViewModel** invoca **GetPlayerTimeStatsUseCase**
3. **GetPlayerTimeStatsUseCase** combina:
   - PlayerRepository.getAllPlayers()
   - PlayerTimeHistoryRepository.getAllPlayerTimeHistory()
4. UseCase agrega datos por jugador:
   - Suma de tiempo total (elapsedTimeMillis)
   - Conteo de partidos únicos (distinct matchId)
5. Datos se ordenan por tiempo total (mayor a menor)
6. ViewModel emite estado Success con lista de PlayerTimeStats
7. UI renderiza gráfico de barras

## Navegación

### Rutas actualizadas:
- **domain/navigation/Route.kt**: Agregado `Route.Analysis`
- **ui/navigation/Navigation.kt**: Agregado composable para AnalysisScreen
- **ui/navigation/BottomNavigationBar.kt**: Agregado tab de Análisis

### Comportamiento:
- Desde cualquier pestaña del bottom bar se puede acceder a Análisis
- Presionar "back" desde Análisis navega a Partidos (Matches)
- La pestaña persiste su estado al cambiar entre tabs

## Localización (i18n)

### Strings en español (values-es/strings.xml):
```xml
<string name="nav_analysis">Análisis</string>
<string name="analysis_title">Análisis</string>
<string name="analysis_times_tab">TIEMPOS</string>
<string name="analysis_no_data">No hay datos de tiempo de juego disponibles</string>
<string name="analysis_player_time_chart_title">Distribución del Tiempo de Juego</string>
<string name="analysis_minutes_label">min</string>
<string name="analysis_matches_played">%d partidos</string>
```

### Strings en inglés (values/strings.xml):
```xml
<string name="nav_analysis">Analysis</string>
<string name="analysis_title">Analysis</string>
<string name="analysis_times_tab">TIMES</string>
<string name="analysis_no_data">No playing time data available</string>
<string name="analysis_player_time_chart_title">Playing Time Distribution</string>
<string name="analysis_minutes_label">min</string>
<string name="analysis_matches_played">%d matches</string>
```

## Inyección de dependencias (Koin)

### UseCase Module:
```kotlin
singleOf(::GetPlayerTimeStatsUseCase)
```

### ViewModel Module:
```kotlin
viewModel {
    AnalysisViewModel(
        getPlayerTimeStats = get(),
    )
}
```

## Diseño visual

### Componentes del gráfico:
1. **Encabezado de cada jugador**:
   - Nombre del jugador (izquierda)
   - Tiempo en minutos (derecha, en negrita, color primario)

2. **Barra horizontal**:
   - Canvas con rectángulo redondeado
   - Color: Primary (#003366)
   - Ancho proporcional al tiempo total
   - Altura: 32dp

3. **Información adicional**:
   - Cantidad de partidos jugados
   - Texto pequeño, alineado a la derecha
   - Color gris (onSurfaceVariant)

### Espaciado:
- Padding general: TFMSpacing.spacing04 (16dp)
- Espaciado entre barras: TFMSpacing.spacing03 (12dp)
- Padding interno: TFMSpacing.spacing01 y spacing02 (4dp, 8dp)

## Estados de la pantalla

1. **Loading**: Muestra indicador de carga circular
2. **Empty**: Muestra mensaje "No hay datos de tiempo de juego disponibles"
3. **Success**: Muestra el gráfico de barras con todos los jugadores

## Datos utilizados

La pantalla utiliza datos ya existentes en la base de datos:
- **Tabla Player**: Información de jugadores (nombre, apellido)
- **Tabla PlayerTimeHistory**: Registro histórico de tiempo de juego por partido
  - playerId: ID del jugador
  - matchId: ID del partido
  - elapsedTimeMillis: Tiempo jugado en milisegundos
  - savedAtMillis: Timestamp del registro

## Criterios de aceptación cumplidos

✅ **El gráfico debe ser claro y comprensible**
- Barras horizontales simples y fáciles de interpretar
- Nombres de jugadores claramente visibles
- Tiempo mostrado en minutos (unidad comprensible)

✅ **El gráfico debe estar actualizado**
- Usa Flow para reactividad automática
- Se actualiza cuando cambian datos en la base de datos
- Ordena datos por tiempo total para mejor análisis

✅ **Navegación correcta**
- Se accede desde la pestaña "Análisis" en el bottom bar
- Título correcto en el top bar
- Navegación back funcional

## Pruebas manuales recomendadas

1. Navegar a la pestaña Análisis desde Partidos
2. Verificar que se muestran todos los jugadores con tiempo registrado
3. Verificar que los jugadores están ordenados por tiempo (mayor a menor)
4. Verificar que las barras son proporcionales
5. Verificar que se muestra el número correcto de partidos para cada jugador
6. Verificar que el estado Empty se muestra cuando no hay datos
7. Cambiar idioma de la app y verificar strings traducidos
8. Verificar scroll vertical en pantallas con muchos jugadores

## Archivos modificados/creados

### Nuevos archivos:
- `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/PlayerTimeStats.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/GetPlayerTimeStatsUseCase.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/AnalysisViewModel.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/analysis/AnalysisScreen.kt`

### Archivos modificados:
- `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/navigation/Route.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/di/ViewModelModule.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/navigation/Navigation.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/navigation/BottomNavigationBar.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/main/MainScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-es/strings.xml`

## Notas técnicas

- **No se crearon tests** según instrucciones del agente (tests están rotos)
- Se reutilizan componentes existentes (Loading, EmptyContent, TFMSpacing)
- Usa Canvas de Compose para dibujar barras (sin dependencias externas de gráficos)
- Sigue la arquitectura clean del proyecto (UseCase → ViewModel → UI)
- Respeta el patrón de diseño existente (Koin DI, Flow, Compose)
