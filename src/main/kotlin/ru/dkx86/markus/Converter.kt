package ru.dkx86.markus

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

const val PUBLISHED_DIR: String = "published"

const val DEFAULT_TEMPLATE_NAME: String = "simple"

const val TEMPLATES_DIR: String = "templates"
const val IMAGE_DIR: String = "img"
const val CSS_DIR: String = "css"
const val TEXT_DIR: String = "text"

const val TEMPLATE_FILE_NAME: String = "template.html"
const val TEMPLATE_INDEX_CARD_FILE_NAME: String = "tmp_index_card.html"

const val PLACEHOLDER_PROJECT_NAME: String = "{PROJECT_NAME}"
const val PLACEHOLDER_PAGE_TITLE: String = "{PAGE_TITLE}"
const val PLACEHOLDER_PAGE_CONTENT: String = "{PAGE_CONTENT}"
const val PLACEHOLDER_COPYRIGHT_YEAR: String = "{COPYRIGHT_YEAR}"
const val PLACEHOLDER_COPYRIGHT_AUTHOR_NAME: String = "{COPYRIGHT_AUTHOR_NAME}"
const val PLACEHOLDER_PROJECT_KEYWORDS: String = "{PROJECT_KEYWORDS}"

const val PLACEHOLDER_RECORD_TYPE: String = "{RECORD_TYPE}"
const val PLACEHOLDER_RECORD_TITLE: String = "{RECORD_TITLE}"
const val PLACEHOLDER_RECORD_SUMMARY_TEXT: String = "{RECORD_SUMMARY_TEXT}"
const val PLACEHOLDER_RECORD_DATE: String = "{RECORD_DATE}"
const val PLACEHOLDER_RECORD_LINK: String = "{RECORD_LINK}"

const val SUMMARY_KEYWORD: String = "SUMMARY>"


fun convertMd2Html(project: Project, templateName: String = DEFAULT_TEMPLATE_NAME): Boolean {
    val projectPublishDir = Paths.get(PUBLISHED_DIR, project.name.underscore()).createDirectories()

    val (template, record) = loadTemplate(templateName)

    val flavour = CommonMarkFlavourDescriptor()
    val files = project.path.resolve(PROJECT_TEXT_DIR).toFile().listFiles() ?: run {
        println("No text files for project '${project.name}' found.")
        return false
    }

    val pageTemplate = template.replace(PLACEHOLDER_PROJECT_NAME, project.name)
        .replace(PLACEHOLDER_COPYRIGHT_YEAR, "2021")
        .replace(PLACEHOLDER_COPYRIGHT_AUTHOR_NAME, project.authorName)
        .replace(PLACEHOLDER_PROJECT_KEYWORDS, project.tags)

    val indexPageBuilder = StringBuilder()
    println("Start processing text files.")
    var processedFiles = 0
    for (file in files.sortedDescending()) {
        val article = compileArticle(file, pageTemplate, flavour)
        projectPublishDir.resolve(article.fileName).writeText(article.content)

        val indexRecord = record.replace(PLACEHOLDER_RECORD_TITLE, article.title)
            .replace(PLACEHOLDER_RECORD_DATE, article.date)
            .replace(PLACEHOLDER_RECORD_TYPE, article.type)
            .replace(PLACEHOLDER_RECORD_SUMMARY_TEXT, article.summary)
            .replace(PLACEHOLDER_RECORD_LINK, article.fileName)

        indexPageBuilder.append(indexRecord)
        println(" > (${++processedFiles}/${files.size})  processed.")
    }

    val indexHtml = pageTemplate.replace(PLACEHOLDER_PAGE_TITLE, project.name)
        .replace(PLACEHOLDER_PAGE_CONTENT, indexPageBuilder.toString())
    projectPublishDir.resolve("index.html").writeText(indexHtml)

    println("Copying template files...")
    copyTemplateServiceFiles(templateName, projectPublishDir)
    println("Copying images...")
    copyImages(project.path, projectPublishDir)

    println("Project '${project.name}' saved to ${projectPublishDir.absolutePathString()}.")
    return true
}

fun copyImages(projectPath: Path, projectPublishDir: Path) {
    val source = projectPath.resolve(IMAGE_DIR).toFile().listFiles() ?: return
    val destination = projectPublishDir.resolve(IMAGE_DIR)
    for (imageFile in source) {
        imageFile.copyTo(destination.resolve(imageFile.name).toFile(), true)
    }
}

fun copyTemplateServiceFiles(templateName: String, projectDir: Path) {
    val templateDir = Paths.get(TEMPLATES_DIR, templateName)
    templateDir.resolve(IMAGE_DIR).toFile().copyRecursively(projectDir.resolve(IMAGE_DIR).toFile(), true)
    templateDir.resolve(CSS_DIR).toFile().copyRecursively(projectDir.resolve(CSS_DIR).toFile(), true)
}

fun compileArticle(file: File, pageTemplate: String, flavour: CommonMarkFlavourDescriptor): Article {
    val title = file.useLines { it.first() }.replace("#*".toRegex(), "").trim()
    val summary = file.useLines { it.firstOrNull { s -> s.startsWith(SUMMARY_KEYWORD) } }
        ?.replace("$SUMMARY_KEYWORD\\s*".toRegex(), "")?.trim()
    val text = file.readText()

    val srcFileName = file.nameWithoutExtension
    val parts = srcFileName.split("__")

    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(text)
    val html = HtmlGenerator(text, parsedTree, flavour).generateHtml()
    val page = pageTemplate.replace(PLACEHOLDER_PAGE_TITLE, title).replace(PLACEHOLDER_PAGE_CONTENT, html)

    return Article(
        title = title,
        date = parts[0],
        type = parts[1],
        content = page,
        summary = summary?:"",
        fileName = "${parts[2]}.html"
    )
    
}

fun loadTemplate(templateName: String): Pair<String, String> {
    val dir = Paths.get(TEMPLATES_DIR, templateName)
    val pageTemplate = dir.resolve(TEMPLATE_FILE_NAME).toFile().readText();
    val indexCardTemplate = dir.resolve(TEMPLATE_INDEX_CARD_FILE_NAME).toFile().readText();
    return Pair(pageTemplate, indexCardTemplate)
}

