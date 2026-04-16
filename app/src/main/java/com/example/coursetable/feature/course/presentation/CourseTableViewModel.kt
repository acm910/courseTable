package com.example.coursetable.feature.course.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursetable.core.time.SemesterStartDateStore
import com.example.coursetable.data.repository.CourseRepositoryProvider
import com.example.coursetable.domain.model.CourseDraftVo
import com.example.coursetable.domain.model.CourseSessionDraftVo
import com.example.coursetable.domain.model.CourseSlotVo
import com.example.coursetable.feature.course.presentation.model.CourseDialogState
import com.example.coursetable.feature.course.presentation.model.CourseFormMode
import com.example.coursetable.feature.course.presentation.model.CourseFormState
import com.example.coursetable.feature.course.presentation.model.CourseSelection
import com.example.coursetable.feature.course.presentation.ui.table.calculateCurrentWeek
import com.example.coursetable.feature.course.presentation.ui.table.buildSectionRangeOptions
import com.example.coursetable.feature.course.presentation.ui.table.findPeriodIndexByStartSection
import com.example.coursetable.feature.webView.JwxtCourseImportParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CourseTableViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CourseRepositoryProvider.get(application)
    private val appContext = application.applicationContext
    private val initialSemesterStartDate = SemesterStartDateStore.get(appContext)
    private val initialWeek = calculateCurrentWeek(initialSemesterStartDate)

    private val selectedWeekFlow = MutableStateFlow(initialWeek)
    private val semesterStartDateFlow = MutableStateFlow(initialSemesterStartDate)
    private val showWeekPickerFlow = MutableStateFlow(false)
    private val dialogStateFlow = MutableStateFlow<CourseDialogState>(CourseDialogState.None)
    private val formStateFlow = MutableStateFlow(CourseFormState())
    private val activeSelectionFlow = MutableStateFlow<CourseSelection?>(null)
    private val isSavingFlow = MutableStateFlow(false)
    private val isDeletingFlow = MutableStateFlow(false)
    private val isImportingFlow = MutableStateFlow(false)
    private val importMessageFlow = MutableStateFlow<String?>(null)

    private val weekCoursesFlow = selectedWeekFlow.flatMapLatest { week ->
        repository.observeWeekCourses(week)
    }

    private val weekAndStartDateFlow = combine(selectedWeekFlow, semesterStartDateFlow) { selectedWeek, semesterStartDate ->
        selectedWeek to semesterStartDate
    }

    private val coreUiStateFlow = combine(
        weekAndStartDateFlow,
        showWeekPickerFlow,
        weekCoursesFlow,
        dialogStateFlow,
        formStateFlow
    ) { weekAndStartDate, showWeekPicker, weekCourses, dialogState, formState ->
        val (selectedWeek, semesterStartDate) = weekAndStartDate
        CourseTableUiState(
            selectedWeek = selectedWeek,
            semesterStartDate = semesterStartDate,
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
        isDeletingFlow,
        isImportingFlow,
        importMessageFlow
    ) { coreState, selectionAndSaving, isDeleting, isImporting, importMessage ->
        coreState.copy(
            activeSelection = selectionAndSaving.first,
            isSaving = selectionAndSaving.second,
            isDeleting = isDeleting,
            isImporting = isImporting,
            importMessage = importMessage
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

    fun onSemesterStartDateChange(startDate: LocalDate) {
        val normalizedDate = startDate
        semesterStartDateFlow.value = normalizedDate
        SemesterStartDateStore.set(appContext, normalizedDate)
    }

    fun onCourseBlockClick(slot: CourseSlotVo) {
        activeSelectionFlow.value = CourseSelection.ExistingCourse(slot)
        dialogStateFlow.value = CourseDialogState.Detail(slot)
    }

    fun onEmptySlotClick(weekDay: Int, periodIndex: Int) {
        val week = selectedWeekFlow.value
        val defaultOption = buildSectionRangeOptions(periodIndex).first()
        activeSelectionFlow.value = CourseSelection.EmptySlot(
            weekDay = weekDay,
            periodIndex = periodIndex
        )
        formStateFlow.value = CourseFormState(
            weekDay = weekDay,
            periodIndex = periodIndex,
            startSection = defaultOption.startSection,
            sectionCount = defaultOption.sectionCount,
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
        val periodIndex = findPeriodIndexByStartSection(slot.startSection)
        formStateFlow.value = CourseFormState(
            weekDay = slot.weekDay,
            periodIndex = periodIndex,
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
        val periodIndex = findPeriodIndexByStartSection(slot.startSection)
        formStateFlow.value = CourseFormState(
            courseId = slot.courseId,
            sessionId = slot.sessionId,
            name = slot.courseName,
            teacher = slot.teacher,
            location = slot.location,
            weekDay = slot.weekDay,
            periodIndex = periodIndex,
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

    fun onSectionRangeChange(startSection: Int, sectionCount: Int) {
        val normalizedCount = sectionCount.coerceIn(2, 3)
        formStateFlow.value = formStateFlow.value.copy(
            startSection = startSection.coerceAtLeast(1),
            sectionCount = normalizedCount,
            errorMessage = null
        )
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

    fun onWeekRangeChange(start: Int, end: Int) {
        val normalizedStart = start.coerceIn(1, 20)
        val normalizedEnd = end.coerceIn(1, 20)
        formStateFlow.value = formStateFlow.value.copy(
            weekStart = minOf(normalizedStart, normalizedEnd),
            weekEnd = maxOf(normalizedStart, normalizedEnd),
            errorMessage = null
        )
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

    fun importCoursesFromWebPayload(rawJson: String) {
        viewModelScope.launch {
            isImportingFlow.value = true
            try {
                val importItems = JwxtCourseImportParser.parseImportItems(rawJson)
                if (importItems.isEmpty()) {
                    importMessageFlow.value = "未解析到可导入的课程数据"
                    return@launch
                }
                val result = repository.replaceAllCourses(importItems)
                importMessageFlow.value = "导入完成：${result.importedCourseCount} 门课，${result.importedSessionCount} 条排课"
            } catch (t: Throwable) {
                importMessageFlow.value = "导入失败：${t.message ?: "未知错误"}"
            } finally {
                isImportingFlow.value = false
            }
        }
    }

    fun clearAllCourses(onCompleted: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            isImportingFlow.value = true
            var success = false
            try {
                repository.clearAllCourses()
                importMessageFlow.value = "已删除全部课程"
                success = true
            } catch (t: Throwable) {
                importMessageFlow.value = "删除失败：${t.message ?: "未知错误"}"
            } finally {
                isImportingFlow.value = false
                onCompleted?.invoke(success)
            }
        }
    }

    fun consumeImportMessage() {
        importMessageFlow.value = null
    }
}
