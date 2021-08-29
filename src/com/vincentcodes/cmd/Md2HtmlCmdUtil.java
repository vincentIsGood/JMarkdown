package com.vincentcodes.cmd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.markdown.renderer.HtmlRenderer;

public class Md2HtmlCmdUtil {
    /**
     * @return successful or not
     */
    public static void handle(String[] args){
        try(FileInputStream fis = new FileInputStream(args[1]);
            FileOutputStream fos = new FileOutputStream(args[2])){
            HtmlRenderer renderer = new HtmlRenderer();
            MarkdownParser parser = new MarkdownParser(renderer);
            parser.parse(new String(fis.readAllBytes()));
            
            fos.write(renderer.getRenderedHtml().getBytes());
            System.out.println("File created");
        }catch(FileNotFoundException ignored){
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }
}
