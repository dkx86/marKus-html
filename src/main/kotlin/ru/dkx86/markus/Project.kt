package ru.dkx86.markus

import java.io.File

data class Project(
    val projectName: String,
    val projectDescription: String,
    val authorName: String,
    val projectTags: String


) {
    override fun toString(): String {
        return "$projectName ( $projectDescription )"
    }

    fun fullInfo() : String = " Name: $projectName ${System.lineSeparator()} Description: $projectDescription ${System.lineSeparator()} Author: $authorName ${System.lineSeparator()} Tags: $projectTags"
}

fun loadProject(str : String) : Project {
    val parts = str.split(';')
    return Project(parts[0], parts[1], parts[2], parts[3])
}

fun saveProject(fileName: String, project: Project) = File(fileName).run {
    createNewFile()
    writeText("${project.projectName};${project.projectDescription};${project.authorName};${project.projectTags}")
}

//fun loadProject(fileName: String): Project {
//    val line = File(fileName).readText()
//    val fields = line.split(";")
//    val project = Project(fields[0], fields[1], fields[2], fields[3])
//    println("Project '${project.projectName}' has been successfully loaded.")
//    return project
//}