package com.example.coursetable.domain.model

object CourseColorPalette {
    // 10 preset colors for consistent course color selection.
    val presetColors: List<Int> = listOf(
        0xFFE57373.toInt(),
        0xFF64B5F6.toInt(),
        0xFF81C784.toInt(),
        0xFFFFB74D.toInt(),
        0xFFBA68C8.toInt(),
        0xFFFF8A65.toInt(),
        0xFF4DB6AC.toInt(),
        0xFFA1887F.toInt(),
        0xFF90A4AE.toInt(),
        0xFFFFD54F.toInt()
    )

    const val emptySlotColor: Int = 0x1A9CA3AF
}

