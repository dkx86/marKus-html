package ru.dkx86.markus

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.system.exitProcess

const val PROJECTS_FILE: String = "projects.csv"

val projects = mutableListOf<Project>()

fun main(args: Array<String>) {
    println("Hello, marKus here! ^^/")
    loadProjects()
    if(args.isEmpty()){
        listenInput()
    }else{
        processProgramArgs(args)
    }
}

fun processProgramArgs(args: Array<String>){
    if(args[0] != "-d" || args.size < 5 || args[1] != "-p"){
        println("ERROR: Invalid params")
        return
    }

    val projectNumber = args[2].toInt()
    val templateName =  if(args[3] == "-t") args[4]  else "simple"
    convertProjectToHtml(projectNumber, templateName)
}

fun listenInput() {
    print("> ")
    val cmd = readlnOrNull() ?: "help"
    try {
        processCommand(cmd.trim().removeMultiSpaces())
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
        println("-ERROR- Invalid argument '$arg'")
    }
}

fun showProject(index: Int) {
    println("Project #$index:")
    println(projects[index].fullInfo())
}

fun convertProjectToHtml(index: Int) {
    println("Input template dir name (default: 'simple'):")
    val templateName = readInput("simple")
    convertProjectToHtml(index, templateName)
}

fun convertProjectToHtml(index: Int, templateName : String = "simple") {
    val project = projects[index]
    println("Start processing project #$index '${project.name}' with template '$templateName' ...")
    if (convertMd2Html(project, templateName)) println("Project #$index '${project.name}' successfully converted.")

}

fun loadProjects() {
    println("Loading existed projects...")
    projects.clear()
    val file = File(PROJECTS_FILE)
    if (file.exists()) {
        val lines = File(PROJECTS_FILE).readLines()
        try {
            projects.addAll(lines.map { fromCSV(it) })
        } catch (e: Exception) {
            println(e.message)
        }
    }

    println("${projects.size} projects loaded.")
}

fun saveProjects() {
    val content = StringBuilder()
    projects.forEach { p ->
        content.append(p.toCSV()).append("\n")
    }

    File(PROJECTS_FILE).run {
        createNewFile()
        writeText(content.toString())
    }
}

fun listProjects() {
    println("Existed projects: ")
    projects.forEachIndexed { index, project -> println("$index: $project") }
}

fun createNewProject() {
    print("Input project name: ")
    val projectName = readInput("weblog")

    print("Input weblog description: ")
    val projectDescription = readInput("my personal weblog")

    print("Input author name: ")
    val authorName = readInput("author")

    print("Input weblog tags: ")
    val projectTags = readInput("")

    print("Input project URL (need for RSS): ")
    val projectUrl = readInput("")

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
    project.name = readInput(project.name)

    print("Input project description (${project.description}): ")
    project.description = readInput(project.description)

    print("Input author name (${project.authorName}): ")
    project.authorName = readInput(project.authorName)

    print("Input weblog tags (${project.tags}): ")
    project.tags = readInput(project.tags)

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

fun readInput(default: String): String {
    val input = readlnOrNull() ?: return default
    return input.ifBlank { default }
}

fun printHelp() {
    println("'MarKus-html v0.2' a simple tool from making static html-weblog from markdown text.\n")
    println("Params:\n")
    println("-h         Print this help")
    println("-d         Run in non-interactive mode to convert one existed project")
    println("-p         Project number")
    println("-t         Template name (directory name)")
    println("Example: '-d -p 0 -t simple' - convert project #0 with template 'simple'\n")

    println("Commands:\n")
    println("list         List existed projects")
    println("show n       Show detailed information about project #n")
    println("create       Create new project")
    println("edit n       Edit project #n")
    println("delete n     Delete project #n")
    println("convert n    Convert project #n to html")
    println("exit         Exit the program")
    println("help         Print this help")
    println("license      Print license text")
}

fun printLicense() {
    println(
        "MIT License\n" +
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
                "SOFTWARE."
    )
}