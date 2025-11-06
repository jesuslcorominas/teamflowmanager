# US-7.1.6: Voyager Migration - Visual Guide

## Navigation Architecture

### Before: Jetpack Navigation Compose
```
MainActivity
    └── MainScreen (Scaffold with NavController)
            ├── TopBar (with NavController)
            ├── BottomBar (with NavController)
            └── NavHost
                    ├── composable("splash")
                    ├── composable("team/{mode}")
                    ├── composable("players")
                    ├── composable("player_wizard/{id}")
                    ├── composable("matches")
                    ├── composable("match/{id}/{team}/{opponent}")
                    └── ...
```

### After: Voyager
```
MainActivity
    └── Navigator (Voyager)
            └── SplashScreen
                    ├── → TeamScreen(mode)
                    └── → MainTabScreen
                            └── TabNavigator
                                    ├── MatchesTab
                                    │   └── Navigator (nested)
                                    │       ├── → MatchDetailScreen
                                    │       ├── → ArchivedMatchesScreen
                                    │       └── → MatchCreationWizardScreen
                                    ├── PlayersTab
                                    │   └── Navigator (nested)
                                    │       └── → PlayerWizardScreen
                                    ├── AnalysisTab
                                    └── TeamTab(mode)
```

## Screen Definitions

### Before: String Routes
```kotlin
// Route definition
object Route {
    object Team {
        const val ARG_MODE = "mode"
        const val FULL_ROUTE = "team/{mode}"
        fun createRoute(mode: String) = "team/$mode"
    }
}

// Navigation
navController.navigate(Route.Team.createRoute("edit"))

// Getting parameters
val mode = backStackEntry.arguments?.getString(Route.Team.ARG_MODE)
```

### After: Type-Safe Classes
```kotlin
// Screen definition
data class TeamScreen(val mode: String) : Screen {
    @Composable
    override fun Content() {
        TeamContent(mode = mode, ...)
    }
}

// Navigation
navigator.push(TeamScreen(mode = "edit"))

// Parameters are directly accessible as properties
```

## Tab Navigation

### Before: Custom Implementation
```kotlin
// BottomNavigationBar.kt (139 lines)
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Route.Matches, Route.Players, ...)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { route ->
            val selected = /* complex logic */
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(route.createRoute()) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
```

### After: Built-in TabNavigator
```kotlin
// Tab definition
object MatchesTab : Tab {
    @Composable
    override fun Content() {
        MatchListScreen(...)
    }
    
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = stringResource(R.string.nav_matches),
            icon = Icons.Default.SportsSoccer
        )
}

// Usage
TabNavigator(MatchesTab) {
    // Automatic bottom bar management
}

// Switch tabs
tabNavigator.current = PlayersTab
```

## Back Navigation

### Before: Complex Logic
```kotlin
// Navigation.kt
BackHandler {
    when (route) {
        Route.Matches -> if (searchState.isActive) {
            searchState.clear()
        } else {
            activity?.finish()
        }
        Route.Team -> {
            val mode = backStackEntry?.arguments?.getString(Route.Team.ARG_MODE)
            when (mode) {
                Route.Team.MODE_CREATE -> activity?.finish()
                Route.Team.MODE_VIEW -> navController.navigateToMatches()
            }
        }
        Route.Players -> navController.navigateToMatches()
        else -> navController.popBackStack()
    }
}
```

### After: Clean Handlers
```kotlin
// VoyagerBackHandler.kt
@Composable
fun VoyagerBackHandler() {
    val navigator = LocalNavigator.currentOrThrow
    val isInMainTab = navigator.lastItem is MainTabScreen
    
    if (isInMainTab) {
        TabBackHandler()
    } else {
        BackHandler {
            if (!navigator.pop()) {
                activity?.finish()
            }
        }
    }
}

// Tab-specific back handling
@Composable
private fun TabBackHandler() {
    val tabNavigator = LocalTabNavigator.current
    BackHandler {
        when (tabNavigator.current) {
            is MatchesTab -> handleMatchesBack()
            else -> tabNavigator.current = MatchesTab
        }
    }
}
```

## Navigation Examples

### Example 1: Simple Navigation

**Before:**
```kotlin
navController.navigate("player_wizard/123")
```

**After:**
```kotlin
navigator.push(PlayerWizardScreen(playerId = 123))
```

### Example 2: Navigation with Replace

**Before:**
```kotlin
navController.navigate("matches") {
    popUpTo("splash") { inclusive = true }
}
```

**After:**
```kotlin
navigator.replaceAll(MainTabScreen())
```

### Example 3: Getting Parameters

**Before:**
```kotlin
composable(
    route = "match/{id}/{team}/{opponent}",
    arguments = listOf(
        navArgument("id") { type = NavType.LongType },
        navArgument("team") { type = NavType.StringType },
        navArgument("opponent") { type = NavType.StringType }
    )
) { backStackEntry ->
    val id = backStackEntry.arguments?.getLong("id") ?: 0
    val team = backStackEntry.arguments?.getString("team") ?: ""
    val opponent = backStackEntry.arguments?.getString("opponent") ?: ""
    MatchScreen(id, team, opponent)
}
```

**After:**
```kotlin
data class MatchDetailScreen(
    val matchId: Long,
    val teamName: String,
    val opponent: String
) : Screen {
    @Composable
    override fun Content() {
        MatchContent(matchId, teamName, opponent)
    }
}
```

### Example 4: Tab Switching

**Before:**
```kotlin
navController.navigate("team/view") {
    popUpTo(navController.graph.startDestinationId) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

**After:**
```kotlin
tabNavigator.current = TeamTab(mode = "view")
```

## Back Interception for Edit Screens

### Before: Complex Setup

```kotlin
// In TeamScreen
val backHandlerController = remember { BackHandlerController() }

// In MainScreen
val currentBackHandler = remember { BackHandlerController() }

// Pass down through navigation graph
composable(
    route = Route.Team.FULL_ROUTE,
) { backStackEntry ->
    TeamScreen(
        currentBackHandler = if (mode == "edit") currentBackHandler else null
    )
}

// In TopBar - needs NavController
IconButton(
    onClick = {
        backHandlerController.onBackRequested?.invoke()
            ?: navController.popBackStack()
    }
)
```

### After: Cleaner Implementation

```kotlin
// In TeamTab
val backHandlerController = remember { BackHandlerController() }

TeamScreen(
    currentBackHandler = if (mode == "edit") backHandlerController else null
)

// In VoyagerAppTopBar - no NavController needed
IconButton(
    onClick = onBackClick  // Simple callback
)

// In MainTabScreen
VoyagerAppTopBar(
    onBackClick = {
        backHandlerController.onBackRequested?.invoke() ?: run {
            navigator.pop()
        }
    }
)
```

## Search Bar Integration

### Before: Scattered Logic

```kotlin
// In Navigation.kt
BackHandler {
    when (route) {
        Route.Matches -> if (searchState.isActive) {
            searchState.clear()
            searchState.isActive = false
        } else {
            activity?.finish()
        }
    }
}

// In AppTopBar.kt
AppTopBar(
    navController = navController,  // Needed for back
    ...
)
```

### After: Centralized

```kotlin
// In VoyagerBackHandler.kt
@Composable
private fun TabBackHandler() {
    val searchState = LocalSearchState.current
    BackHandler {
        when (tabNavigator.current) {
            is MatchesTab -> {
                if (searchState.isActive) {
                    searchState.clear()
                    searchState.isActive = false
                } else {
                    activity?.finish()
                }
            }
        }
    }
}

// In VoyagerAppTopBar.kt
VoyagerAppTopBar(
    onBackClick = onBackClick,  // No NavController needed
    ...
)
```

## Code Metrics

### Lines of Code Reduction

| File | Before | After | Reduction |
|------|--------|-------|-----------|
| Navigation.kt | 197 lines | 0 (replaced by screens) | -197 |
| BottomNavigationBar.kt | 139 lines | Integrated in MainTabScreen | -139 |
| MainScreen.kt | 138 lines | Simple Navigator call | -130 |
| Total | 474 lines | ~200 lines in screens | **-274 lines** |

### New Code

| File | Lines | Purpose |
|------|-------|---------|
| SplashScreen.kt | ~24 | Voyager screen wrapper |
| MainTabScreen.kt | ~350 | Tab navigation + scaffold |
| TeamScreen.kt | ~28 | Voyager screen wrapper |
| PlayerWizardScreen.kt | ~20 | Voyager screen wrapper |
| MatchCreationWizardScreen.kt | ~22 | Voyager screen wrapper |
| MatchDetailScreen.kt | ~16 | Voyager screen wrapper |
| ArchivedMatchesScreen.kt | ~20 | Voyager screen wrapper |
| VoyagerBackHandler.kt | ~60 | Global back handling |
| VoyagerAppTopBar.kt | ~180 | TopBar without NavController |
| **Total** | **~720 lines** | Better organized |

**Net Change**: +446 lines (but with better structure, type safety, and organization)

## Type Safety Benefits

### Compile-Time Errors

**Before** - Runtime errors:
```kotlin
// Typo in route string - compiles fine, crashes at runtime
navController.navigate("tema/edit")  // Should be "team/edit"

// Wrong parameter type - compiles fine, crashes at runtime
navController.navigate("player_wizard/abc")  // Should be number
```

**After** - Compile-time errors:
```kotlin
// Typo in class name - won't compile
navigator.push(TemaScreen(mode = "edit"))  // Compiler error

// Wrong parameter type - won't compile
navigator.push(PlayerWizardScreen(playerId = "abc"))  // Compiler error
```

## State Management

### Before: Saved State Handle
```kotlin
// In ViewModel
class TeamViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val mode: String? = savedStateHandle["mode"]
}
```

### After: Constructor Parameters
```kotlin
// In Screen
data class TeamScreen(val mode: String) : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<TeamViewModel>()
        TeamContent(mode = mode, viewModel = viewModel)
    }
}

// In ViewModel - mode passed from screen
class TeamViewModel : ViewModel() {
    // No need for SavedStateHandle for navigation params
}
```

## Testing Benefits

### Before: Complex Setup
```kotlin
@Test
fun testNavigation() {
    val navController = TestNavHostController(context)
    navController.setGraph(R.navigation.nav_graph)
    
    // Simulate navigation
    navController.navigate("team/edit")
    
    // Assert destination
    assertEquals("team/{mode}", navController.currentDestination?.route)
}
```

### After: Simple Object Creation
```kotlin
@Test
fun testNavigation() {
    val screen = TeamScreen(mode = "edit")
    
    // Screen is just a data class
    assertEquals("edit", screen.mode)
    
    // Can test Content composable directly
}
```

## Summary of Improvements

| Aspect | Before | After | Benefit |
|--------|--------|-------|---------|
| Type Safety | Runtime | Compile-time | ✅ Catch errors early |
| Code Size | 474 lines | ~720 lines* | ⚠️ More but better organized |
| Complexity | High | Low | ✅ Easier to understand |
| Navigation API | String routes | Type-safe objects | ✅ IntelliSense support |
| Tab Navigation | Custom | Built-in | ✅ Less code to maintain |
| Back Handling | Scattered | Centralized | ✅ Easier to debug |
| Testing | Complex | Simple | ✅ Better testability |
| Parameters | NavArgs | Properties | ✅ Type-safe |
| DI Integration | SavedStateHandle | Direct | ✅ Cleaner |

*Includes comprehensive documentation and better separation of concerns

---

**Note**: While line count increased, code quality and maintainability improved significantly. The trade-off favors long-term benefits.
