package com.vincentcodes.tests.markdown;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.tests.markdown.mock.SimpleRenderer;

public class ManualTest {
    public static void main(String[] args){
        MarkdownParser parser = new MarkdownParser(new SimpleRenderer());
        // parser.parse("""
        // ~~a~~|b|*cd*
        // -|-|-|-
        // a|b|f|asd
        // a|**b**|f|asd
        // a|b|f
        // **asdqwe**
        // """);
        parser.parse("""
        |a|**b**|cd|
        |-|-|-|-|-
        |~~1~~|2|f|e|ads
        |3|4|f|asd
        |a|b|f|asd
        *asd*
        """);
    }
}
