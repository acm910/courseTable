package com.example.coursetable.feature.webView

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JwxtCourseImportParserTest {

    @Test
    fun parseImportItems_extractsUsefulRows() {
        val json = """
            {
              "code": "0",
              "datas": {
                "cxxskcb": {
                  "rows": [
                    {
                      "KCM": "微机原理与通信接口A",
                      "SKJS": "黄涛,程鑫",
                      "JASMC": "南湖北-北教三-503",
                      "SKXQ": 3,
                      "KSJC": 11,
                      "JSJC": 12,
                      "ZCMC": "9-16周"
                    },
                    {
                      "KCM": "形势与政策",
                      "SKJS": "潘星",
                      "JASMC": "南湖南-博学主楼-310",
                      "SKXQ": 4,
                      "KSJC": 6,
                      "JSJC": 7,
                      "ZCMC": "13-16周"
                    }
                  ]
                }
              }
            }
        """.trimIndent()

        val items = JwxtCourseImportParser.parseImportItems(json)
        assertEquals(2, items.size)

        assertEquals("微机原理与通信接口A", items[0].courseName)
        assertEquals(3, items[0].weekDay)
        assertEquals(11, items[0].startSection)
        assertEquals(2, items[0].sectionCount)
        assertEquals(9, items[0].weekStart)
        assertEquals(16, items[0].weekEnd)

        assertEquals("形势与政策", items[1].courseName)
        assertEquals(4, items[1].weekDay)
        assertEquals(6, items[1].startSection)
        assertEquals(2, items[1].sectionCount)
    }

    @Test
    fun parseImportItems_invalidJson_returnsEmpty() {
        val items = JwxtCourseImportParser.parseImportItems("{}")
        assertTrue(items.isEmpty())
    }
}

