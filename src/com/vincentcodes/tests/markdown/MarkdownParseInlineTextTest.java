package com.vincentcodes.tests.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.markdown.inline.TextNode;

import org.junit.jupiter.api.Test;

// @TestInstance(Lifecycle.PER_CLASS)
public class MarkdownParseInlineTextTest {
    @Test
    void testParseText_resetline() {
        var parser = new MarkdownParser(null);
        parser.setText("***asdasd***\ntext asdasd~~asd~~1~2~3\n\nasd*as**d*asd**");
        TextNode result = parser.parseText();
        assertTrue(result.groups.size() == 4);

        assertEquals("asdasd", result.groups.get(0).value);
        assertTrue(result.groups.get(0).isStrong);
        assertTrue(result.groups.get(0).isEmphasis);

        assertEquals(" text asdasd", result.groups.get(1).value);
        
        assertEquals("asd", result.groups.get(2).value);
        assertTrue(result.groups.get(2).isStrikeThrough);

        assertEquals("1~2~3", result.groups.get(3).value);
    }

    @Test
    void testParseText_multiline() {
        var parser = new MarkdownParser(null);
        parser.setText("asd*as**d*asd**123*\n*123**123");
        TextNode result = parser.parseText();
        assertTrue(result.groups.size() == 7);
        assertEquals("123", result.groups.get(6).value);
        assertEquals("d", result.groups.get(2).value);
        assertTrue(result.groups.get(2).isStrong);
        assertTrue(result.groups.get(2).isEmphasis);
    }

    @Test
    void testParseText_code() {
        var parser = new MarkdownParser(null);
        parser.setText("asd*as`*asd`");
        TextNode result = parser.parseText();
        assertTrue(result.groups.size() == 2);
        assertEquals("asd*as", result.groups.get(0).value);
        
        assertEquals("*asd", result.groups.get(1).value);
        assertTrue(result.groups.get(1).isCode);
    }

    @Test
    void testParseText_link() {
        var parser = new MarkdownParser(null);
        parser.setText("asd*[momomia](https://google.com)*asd");
        TextNode result = parser.parseText();
        assertTrue(result.groups.size() == 3);
        assertEquals("asd", result.groups.get(0).value);

        assertEquals("momomia", result.groups.get(1).desc);
        assertEquals("https://google.com", result.groups.get(1).url);
        assertTrue(result.groups.get(1).isEmphasis);
        assertTrue(result.groups.get(1).isLink);
        
        assertEquals("asd", result.groups.get(2).value);
    }

    @Test
    void testParseText_image() {
        var parser = new MarkdownParser(null);
        parser.setText("asd*![momomia](https://google.com)a*sd");
        TextNode result = parser.parseText();
        assertTrue(result.groups.size() == 4);
        assertEquals("asd", result.groups.get(0).value);

        assertEquals("momomia", result.groups.get(1).desc);
        assertEquals("https://google.com", result.groups.get(1).url);
        assertTrue(result.groups.get(1).isEmphasis);
        assertTrue(result.groups.get(1).isImage);

        assertEquals("a", result.groups.get(2).value);
        assertTrue(result.groups.get(2).isEmphasis);

        assertEquals("sd", result.groups.get(3).value);
    }
}
