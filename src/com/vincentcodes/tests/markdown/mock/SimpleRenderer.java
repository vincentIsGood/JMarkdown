package com.vincentcodes.tests.markdown.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vincentcodes.markdown.inline.TextNode;
import com.vincentcodes.markdown.renderer.Renderer;

public class SimpleRenderer implements Renderer{
    public static class Container{
        public String type;
        public Object value;
    
        Container(String type, Object value){
            this.type = type;
            this.value = value;
        }
    
        public String toString(){
            return "{Container type:'" + type + "' value:'" + value + "'}";
        }
    }

    public List<Container> body;

    // Initialization
    public void body(){
        body = new ArrayList<>();
    }

    // Closing
    public void done(){
        
    }

    // Horizontal line
    public void hr(){
        body.add(new Container("hr", ""));
        System.out.println("hr");
    }

    // Headings
    public void h1(TextNode texts){
        body.add(new Container("h1", texts));
        System.out.println("h1: " + texts);
    }
    public void h2(TextNode texts){
        body.add(new Container("h2", texts));
        System.out.println("h2: " + texts);
    }
    public void h3(TextNode texts){
        body.add(new Container("h3", texts));
        System.out.println("h3: " + texts);
    }
    public void h4(TextNode texts){
        body.add(new Container("h4", texts));
        System.out.println("h4: " + texts);
    }
    public void h5(TextNode texts){
        body.add(new Container("h5", texts));
        System.out.println("h5: " + texts);
    }
    public void h6(TextNode texts){
        body.add(new Container("h6", texts));
        System.out.println("h6: " + texts);
    }

    // Adding normal texts
    public void p(TextNode texts){
        body.add(new Container("p", texts));
        System.out.println("p: " + texts);
    }
    public void append(String text){
        body.add(new Container("append", text));
        System.out.println("append: " + text);
    }

    // List
    public void ol(TextNode[] texts){
        body.add(new Container("ol", texts));
        System.out.println("Adding text to ol: " + Arrays.toString(texts));
    }
    public void ul(TextNode[] texts){
        body.add(new Container("ul", texts));
        System.out.println("Adding text to ul: " + Arrays.toString(texts));
    }

    public void ol() {
        body.add(new Container("ol", null));
        System.out.println("ol");
    }
    public void ul() {
        body.add(new Container("ul", null));
        System.out.println("ul");
    }
    public void li(TextNode texts) {
        body.add(new Container("li", texts));
        System.out.println("li: " + texts.toString());
    }
    public void endol() {
        body.add(new Container("endol", null));
        System.out.println("end ol");
    }
    public void endul() {
        body.add(new Container("endul", null));
        System.out.println("end ul");
    }

    // Table
    public void table(TextNode[] headings){
        body.add(new Container("table", headings));
        System.out.println("Creating table with headings: " + Arrays.toString(headings));
    }
    public void tr(TextNode[] rowEntries){
        body.add(new Container("tr", rowEntries));
        System.out.println("Adding table entries: " + Arrays.toString(rowEntries));
    }
    public void endTable(){
        System.out.println("Table end");
    }

    // Code block
    public void codeblock(String text, String lang){
        body.add(new Container("codeblock("+ lang +")", text));
        System.out.println("codeblock("+ lang +"): " + text);
    }

    // Block quote
    public void startBlockQuote(){
        System.out.println("Start block quote");
    }
    public void blockquote(TextNode text, int level){
        body.add(new Container("blockquote("+ level +")", text));
        System.out.println("blockquote("+ level +"): " + text);
    }
    public void endBlockQuote(){
        System.out.println("End block quote");
    }
}