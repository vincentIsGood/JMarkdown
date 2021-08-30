package com.vincentcodes.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.vincentcodes.markdown.MarkdownParser;
import com.vincentcodes.markdown.renderer.OoxmlWordRenderer;
import com.vincentcodes.util.commandline.Command;

/**
 * jmarkdown-cmdutil-vx.y.z.jar md2doc <in file> [<out file>] [<toc? true / false>] [<styles file>]
 */
public class Md2DocCmdUtil {
    /**
     * @return successful or not
     */
    public static void handle(Command cmd){
        if(!cmd.hasOption("-i"))
            Main.printHelpAndExit();
        
        try(FileInputStream fis = new FileInputStream(cmd.getOptionValue("-i"))){
            OoxmlWordRenderer renderer;
            MarkdownParser parser;
            
            File out;
            if(cmd.hasOption("-o")){
                out = new File(cmd.getOptionValue("-o"));
            }else{
                out = new File("out.docx");
            }

            File style;
            if(cmd.hasOption("--styles")){
                style = new File(cmd.getOptionValue("--styles"));
            }else{
                style = loadInJarStyle();
            }

            renderer = new OoxmlWordRenderer(out, style);

            if(cmd.hasOption("--toc")){
                renderer.createTOC();
            }

            parser = new MarkdownParser(renderer);
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
