package com.example.coursetable.feature.course.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import com.example.coursetable.domain.model.CourseSlotVo

@Composable
fun CourseDetailDialog(
    slot: CourseSlotVo,
    onDismiss: () -> Unit,
    onAddCourse: () -> Unit,
    onEditCourse: () -> Unit,
    onDeleteCourse: () -> Unit
) {
    val endSection = slot.startSection + slot.sectionCount - 1
    val timeRangeText = buildSectionTimeRange(slot.startSection, endSection)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = onDeleteCourse) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "删除课程"
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        text = "课程详情",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(text = slot.courseName, style = MaterialTheme.typography.titleLarge)
                Text(text = "授课教师: ${slot.teacher}", modifier = Modifier.padding(top = 6.dp))
                Text(
                    text = "授课周数: 第${slot.weekStart}~${slot.weekEnd}周",
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(text = "授课地点: ${slot.location}", modifier = Modifier.padding(top = 4.dp))
                Text(
                    text = "授课时间: 第${slot.startSection}-${endSection}节（$timeRangeText）",
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onAddCourse) {
                        Text("添加课程")
                    }
                    TextButton(onClick = onEditCourse) {
                        Text("修改课程")
                    }
                }
            }
        }
    }
}

private fun buildSectionTimeRange(startSection: Int, endSection: Int): String {
    val start = sectionTimeMap[startSection]?.first
    val end = sectionTimeMap[endSection]?.second
    return if (start != null && end != null) "$start-$end" else "时间待定"
}

private val sectionTimeMap = mapOf(
    1 to ("8:00" to "8:45"),
    2 to ("8:50" to "9:35"),
    3 to ("9:55" to "10:40"),
    4 to ("10:45" to "11:30"),
    5 to ("11:35" to "12:20"),
    6 to ("14:00" to "14:45"),
    7 to ("14:50" to "15:35"),
    8 to ("15:40" to "16:25"),
    9 to ("16:45" to "17:30"),
    10 to ("17:35" to "18:20"),
    11 to ("19:00" to "19:40"),
    12 to ("19:45" to "20:30"),
    13 to ("20:35" to "21:20")
)



