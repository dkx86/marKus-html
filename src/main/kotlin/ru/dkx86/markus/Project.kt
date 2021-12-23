package ru.dkx86.markus

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

const val PROJECTS_DIR: String = "projects"
const val PROJECT_TEXT_DIR: String = "text"
const val PROJECT_IMAGE_DIR: String = "img"

data class Project(
    var name: String,
    var description: String,
    var authorName: String,
    var tags: String,
    var path: Path,
    var url: String
) {

    override fun toString(): String {
        return "$name ( $description ) at $url"
    }

    fun fullInfo(): String =
        " Name: $name ${System.lineSeparator()} Description: $description ${System.lineSeparator()} Author: $authorName ${System.lineSeparator()} Tags: $tags ${System.lineSeparator()} Path: ${path.absolutePathString()} ${System.lineSeparator()} URL: $url"

    fun toCSV() : String = "${name};${description};${authorName};${tags};${path};${url}"
}

fun fromCSV(csv : String) : Project {
    val parts = csv.split(';')
    if(parts.size != 6) throw Exception("-ERROR- Incorrect record in 'projects.csv': $csv")
    return Project(
        name = parts[0],
        description = parts[1],
        authorName = parts[2],
        tags = parts[3],
        path = Paths.get(parts[4]),
        url = parts[5]
    )
}