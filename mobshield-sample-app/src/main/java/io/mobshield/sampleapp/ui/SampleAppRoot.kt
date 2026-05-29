/*
 * Copyright 2025 MobShield Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mobshield.sampleapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.mobshield.sampleapp.ui.about.AboutScreen
import io.mobshield.sampleapp.ui.config.ConfigScreen
import io.mobshield.sampleapp.ui.diagnostics.DiagnosticsScreen
import io.mobshield.sampleapp.ui.home.HomeScreen
import io.mobshield.sampleapp.viewmodel.SampleViewModel

private sealed class SampleRoute(val route: String, val label: String) {
    data object Home : SampleRoute("home", "Threats")

    data object Config : SampleRoute("config", "Config")

    data object Diagnostics : SampleRoute("diagnostics", "Signals")

    data object About : SampleRoute("about", "About")
}

@Composable
fun SampleAppRoot(viewModel: SampleViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val routes =
        listOf(
            SampleRoute.Home,
            SampleRoute.Config,
            SampleRoute.Diagnostics,
            SampleRoute.About,
        )

    Scaffold(
        bottomBar = {
            NavigationBar {
                routes.forEach { item ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector =
                                    when (item) {
                                        SampleRoute.Home -> Icons.Default.Home
                                        SampleRoute.Config -> Icons.Default.Settings
                                        SampleRoute.Diagnostics -> Icons.Default.List
                                        SampleRoute.About -> Icons.Default.Info
                                    },
                                contentDescription = item.label,
                            )
                        },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SampleRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(SampleRoute.Home.route) { HomeScreen(viewModel) }
            composable(SampleRoute.Config.route) { ConfigScreen(viewModel) }
            composable(SampleRoute.Diagnostics.route) { DiagnosticsScreen(viewModel) }
            composable(SampleRoute.About.route) { AboutScreen(viewModel) }
        }
    }
}
