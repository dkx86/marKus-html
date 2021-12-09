package ru.dkx86.markus

import java.io.File
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
        processCommand(cmd.trim().replace("\\s+".toRegex(), " "))
    } catch (e: Exception) {
        println("ERROR: ${e.message}")
    }

    listenInput()
}

fun processCommand(commandLine: String) {
    val parts = commandLine.split(" ")
    val arg = if (parts.size > 1) parts[1] else ""
    when (parts.first()) {
        "help" -> printHelp()
        "create" -> createNewProject()
        "show" -> showProject(arg)
        "list" -> listProjects()
        "convert" -> convertProjectToHtml(arg)
        "exit" -> exitProcess(0)
    }
}

fun printHelp() = println("Commands: list, create, show, edit, delete, help, convert, exit")

fun showProject(arg: String) {
    try {
        val index = arg.toInt()
        println("Project #$index:")
        println(projects[index].fullInfo())
    } catch (e: NumberFormatException) {
        println("Invalid argument '$arg'")
    }

}

fun convertProjectToHtml(arg: String) {
    try {
        val index = arg.toInt()
        val project = projects[index]
    } catch (e: NumberFormatException) {
        println("Invalid argument '$arg'")
    }

}

fun loadProjects() {
    println("Loading existed projects...")
    val lines = File(PROJECTS_FILE).readLines()
    projects.clear()
    projects.addAll(lines.map { loadProject(it) })
    println("${projects.size} projects loaded.")
}

fun listProjects() {
    println("Existed projects: ")
    projects.forEachIndexed { index, project -> println("$index: $project") }
}

fun createNewProject(): Project {
    print("Input weblog name: ")
    val projectName = readlnOrNull() ?: "weblog"

    print("Input weblog description: ")
    val projectDescription = readlnOrNull() ?: "my personal weblog"

    print("Input author name: ")
    val authorName = readlnOrNull() ?: "author"

    print("Input weblog tags: ")
    val projectTags = readlnOrNull() ?: ""

    val project = Project(projectName, projectDescription, authorName, projectTags)
    // save to file
    saveProject(PROJECTS_FILE, project)
    println("Project '$projectName' has been successfully saved.")
    return project
}

fun editProject(arg: String){
    TODO()
}

fun deleteProject(arg: String){
    TODO()
}
