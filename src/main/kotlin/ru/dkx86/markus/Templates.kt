package ru.dkx86.markus

import java.nio.file.Paths

data class Templates(val pageTemplate: String, val indexCardTemplate: String, val typeLinkTemplate: String)

const val TEMPLATES_DIR: String = "templates"
const val TEMPLATE_FILE_NAME: String = "template.html"
const val TEMPLATE_INDEX_CARD_FILE_NAME: String = "tmp_index_card.html"
const val TEMPLATE_RECORD_TYPE_LINK: String = "tmp_record_type_link.html"

fun loadTemplates(templateName: String): Templates {
    val dir = Paths.get(TEMPLATES_DIR, templateName)
    val pageTemplate = dir.resolve(TEMPLATE_FILE_NAME).toFile().readText()
    val indexCardTemplate = dir.resolve(TEMPLATE_INDEX_CARD_FILE_NAME).toFile().readText()
    val typeLinkTemplate = dir.resolve(TEMPLATE_RECORD_TYPE_LINK).toFile().readText()
    return Templates(pageTemplate, indexCardTemplate, typeLinkTemplate)
}