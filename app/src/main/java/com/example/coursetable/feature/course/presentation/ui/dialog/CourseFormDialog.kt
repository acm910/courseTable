package com.example.coursetable.feature.course.presentation.ui.dialog

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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.coursetable.feature.course.presentation.model.CourseFormMode
import com.example.coursetable.feature.course.presentation.model.CourseFormState
import com.example.coursetable.feature.course.presentation.ui.table.SectionRangeOption
import com.example.coursetable.feature.course.presentation.ui.table.buildSectionRangeOptions

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
    onSectionRangeChange: (startSection: Int, sectionCount: Int) -> Unit,
    onWeekRangeChange: (start: Int, end: Int) -> Unit,
    onColorChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    var showTimeRangePicker by remember { mutableStateOf(false) }
    var showWeekRangePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val sectionOptions = remember(formState.periodIndex) {
        buildSectionRangeOptions(formState.periodIndex)
    }
    val selectedSectionOption = remember(formState.startSection, formState.sectionCount, sectionOptions) {
        sectionOptions.firstOrNull {
            it.startSection == formState.startSection && it.sectionCount == formState.sectionCount
        } ?: sectionOptions.first()
    }
    var pendingSectionOption by remember(selectedSectionOption) { mutableStateOf(selectedSectionOption) }
    var pendingWeekStart by remember(formState.weekStart) { mutableStateOf(formState.weekStart) }
    var pendingWeekEnd by remember(formState.weekEnd) { mutableStateOf(formState.weekEnd) }

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

                PickerLikeField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    label = "授课时间",
                    value = "第${selectedSectionOption.startSection}-${selectedSectionOption.endSection}节",
                    onClick = {
                        pendingSectionOption = if (mode == CourseFormMode.Edit) {
                            selectedSectionOption
                        } else {
                            // Create mode defaults to the largest possible option (whole period).
                            sectionOptions.first()
                        }
                        showTimeRangePicker = true
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PickerLikeField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "授课周数",
                        value = "第${formState.weekStart}~${formState.weekEnd}周",
                        onClick = {
                            if (mode == CourseFormMode.Edit) {
                                pendingWeekStart = formState.weekStart
                                pendingWeekEnd = formState.weekEnd
                            } else {
                                // Create mode starts from the current selected week range.
                                pendingWeekStart = formState.weekStart
                                pendingWeekEnd = formState.weekEnd
                            }
                            showWeekRangePicker = true
                        }
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

    if (showTimeRangePicker) {
        TimeRangeBottomSheet(
            title = "选择授课时间",
            options = sectionOptions,
            selected = pendingSectionOption,
            onSelect = { pendingSectionOption = it },
            onDismiss = { showTimeRangePicker = false },
            onConfirm = {
                onSectionRangeChange(pendingSectionOption.startSection, pendingSectionOption.sectionCount)
                showTimeRangePicker = false
            },
            sheetState = sheetState
        )
    }

    if (showWeekRangePicker) {
        TwoColumnRangeBottomSheet(
            title = "选择授课周数",
            leftLabel = "开始周",
            rightLabel = "结束周",
            leftItems = (1..20).toList(),
            rightItems = (1..20).toList(),
            leftSelected = pendingWeekStart,
            rightSelected = pendingWeekEnd,
            leftFormatter = { "第${it}周" },
            rightFormatter = { "第${it}周" },
            onLeftSelect = { value ->
                pendingWeekStart = value
                if (pendingWeekEnd < value) pendingWeekEnd = value
            },
            onRightSelect = { value ->
                pendingWeekEnd = value
                if (pendingWeekStart > value) pendingWeekStart = value
            },
            onDismiss = { showWeekRangePicker = false },
            onConfirm = {
                onWeekRangeChange(pendingWeekStart, pendingWeekEnd)
                showWeekRangePicker = false
            },
            sheetState = sheetState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeRangeBottomSheet(
    title: String,
    options: List<SectionRangeOption>,
    selected: SectionRangeOption,
    onSelect: (SectionRangeOption) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    sheetState: androidx.compose.material3.SheetState
) {
    val selectedIndex = options.indexOfFirst {
        it.startSection == selected.startSection && it.sectionCount == selected.sectionCount
    }.coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceAtLeast(0))

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(top = 12.dp)
            ) {
                items(options) { option ->
                    val isSelected = option.startSection == selected.startSection && option.sectionCount == selected.sectionCount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "第${option.startSection}-${option.endSection}节",
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Button(onClick = onConfirm, modifier = Modifier.padding(start = 8.dp)) {
                    Text("确定")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoColumnRangeBottomSheet(
    title: String,
    leftLabel: String,
    rightLabel: String,
    leftItems: List<Int>,
    rightItems: List<Int>,
    leftSelected: Int,
    rightSelected: Int,
    leftFormatter: (Int) -> String,
    rightFormatter: (Int) -> String,
    onLeftSelect: (Int) -> Unit,
    onRightSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    sheetState: androidx.compose.material3.SheetState
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PickerColumn(
                    label = leftLabel,
                    items = leftItems,
                    selected = leftSelected,
                    formatter = leftFormatter,
                    modifier = Modifier.weight(1f),
                    onSelect = onLeftSelect
                )
                PickerColumn(
                    label = rightLabel,
                    items = rightItems,
                    selected = rightSelected,
                    formatter = rightFormatter,
                    modifier = Modifier.weight(1f),
                    onSelect = onRightSelect
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("取消") }
                Button(onClick = onConfirm, modifier = Modifier.padding(start = 8.dp)) {
                    Text("确定")
                }
            }
        }
    }
}

@Composable
private fun PickerColumn(
    label: String,
    items: List<Int>,
    selected: Int,
    formatter: (Int) -> String,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    val selectedIndex = items.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            items(items) { item ->
                val isSelected = item == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(item) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formatter(item),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerLikeField(
    modifier: Modifier,
    label: String,
    value: String,
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
            Text(text = value)
        }
    }
}
