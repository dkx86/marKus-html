package ru.dkx86.markus

import java.nio.file.Path
import kotlin.io.path.absolutePathString

const val PROJECTS_DIR: String = "projects"
const val PROJECT_TEXT_DIR: String = "text"
const val PROJECT_IMAGE_DIR: String = "img"

data class Project(
    var name: String,
    var description: String,
    var authorName: String,
    var tags: String,
    var path: Path
) {

    override fun toString(): String {
        return "$name ( $description )"
    }

    fun fullInfo(): String =
        " Name: $name ${System.lineSeparator()} Description: $description ${System.lineSeparator()} Author: $authorName ${System.lineSeparator()} Tags: $tags ${System.lineSeparator()} Path: ${path.absolutePathString()}"
}
