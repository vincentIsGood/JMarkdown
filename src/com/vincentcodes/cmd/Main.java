package com.vincentcodes.cmd;

import java.io.File;

/**
 * Handles command like this:
 * jmarkdown-cmdutil-vx.y.z.jar <md2html> <in file> [<out file>]
 * jmarkdown-cmdutil-vx.y.z.jar <md2doc> <in file> [<out file>] [<styles file>]
 */
public class Main {
    public static void main(String[] args){
        if(!basicValidate(args)){
            printHelpAndExit();
        }

        switch(args[0]){
            case "md2html":
                Md2HtmlCmdUtil.handle(args);
                break;
            case "md2doc":
                Md2DocCmdUtil.handle(args);
                break;
            default:
                printHelpAndExit();
        }
    }

    /**
     * @return fine or not
     */
    public static boolean basicValidate(String[] args){
        if(args.length < 2)
            return false;
        
        File in = new File(args[1]);
        if(!in.isFile())
            throw new IllegalArgumentException(args[1] + " is not a file");
        return true;
    }
    public static void printHelpAndExit(){
        System.out.println("jmarkdown-cmdutil-vx.y.z.jar <md2html / md2doc> <args>");
        System.out.println("Details:");
        System.out.println("jmarkdown-cmdutil-vx.y.z.jar md2html <in file> [<out file>]");
        System.out.println("jmarkdown-cmdutil-vx.y.z.jar md2doc <in file> [<out file>] [<styles file>]");
        System.exit(-1);
    }
}
