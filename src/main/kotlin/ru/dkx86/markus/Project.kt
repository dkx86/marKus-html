package ru.dkx86.markus

data class Project(
    var name: String,
    var description: String,
    var authorName: String,
    var tags: String
) {

    override fun toString(): String {
        return "$name ( $description )"
    }

    fun fullInfo(): String =
        " Name: $name ${System.lineSeparator()} Description: $description ${System.lineSeparator()} Author: $authorName ${System.lineSeparator()} Tags: $tags"
}
