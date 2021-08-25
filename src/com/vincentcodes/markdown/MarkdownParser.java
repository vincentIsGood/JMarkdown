package com.vincentcodes.markdown;

import java.util.ArrayList;
import java.util.List;

import com.vincentcodes.markdown.inline.TextGroup;
import com.vincentcodes.markdown.inline.TextNode;
import com.vincentcodes.markdown.renderer.Renderer;

/**
 * <p>
 * I implemented the basic functions needed in Markdown.
 * Since I didn't really read the original specification 
 * of markdown files, there could be some missing 
 * functions.
 * 
 * @author Vincent Ko
 * @see https://www.markdownguide.org/cheat-sheet/
 * @see https://spec.commonmark.org/
 */
public class MarkdownParser{
    private Renderer renderer;
    private String text;
    private int currentIndex = 0;

    public MarkdownParser(Renderer renderer){
        this.renderer = renderer;
    }

    public void reset(){
        text = "";
        currentIndex = 0;
    }

    /**
     * Set markdown text manually, for testing purposes
     * @see #parseText()
     */
    public void setText(String text){
        this.text = text;
    }

    public Renderer getRenderer(){
        return renderer;
    }

    // Tokenizing stuff (Put inside parser for easier integration) //

    /**
     * Includes the current char
     * @return null if eof is reached
     */
    private String getUntilLineEnd(){
        if(currentIndex+1 >= text.length()) 
            return null;
        int startingIndex = currentIndex;
        while(text.charAt(currentIndex++) != '\n'){
            if(currentIndex >= text.length()){
                currentIndex++; // add 1 to offset "currentIndex - 1" because eof has no '\n'
                break;
            }
        }
        return text.substring(startingIndex, currentIndex - 1);
    }

    /**
     * Includes the current char
     * @return null if eof is reached
     */
    public String peekUntilLineEnd(int startOffset){
        if(currentIndex+startOffset+1 >= text.length()) 
            return null;
        int cIndex = currentIndex + startOffset;
        int startingIndex = cIndex;
        while(text.charAt(cIndex++) != '\n'){
            // importantly "cIndex == text.length()"
            if(cIndex >= text.length()){
                cIndex++;
                break;
            }
        }
        return text.substring(startingIndex, cIndex - 1);
    }

    /**
     * @return 0 if eof is reached
     */
    private char currentChar(){
        if(currentIndex >= text.length())
            return 0;
        return text.charAt(currentIndex);
    }

    /**
     * Include the current index char
     * @return null if eof will reach after n chars
     */
    private String peekNChar(int n){
        if(n <= 0) return null;
        if(currentIndex + n > text.length())
            return null;
        return text.substring(currentIndex, currentIndex + n);
    }

    /**
     * @return 0 if eof is reached
     */
    private char peekNextChar(){
        if(currentIndex + 1 >= text.length())
            return 0;
        return text.charAt(currentIndex + 1);
    }

    private void next(){
        currentIndex++;
    }

    private void skipSpaces(){
        while(currentIndex < text.length() && 
        (currentChar() == ' ' || currentChar() == '\t')){
            next();
        }
    }

    /**
     * Exclusive, pointer goes after c
     */
    private String readUntilChar(char c){
        StringBuilder builder = new StringBuilder();
        while(currentIndex < text.length() && currentChar() != c){
            builder.append(currentChar());
            next();
        }
        return builder.toString();
    }

    /**
     * Exclusive, pointer goes after str
     */
    // private String readUntilString(String str){
    //     StringBuilder builder = new StringBuilder();
    //     int matchingChars = 0;
    //     while(currentIndex < text.length() && matchingChars < str.length()){
    //         if(currentChar() == str.charAt(matchingChars++));
    //         else matchingChars = 0;
    //         builder.append(currentChar());
    //         next();
    //     }
    //     return builder.substring(0, builder.length()-str.length());
    // }

    /**
     * As the name says
     * @return -1 if not found
     */
    private int findStrOnSameLine(String c, int startingIndex){
        if(startingIndex >= text.length() || text.charAt(startingIndex) == '\n') return -1;

        int newlineIndex = text.indexOf('\n', startingIndex);
        int matchingCharIndex = text.indexOf(c, startingIndex);
        if(newlineIndex == -1) newlineIndex = text.length();
        if(matchingCharIndex > newlineIndex)
            return -1;
        return matchingCharIndex;
    }
    /**
     * As the name says
     * @return -1 if not found
     */
    private int findCharOnSameLine(char c, int startingIndex){
        if(startingIndex >= text.length() || text.charAt(startingIndex) == '\n') return -1;

        int newlineIndex = text.indexOf('\n', startingIndex);
        int matchingCharIndex = text.indexOf(c, startingIndex + 1);
        if(matchingCharIndex == -1) return -1;
        if(newlineIndex == -1) newlineIndex = text.length();
        if(matchingCharIndex > newlineIndex)
            return -1;
        return matchingCharIndex;
    }
    /**
     * '`' and '\n' acts as a barrier
     */
    private boolean canReachPair(char c){
        int pairPos;
        int codeInlinePos = findCharOnSameLine('`', currentIndex + 1);
        if(codeInlinePos != -1)
            return (pairPos = findCharOnSameLine(c, currentIndex + 1)) != -1
            && codeInlinePos > pairPos;
        return findCharOnSameLine(c, currentIndex + 1) != -1;
    }
    /**
     * '`' and '\n'  acts as a barrier
     */
    private boolean canReachPair(String c){
        int pairPos;
        int codeInlinePos = findCharOnSameLine('`', currentIndex + 1);
        if(codeInlinePos != -1)
            return (pairPos = findStrOnSameLine(c, currentIndex + 1)) != -1
            && codeInlinePos > pairPos;
        return findStrOnSameLine(c, currentIndex + 1) != -1;
    }
    private int numOfCharInString(String str, char c){
        int counter = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == c)
                counter++;
        }
        return counter;
    }
    private String repeatChar(String c, int n){
        return new String(new char[n]).replaceAll("\0", c);
    }

    // Parsing starts here //

    /**
     * @see #parse(String, int)
     */
    public void parse(String text){
        parse(text, 0);
    }
    /**
     * The renderer will take any events invoked from this
     * function during the parsing process.
     * @param text markdown text
     * @param startOffset based on 0
     */
    public void parse(String text, int startOffset){
        reset();
        this.text = text;
        currentIndex = startOffset;
        
        renderer.body();
        for(; currentChar() != 0; ){
            if(parseHrLine()) continue;
            if(parseHeading()) continue;
            if(parseQuoteBlock()) continue;
            if(parseOrderedListItem()) continue;
            if(parseUnorderedListItem()) continue;
            if(parseTable()) continue;
            if(parseCodeBlock()) continue;

            renderer.p(parseText());
            next();
        }
        renderer.done();
    }
    private boolean parseHrLine(){
        String peek4Char = peekNChar(4);
        if(peek4Char == null) 
            return false;
        if((currentChar() == '-' && peekNChar(4).equals("---\n"))
        || (currentChar() == '*' && peekNChar(4).equals("***\n"))
        || (currentChar() == '_' && peekNChar(4).equals("___\n"))){
            renderer.hr();
            next();next();next();next();
            return true;
        }
        return false;
    }
    private boolean parseHeading(){
        if(currentChar() == '#'){
            int numOfHash = 1;
            while(peekNextChar() == '#'){
                numOfHash++;
                next();
            }
            next();
            skipSpaces();
            switch(numOfHash){
                case 1: renderer.h1(parseText(true)); break;
                case 2: renderer.h2(parseText(true)); break;
                case 3: renderer.h3(parseText(true)); break;
                case 4: renderer.h4(parseText(true)); break;
                case 5: renderer.h5(parseText(true)); break;
                case 6: renderer.h6(parseText(true)); break;
                default: renderer.append(repeatChar("#", numOfHash)); break;
            }
            return true;
        }
        return false;
    }
    private boolean parseQuoteBlock(){
        if(currentChar() == '>'){
            renderer.startBlockQuote();
            while(currentChar() == '>'){
                int numOfArrow = 1;
                while(peekNextChar() == '>'){
                    numOfArrow++;
                    next();
                }
                String arrowString = repeatChar(">", numOfArrow);
                next(); // skip '>'
                skipSpaces();

                StringBuilder lines = new StringBuilder();
                String line;
                // combine same level block quotes into one line
                while((line = peekUntilLineEnd(0)) != null){
                    if(line.matches("^" + arrowString + "([^>]*)"))
                        line = line.replace(arrowString, "");
                    if(line.startsWith(">") || line.trim().isEmpty()){
                        break;
                    }
                    lines.append(line).append('\n');
                    getUntilLineEnd(); // completed this line
                }
                lines.deleteCharAt(lines.length()-1); // delete '\n'
                MarkdownParser innerParser = new MarkdownParser(null);
                innerParser.setText(lines.toString());
                renderer.blockquote(innerParser.parseText(), numOfArrow);
            }
            renderer.endBlockQuote();
            next(); // skip '\n'
            return true;
        }
        return false;
    }
    private boolean parseOrderedListItem(){
        if(incomingOrderedList()){
            List<TextNode> listItems = new ArrayList<>();
            while(incomingOrderedList()){
                // strip num
                TextNode node = parseText();
                TextGroup textGroup = node.groups.get(0);
                textGroup.value = textGroup.value.substring(textGroup.value.indexOf(' ')+1); // skip ' ' itself
                // just in case "1. *asd*" happens where "1. " becomes "" (empty)
                if(textGroup.value.equals("")) 
                    node.groups.remove(0);
                listItems.add(node);
                next();
            }
            renderer.ol(listItems.toArray(TextNode[]::new));
            return true;
        }
        return false;
    }
    private boolean incomingOrderedList(){
        return currentChar() >= '0' && currentChar() <= '9'
        && peekUntilLineEnd(0).matches("^[0-9]{1,}\\. (.*)");
    }
    private boolean parseUnorderedListItem(){
        if(incomingUnorderedList()){
            List<TextNode> listItems = new ArrayList<>();
            while(incomingUnorderedList()){
                // strip bullet point (ie. -*+)
                TextNode node = parseText();
                TextGroup textGroup = node.groups.get(0);
                textGroup.value = textGroup.value.substring(textGroup.value.indexOf(' ')+1); // skip ' ' itself
                if(textGroup.value.equals("")) 
                    node.groups.remove(0);
                listItems.add(node);
                next();
            }
            renderer.ul(listItems.toArray(TextNode[]::new));
            return true;
        }
        return false;
    }
    private boolean incomingUnorderedList(){
        return (currentChar() == '-'
        || currentChar() == '*'
        || currentChar() == '+') && peekUntilLineEnd(0).matches("^[-\\*+] (.*)");
    }
    private boolean parseTable(){
        if(currentChar() == '|'){
            // look ahead (next() is not used because it may get into the buffer)
            String line = peekUntilLineEnd(1);
            int noOfItems = numOfCharInString(line, '|');
            if(noOfItems > 0){
                String nextLine = peekUntilLineEnd(line.length()+2); // skip '\n' + offset (1)
                // -1 is needed to skip the first '|'
                // bug: | - | a | also counts
                if(nextLine != null && nextLine.contains("-") && numOfCharInString(nextLine, '|')-1 >= noOfItems){
                    // Parse headings
                    next(); // skip '|' (not peek anymore)
                    TextNode[] headings = new TextNode[noOfItems];
                    for(int i = 0; i < noOfItems; i++){
                        headings[i] = parseText('|', true);
                        if(headings[i].groups.size() > 0){
                            TextGroup textGroup = headings[i].groups.get(0);
                            textGroup.value = textGroup.value.trim();
                        }
                        next(); next(); // move forward to '|' then go pass '|' (go to next cell)
                    }
                    renderer.table(headings);
                    getUntilLineEnd(); // skip headings
                    getUntilLineEnd(); // skip |-|-|-|...

                    // make sure each line has the same or more amount of cells 
                    // (more: excess cells will be skipped)
                    nextLine = peekUntilLineEnd(0); // do not get until eol because this will move our pointer forward
                    while(nextLine != null && numOfCharInString(nextLine, '|')-1 >= noOfItems){
                        TextNode[] nodes = new TextNode[noOfItems];
                        next(); // skip '|'
                        for(int i = 0; i < noOfItems; i++){
                            nodes[i] = parseText('|', true);
                            if(nodes[i].groups.size() > 0){
                                TextGroup textGroup = nodes[i].groups.get(0);
                                textGroup.value = textGroup.value.trim();
                            }
                            next(); next(); // move forward to '|' then go pass '|' (go to next cell)
                        }
                        renderer.tr(nodes);
                        getUntilLineEnd(); // skip the rest of the line
                        nextLine = peekUntilLineEnd(0);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    private boolean parseCodeBlock(){
        if(peekNChar(3) != null && peekNChar(3).equals("```")){
            next(); next(); next();
            String lang = readUntilChar('\n');
            next(); // skip '\n'

            StringBuilder codeBlock = new StringBuilder();
            String line;
            while((line = getUntilLineEnd()) != null && !line.equals("```")){
                codeBlock.append(line).append("\n");
            }
            codeBlock.deleteCharAt(codeBlock.length()-1); // delete '\n'
            renderer.codeblock(codeBlock.toString(), lang);
            if(currentChar() == '\n')
                next();
            return true;
        }
        return false;
    }


    // Parse Inline Text //

    /**
     * Not recommended to call this method. This 
     * is set public for testing purposes only
     */
    public TextNode parseText(){
        return parseText('\0', false);
    }
    public TextNode parseText(boolean terminateOnOneNewLine){
        return parseText('\0', terminateOnOneNewLine);
    }
    /**
     * Not recommended to call this method. This 
     * is set public for testing purposes only
     */
    public TextNode parseText(char additionalTerminator, boolean terminateOnOneNewLine){
        TextNode node = new TextNode();
        TextGroup inEffect = new TextGroup();
        StringBuilder builder = new StringBuilder();
        for(; currentIndex < text.length(); next()){
            // Return if a newline and the next newline is empty is reached
            if(currentChar() == '\n'){
                //// Specific to parse() because parse() has next() itself
                if(terminateOnOneNewLine) 
                    break;
                // for ol and ul, I have to use Regex here to look ahead for simplicity
                // read until the next bullet point / item
                if(peekUntilLineEnd(1) != null && peekUntilLineEnd(1).matches("^[0-9]{1,}\\. (.*)"))
                    break;
                if(peekUntilLineEnd(1) != null && peekUntilLineEnd(1).matches("^[-\\*+] (.*)"))
                    break;
                if(peekUntilLineEnd(1) != null && peekUntilLineEnd(1).matches("^#(.*)"))
                    break;
                if(peekUntilLineEnd(1) != null && peekUntilLineEnd(1).matches("^>(.*)"))
                    break;
                //// End

                // offset 1 to skip '\n'
                if(peekUntilLineEnd(1) != null && peekUntilLineEnd(1).trim().equals("")){
                    next(); // add 1 to skip '\n' (used for parseText() exit)
                    break;
                }
                // line breaks
                if(text.charAt(currentIndex-1) == ' ' && text.charAt(currentIndex-2) == ' '){
                    // I could have use trim() in the end, but anyways
                    builder.deleteCharAt(builder.length()-1).deleteCharAt(builder.length()-1);
                    break;
                }

                inEffect = new TextGroup(); // reset styles
                builder.append(" ");
                continue;
            }

            // don't forget '_' and '__'
            if(parseSingleAsterisk(node, inEffect, builder))
                continue; // do not add char to buffer
            if(parseDoubleAsterisk(node, inEffect, builder))
                continue;
            if(parseDoubleTilde(node, inEffect, builder))
                continue;
            if(parseInlineCode(node, inEffect, builder))
                continue;
            if(parseInlineLink(node, inEffect, builder))
                continue;
            if(parseInlineImage(node, inEffect, builder))
                continue;
            
            builder.append(currentChar());

            //// Specific to parse()
            // Additional term is used: Do not next() into the terminator
            if(additionalTerminator != '\0' && peekNextChar() == additionalTerminator)
                break;
        }
        createGroupToNode(node, inEffect.copy(), builder);
        return node;
    }
    /**
     * @return successful or not
     */
    private boolean parseDoubleAsterisk(TextNode node, TextGroup inEffect, StringBuilder builder){
        if(currentChar() == '*' && peekNextChar() == '*'){
            if(!inEffect.isStrong && canReachPair("**")){
                createGroupToNode(node, inEffect, builder);
                inEffect.isStrong = true;
                next(); // skip 2nd '*'
                return true;
            }else if(inEffect.isStrong){
                createGroupToNode(node, inEffect, builder);
                inEffect.isStrong = false;
                next();
                return true;
            }
        }
        return false;
    }
    private boolean parseSingleAsterisk(TextNode node, TextGroup inEffect, StringBuilder builder){
        if(currentChar() == '*' && peekNextChar() != '*'){
            if(!inEffect.isEmphasis && canReachPair('*')){
                createGroupToNode(node, inEffect, builder); // before setting
                inEffect.isEmphasis = true;
                return true;
            }else if(inEffect.isEmphasis){
                // turn it off if '*' is alone without a pair
                createGroupToNode(node, inEffect, builder);
                inEffect.isEmphasis = false;
                return true;
            }
        }
        return false;
    }
    private boolean parseDoubleTilde(TextNode node, TextGroup inEffect, StringBuilder builder){
        if(currentChar() == '~' && peekNextChar() == '~'){
            if(!inEffect.isStrikeThrough && canReachPair("~~")){
                createGroupToNode(node, inEffect, builder);
                inEffect.isStrikeThrough = true;
                next(); // skip 2nd '~'
                return true;
            }else if(inEffect.isStrikeThrough){
                createGroupToNode(node, inEffect, builder);
                inEffect.isStrikeThrough = false;
                next();
                return true;
            }
        }
        return false;
    }
    private boolean parseInlineCode(TextNode node, TextGroup inEffect, StringBuilder builder){
        if(currentChar() == '`'){
            if(findStrOnSameLine("`", currentIndex + 1) != -1){
                createGroupToNode(node, inEffect, builder);
                inEffect = new TextGroup(); // reset styles
                inEffect.isCode = true;
                StringBuilder codeInline = new StringBuilder();
                while(peekNextChar() != '`'){
                    next();
                    codeInline.append(currentChar());
                }
                next();
                if(codeInline.length() != 0){
                    createGroupToNode(node, inEffect, codeInline);
                }else{
                    builder.append("``");
                }
                inEffect.isCode = false;
                return true;
            }
        }
        return false;
    }
    private boolean parseInlineLink(TextNode node, TextGroup inEffect, StringBuilder builder){
        return parseInlineLink(node, inEffect, builder, false);
    }
    private boolean parseInlineLink(TextNode node, TextGroup inEffect, StringBuilder builder, boolean toImage){
        // crude way of searching for [...](...)
        // I could have used Regex for that
        if(currentChar() == '[' || currentChar() == ']'
        || currentChar() == '(' || currentChar() == ')'){
            if(currentChar() == '[' && !inEffect.isLink){
                createGroupToNode(node, inEffect, builder);
                if(toImage)
                    inEffect.isImage = true;
                else 
                    inEffect.isLink = true;
                return true;
            }else if(currentChar() == ']' && (inEffect.isLink || inEffect.isImage)){
                // turn it off if '*' is alone without a pair
                if(peekNextChar() == '('){
                    next(); // now on '('
                    if(findStrOnSameLine(")", currentIndex + 1) != -1){
                        TextGroup newGroup = inEffect.copy();
                        newGroup.desc = builder.toString().trim();
                        next(); // skip '('
                        newGroup.url = readUntilChar(')');
                        node.add(newGroup);
                        builder.setLength(0);
                    }
                }
                inEffect.isLink = false;
                inEffect.isImage = false;
                return true;
            }
        }
        return false;
    }
    private boolean parseInlineImage(TextNode node, TextGroup inEffect, StringBuilder builder){
        // crude way of searching for [...](...)
        // I could have used Regex for that
        if(currentChar() == '!' && peekNextChar() == '['){
            next();
            return parseInlineLink(node, inEffect, builder, true);
        }
        return false;
    }

    /**
     * Add new group to node and resets stringbuilder 
     */
    private void createGroupToNode(TextNode node, TextGroup stylesInEffect, StringBuilder builder){
        if(builder.toString().trim().equals("")) return;
        
        TextGroup newGroup = stylesInEffect.copy();
        newGroup.value = builder.toString();
        node.add(newGroup);

        builder.setLength(0); // reset stringbuilder
    }
    
}