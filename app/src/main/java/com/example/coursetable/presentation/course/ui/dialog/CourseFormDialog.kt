package com.example.coursetable.presentation.course.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import com.example.coursetable.domain.model.CourseColorPalette
import com.example.coursetable.presentation.course.model.CourseFormMode
import com.example.coursetable.presentation.course.model.CourseFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFormDialog(
    mode: CourseFormMode,
    formState: CourseFormState,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onTeacherChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onWeekDayChange: (Int) -> Unit,
    onStartSectionChange: (Int) -> Unit,
    onSectionCountChange: (Int) -> Unit,
    onWeekStartChange: (Int) -> Unit,
    onWeekEndChange: (Int) -> Unit,
    onColorChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    var weekPickerTarget by remember { mutableStateOf<WeekPickerTarget?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (mode == CourseFormMode.Create) "新建课程" else "修改课程",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = formState.name,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    label = { Text("课程名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = formState.teacher,
                    onValueChange = onTeacherChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text("授课教师") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = formState.location,
                    onValueChange = onLocationChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text("授课地点") },
                    singleLine = true
                )

                NumericSelectionRow(
                    label = "周几",
                    value = formState.weekDay,
                    options = (1..7).toList(),
                    onValueSelected = onWeekDayChange
                )

                NumericSelectionRow(
                    label = "开始节次",
                    value = formState.startSection,
                    options = listOf(1, 3, 4, 6, 7, 9, 11, 12),
                    onValueSelected = onStartSectionChange
                )

                NumericSelectionRow(
                    label = "课程节数",
                    value = formState.sectionCount,
                    options = listOf(2, 3),
                    onValueSelected = onSectionCountChange
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PickerLikeField(
                        modifier = Modifier.weight(1f),
                        label = "开始周",
                        value = formState.weekStart,
                        onClick = { weekPickerTarget = WeekPickerTarget.Start }
                    )
                    PickerLikeField(
                        modifier = Modifier.weight(1f),
                        label = "结束周",
                        value = formState.weekEnd,
                        onClick = { weekPickerTarget = WeekPickerTarget.End }
                    )
                }

                Text(
                    text = "课程颜色",
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CourseColorPalette.presetColors.forEachIndexed { index, colorInt ->
                        val selected = index == formState.colorIndex
                        Box(
                            modifier = Modifier
                                .size(if (selected) 30.dp else 24.dp)
                                .background(Color(colorInt), CircleShape)
                                .clickable { onColorChange(index) }
                        )
                    }
                }

                formState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text("取消")
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = !isSaving,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(if (isSaving) "保存中..." else "保存")
                    }
                }
            }
        }
    }

    weekPickerTarget?.let { target ->
        ModalBottomSheet(
            onDismissRequest = { weekPickerTarget = null },
            sheetState = sheetState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                items((1..20).toList()) { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (target == WeekPickerTarget.Start) {
                                    onWeekStartChange(week)
                                } else {
                                    onWeekEndChange(week)
                                }
                                weekPickerTarget = null
                            }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "第 $week 周")
                    }
                }
            }
        }
    }
}

@Composable
private fun NumericSelectionRow(
    label: String,
    value: Int,
    options: List<Int>,
    onValueSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val selected = option == value
                Box(
                    modifier = Modifier
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable { onValueSelected(option) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = option.toString())
                }
            }
        }
    }
}

@Composable
private fun PickerLikeField(
    modifier: Modifier,
    label: String,
    value: Int,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .background(Color(0xFFEFF1F5), MaterialTheme.shapes.small)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(text = "第 $value 周")
        }
    }
}

private enum class WeekPickerTarget {
    Start,
    End
}

