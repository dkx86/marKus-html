package ru.dkx86.markus

import ru.dkx86.markus.rss.generateRssFeed
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

const val PUBLISHED_DIR: String = "published"

const val DEFAULT_TEMPLATE_NAME: String = "simple"

const val IMAGE_DIR: String = "img"
const val CSS_DIR: String = "css"
const val JS_DIR: String = "js"

const val PLACEHOLDER_PROJECT_NAME: String = "{PROJECT_NAME}"
const val PLACEHOLDER_PAGE_TITLE: String = "{PAGE_TITLE}"
const val PLACEHOLDER_PAGE_CONTENT: String = "{PAGE_CONTENT}"
const val PLACEHOLDER_COPYRIGHT_YEAR: String = "{COPYRIGHT_YEAR}"
const val PLACEHOLDER_COPYRIGHT_AUTHOR_NAME: String = "{COPYRIGHT_AUTHOR_NAME}"
const val PLACEHOLDER_PROJECT_KEYWORDS: String = "{PROJECT_KEYWORDS}"

const val RECORD_TYPE_LINK_FILE: String = "{RECORD_TYPE_LINK_FILE}"
const val RECORD_TYPE_LINK_TITLE: String = "{RECORD_TYPE_LINK_TITLE}"
const val RECORD_TYPES: String = "{RECORD_TYPES}"

const val SUMMARY_KEYWORD: String = "SUMMARY>"

fun convertMd2Html(project: Project, templateName: String = DEFAULT_TEMPLATE_NAME): Boolean {
    val projectPublishDir = Paths.get(PUBLISHED_DIR, project.name.underscore()).createDirectories()

    val files = project.path.resolve(PROJECT_TEXT_DIR).toFile().listFiles() ?: run {
        println("No text files for project '${project.name}' found.")
        return false
    }

    val indexRecordsMap = mutableMapOf<String, MutableList<IndexRecord>>()
    val articles = mutableListOf<Article>()
    val articleTypes = mutableSetOf<String>()
    var copyrightMinYear = LocalDate.now().year

    println("Start processing text files.")
    val templates = loadTemplates(templateName)
    val parser = MarkdownParser()

    for ((processedFiles, file) in files.sortedDescending().withIndex()) {
        val article = compileArticle(file, parser)
        val indexRecord = IndexRecord(
            template = templates.indexCardTemplate,
            title = article.title,
            date = article.date,
            type = article.type,
            summary = article.summary,
            fileName = article.fileName
        )

        indexRecordsMap.getOrPut(indexRecord.type) { mutableListOf() }.add(indexRecord)
        articles.add(article)

        if (article.date.year < copyrightMinYear) copyrightMinYear = article.date.year
        articleTypes.add(article.type)

        println(" > (${processedFiles + 1}/${files.size})  processed.")
    }

    val currentYear = LocalDate.now().year
    val copyrightYear = if (copyrightMinYear < currentYear) "$copyrightMinYear - $currentYear" else "$currentYear"
    val page = templates.pageTemplate.replace(PLACEHOLDER_PROJECT_NAME, project.name)
        .replace(PLACEHOLDER_COPYRIGHT_YEAR, copyrightYear)
        .replace(PLACEHOLDER_COPYRIGHT_AUTHOR_NAME, project.authorName)
        .replace(PLACEHOLDER_PROJECT_KEYWORDS, project.tags)
        .replace(RECORD_TYPES, compileTypes(indexRecordsMap.keys, templates.typeLinkTemplate))

    println("Saving articles...")
    saveArticles(page, projectPublishDir, articles)

    println("Saving index...")
    saveIndexFiles(page, project.name, projectPublishDir, indexRecordsMap)
    println("Generating RSS...")
    generateRssFeed(project, indexRecordsMap.flatMap { it.value }, projectPublishDir)

    println("Copying template files...")
    copyTemplateServiceFiles(templateName, projectPublishDir)
    println("Copying images...")
    copyImages(project.path, projectPublishDir)

    println("Project '${project.name}' saved to ${projectPublishDir.absolutePathString()}.")
    return true
}

private fun compileTypes(types: Set<String>, typeLinkTemplate: String): String {
    val builder = StringBuilder()
    for (type in types) {
        val html = typeLinkTemplate.replace(RECORD_TYPE_LINK_TITLE, type.uppercase())
            .replace(RECORD_TYPE_LINK_FILE, "${type}_index.html")
        builder.append(html)
    }
    return builder.toString()
}

private fun saveArticles(pageTemplate: String, projectPublishDir: Path, articles: List<Article>) {
    for (article in articles) {
        val content = pageTemplate.replace(PLACEHOLDER_PAGE_TITLE, article.title)
            .replace(PLACEHOLDER_PAGE_CONTENT, article.content)
        projectPublishDir.resolve(article.fileName).writeText(content)
    }
}

private fun saveIndexFiles(
    page: String,
    projectName: String,
    projectPublishDir: Path,
    indexRecordsMap: Map<String, List<IndexRecord>>
) {
    // full index
    saveIndexFile(
        records = indexRecordsMap.flatMap { it.value },
        pageTitle = projectName.uppercase(),
        page = page,
        projectPublishDir = projectPublishDir,
        fileName = "index"
    )

    // by type
    for (entry in indexRecordsMap) {
        saveIndexFile(
            records = entry.value,
            pageTitle = "${entry.key.uppercase()} | ${projectName.uppercase()}",
            page = page,
            projectPublishDir = projectPublishDir,
            fileName = "${entry.key}_index"
        )
    }
}

private fun saveIndexFile(
    records: List<IndexRecord>,
    pageTitle: String,
    page: String,
    projectPublishDir: Path,
    fileName: String
) {
    val total = records.sortedByDescending { it.date }
    val indexPageBuilder = StringBuilder()
    for (record in total) {
        indexPageBuilder.append(record.toString())
    }
    val indexHtml = page.replace(PLACEHOLDER_PAGE_TITLE, pageTitle)
        .replace(PLACEHOLDER_PAGE_CONTENT, indexPageBuilder.toString())
    projectPublishDir.resolve("$fileName.html").writeText(indexHtml)
}

private fun copyImages(projectPath: Path, projectPublishDir: Path) {
    val source = projectPath.resolve(IMAGE_DIR).toFile().listFiles() ?: return
    val destination = projectPublishDir.resolve(IMAGE_DIR)
    for (imageFile in source) {
        imageFile.copyTo(destination.resolve(imageFile.name).toFile(), true)
    }
}

private fun copyTemplateServiceFiles(templateName: String, projectDir: Path) {
    val templateDir = Paths.get(TEMPLATES_DIR, templateName)
    val imgDir = templateDir.resolve(IMAGE_DIR)
    if (imgDir.exists()) imgDir.toFile().copyRecursively(projectDir.resolve(IMAGE_DIR).toFile(), true)

    val cssDir = templateDir.resolve(CSS_DIR)
    if (cssDir.exists()) cssDir.toFile().copyRecursively(projectDir.resolve(CSS_DIR).toFile(), true)

    val jsDir = templateDir.resolve(JS_DIR)
    if (jsDir.exists()) jsDir.toFile().copyRecursively(projectDir.resolve(JS_DIR).toFile(), true)
}

private fun compileArticle(file: File, parser: MarkdownParser): Article {
    val title = file.useLines { it.first() }.replace("#*".toRegex(), "").trim()
    val summary = file.useLines { it.firstOrNull { s -> s.startsWith(SUMMARY_KEYWORD) } }
        ?.replace("$SUMMARY_KEYWORD\\s*".toRegex(), "")?.trim()

    val text = file.readText().replace("$SUMMARY_KEYWORD\\s*".toRegex(), "").trim()

    val srcFileName = file.nameWithoutExtension
    val parts = srcFileName.split("__")
    val html = parser.md2html(text).replace("<img ", "<img class=\"w3-image centered_image\" ")

    return Article(
        title = title,
        date = parseDate(parts[0]),
        type = parts[1],
        content = html,
        summary = summary ?: "",
        fileName = "${parts[2]}.html"
    )

}

private fun parseDate(str: String): LocalDate =
    LocalDate.parse(str, DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: LocalDate.now()




