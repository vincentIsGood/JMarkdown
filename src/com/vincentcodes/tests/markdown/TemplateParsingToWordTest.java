package com.vincentcodes.tests.markdown;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.markdown.renderer.OoxmlWordRenderer;

public class TemplateParsingToWordTest {
    public static void main(String[] args) throws IOException{
        try(FileInputStream fis = new FileInputStream("README.md")){
            MarkdownParser parser = new MarkdownParser(new OoxmlWordRenderer(new File("out.docx"), new File("./classes/Styles.docx")));
            
            long startingTime = System.currentTimeMillis();
            parser.parse(new String(fis.readAllBytes()));
            System.out.println("Time taken to parse and generate docx: " + (System.currentTimeMillis() - startingTime) + "ms");
        }
    }
}
