package com.vincentcodes.markdown.renderer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vincentcodes.markdown.inline.TextGroup;
import com.vincentcodes.markdown.inline.TextNode;
import com.vincentcodes.ooxml.word.WordDocument;
import com.vincentcodes.ooxml.word.WordList;
import com.vincentcodes.ooxml.word.WordParagraph;
import com.vincentcodes.ooxml.word.WordTable;
import com.vincentcodes.ooxml.word.WordTextStyles;

import org.apache.poi.xwpf.usermodel.XWPFTableCell;

public class OoxmlWordRenderer implements Renderer{
    private boolean createToc = false;

    private File outputFile;
    private File themeFile;

    private WordDocument document;
    private WordTable table;

    private static WordTextStyles CONSOLAS_FONT = new WordTextStyles();

    public OoxmlWordRenderer(){
        this.outputFile = null;

        CONSOLAS_FONT.addFontFamily("Consolas");
    }
    public OoxmlWordRenderer(File outputFile, File themeFile){
        this();
        this.outputFile = outputFile;
        this.themeFile = themeFile;
        
        if(themeFile != null && !themeFile.isFile()){
            throw new IllegalArgumentException("Invalid theme file");
        }
    }

    private String textNodeToString(TextNode text){
        StringBuilder builder = new StringBuilder();
        for(TextGroup g : text.groups){
            builder.append(g.value);
        }
        return builder.toString();
    }

    private WordTextStyles getTextStyles(TextGroup group){
        WordTextStyles styles = new WordTextStyles();
        if(group.isStrong) styles.addBold();
        if(group.isEmphasis) styles.addItalic();
        if(group.isStrikeThrough) styles.addStrikeThrough();
        if(group.isCode) {
            styles.addFontFamily("Consolas");
            styles.addColor("C00000"); // dark red
        }
        // add more custom styles if you want...
        return styles;
    }

    private void addTextToParagraph(TextNode texts, WordParagraph para){
        for(TextGroup g : texts.groups){
            // do not fully support images yet
            if(g.isImage){
                if(!g.url.startsWith("data:"))
                    continue;
                int colonIndex = g.url.indexOf(':');
                int semicolonIndex = g.url.indexOf(';');
                int comma = g.url.indexOf(',');
                if(colonIndex == -1 || semicolonIndex == -1 || comma == -1)
                    continue;
                String imageType = g.url.substring(colonIndex+1, semicolonIndex).toLowerCase();
                ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(g.url.substring(comma+1)));
                try{
                    if(imageType.endsWith("png")){
                        para.insertPng(bais, "", 150);
                    }else if(imageType.endsWith("jpeg")){
                        para.insertJpeg(bais, "", 150);
                    }else if(imageType.endsWith("gif")){
                        para.insertGif(bais, "", 150);
                    }
                }catch(IOException e){
                    throw new UncheckedIOException(e);
                }
            }
            
            if(g.isLink){
                WordTextStyles linkStyle = getTextStyles(g);
                linkStyle.addColor("0000ff"); // blue only
                linkStyle.addUnderline("0000ff");
                para.insertLink(g.url, g.desc, linkStyle);
            }else
                para.insertText(g.value, getTextStyles(g));
        }
    }

    public void createTOC(){
        createToc = true;
    }

    @Override
    public void body() {
        if(outputFile != null && themeFile != null){
            document = new WordDocument(outputFile, themeFile);
        }else if(outputFile != null){
            document = new WordDocument(outputFile);
        }else
            document = new WordDocument();

        if(createToc){
            document.createTOCPage();
        }
    }

    @Override
    public void done() {
        try{
            document.export();
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void hr() {
        // ...
    }

    @Override
    public void h1(TextNode texts) {
        document.insertHeading(textNodeToString(texts), "Heading1");
    }

    @Override
    public void h2(TextNode texts) {
        document.insertHeading(textNodeToString(texts), "Heading2");
    }

    @Override
    public void h3(TextNode texts) {
        document.insertHeading(textNodeToString(texts), "Heading3");
    }

    @Override
    public void h4(TextNode texts) {
        document.insertHeading(textNodeToString(texts), "Heading4");
    }

    @Override
    public void h5(TextNode texts) {
        document.insertHeading(textNodeToString(texts), "Heading5");
    }

    @Override
    public void h6(TextNode texts) {
        document.insertHeading(textNodeToString(texts), "Heading6");
    }

    @Override
    public void p(TextNode texts) {
        WordParagraph para = document.createParagraph();
        addTextToParagraph(texts, para);
    }

    @Override
    public void append(String text) {
        WordParagraph para = document.createParagraph();
        para.insertText(text);
    }

    @Override
    public void ol(TextNode[] texts) {
        WordList list = document.createNumberedList();
        for(TextNode node : texts){
            WordParagraph para = document.createParagraph();
            addTextToParagraph(node, para);
            list.addListItem(para);
        }
    }

    @Override
    public void ul(TextNode[] texts) {
        WordList list = document.createBulletList();
        for(TextNode node : texts){
            WordParagraph para = document.createParagraph();
            addTextToParagraph(node, para);
            list.addListItem(para);
        }
    }

    @Override
    public void table(TextNode[] headings) {
        WordTable table = document.createTable();
        List<String> strHeadings = Stream.of(headings).map((ele)->{
            return textNodeToString(ele);
        }).collect(Collectors.toList());
        table.addRow(strHeadings);
    }

    @Override
    public void tr(TextNode[] rowEntries) {
        List<String> strEntries = Stream.of(rowEntries).map((ele)->{
            return textNodeToString(ele);
        }).collect(Collectors.toList());
        table.addRow(strEntries);
    }

    @Override
    public void endTable() {
        // No need
    }

    @Override
    public void codeblock(String text, String lang) {
        WordTable table = document.createTable();
        table.setWidth(document.getEffWidth());

        // lower level stuff
        XWPFTableCell codeBlock = table.getRaw().createRow().addNewTableCell();
        codeBlock.removeParagraph(0); // clear it
        table.setBgColorForCell(0, 0, "e7e6e6");

        String[] lines = text.split("(\r)?\n");
        for(String line : lines){
            WordParagraph para = new WordParagraph(codeBlock.addParagraph());
            para.insertText(line, CONSOLAS_FONT);
            para.setSpacingAfter(0);
        }
    }

    @Override
    public void startBlockQuote() {
        // No equivalent, style it in the future
    }

    @Override
    public void blockquote(TextNode text, int level) {
        // No equivalent, style it in the future
    }

    @Override
    public void endBlockQuote() {
        // No need
    }

}