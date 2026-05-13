package com.pokerhelper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pokerhelper.app.data.HandHistoryRepository
import com.pokerhelper.app.ui.Routes
import com.pokerhelper.app.ui.screens.HelpScreen
import com.pokerhelper.app.ui.screens.HomeScreen
import com.pokerhelper.app.ui.screens.MainScreen
import com.pokerhelper.app.ui.screens.StatsScreen
import com.pokerhelper.app.ui.theme.PokerHelperTheme
import com.pokerhelper.app.viewmodel.MainViewModel
import com.pokerhelper.app.viewmodel.StatsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var historyRepository: HandHistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyRepository = HandHistoryRepository(applicationContext)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
                modelClass.isAssignableFrom(MainViewModel::class.java) ->
                    MainViewModel(historyRepository) as T
                modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                    StatsViewModel(historyRepository) as T
                else -> throw IllegalArgumentException("Unknown VM: $modelClass")
            }
        }

        setContent {
            PokerHelperTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            onStartHand = { navController.navigate(Routes.HAND) },
                            onOpenStats = { navController.navigate(Routes.STATS) },
                            onOpenHelp = { navController.navigate(Routes.HELP) }
                        )
                    }
                    composable(Routes.HAND) {
                        val vm: MainViewModel = viewModel(factory = factory)
                        MainScreen(viewModel = vm, onBack = { navController.popBackStack() })
                    }
                    composable(Routes.STATS) {
                        val vm: StatsViewModel = viewModel(factory = factory)
                        StatsScreen(viewModel = vm, onBack = { navController.popBackStack() })
                    }
                    composable(Routes.HELP) {
                        HelpScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
