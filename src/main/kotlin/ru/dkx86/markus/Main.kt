package ru.dkx86.markus

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.system.exitProcess

const val PROJECTS_FILE: String = "projects.csv"

val projects = mutableListOf<Project>()

fun main() {
    println("Hello! MarKus here! (^0^)/")
    loadProjects()
    listenInput()
}

fun listenInput() {
    print("> ")
    val cmd = readlnOrNull() ?: "help"
    try {
        processCommand(cmd.trim().removeMultiSpaces())
    } catch (e: Exception) {
        println("ERROR: ${e.message}")
        e.printStackTrace() // TODO
    }

    listenInput()
}

fun processCommand(commandLine: String) {
    val parts = commandLine.split(" ")
    val arg = if (parts.size > 1) parts[1] else ""
    when (parts.first()) {
        "help" -> printHelp()
        "license" -> printLicense()
        "create" -> createNewProject()
        "edit" -> validate(arg, ::editProject)
        "delete" -> validate(arg, ::deleteProject)
        "show" -> validate(arg, ::showProject)
        "list" -> listProjects()
        "convert" -> validate(arg, ::convertProjectToHtml)
        "exit" -> exitProcess(0)
    }
}

fun validate(arg: String, operation: (index: Int) -> Unit) {
    try {
        val index = arg.toInt()
        if (index in 0..projects.size) operation(index)
        else println("Project #$index not found")
    } catch (e: NumberFormatException) {
        println("Invalid argument '$arg'")
    }
}

fun showProject(index: Int) {
    println("Project #$index:")
    println(projects[index].fullInfo())
}

fun convertProjectToHtml(index: Int) {
    val project = projects[index]
    println("Start processing project #$index '${project.name}' ...")
    if (convertMd2Html(project)) println("Project #$index '${project.name}' successfully converted.")

}

fun loadProjects() {
    println("Loading existed projects...")
    val lines = File(PROJECTS_FILE).readLines()
    projects.clear()
    projects.addAll(lines.map { readProject(it) })
    println("${projects.size} projects loaded.")
}

fun saveProjects() {
    val content = StringBuilder()
    projects.forEach { p ->
        content.append("${p.name};${p.description};${p.authorName};${p.tags};${p.path}").append("\n")
    }

    File(PROJECTS_FILE).run {
        createNewFile()
        writeText(content.toString())
    }
}

fun readProject(line: String): Project {
    val parts = line.split(';')
    return Project(
        name = parts[0],
        description = parts[1],
        authorName = parts[2],
        tags = parts[3],
        path = Paths.get(parts[4]),
        url = parts[5]
    )
}

fun listProjects() {
    println("Existed projects: ")
    projects.forEachIndexed { index, project -> println("$index: $project") }
}

fun createNewProject() {
    print("Input project name: ")
    val projectName = readlnOrNull() ?: "weblog"

    print("Input weblog description: ")
    val projectDescription = readlnOrNull() ?: "my personal weblog"

    print("Input author name: ")
    val authorName = readlnOrNull() ?: "author"

    print("Input weblog tags: ")
    val projectTags = readlnOrNull() ?: ""

    print("Input project URL (need for RSS): ")
    val projectUrl = readlnOrNull() ?: ""

    val projectPath = Paths.get(PROJECTS_DIR, projectName.underscore()).createDirectories()
    projectPath.resolve(PROJECT_TEXT_DIR).createDirectories()
    projectPath.resolve(PROJECT_IMAGE_DIR).createDirectories()

    val url = if (projectUrl.endsWith('/')) projectUrl else "$projectUrl/"
    val project = Project(projectName, projectDescription, authorName, projectTags, projectPath, url)
    projects.add(project)

    saveProjects()
    println("Project '$projectName' has been successfully CREATED. The project's files must be located at '$projectPath' directory.")
}

fun editProject(index: Int) {
    val project = projects[index]
    println("Editing project #$index (${project.name}): ")

    print("Input project name (${project.name}): ")
    project.name = readlnOrNull() ?: project.name

    print("Input project description (${project.description}): ")
    project.description = readlnOrNull() ?: project.description

    print("Input author name (${project.authorName}): ")
    project.authorName = readlnOrNull() ?: project.authorName

    print("Input weblog tags (${project.tags}): ")
    project.tags = readlnOrNull() ?: project.tags

    // save to file
    saveProjects()
    println("Project #$index ('${project.name}') has been successfully SAVED.")
}

fun deleteProject(index: Int) {
    val project = projects[index]
    print("Delete project #$index with name '${project.name}'? (yes/NO): ")
    val answer = readlnOrNull() ?: "no"
    if (answer.lowercase() == "yes") {
        projects.removeAt(index)
        saveProjects()
        println("Project has been successfully DELETED.")
    }
}

fun printHelp() {
    println("'MarKus-html v0.1' a simple tool from making static html-weblog from markdown text.\n")
    println("Commands:\n")
    println("list       : list existed projects")
    println("show n     : show detailed information about project #n")
    println("create     : create new project")
    println("edit n     : edit project #n")
    println("delete n   : delete project #n")
    println("convert n  : convert project #n to html")
    println("exit       : exit the program")
    println("help       : print this help")
    println("license    : print license text")
}

fun printLicense() {
    println("MIT License\n" +
            "\n" +
            "Copyright (c) 2021 Dmitry Kuznetsov aka dkx86\n" +
            "\n" +
            "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
            "of this software and associated documentation files (the \"Software\"), to deal\n" +
            "in the Software without restriction, including without limitation the rights\n" +
            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
            "copies of the Software, and to permit persons to whom the Software is\n" +
            "furnished to do so, subject to the following conditions:\n" +
            "\n" +
            "The above copyright notice and this permission notice shall be included in all\n" +
            "copies or substantial portions of the Software.\n" +
            "\n" +
            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
            "SOFTWARE.")
}