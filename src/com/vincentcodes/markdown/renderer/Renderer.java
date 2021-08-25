package com.vincentcodes.markdown.renderer;

import com.vincentcodes.markdown.inline.TextNode;

/**
 * A renderer converts Markdown to something else. 
 * The parser do not support embedding HTML codes,
 * for the time being.
 */
public interface Renderer {

    /**
     * Init
     */
    void body();

    /**
     * Close
     */
    void done();

    // horizontal line
    void hr();

    // Headings
    void h1(TextNode texts);
    void h2(TextNode texts);
    void h3(TextNode texts);
    void h4(TextNode texts);
    void h5(TextNode texts);
    void h6(TextNode texts);

    // Normal texts
    void p(TextNode texts);
    /**
     * append plain text to (new / old) paragraph
     */
    void append(String text);

    // Lists
    /**
     * Ordered list (ol)
     */
    void ol(TextNode[] texts);
    /**
     * Unordered list (ul)
     */
    void ul(TextNode[] texts);

    // Table
    void table(TextNode[] headings);
    void tr(TextNode[] rowEntries);
    void endTable();

    void codeblock(String text, String lang);

    // Block quote
    void startBlockQuote();
    void blockquote(TextNode text, int level);
    void endBlockQuote();

}
