package com.vincentcodes.cmd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.markdown.renderer.HtmlRenderer;
import com.vincentcodes.util.commandline.Command;

public class Md2HtmlCmdUtil {
    /**
     * @return successful or not
     */
    public static void handle(Command cmd){
        if(!cmd.hasOption("-i"))
            Main.printHelpAndExit();
        
        try(FileInputStream fis = new FileInputStream(cmd.getOptionValue("-i"));
            FileOutputStream fos = new FileOutputStream(cmd.hasOption("-o")? cmd.getOptionValue("-o") : "out.html")){
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
