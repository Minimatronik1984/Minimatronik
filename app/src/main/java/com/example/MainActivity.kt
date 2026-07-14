package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

enum class ScreenTab(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val tag: String
) {
    DASHBOARD("Kontrola", Icons.Filled.Home, Icons.Outlined.Home, "tab_dashboard"),
    KNOWLEDGE("Baza Znanja", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_knowledge"),
    CONTRACTS("Ugovori", Icons.Filled.Description, Icons.Outlined.Description, "tab_contracts"),
    SETTINGS("Podešavanja", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            ScreenTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    label = { Text(tab.title) },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    when (currentTab) {
                        ScreenTab.DASHBOARD -> DashboardScreen(viewModel = viewModel, modifier = modifier)
                        ScreenTab.KNOWLEDGE -> KnowledgeBaseScreen(viewModel = viewModel, modifier = modifier)
                        ScreenTab.CONTRACTS -> ContractGeneratorScreen(viewModel = viewModel, modifier = modifier)
                        ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel, modifier = modifier)
                    }
                }
            }
        }
    }
}
