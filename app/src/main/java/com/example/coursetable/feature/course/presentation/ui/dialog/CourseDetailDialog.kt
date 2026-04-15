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
                    text = "授课时间: 第${slot.startSection}-${slot.startSection + slot.sectionCount - 1}节（具体时间待补充）",
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



