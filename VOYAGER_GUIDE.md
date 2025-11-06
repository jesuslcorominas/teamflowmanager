# Guía de Voyager para TeamFlowManager

## Índice
1. [Introducción](#introducción)
2. [Conceptos Básicos](#conceptos-básicos)
3. [Implementación en TeamFlowManager](#implementación-en-teamflowmanager)
4. [Navegación entre Pantallas](#navegación-entre-pantallas)
5. [Navegación con Tabs](#navegación-con-tabs)
6. [Manejo de Back Button](#manejo-de-back-button)
7. [Parámetros de Navegación](#parámetros-de-navegación)
8. [TopBar y BottomBar](#topbar-y-bottombar)
9. [Ejemplos Prácticos](#ejemplos-prácticos)

---

## Introducción

Esta guía documenta la implementación de [Voyager](https://voyager.adriel.cafe/) en TeamFlowManager, una biblioteca de navegación moderna para Jetpack Compose que reemplaza a Jetpack Navigation Compose.

### ¿Por qué Voyager?

- **Tipo-seguro**: Las pantallas son objetos de Kotlin, no rutas de cadena
- **Más simple**: API más intuitiva y menos código repetitivo
- **Mejor integración con Compose**: Diseñado específicamente para Compose
- **Soporte de tabs integrado**: TabNavigator incluido de serie
- **Transiciones animadas**: Fácil personalización de animaciones

---

## Conceptos Básicos

### Screen

En Voyager, cada pantalla es una clase que implementa la interfaz `Screen`:

```kotlin
class MiPantalla : Screen {
    @Composable
    override fun Content() {
        // Tu composable aquí
        Text("Hola Mundo")
    }
}
```

### Navigator

El `Navigator` maneja la pila de navegación:

```kotlin
// En MainActivity
Navigator(SplashScreen()) { navigator ->
    SlideTransition(navigator)
}
```

### TabNavigator

Para navegación con tabs:

```kotlin
TabNavigator(tab = HomeTab) {
    // Contenido con tabs
}
```

---

## Implementación en TeamFlowManager

### Estructura de Archivos

```
app/src/main/java/com/jesuslcorominas/teamflowmanager/
├── ui/
│   ├── screens/                    # Screens de Voyager
│   │   ├── SplashScreen.kt
│   │   ├── MainTabScreen.kt
│   │   ├── TeamScreen.kt
│   │   ├── PlayerWizardScreen.kt
│   │   ├── MatchCreationWizardScreen.kt
│   │   ├── MatchDetailScreen.kt
│   │   └── ArchivedMatchesScreen.kt
│   ├── navigation/
│   │   └── VoyagerBackHandler.kt   # Manejo del back button
│   └── components/
│       └── topbar/
│           └── VoyagerAppTopBar.kt # TopBar compatible con Voyager
```

### Dependencias

En `gradle/libs.versions.toml`:

```toml
[versions]
voyager = "1.1.0-beta03"

[libraries]
voyager-navigator = { group = "cafe.adriel.voyager", name = "voyager-navigator", version.ref = "voyager" }
voyager-tab-navigator = { group = "cafe.adriel.voyager", name = "voyager-tab-navigator", version.ref = "voyager" }
voyager-transitions = { group = "cafe.adriel.voyager", name = "voyager-transitions", version.ref = "voyager" }
voyager-koin = { group = "cafe.adriel.voyager", name = "voyager-koin", version.ref = "voyager" }
```

En `app/build.gradle.kts`:

```kotlin
implementation(libs.voyager.navigator)
implementation(libs.voyager.tab.navigator)
implementation(libs.voyager.transitions)
implementation(libs.voyager.koin)
```

---

## Navegación entre Pantallas

### Navegar a una Nueva Pantalla

```kotlin
val navigator = LocalNavigator.currentOrThrow

// Push - añade a la pila
navigator.push(PlayerWizardScreen(playerId = 123))

// Replace - reemplaza la pantalla actual
navigator.replace(TeamScreen(mode = Route.Team.MODE_EDIT))

// ReplaceAll - limpia la pila y añade nueva pantalla
navigator.replaceAll(MainTabScreen())
```

### Volver Atrás

```kotlin
// Pop - quita la pantalla actual de la pila
navigator.pop()

// PopAll - limpia toda la pila
navigator.popAll()

// PopUntil - vuelve hasta una pantalla específica
navigator.popUntil { screen -> screen is MainTabScreen }
```

### Ejemplo Completo

```kotlin
@Composable
fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    
    Button(
        onClick = { 
            navigator.push(PlayerWizardScreen(playerId = 0L))
        }
    ) {
        Text("Crear Jugador")
    }
}
```

---

## Navegación con Tabs

### Definir un Tab

```kotlin
object MatchesTab : Tab {
    @Composable
    override fun Content() {
        MatchListScreen(
            onNavigateToMatch = { match ->
                val navigator = LocalNavigator.currentOrThrow
                navigator.push(MatchDetailScreen(match.id, match.teamName, match.opponent))
            }
        )
    }

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = stringResource(R.string.nav_matches),
            icon = Icons.Default.SportsSoccer
        )
}
```

### Tabs con Estado (Data Class)

Para tabs que necesitan estado:

```kotlin
data class TeamTab(val mode: String = Route.Team.MODE_VIEW) : Tab {
    @Composable
    override fun Content() {
        val tabNavigator = LocalTabNavigator.current
        
        TeamScreen(
            mode = mode,
            onEdit = { 
                // Cambiar al modo edición
                tabNavigator.current = TeamTab(Route.Team.MODE_EDIT)
            },
            onSave = {
                // Volver al modo vista
                tabNavigator.current = TeamTab(Route.Team.MODE_VIEW)
            }
        )
    }

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = stringResource(R.string.nav_team),
            icon = Icons.Default.Groups
        )
}
```

### Cambiar de Tab

```kotlin
val tabNavigator = LocalTabNavigator.current

// Cambiar a un tab específico
tabNavigator.current = PlayersTab

// Cambiar con estado
tabNavigator.current = TeamTab(Route.Team.MODE_EDIT)
```

### BottomBar con Tabs

```kotlin
@Composable
private fun BottomBar() {
    val tabNavigator = LocalTabNavigator.current

    NavigationBar {
        TabNavigationItem(MatchesTab)
        TabNavigationItem(PlayersTab)
        TabNavigationItem(AnalysisTab)
        TabNavigationItem(TeamTab())
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationBarItem(
        icon = { Icon(tab.options.icon, contentDescription = null) },
        selected = isSelected,
        onClick = {
            if (!isSelected) {
                tabNavigator.current = tab
            }
        }
    )
}
```

---

## Manejo de Back Button

### Back Handler Global

El `VoyagerBackHandler` maneja el comportamiento del botón back del sistema:

```kotlin
@Composable
fun VoyagerBackHandler() {
    val navigator = LocalNavigator.currentOrThrow
    val activity = LocalContext.current as? Activity
    val searchState = LocalSearchState.current

    val isInMainTab = navigator.lastItem is MainTabScreen

    if (isInMainTab) {
        TabBackHandler(activity, searchState)
    } else {
        BackHandler {
            if (!navigator.pop()) {
                activity?.finish()
            }
        }
    }
}
```

### Back Handler en Tabs

```kotlin
@Composable
private fun TabBackHandler(activity: Activity?, searchState: SearchState) {
    val tabNavigator = LocalTabNavigator.current

    BackHandler {
        when (val currentTab = tabNavigator.current) {
            is MatchesTab -> {
                if (searchState.isActive) {
                    // Cerrar búsqueda
                    searchState.clear()
                    searchState.isActive = false
                } else {
                    // Salir de la app
                    activity?.finish()
                }
            }
            else -> {
                // Volver al tab principal (Matches)
                tabNavigator.current = MatchesTab
            }
        }
    }
}
```

### Interceptar el Back en Pantallas de Edición

Para pantallas que necesitan confirmación antes de salir (Team Edit, Player Edit, Match Edit):

```kotlin
data class TeamTab(val mode: String = Route.Team.MODE_VIEW) : Tab {
    @Composable
    override fun Content() {
        val tabNavigator = LocalTabNavigator.current
        val backHandlerController = remember { BackHandlerController() }

        TeamScreen(
            currentBackHandler = if (mode == Route.Team.MODE_EDIT) backHandlerController else null,
            onNavigateBackRequest = {
                if (mode == Route.Team.MODE_EDIT) {
                    tabNavigator.current = TeamTab(Route.Team.MODE_VIEW)
                } else {
                    tabNavigator.current = MatchesTab
                }
            }
        )
    }
}
```

En la pantalla de Team:

```kotlin
@Composable
fun TeamScreen(
    currentBackHandler: BackHandlerController?,
    onNavigateBackRequest: () -> Unit
) {
    val viewModel = koinViewModel<TeamViewModel>()
    val showExitDialog by viewModel.showExitDialog.collectAsState()

    // Registrar callback personalizado
    currentBackHandler?.let {
        DisposableEffect(currentBackHandler) {
            currentBackHandler.onBackRequested = {
                viewModel.requestBack(onNavigateBackRequest)
            }
            onDispose {
                currentBackHandler.onBackRequested = null
            }
        }

        BackHandler(enabled = !showExitDialog) {
            viewModel.requestBack(onNavigateBackRequest)
        }
    }

    // UI...
}
```

---

## Parámetros de Navegación

### Con Data Classes

```kotlin
// Definir screen con parámetros
data class PlayerWizardScreen(val playerId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        PlayerWizardContent(
            playerId = playerId,  // Usar el parámetro
            onNavigateBack = { navigator.pop() }
        )
    }
}

// Navegar con parámetros
navigator.push(PlayerWizardScreen(playerId = 123))
```

### Parámetros Múltiples

```kotlin
data class MatchDetailScreen(
    val matchId: Long,
    val teamName: String,
    val opponent: String
) : Screen {
    @Composable
    override fun Content() {
        MatchDetailContent(
            matchId = matchId,
            teamName = teamName,
            opponent = opponent
        )
    }
}

// Uso
navigator.push(
    MatchDetailScreen(
        matchId = 456,
        teamName = "FC Barcelona",
        opponent = "Real Madrid"
    )
)
```

---

## TopBar y BottomBar

### TopBar Compatible con Voyager

Se ha creado `VoyagerAppTopBar` que no depende de `NavHostController`:

```kotlin
@Composable
fun VoyagerAppTopBar(
    modifier: Modifier = Modifier,
    uiConfig: Route.UiConfig?,
    title: String?,
    searchPlaceholder: String,
    onBackClick: () -> Unit
) {
    val searchState = LocalSearchState.current

    if (uiConfig?.showTopBar == true) {
        Crossfade(
            targetState = searchState.isActive,
            animationSpec = tween(durationMillis = 300)
        ) { isSearchActive ->
            if (uiConfig.hasSearchBar && isSearchActive) {
                SearchTopBar(modifier, searchPlaceholder)
            } else {
                VoyagerDefaultTopBar(
                    modifier = modifier,
                    uiConfig = uiConfig,
                    title = title,
                    onBackClick = onBackClick
                )
            }
        }
    }
}
```

### Configuración del TopBar

```kotlin
// En MainTabScreen
@Composable
private fun VoyagerAppTopBarWrapper(
    modifier: Modifier,
    uiConfig: Route.UiConfig?,
    title: String?,
    backHandlerController: BackHandlerController,
    searchPlaceholder: String
) {
    val navigator = LocalNavigator.currentOrThrow
    val tabNavigator = LocalTabNavigator.current

    VoyagerAppTopBar(
        modifier = modifier,
        uiConfig = uiConfig,
        title = title,
        searchPlaceholder = searchPlaceholder,
        onBackClick = {
            backHandlerController.onBackRequested?.invoke() ?: run {
                if (tabNavigator.current is TeamTab) {
                    tabNavigator.current = TeamTab(Route.Team.MODE_VIEW)
                } else {
                    navigator.pop()
                }
            }
        }
    )
}
```

### Mostrar/Ocultar TopBar y BottomBar

La configuración se mantiene en `Route.UiConfig`:

```kotlin
val uiConfig = when (currentTab) {
    is MatchesTab -> Route.Matches.uiConfig(null)
    // showBottomBar = true, showFab = true, hasSearchBar = true
    
    is PlayersTab -> Route.Players.uiConfig(null)
    // showBottomBar = true
    
    is TeamTab -> {
        val mode = (currentTab as? TeamTab)?.mode ?: Route.Team.MODE_VIEW
        Route.Team.uiConfig(mapOf(Route.Team.ARG_MODE to mode))
        // showBottomBar depende del modo
    }
    
    else -> null
}

// Uso en Scaffold
Scaffold(
    topBar = {
        if (uiConfig?.showTopBar == true) {
            VoyagerAppTopBar(...)
        }
    },
    bottomBar = {
        if (uiConfig?.showBottomBar == true) {
            BottomBar()
        }
    }
)
```

---

## Ejemplos Prácticos

### Ejemplo 1: Navegación Simple (Partidos Archivados)

```kotlin
class ArchivedMatchesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ArchivedMatchesContent(
            onNavigateToMatchSummary = { match ->
                navigator.push(
                    MatchDetailScreen(
                        match.id,
                        match.teamName,
                        match.opponent
                    )
                )
            }
        )
    }
}
```

### Ejemplo 2: Wizard con Navegación Paso a Paso

```kotlin
data class PlayerWizardScreen(val playerId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val backHandlerController = remember { BackHandlerController() }

        PlayerWizardContent(
            playerId = playerId,
            onNavigateBack = { navigator.pop() },
            currentBackHandler = backHandlerController
        )
    }
}
```

El contenido del wizard maneja sus propios pasos internamente pero usa el `backHandlerController` para interceptar el back.

### Ejemplo 3: Navegación desde Tab a Pantalla Detalle

```kotlin
object MatchesTab : Tab {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        MatchListScreen(
            onNavigateToEditMatch = { matchId ->
                // Navegar a wizard de edición
                navigator.push(MatchCreationWizardScreen(matchId))
            },
            onNavigateToMatch = { match ->
                // Navegar a detalle del partido
                navigator.push(
                    MatchDetailScreen(
                        match.id,
                        match.teamName,
                        match.opponent
                    )
                )
            },
            onNavigateToArchivedMatches = {
                // Navegar a partidos archivados
                navigator.push(ArchivedMatchesScreen())
            }
        )
    }
}
```

### Ejemplo 4: FAB con Navegación

```kotlin
@Composable
private fun MainScaffold() {
    val tabNavigator = LocalTabNavigator.current
    val navigator = LocalNavigator.currentOrThrow
    
    Scaffold(
        floatingActionButton = {
            if (uiConfig?.showFab == true) {
                FloatingActionButton(
                    onClick = {
                        when (val currentTab = tabNavigator.current) {
                            is TeamTab -> {
                                // Cambiar a modo edición en el mismo tab
                                tabNavigator.current = TeamTab(Route.Team.MODE_EDIT)
                            }
                            is MatchesTab -> {
                                // Navegar a crear nuevo partido
                                navigator.push(MatchCreationWizardScreen(0L))
                            }
                        }
                    }
                ) {
                    Icon(...)
                }
            }
        }
    ) { ... }
}
```

### Ejemplo 5: Splash Screen con Navegación Condicional

```kotlin
class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        SplashContent(
            onNavigateToCreateTeam = {
                // Primera vez: crear equipo
                navigator.replaceAll(TeamScreen(Route.Team.MODE_CREATE))
            },
            onNavigateToMatches = {
                // Ya existe equipo: ir a pantalla principal
                navigator.replaceAll(MainTabScreen())
            }
        )
    }
}
```

---

## Resumen de Diferencias con Navigation Compose

| Aspecto | Navigation Compose | Voyager |
|---------|-------------------|---------|
| Definición de rutas | Strings | Clases/Objects |
| Parámetros | NavArgs en URL | Propiedades de clase |
| Tipo-seguro | No (runtime) | Sí (compile-time) |
| Tab Navigation | Separado | Integrado (TabNavigator) |
| Back handling | Complejo | Simplificado |
| Transiciones | Limitadas | Fácil personalización |
| Integración DI | NavBackStackEntry | Screen directamente |

---

## Buenas Prácticas

1. **Usa data classes para screens con parámetros**: Permite paso de parámetros tipo-seguro
2. **Objects para tabs sin estado**: Más eficiente que crear nuevas instancias
3. **Manejo consistente del back**: Usa `BackHandlerController` para lógica compleja
4. **Validación en wizards**: Intercepta el back para evitar pérdida de datos
5. **Clean navigation stacks**: Usa `replaceAll` para flujos principales
6. **Composición sobre herencia**: Mantén las screens simples, delega la lógica
7. **Estado en ViewModels**: No guardes estado en las screens, usa ViewModels

---

## Solución de Problemas Comunes

### Navigator null en @Preview
```kotlin
@Preview
@Composable
fun MyScreenPreview() {
    Navigator(MyScreen()) {
        SlideTransition(it)
    }
}
```

### Múltiples back handlers
```kotlin
// El orden importa, el más específico primero
BackHandler(enabled = showDialog) { /* Cerrar diálogo */ }
BackHandler(enabled = true) { /* Back normal */ }
```

### Pérdida de estado en configuración
- Usa `rememberSaveable` para estado UI
- ViewModels se preservan automáticamente

---

## Recursos Adicionales

- [Documentación Oficial de Voyager](https://voyager.adriel.cafe/)
- [Ejemplos en GitHub](https://github.com/adrielcafe/voyager)
- [Comparativa con Navigation Compose](https://voyager.adriel.cafe/navigation-compose)

---

**Versión del documento**: 1.0  
**Fecha**: Noviembre 2025  
**Mantenedor**: TeamFlowManager Team
