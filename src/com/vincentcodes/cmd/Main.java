package com.vincentcodes.cmd;

import com.vincentcodes.util.commandline.Command;
import com.vincentcodes.util.commandline.CommandLineParser;
import com.vincentcodes.util.commandline.ParserConfig;

/**
 * Handles command like this:
 * jmarkdown-cmdutil-vx.y.z.jar <md2html> <in file> [<out file>]
 * jmarkdown-cmdutil-vx.y.z.jar <md2doc> <in file> [<out file>] [<styles file>]
 */
public class Main {
    private static ParserConfig config;

    public static void main(String[] args){
        config = new ParserConfig();
        config.addOption("-h", true, "print help");
        config.addOption("-o", false, "<file>", "output file");
        config.addOption("-i", false, "<file>", "input file");
        config.addOption("--styles", false, "<file>", "style file with .docx file extension");
        config.addOption("--toc", true, "create table of contents (for ooxml word only)");
        config.addOption("--extern", true, "enable fetching images from the internet (for ooxml word only)");

        CommandLineParser parser = new CommandLineParser(config);
        Command result = parser.parse(args);

        if(result.hasOption("-h") || result.getParameters().size() == 0){
            printHelpAndExit();
        }

        switch(result.getParameter(0)){
            case "md2html":
                Md2HtmlCmdUtil.handle(result);
                break;
            case "md2doc":
                Md2DocCmdUtil.handle(result);
                break;
            default:
                printHelpAndExit();
        }
    }
    public static void printHelpAndExit(){
        System.out.println("jmarkdown-cmdutil-vx.y.z.jar <md2html / md2doc> [options]");
        System.out.println(config.getOptionsHelpString());
        System.exit(-1);
    }
}
