package ru.dkx86.markus.rss

import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.stream.Format
import ru.dkx86.markus.IndexRecord
import ru.dkx86.markus.Project
import java.nio.file.Path
import java.time.LocalDateTime


const val RSS_FILE_NAME: String = "rss.xml"

fun generateRssFeed(project: Project, records: List<IndexRecord>, path: Path) {
    val rssItems = mutableListOf<RssItem>()
    val sortedRecords = records.sortedByDescending { it.date }
    for (record in sortedRecords) {
        val item = RssItem(
            title = record.title,
            description = record.summary,
            link = "${project.url}${record.fileName}",
            pubDate = record.date.toString()
        )
        rssItems.add(item)
    }

    publishFeed(createFeed(project, rssItems), path)
}


private fun publishFeed(feed: RssFeed, path: Path) {
    val format = Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>")
    val serializer: Serializer = Persister(format)
    serializer.write(feed, path.resolve(RSS_FILE_NAME).toFile())
}

private fun createFeed(project: Project, rssItems: List<RssItem>): RssFeed {
    val channel = RssChannel(
        title = project.name,
        link = project.url,
        description = project.description,
        copyright = project.authorName,
        lastBuildDate = LocalDateTime.now().toString(),
        items = rssItems
    )

    return RssFeed(channel)
}


