package ru.dkx86.markus

import com.vladsch.flexmark.ext.gitlab.GitLabExtension
import com.vladsch.flexmark.ext.media.tags.MediaTagsExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet

class MarkdownParser {
    private val parser: Parser
    private val renderer: HtmlRenderer

    init {
        val options = MutableDataSet()
        val extensions = listOf(GitLabExtension.create(), MediaTagsExtension.create())
        options.set(Parser.EXTENSIONS, extensions)
        parser = Parser.builder(options).build()
        renderer = HtmlRenderer.builder(options).build()
    }

    fun md2html(text: String): String = renderer.render(parser.parse(text))
}