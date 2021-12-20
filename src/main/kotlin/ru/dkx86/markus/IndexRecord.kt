package ru.dkx86.markus

import java.time.LocalDate

const val PLACEHOLDER_RECORD_TYPE: String = "{RECORD_TYPE}"
const val PLACEHOLDER_RECORD_TITLE: String = "{RECORD_TITLE}"
const val PLACEHOLDER_RECORD_SUMMARY_TEXT: String = "{RECORD_SUMMARY_TEXT}"
const val PLACEHOLDER_RECORD_DATE: String = "{RECORD_DATE}"
const val PLACEHOLDER_RECORD_LINK: String = "{RECORD_LINK}"
const val PLACEHOLDER_RECORD_TYPE_LINK: String = "{RECORD_TYPE_LINK}"

class IndexRecord(
    val template: String,
    val title: String,
    val summary: String,
    val date: LocalDate,
    val type: String,
    val fileName: String
) {

    override fun toString(): String {
        return template.replace(PLACEHOLDER_RECORD_TITLE, title)
            .replace(PLACEHOLDER_RECORD_SUMMARY_TEXT, summary)
            .replace(PLACEHOLDER_RECORD_DATE, date.toString())
            .replace(PLACEHOLDER_RECORD_TYPE, type.uppercase())
            .replace(PLACEHOLDER_RECORD_TYPE_LINK, "${type}_index.html")
            .replace(PLACEHOLDER_RECORD_LINK, fileName)
    }
}


