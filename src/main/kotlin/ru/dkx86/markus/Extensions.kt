package ru.dkx86.markus

fun String.removeMultiSpaces() : String = this.replace("\\s+".toRegex(), " ")
fun String.underscore() : String = this.replace("\\s+".toRegex(), "_")