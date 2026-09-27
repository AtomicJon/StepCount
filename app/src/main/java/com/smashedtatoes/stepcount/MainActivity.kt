package com.smashedtatoes.stepcount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smashedtatoes.stepcount.data.db.StepDatabase
import com.smashedtatoes.stepcount.data.pref.UserPreferencesRepository
import com.smashedtatoes.stepcount.data.repository.StepRepositoryImpl
import com.smashedtatoes.stepcount.service.StepTrackerService
import com.smashedtatoes.stepcount.ui.components.hasPhysicalActivityPermission
import com.smashedtatoes.stepcount.ui.screens.history.HistoryScreen
import com.smashedtatoes.stepcount.ui.screens.history.HistoryViewModel
import com.smashedtatoes.stepcount.ui.screens.home.HomeScreen
import com.smashedtatoes.stepcount.ui.screens.home.HomeViewModel
import com.smashedtatoes.stepcount.ui.screens.settings.SettingsScreen
import com.smashedtatoes.stepcount.ui.screens.settings.SettingsViewModel
import com.smashedtatoes.stepcount.ui.theme.StepCountTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StepCountTheme {
                StepCountApp()
            }
        }
    }
}

fun startTrackerService(context: Context) {
    if (hasPhysicalActivityPermission(context)) {
        val serviceIntent = Intent(context, StepTrackerService::class.java).apply {
            action = StepTrackerService.ACTION_START_TRACKING
        }
        context.startForegroundService(serviceIntent)
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    HISTORY("History", R.drawable.ic_history),
    SETTINGS("Settings", R.drawable.ic_settings),
}

@Composable
fun StepCountApp() {
    val context = LocalContext.current

    // Initialize Database, Preferences & Repository
    val db = remember { StepDatabase.getInstance(context) }
    val prefs = remember { UserPreferencesRepository(context) }
    val repository = remember { StepRepositoryImpl(db.stepDao(), prefs) }

    // ViewModels
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(repository))
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repository))

    // Start background tracking service on launch if permission granted
    LaunchedEffect(Unit) {
        startTrackerService(context)
    }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            painterResource(destination.icon),
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(
                    viewModel = homeViewModel,
                    onPermissionGranted = { startTrackerService(context) },
                    modifier = modifier
                )
                AppDestinations.HISTORY -> HistoryScreen(
                    viewModel = historyViewModel,
                    modifier = modifier
                )
                AppDestinations.SETTINGS -> SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = modifier
                )
            }
        }
    }
}
