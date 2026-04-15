package com.example.coursetable.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.coursetable.R
import com.example.coursetable.feature.course.presentation.CourseTableViewModel
import com.example.coursetable.feature.course.presentation.ui.CourseTableRoute
import com.example.coursetable.ui.theme.CourseTableTheme

class MainActivity : ComponentActivity() {
    private val courseTableViewModel: CourseTableViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourseTableTheme {
                CourseTableApp(courseTableViewModel)
            }
        }
    }
}

@Composable
fun CourseTableApp(courseTableViewModel: CourseTableViewModel) {
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            MyBottomNavigation(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (selectedItem) {
                0 -> CourseTableRoute(
                    viewModel = courseTableViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> Text(text = "Music")
                else -> Text(text = "Settings")
            }
        }
    }
}

@Composable
fun MyBottomNavigation(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        for (index in 0..2) {
            NavigationBarItem(
                selected = selectedItem == index,
                onClick = {
                    onItemSelected(index)
                },
                icon = {
                    NavigationIcon(index, selectedItem)
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun NavigationIcon(index: Int, selectedItem: Int) {
    val alpha = if (selectedItem != index) 0.5f else 1f

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (index) {
            0 -> Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.alpha(alpha)
            )
            1 -> Icon(
                painter = painterResource(R.drawable.musicnote),
                contentDescription = null,
                modifier = Modifier.alpha(alpha)
            )
            else -> Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null,
                modifier = Modifier.alpha(alpha)
            )
        }
        Spacer(Modifier.padding(top = 2.dp))
        AnimatedVisibility(visible = index == selectedItem) {
            Surface(shape = CircleShape, modifier = Modifier.size(5.dp), color = Color(0xFF252527)) { }
        }
    }
}

