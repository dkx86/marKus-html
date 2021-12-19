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
        "create" -> createNewProject()
        "edit" -> validate(arg, ::editProject)
        "delete" -> validate(arg, ::deleteProject)
        "show" -> validate(arg, ::showProject)
        "list" -> listProjects()
        "convert" -> validate(arg, ::convertProjectToHtml)
        "exit" -> exitProcess(0)
    }
}

fun printHelp() = println("Commands: list, create, show, edit, delete, help, convert, exit")

fun validate(arg : String, operation : (index: Int) -> Unit) {
    try {
        val index = arg.toInt()
        if(index in 0..projects.size) operation(index)
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
    if(convertMd2Html(project)) println("Project #$index '${project.name}' successfully converted.")

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
    projects.forEach { p -> content.append("${p.name};${p.description};${p.authorName};${p.tags};${p.path}").append("\n") }

    File(PROJECTS_FILE).run {
        createNewFile()
        writeText(content.toString())
    }
}

fun readProject(line: String): Project {
    val parts = line.split(';')
    return Project(parts[0], parts[1], parts[2], parts[3], Paths.get(parts[4]))
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

    val projectPath = Paths.get(PROJECTS_DIR, projectName.underscore()).createDirectories()
    projectPath.resolve(PROJECT_TEXT_DIR).createDirectories()
    projectPath.resolve(PROJECT_IMAGE_DIR).createDirectories()

    val project = Project(projectName, projectDescription, authorName, projectTags, projectPath)
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
    if(answer.lowercase() == "yes") {
        projects.removeAt(index)
        saveProjects()
        println("Project has been successfully DELETED.")
    }
}
