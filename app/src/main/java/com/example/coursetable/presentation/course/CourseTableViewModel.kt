package com.example.coursetable.presentation.course

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursetable.data.repository.CourseRepositoryProvider
import com.example.coursetable.domain.model.CourseDraftVo
import com.example.coursetable.domain.model.CourseSessionDraftVo
import com.example.coursetable.domain.model.CourseSlotVo
import com.example.coursetable.presentation.course.model.CourseDialogState
import com.example.coursetable.presentation.course.model.CourseFormMode
import com.example.coursetable.presentation.course.model.CourseFormState
import com.example.coursetable.presentation.course.model.CourseSelection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CourseTableViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CourseRepositoryProvider.get(application)

    private val selectedWeekFlow = MutableStateFlow(1)
    private val showWeekPickerFlow = MutableStateFlow(false)
    private val dialogStateFlow = MutableStateFlow<CourseDialogState>(CourseDialogState.None)
    private val formStateFlow = MutableStateFlow(CourseFormState())
    private val activeSelectionFlow = MutableStateFlow<CourseSelection?>(null)
    private val isSavingFlow = MutableStateFlow(false)
    private val isDeletingFlow = MutableStateFlow(false)

    private val weekCoursesFlow = selectedWeekFlow.flatMapLatest { week ->
        repository.observeWeekCourses(week)
    }

    private val coreUiStateFlow = combine(
        selectedWeekFlow,
        showWeekPickerFlow,
        weekCoursesFlow,
        dialogStateFlow,
        formStateFlow
    ) { selectedWeek, showWeekPicker, weekCourses, dialogState, formState ->
        CourseTableUiState(
            selectedWeek = selectedWeek,
            showWeekPicker = showWeekPicker,
            weekCourses = weekCourses,
            dialogState = dialogState,
            formState = formState
        )
    }

    private val selectionAndSavingFlow = combine(activeSelectionFlow, isSavingFlow) { activeSelection, isSaving ->
        activeSelection to isSaving
    }

    val uiState: StateFlow<CourseTableUiState> = combine(
        coreUiStateFlow,
        selectionAndSavingFlow,
        isDeletingFlow
    ) { coreState, selectionAndSaving, isDeleting ->
        coreState.copy(
            activeSelection = selectionAndSaving.first,
            isSaving = selectionAndSaving.second,
            isDeleting = isDeleting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CourseTableUiState()
    )

    fun onWeekSelectorClick() {
        showWeekPickerFlow.value = true
    }

    fun onWeekPickerDismiss() {
        showWeekPickerFlow.value = false
    }

    fun onWeekSelected(week: Int) {
        selectedWeekFlow.value = week.coerceAtLeast(1)
        showWeekPickerFlow.value = false
    }

    fun onCourseBlockClick(slot: CourseSlotVo) {
        activeSelectionFlow.value = CourseSelection.ExistingCourse(slot)
        dialogStateFlow.value = CourseDialogState.Detail(slot)
    }

    fun onEmptySlotClick(weekDay: Int, periodIndex: Int, startSection: Int, defaultSectionCount: Int) {
        val week = selectedWeekFlow.value
        activeSelectionFlow.value = CourseSelection.EmptySlot(
            weekDay = weekDay,
            periodIndex = periodIndex,
            startSection = startSection,
            defaultSectionCount = defaultSectionCount
        )
        formStateFlow.value = CourseFormState(
            weekDay = weekDay,
            startSection = startSection,
            sectionCount = defaultSectionCount,
            weekStart = week,
            weekEnd = week,
            colorIndex = 0
        )
        dialogStateFlow.value = CourseDialogState.Form(CourseFormMode.Create)
    }

    fun onDismissDialog() {
        dialogStateFlow.value = CourseDialogState.None
        activeSelectionFlow.value = null
        formStateFlow.value = CourseFormState()
    }

    fun onAddCourseFromDetail() {
        val selection = activeSelectionFlow.value as? CourseSelection.ExistingCourse ?: return
        val slot = selection.slot
        formStateFlow.value = CourseFormState(
            weekDay = slot.weekDay,
            startSection = slot.startSection,
            sectionCount = slot.sectionCount,
            weekStart = selectedWeekFlow.value,
            weekEnd = selectedWeekFlow.value,
            colorIndex = slot.colorIndex
        )
        dialogStateFlow.value = CourseDialogState.Form(CourseFormMode.Create)
    }

    fun onEditCourseFromDetail() {
        val selection = activeSelectionFlow.value as? CourseSelection.ExistingCourse ?: return
        val slot = selection.slot
        formStateFlow.value = CourseFormState(
            courseId = slot.courseId,
            sessionId = slot.sessionId,
            name = slot.courseName,
            teacher = slot.teacher,
            location = slot.location,
            weekDay = slot.weekDay,
            startSection = slot.startSection,
            sectionCount = slot.sectionCount,
            weekStart = slot.weekStart,
            weekEnd = slot.weekEnd,
            colorIndex = slot.colorIndex
        )
        dialogStateFlow.value = CourseDialogState.Form(CourseFormMode.Edit)
    }

    fun onDeleteFromDetail() {
        val selection = activeSelectionFlow.value as? CourseSelection.ExistingCourse ?: return
        dialogStateFlow.value = CourseDialogState.DeleteConfirm(selection.slot)
    }

    fun onNameChange(value: String) {
        formStateFlow.value = formStateFlow.value.copy(name = value, errorMessage = null)
    }

    fun onTeacherChange(value: String) {
        formStateFlow.value = formStateFlow.value.copy(teacher = value, errorMessage = null)
    }

    fun onLocationChange(value: String) {
        formStateFlow.value = formStateFlow.value.copy(location = value, errorMessage = null)
    }

    fun onWeekDayChange(value: Int) {
        formStateFlow.value = formStateFlow.value.copy(weekDay = value.coerceIn(1, 7), errorMessage = null)
    }

    fun onStartSectionChange(value: Int) {
        formStateFlow.value = formStateFlow.value.copy(startSection = value.coerceAtLeast(1), errorMessage = null)
    }

    fun onSectionCountChange(value: Int) {
        formStateFlow.value = formStateFlow.value.copy(sectionCount = value.coerceIn(2, 3), errorMessage = null)
    }

    fun onWeekStartChange(value: Int) {
        val current = formStateFlow.value
        val normalized = value.coerceIn(1, 20)
        val end = current.weekEnd.coerceAtLeast(normalized)
        formStateFlow.value = current.copy(weekStart = normalized, weekEnd = end, errorMessage = null)
    }

    fun onWeekEndChange(value: Int) {
        val current = formStateFlow.value
        val normalized = value.coerceIn(1, 20)
        val start = current.weekStart.coerceAtMost(normalized)
        formStateFlow.value = current.copy(weekStart = start, weekEnd = normalized, errorMessage = null)
    }

    fun onColorChange(value: Int) {
        formStateFlow.value = formStateFlow.value.copy(colorIndex = value.coerceAtLeast(0), errorMessage = null)
    }

    fun onSubmitForm() {
        val mode = (dialogStateFlow.value as? CourseDialogState.Form)?.mode ?: return
        val state = formStateFlow.value
        if (state.name.isBlank()) {
            formStateFlow.value = state.copy(errorMessage = "课程名称不能为空")
            return
        }
        if (state.weekStart > state.weekEnd) {
            formStateFlow.value = state.copy(errorMessage = "课程周数范围不合法")
            return
        }

        viewModelScope.launch {
            isSavingFlow.value = true
            try {
                val courseDraft = CourseDraftVo(
                    name = state.name.trim(),
                    teacher = state.teacher.trim(),
                    colorIndex = state.colorIndex
                )
                val sessionDraft = CourseSessionDraftVo(
                    weekDay = state.weekDay,
                    startSection = state.startSection,
                    sectionCount = state.sectionCount,
                    weekStart = state.weekStart,
                    weekEnd = state.weekEnd,
                    location = state.location.trim()
                )
                if (mode == CourseFormMode.Create) {
                    repository.addCourseWithSession(courseDraft, sessionDraft)
                } else {
                    val courseId = state.courseId
                    val sessionId = state.sessionId
                    if (courseId == null || sessionId == null) {
                        formStateFlow.value = state.copy(errorMessage = "缺少课程标识，无法修改")
                        return@launch
                    }
                    repository.updateCourse(courseId, courseDraft)
                    repository.updateSession(sessionId, courseId, sessionDraft)
                }
                onDismissDialog()
            } finally {
                isSavingFlow.value = false
            }
        }
    }

    fun onConfirmDeleteCourse() {
        val slot = (dialogStateFlow.value as? CourseDialogState.DeleteConfirm)?.slot ?: return
        viewModelScope.launch {
            isDeletingFlow.value = true
            try {
                repository.deleteCourse(slot.courseId)
                onDismissDialog()
            } finally {
                isDeletingFlow.value = false
            }
        }
    }
}
