package com.myth.atomic.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.myth.atomic.R
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
    val showBottomBar = TopLevelDestination.entries.any { currentDestination?.hasRoute(it.routeClass) == true }

    Scaffold(
        // Each screen draws its own top bar and handles its own insets.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TodayRoute,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            composable<TodayRoute> {
                TodayScreen(onOpenGoal = { navController.navigate(GoalDetailRoute(it)) })
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
            ) {
                GoalDetailScreen(
                    onBack = navController::popBackStack,
                    onWriteEntry = { goalId -> navController.navigate(JournalEditorRoute(goalId = goalId)) },
                    onOpenEntry = { navController.navigate(JournalEditorRoute(entryId = it)) },
                )
            }
            composable<JournalEditorRoute> {
                JournalEditorScreen(onDone = navController::popBackStack)
            }
        }
    }
}
