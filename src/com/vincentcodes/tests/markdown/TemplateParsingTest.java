package com.vincentcodes.tests.markdown;

import java.io.FileInputStream;
import java.io.IOException;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.markdown.renderer.HtmlRenderer;

public class TemplateParsingTest {
    public static void main(String[] args) throws IOException{
        try(FileInputStream fis = new FileInputStream("Template.md")){
            MarkdownParser parser = new MarkdownParser(new HtmlRenderer());
            
            long startingTime = System.currentTimeMillis();
            parser.parse(new String(fis.readAllBytes()));
            // System.out.println(((HtmlRenderer)parser.getRenderer()).getRenderedHtml());
            System.out.println("Time taken to parse and generate html: " + (System.currentTimeMillis() - startingTime) + "ms");
        }
    }
}
