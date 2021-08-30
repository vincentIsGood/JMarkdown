package com.vincentcodes.tests.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.markdown.inline.TextNode;
import com.vincentcodes.tests.markdown.mock.SimpleRenderer;
import com.vincentcodes.tests.markdown.mock.SimpleRenderer.Container;

import org.junit.jupiter.api.Test;

public class MarkdownParserMainTest {
    @Test
    void testParse_heading_paragraph() {
        var renderer = new SimpleRenderer();
        var parser = new MarkdownParser(renderer);
        parser.parse("### asdsad\nawds*asd&*##ads\n\n#Heading1");

        List<Container> result = renderer.body;
        assertEquals("h3", result.get(0).type);
        assertEquals("asdsad", ((TextNode)result.get(0).value).groups.get(0).value);

        assertEquals("p", result.get(1).type);
        assertEquals("asd&", ((TextNode)result.get(1).value).groups.get(1).value);
        assertTrue(((TextNode)result.get(1).value).groups.get(1).isEmphasis);

        assertEquals("h1", result.get(2).type);
        assertEquals("Heading1", ((TextNode)result.get(2).value).groups.get(0).value);
    }

    @Test
    void testParse_blockquote_blockcode() {
        var renderer = new SimpleRenderer();
        var parser = new MarkdownParser(renderer);
        parser.parse(">asd\nasd\n>>asd\n\n```js\nlet a = '';\n```");

        List<Container> result = renderer.body;
        assertEquals("blockquote(1)", result.get(0).type);
        assertEquals("asd asd", ((TextNode)result.get(0).value).groups.get(0).value);
        
        assertEquals("blockquote(2)", result.get(1).type);

        assertEquals("codeblock(js)", result.get(2).type);
        assertEquals("let a = '';", (String)result.get(2).value);
    }

    @Test
    void testParse_ordered_list() {
        var renderer = new SimpleRenderer();
        var parser = new MarkdownParser(renderer);
        parser.parse("1. a*sd,2. qw*e\n2. re*al");

        List<Container> result = renderer.body;
        assertEquals("ol", result.get(0).type);

        TextNode[] listItems = (TextNode[])result.get(0).value;
        assertEquals("sd,2. qw", listItems[0].groups.get(1).value);
        assertTrue(listItems[0].groups.get(1).isEmphasis);
        
        assertEquals("re*al", listItems[1].groups.get(0).value);
    }

    @Test
    void testParse_unordered_list() {
        var renderer = new SimpleRenderer();
        var parser = new MarkdownParser(renderer);
        parser.parse("- **asd,- qwe**\n- re**al");

        List<Container> result = renderer.body;
        assertEquals("ul", result.get(0).type);

        TextNode[] listItems = (TextNode[])result.get(0).value;
        assertEquals("asd,- qwe", listItems[0].groups.get(0).value);
        assertTrue(listItems[0].groups.get(0).isStrong);
        
        assertEquals("re**al", listItems[1].groups.get(0).value);
    }

    @Test
    void testParse_table() {
        var renderer = new SimpleRenderer();
        var parser = new MarkdownParser(renderer);
        parser.parse("""
        |a|**b**|cd|
        |-|-|-|-|-
        |~~1~~|2|f|e|ads
        |3|4|f|asd
        |a|b|f|asd
        *asd*""");

        List<Container> result = renderer.body;
        System.out.println(result);
        assertEquals("table", result.get(0).type);
        assertEquals("a", ((TextNode[])result.get(0).value)[0].groups.get(0).value);
        assertEquals("b", ((TextNode[])result.get(0).value)[1].groups.get(0).value);
        assertTrue(((TextNode[])result.get(0).value)[1].groups.get(0).isStrong);
        
        assertEquals("tr", result.get(1).type);
        assertEquals("1", ((TextNode[])result.get(1).value)[0].groups.get(0).value);
        assertTrue(((TextNode[])result.get(1).value)[0].groups.get(0).isStrikeThrough);
        assertEquals("2", ((TextNode[])result.get(1).value)[1].groups.get(0).value);
        
        assertEquals("tr", result.get(2).type);
        assertEquals("3", ((TextNode[])result.get(2).value)[0].groups.get(0).value);
        assertEquals("4", ((TextNode[])result.get(2).value)[1].groups.get(0).value);
        
        assertEquals("p", result.get(4).type);
        assertEquals("asd", ((TextNode)result.get(4).value).groups.get(0).value);
        assertTrue(((TextNode)result.get(4).value).groups.get(0).isEmphasis);
    }

    @Test
    void testParse_table2() {
        var renderer = new SimpleRenderer();
        var parser = new MarkdownParser(renderer);
        parser.parse("""
        a|**b**|cd
        -|-|-|-|-
        ~~1~~|2|f|e|ads
        3|4|f|asd
        a|b|f|asd
        *asd*""");

        List<Container> result = renderer.body;
        System.out.println(result);
        assertEquals("table", result.get(0).type);
        assertEquals("a", ((TextNode[])result.get(0).value)[0].groups.get(0).value);
        assertEquals("b", ((TextNode[])result.get(0).value)[1].groups.get(0).value);
        assertTrue(((TextNode[])result.get(0).value)[1].groups.get(0).isStrong);
        
        assertEquals("tr", result.get(1).type);
        assertEquals("1", ((TextNode[])result.get(1).value)[0].groups.get(0).value);
        assertTrue(((TextNode[])result.get(1).value)[0].groups.get(0).isStrikeThrough);
        assertEquals("2", ((TextNode[])result.get(1).value)[1].groups.get(0).value);
        
        assertEquals("tr", result.get(2).type);
        assertEquals("3", ((TextNode[])result.get(2).value)[0].groups.get(0).value);
        assertEquals("4", ((TextNode[])result.get(2).value)[1].groups.get(0).value);
        
        assertEquals("p", result.get(4).type);
        assertEquals("asd", ((TextNode)result.get(4).value).groups.get(0).value);
        assertTrue(((TextNode)result.get(4).value).groups.get(0).isEmphasis);
    }
}
