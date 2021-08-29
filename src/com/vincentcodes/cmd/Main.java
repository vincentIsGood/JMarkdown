package com.vincentcodes.cmd;

import java.io.File;

/**
 * Handles command like this:
 * jmarkdown-cmdutil-v1.0.0.jar <md2html / md2doc> <in file> <out file>
 */
public class Main {
    public static void main(String[] args){
        if(!validateCommand(args)){
            printHelpAndExit();
        }

        switch(args[0]){
            case "md2html":
                Md2HtmlCmdUtil.handle(args);
                break;
            case "md2doc":
                System.out.println("nop");
                break;
            default:
                printHelpAndExit();
        }
    }

    /**
     * @return fine or not
     */
    public static boolean validateCommand(String[] args){
        if(args.length != 3)
            return false;
        
        File in = new File(args[1]);
        if(!in.isFile())
            throw new IllegalArgumentException(args[1] + " is not a file");
        return true;
    }
    public static void printHelpAndExit(){
        System.out.println("jmarkdown-cmdutil-vx.y.z.jar <md2html / md2doc> <in file> <out file>");
        System.exit(-1);
    }
}
