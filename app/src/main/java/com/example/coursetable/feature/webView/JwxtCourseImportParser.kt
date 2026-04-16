package com.example.coursetable.feature.webView

import com.example.coursetable.domain.model.CourseImportItemVo
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object JwxtCourseImportParser {
    private val weekRangeRegex = Regex("(\\d+)(?:-(\\d+))?周")

    fun parseImportItems(rawJson: String): List<CourseImportItemVo> {
        val root = JsonParser.parseString(rawJson)
        if (!root.isJsonObject) return emptyList()

        val rows = findRows(root.asJsonObject) ?: return emptyList()
        return rows.mapNotNull { parseRow(it) }
    }

    private fun findRows(root: JsonObject): JsonArray? {
        val cxxskcbRows = root
            .optObject("cxxskcb")
            ?.optArray("rows")
        if (cxxskcbRows != null) return cxxskcbRows

        val nestedRows = root
            .optObject("datas")
            ?.optObject("cxxskcb")
            ?.optArray("rows")
        if (nestedRows != null) return nestedRows

        return root.optArray("rows")
    }

    private fun parseRow(element: JsonElement): CourseImportItemVo? {
        if (!element.isJsonObject) return null
        val row = element.asJsonObject

        val courseName = row.optString("KCM")
        if (courseName.isBlank()) return null

        val teacher = row.optString("SKJS")
        val location = row.optString("JASMC")
        val weekDay = row.optInt("SKXQ", -1).takeIf { it in 1..7 } ?: parseWeekDay(row.optString("SKXQ_DISPLAY"))
        if (weekDay !in 1..7) return null

        var startSection = row.optInt("KSJC", -1)
        var endSection = row.optInt("JSJC", -1)
        if (startSection <= 0 || endSection < startSection) return null
        if (startSection in 6..7 || startSection == 13 || endSection in 6..7 || endSection == 13) return null
        // 忽略中课1-2，startSection和endSection都减去对应的偏移量
        if(startSection in 8..12 && endSection <= 12) {
            startSection -= 2
            endSection -=2
        }

        // 忽略晚课
        if(startSection in 14..16 && endSection <=16){
            startSection -= 3
            endSection -= 3
        }

        val weekRange = parseWeekRange(row.optString("ZCMC"))
            ?: parseWeekRange(row.optString("YPSJDD"))
            ?: (1 to 20)

        return CourseImportItemVo(
            courseName = courseName,
            teacher = teacher,
            location = location,
            weekDay = weekDay,
            startSection = startSection,
            sectionCount = endSection - startSection + 1,
            weekStart = weekRange.first,
            weekEnd = weekRange.second
        )
    }

    private fun parseWeekRange(rawText: String): Pair<Int, Int>? {
        if (rawText.isBlank()) return null
        val match = weekRangeRegex.find(rawText) ?: return null
        val start = match.groupValues[1].toIntOrNull() ?: return null
        val end = match.groupValues[2].toIntOrNull() ?: start
        return minOf(start, end) to maxOf(start, end)
    }

    private fun parseWeekDay(displayText: String): Int {
        return when {
            "一" in displayText -> 1
            "二" in displayText -> 2
            "三" in displayText -> 3
            "四" in displayText -> 4
            "五" in displayText -> 5
            "六" in displayText -> 6
            "日" in displayText || "天" in displayText -> 7
            else -> -1
        }
    }

    private fun JsonObject.optObject(key: String): JsonObject? {
        val value = this.get(key) ?: return null
        return if (value.isJsonObject) value.asJsonObject else null
    }

    private fun JsonObject.optArray(key: String): JsonArray? {
        val value = this.get(key) ?: return null
        return if (value.isJsonArray) value.asJsonArray else null
    }

    private fun JsonObject.optString(key: String): String {
        val value = this.get(key) ?: return ""
        return if (value.isJsonNull) "" else value.asString.orEmpty()
    }

    private fun JsonObject.optInt(key: String, defaultValue: Int): Int {
        val value = this.get(key) ?: return defaultValue
        return if (value.isJsonNull) {
            defaultValue
        } else {
            value.asInt
        }
    }
}
