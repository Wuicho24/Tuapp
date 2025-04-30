package com.example.tuapp.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import com.example.tuapp.fonetica.AbecedarioScreen
import com.example.tuapp.fonetica.LetraDetalleScreen
import com.example.tuapp.ruleta.RuletaScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.example.tuapp.screens.MainMenuScreen
import com.example.tuapp.screens.WordGameScreen
import com.tuapp.ui.fourwordsoneimage.FourWordsOneImageScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController = navController,
        startDestination = "main_menu",
        enterTransition = { fadeIn(animationSpec = tween(700)) },
        exitTransition = { fadeOut(animationSpec = tween(700)) },
        popEnterTransition = { fadeIn(animationSpec = tween(700)) },
        popExitTransition = { fadeOut(animationSpec = tween(700)) }
    ) {
        composable("main_menu") {
            MainMenuScreen(navController)
        }
        composable("word_game") {
            WordGameScreen(navController)
        }

        composable("modo_avanzado") { AbecedarioScreen(navController) }
        composable("detalle_letra/{letra}") { backStackEntry ->
            val letra = backStackEntry.arguments?.getString("letra") ?: "A"
            LetraDetalleScreen(navController, letra)
        }


       composable("modo_ruleta") {
           RuletaScreen()
        }

        composable("juego_4palabras") {
            FourWordsOneImageScreen(navController)
        }
    }
}
