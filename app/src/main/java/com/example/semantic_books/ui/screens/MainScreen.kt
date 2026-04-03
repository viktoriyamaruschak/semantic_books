package com.example.semantic_books.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.semantic_books.viewmodel.BookViewModel

sealed class BottomNavItem(val route: String, val icon: @Composable () -> Unit, val label: String) {
    object Search : BottomNavItem("search", { Icon(Icons.Filled.Search, contentDescription = "Пошук") }, "Пошук")
    object Saved : BottomNavItem("saved", { Icon(Icons.Filled.Favorite, contentDescription = "Збережені") }, "Збережені")
}

@Composable
fun MainScreen(viewModel: BookViewModel) {
    val navController = rememberNavController()
    
    val items = listOf(
        BottomNavItem.Search,
        BottomNavItem.Saved
    )
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Показуємо меню тільки якщо ми не на екрані деталей
            if (currentRoute == BottomNavItem.Search.route || currentRoute == BottomNavItem.Saved.route) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = item.icon,
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) { saveState = true }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Search.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Search.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onBookClick = { bookId -> navController.navigate("details/$bookId") }
                )
            }
            composable(BottomNavItem.Saved.route) {
                SavedBooksScreen(
                    viewModel = viewModel,
                    onBookClick = { bookId -> navController.navigate("details/$bookId") }
                )
            }
            composable("details/{bookId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")
                BookDetailsScreen(
                    bookId = bookId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onSimilarBookClick = { newId -> navController.navigate("details/$newId") }
                )
            }
        }
    }
}
