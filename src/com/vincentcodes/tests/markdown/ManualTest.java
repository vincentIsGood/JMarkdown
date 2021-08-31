package com.vincentcodes.tests.markdown;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.tests.markdown.mock.SimpleRenderer;

public class ManualTest {
    public static void main(String[] args){
        MarkdownParser parser = new MarkdownParser(new SimpleRenderer());
        // parser.parse("""
        // 1. adsas
        // 2. bdsad
        //    - oke test
        // 3. ddas
        //    - ok 321
        // """);
        parser.setText("asd**as`asd` text**");
        System.out.println(parser.parseText());
    }
}
