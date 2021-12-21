package ru.dkx86.markus.rss

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "item")
data class RssItem(
    @field:Element
    val title: String,
    @field:Element
    val description: String,
    @field:Element
    val link: String,
    @field:Element
    val pubDate: String,
)