package com.vincentcodes.tests.markdown;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.tests.markdown.mock.SimpleRenderer;

public class ManualTest {
    public static void main(String[] args){
        MarkdownParser parser = new MarkdownParser(new SimpleRenderer());
        parser.parse("""
        1. asdasd
            asd

            asd
            - asd
            - asd
        """);
        // parser.parse("""
        // - ad*sa*s
        // as*ds*ad
        // - bd*sad
        // new line
        //    1. oke t*es*t
        // - ddas
        //    2. o*k 3*21
        // """);
        // System.out.println(((HtmlRenderer)parser.getRenderer()).getRenderedHtml());
    }
}
