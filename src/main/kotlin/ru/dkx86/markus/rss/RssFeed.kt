package ru.dkx86.markus.rss

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "rss")
data class RssFeed(
    @field:Element(name = "channel")
    val channel: RssChannel,

    @field:Attribute(name = "version")
    val version: String = "2.0"
)

@Root
data class RssChannel(
    @field:Element
    val title: String,
    @field:Element
    val link: String,
    @field:Element
    val description: String,
    @field:Element
    val copyright: String,
    @field:Element
    val lastBuildDate: String,
    @field:ElementList(entry="item", inline=true)
    val items : List<RssItem>
)
