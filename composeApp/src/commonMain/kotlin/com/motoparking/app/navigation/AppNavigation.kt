package com.motoparking.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.motoparking.app.ui.screens.DetailScreen
import com.motoparking.app.ui.screens.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class Detail(val spotId: String)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen(
                onSpotClick = { spotId ->
                    navController.navigate(Detail(spotId))
                }
            )
        }

        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            DetailScreen(
                spotId = detail.spotId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
