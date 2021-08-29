package com.vincentcodes.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.markdown.renderer.OoxmlWordRenderer;

public class Md2DocCmdUtil {
    /**
     * @return successful or not
     */
    public static void handle(String[] args){
        try(FileInputStream fis = new FileInputStream(args[1])){
            MarkdownParser parser;

            if(args.length == 4){
                File styles = new File(args[3]);
                if(!styles.isFile() || !styles.getName().endsWith(".docx"))
                    throw new IllegalArgumentException(args[3] + " is not a valid style file");
                parser = new MarkdownParser(new OoxmlWordRenderer(new File(args[2]), styles));
            }else{
                File tmpFile = loadInJarStyle();
                if(args.length == 3){
                    parser = new MarkdownParser(new OoxmlWordRenderer(new File(args[2]), tmpFile));
                }else{
                    parser = new MarkdownParser(new OoxmlWordRenderer(new File("out.docx"), tmpFile));
                }
            }

            parser.parse(new String(fis.readAllBytes()));
            System.out.println("File created");
        }catch(FileNotFoundException ignored){
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    private static File loadInJarStyle() throws IOException{
        InputStream stylesUrl = Md2DocCmdUtil.class.getResourceAsStream("/Styles.docx");
        try(FileOutputStream fos = new FileOutputStream("tmp_styles.docx")){
            fos.write(stylesUrl.readAllBytes());
        }
        File tmp = new File("tmp_styles.docx");
        tmp.deleteOnExit();
        return tmp;
    }
}
