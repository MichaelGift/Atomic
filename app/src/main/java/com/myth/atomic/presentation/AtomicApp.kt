package com.myth.atomic.presentation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.myth.atomic.R
import com.myth.atomic.presentation.components.FloatingNavBar
import com.myth.atomic.presentation.components.NavBarItem
import com.myth.atomic.presentation.goaldetail.GoalDetailScreen
import com.myth.atomic.presentation.goals.GoalsScreen
import com.myth.atomic.presentation.journal.JournalScreen
import com.myth.atomic.presentation.journal.editor.JournalEditorScreen
import com.myth.atomic.presentation.navigation.GOAL_DEEP_LINK_BASE
import com.myth.atomic.presentation.navigation.GoalDetailRoute
import com.myth.atomic.presentation.navigation.GoalsRoute
import com.myth.atomic.presentation.navigation.JournalEditorRoute
import com.myth.atomic.presentation.navigation.JournalRoute
import com.myth.atomic.presentation.navigation.TodayRoute
import com.myth.atomic.presentation.today.TodayScreen
import kotlin.reflect.KClass

private enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector,
    @param:StringRes val label: Int,
) {
    Today(TodayRoute, TodayRoute::class, Icons.Default.Home, R.string.nav_today),
    Goals(GoalsRoute, GoalsRoute::class, Icons.Default.Star, R.string.nav_goals),
    Journal(JournalRoute, JournalRoute::class, Icons.Default.Create, R.string.nav_journal),
}

@Composable
fun AtomicApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedIndex = TopLevelDestination.entries.indexOfFirst { destination ->
        currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true
    }

    // A Surface, not a plain Box: it also sets the default content color for every screen.
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = TodayRoute,
                modifier = Modifier.fillMaxSize(),
                // Tabs cross-fade; pushed screens slide in over them.
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(160)) },
            ) {
                composable<TodayRoute> {
                    TodayScreen(
                        onOpenGoal = { navController.navigate(GoalDetailRoute(it)) },
                        onOpenGoals = { navController.navigateToTab(TopLevelDestination.Goals) },
                    )
                }
                composable<GoalsRoute> {
                    GoalsScreen(onOpenGoal = { navController.navigate(GoalDetailRoute(it)) })
                }
                composable<JournalRoute> {
                    JournalScreen(
                        onNewEntry = { navController.navigate(JournalEditorRoute()) },
                        onOpenEntry = { navController.navigate(JournalEditorRoute(entryId = it)) },
                    )
                }
                composable<GoalDetailRoute>(
                    deepLinks = listOf(navDeepLink<GoalDetailRoute>(basePath = GOAL_DEEP_LINK_BASE)),
                    enterTransition = { slideInHorizontally(tween(280)) { it / 4 } + fadeIn(tween(280)) },
                    popExitTransition = { slideOutHorizontally(tween(220)) { it / 4 } + fadeOut(tween(220)) },
                ) {
                    GoalDetailScreen(
                        onBack = navController::popBackStack,
                        onWriteEntry = { goalId -> navController.navigate(JournalEditorRoute(goalId = goalId)) },
                        onOpenEntry = { navController.navigate(JournalEditorRoute(entryId = it)) },
                    )
                }
                composable<JournalEditorRoute>(
                    enterTransition = { slideInVertically(tween(300)) { it / 6 } + fadeIn(tween(300)) },
                    popExitTransition = { slideOutVertically(tween(220)) { it / 6 } + fadeOut(tween(220)) },
                ) {
                    JournalEditorScreen(onDone = navController::popBackStack)
                }
            }

            // Content scrolls edge to edge; keep the clock and icons legible above it.
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f)),
            )

            AnimatedVisibility(
                visible = selectedIndex >= 0,
                enter = slideInVertically { it * 2 } + fadeIn(),
                exit = slideOutVertically { it * 2 } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 12.dp),
            ) {
                FloatingNavBar(
                    items = TopLevelDestination.entries.map { NavBarItem(it.icon, stringResource(it.label)) },
                    selectedIndex = selectedIndex,
                    onSelect = { navController.navigateToTab(TopLevelDestination.entries[it]) },
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
