package com.example.coursetable.feature.course.presentation.ui.table

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WeekSelector(
    selectedWeek: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f, label = "week_selector_arrow")

    Box(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "第 ${selectedWeek} 周",
                textAlign = TextAlign.Center,
                color = Color(0xFF1D2939)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = android.R.drawable.arrow_down_float),
                contentDescription = "选择周数",
                tint = Color(0xFF1D2939),
                modifier = Modifier.graphicsLayer { rotationZ = arrowRotation }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekPickerBottomSheet(
    selectedWeek: Int,
    onWeekSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val weeks = (1..20).toList()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedWeek - 1).coerceAtLeast(0))
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(bottom = 12.dp)
        ) {
            items(weeks) { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWeekSelected(week) }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "第 ${week} 周",
                        color = if (week == selectedWeek) Color(0xFF1D2939) else Color(0xFF667085)
                    )
                }
            }
        }
    }
}

