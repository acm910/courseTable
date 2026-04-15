package com.example.coursetable.feature.course.presentation.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteCourseConfirmDialog(
	courseName: String,
	isDeleting: Boolean,
	onDismiss: () -> Unit,
	onConfirmDelete: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("删除课程") },
		text = { Text("确认删除 $courseName 吗？该课程所有上课时间都会被删除。") },
		dismissButton = {
			TextButton(onClick = onDismiss, enabled = !isDeleting) {
				Text("取消")
			}
		},
		confirmButton = {
			TextButton(onClick = onConfirmDelete, enabled = !isDeleting) {
				Text(if (isDeleting) "删除中..." else "确认删除")
			}
		}
	)
}


