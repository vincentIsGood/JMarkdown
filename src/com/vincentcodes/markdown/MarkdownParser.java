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
 * <p>
 * If anyone is looking for improving this parser, the 
 * two important functions are {@link #parse(String)} and
 * {@link #parseText()}.
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

    private char peekPreviousChar(){
        if(currentIndex - 1 < 0)
            return 0;
        return text.charAt(currentIndex - 1);
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

    private boolean isSpace(int index){
        return text.charAt(index) == ' ' || text.charAt(index) == '\t' || text.charAt(index) == '\n';
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
     * Can reach another char to form a pair
     * '\n' acts as a barrier. And the char is not inside inline code
     */
    private boolean canReachChar(char c, boolean ignoreCharAfterSpace){
        int codeInlineStartingPos = findCharOnSameLine('`', currentIndex + 1);
        int codeInlineEndingingPos = findCharOnSameLine('`', codeInlineStartingPos + 1);
        int pairPos = findCharOnSameLine(c, currentIndex + 1);

        boolean leftIsSpace = (pairPos - 1 >= 0 && isSpace(pairPos-1)) || pairPos - 1 < 0;
        boolean insideInlineCode = codeInlineStartingPos < pairPos && pairPos < codeInlineEndingingPos;
        while(ignoreCharAfterSpace && pairPos != -1 && leftIsSpace && !insideInlineCode){
            pairPos = findCharOnSameLine(c, pairPos + 1);
            leftIsSpace = (pairPos - 1 >= 0 && isSpace(pairPos-1)) || pairPos - 1 < 0;
            insideInlineCode = codeInlineStartingPos < pairPos && pairPos < codeInlineEndingingPos;
        }
        
        // eol also counts as space
        boolean rightIsSpace = (pairPos + 1 < text.length() && isSpace(pairPos+1)) || pairPos + 1 >= text.length();
        boolean isSurroundedBySpaces = leftIsSpace && rightIsSpace;
        
        if(codeInlineStartingPos != -1)
            return pairPos != -1 && !insideInlineCode && !isSurroundedBySpaces;
        return pairPos != -1 && !isSurroundedBySpaces;
    }
    /**
     * Can reach another String to form a pair
     * '\n' acts as a barrier. And the char is not inside inline code
     */
    public boolean canReachStr(String c, boolean ignoreCharAfterSpace){
        int codeInlineStartingPos = findCharOnSameLine('`', currentIndex + 1);
        int codeInlineEndingingPos = findCharOnSameLine('`', codeInlineStartingPos + 1);
        int pairPos = findStrOnSameLine(c, currentIndex + 1);

        boolean leftIsSpace = (pairPos - 1 >= 0 && isSpace(pairPos-1)) || pairPos - 1 < 0;
        boolean insideInlineCode = codeInlineStartingPos < pairPos && pairPos < codeInlineEndingingPos;
        while(ignoreCharAfterSpace && pairPos != -1 && leftIsSpace && !insideInlineCode){
            pairPos = findStrOnSameLine(c, pairPos + 1);
            leftIsSpace = (pairPos - 1 >= 0 && isSpace(pairPos-1)) || pairPos - 1 < 0;
            insideInlineCode = codeInlineStartingPos < pairPos && pairPos < codeInlineEndingingPos;
        }
        
        // eol also counts as space
        boolean rightIsSpace = (pairPos + 1 < text.length() && isSpace(pairPos+1)) || pairPos + 1 >= text.length();
        boolean isSurroundedBySpaces = leftIsSpace && rightIsSpace;
        
        if(codeInlineStartingPos != -1)
            return pairPos != -1 && !insideInlineCode && !isSurroundedBySpaces;
        return pairPos != -1 && !isSurroundedBySpaces;
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
            next();
            if(peekUntilLineEnd(0) != null && peekUntilLineEnd(0).equals("")){
                getUntilLineEnd();
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
        String currentLine = peekUntilLineEnd(0);
        if(currentLine == null) 
            return false;
        
        if(currentChar() == '|' || 
        (currentLine.matches("([^\n\\|]{1,}\\|){1,}([^\n\\|]{1,})?") 
        && peekUntilLineEnd(currentLine.length()+1).matches("([- ]{1,}\\|){1,}([- ]{1,})?"))){
            return parseNormalTable(currentLine);
        }
        return false;
    }
    private boolean parseNormalTable(String currentLine){
        boolean stripPipes = false;

        // No trailing noise are allowed, ie. "|a|b|asd12346?" is not allowed
        if(currentLine.startsWith("|") && currentLine.endsWith("|")){
            stripPipes = true;
        }

        // Parse headings
        TextNode[] headings = parseTableRow(currentLine, stripPipes, -1);
        int numOfCells = headings.length;
        renderer.table(headings);
        getUntilLineEnd(); // skip "|-|-|-...|" or "-|-|-|-..."

        String nextLine;
        while((nextLine = peekUntilLineEnd(0)) != null && ((nextLine.startsWith("|") && numOfCharInString(nextLine, '|')-1 >= numOfCells) 
        || nextLine.matches("([^\n\\|]{1,}\\|){1,}([^\n\\|]{1,})?"))){
            TextNode[] trCells = parseTableRow(nextLine, stripPipes, numOfCells);
            renderer.tr(trCells);
        }
        renderer.endTable();
        return true;
    }
    /**
     * @param noOfCells -1 if you want to compute it on the fly
     */
    private TextNode[] parseTableRow(String peekedLine, boolean stripPipes, int noOfCells){
        if(stripPipes){
            // To be a little bit more flexible
            int startIndex = 1;
            if(!peekedLine.startsWith("|")){
                startIndex = 0;
            }else 
                next(); // skip '|' in real pointer
            peekedLine = peekedLine.substring(startIndex, peekedLine.lastIndexOf("|")-1);
        }
        
        TextNode[] cellsValue = new TextNode[noOfCells == -1? numOfCharInString(peekedLine, '|')+1 : noOfCells];
        for(int i = 0; i < cellsValue.length; i++){
            cellsValue[i] = parseText('|', true);
            if(cellsValue[i].groups.size() > 0){
                TextGroup textGroup = cellsValue[i].groups.get(0);
                textGroup.value = textGroup.value.trim();
            }
            if(i < cellsValue.length-1){
                next(); next(); // move forward to '|' then go pass '|' (go to next cell)
            }
        }
        getUntilLineEnd(); // skip the rest of the line
        return cellsValue;
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
            if(currentChar() == '\\'){
                switch(peekNextChar()){
                    case '!': case '"': case '\'': case '#': case '$': case '%':
                    case '&': case '(': case ')': case '*': case '+': case '-':
                    case ',': case '.': case '/': case ':': case ';': case '<':
                    case '>': case '=': case '?': case '@': case '[': case ']':
                    case '^': case '_': case '`': case '{': case '}': case '|':
                    case '~': case '\\':
                        next();
                        builder.append(currentChar());
                        continue;
                }
            }

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
                if(peekUntilLineEnd(1) != null && peekUntilLineEnd(1).matches("^```(.*)"))
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
            if(parseSingleUnderscore(node, inEffect, builder))
                continue;
            if(parseDoubleUnderscore(node, inEffect, builder))
                continue;
            if(parseDoubleTilde(node, inEffect, builder))
                continue;
            if(parseInlineCode(node, inEffect, builder))
                continue;
            if(parseInlineLink(node, inEffect, builder))
                continue;
            if(parseInlineImage(node, inEffect, builder))
                continue;
            
            // Just in case pointer lands on `additionalTerminator` after some parsing the above functions
            if(additionalTerminator != '\0' && currentChar() == additionalTerminator){
                currentIndex--; // promised to not include the current char
                break;
            }
            
            builder.append(currentChar());

            //// Specific to parse()
            // Additional term is used: Do not next() into the terminator
            if(additionalTerminator != '\0' && peekNextChar() == additionalTerminator){
                break;
            }
        }
        createGroupToNode(node, inEffect.copy(), builder);
        return node;
    }
    /**
     * @return successful or not
     */
    private boolean parseSingleAsterisk(TextNode node, TextGroup inEffect, StringBuilder builder){
        boolean isSurroundedBySpaces = peekPreviousChar() == ' ' && peekNextChar() == ' ';
        if(currentChar() == '*' && peekNextChar() != '*' && !isSurroundedBySpaces){
            if((!inEffect.isEmphasis && canReachChar('*', true))){
                createGroupToNode(node, inEffect, builder); // before setting
                inEffect.isEmphasis = true;
                return true;
            }else if(inEffect.isEmphasis){
                // turn it off
                createGroupToNode(node, inEffect, builder);
                inEffect.isEmphasis = false;
                return true;
            }
        }
        return false;
    }
    private boolean parseDoubleAsterisk(TextNode node, TextGroup inEffect, StringBuilder builder){
        boolean isSurroundedWithSpaces = peekPreviousChar() == ' ' && (peekNChar(3) != null && peekNChar(3).charAt(2) == ' ');
        if(currentChar() == '*' && peekNextChar() == '*' && !isSurroundedWithSpaces){
            if(!inEffect.isStrong && canReachStr("**", true)){
                createGroupToNode(node, inEffect, builder);
                inEffect.isStrong = true;
                next(); // goto 2nd '*'
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
    private boolean parseSingleUnderscore(TextNode node, TextGroup inEffect, StringBuilder builder){
        boolean isSurroundedBySpaces = peekPreviousChar() == ' ' && peekNextChar() == ' ';
        boolean isSurroundedByAlphaNum = Character.toString(peekPreviousChar()).matches("[0-9a-zA-Z_]") && Character.toString(peekNextChar()).matches("[0-9a-zA-Z_]");
        if(currentChar() == '_' && peekNextChar() != '_' && !(isSurroundedBySpaces || isSurroundedByAlphaNum)){
            if(!inEffect.isEmphasis && canReachChar('_', true)){
                createGroupToNode(node, inEffect, builder); // before setting
                inEffect.isEmphasis = true;
                return true;
            }else if(inEffect.isEmphasis){
                createGroupToNode(node, inEffect, builder);
                inEffect.isEmphasis = false;
                return true;
            }
        }
        return false;
    }
    private boolean parseDoubleUnderscore(TextNode node, TextGroup inEffect, StringBuilder builder){
        boolean isSurroundedBySpaces = peekPreviousChar() == ' ' && (peekNChar(3) != null && peekNChar(3).charAt(2) == ' ');
        boolean isSurroundedByAlphaNum = Character.toString(peekPreviousChar()).matches("[0-9a-zA-Z]") && peekNChar(3) != null && Character.toString(peekNChar(3).charAt(2)).matches("[0-9a-zA-Z]");
        if(currentChar() == '_' && peekNextChar() == '_' && !(isSurroundedBySpaces || isSurroundedByAlphaNum)){
            if(!inEffect.isStrong && canReachStr("__", false)){
                createGroupToNode(node, inEffect, builder);
                inEffect.isStrong = true;
                next(); // goto 2nd '_'
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
    private boolean parseDoubleTilde(TextNode node, TextGroup inEffect, StringBuilder builder){
        boolean isSurroundedBySpaces = peekPreviousChar() == ' ' && (peekNChar(3) != null && peekNChar(3).charAt(2) == ' ');
        if(currentChar() == '~' && peekNextChar() == '~' && !isSurroundedBySpaces){
            if(!inEffect.isStrikeThrough && canReachStr("~~", true)){
                createGroupToNode(node, inEffect, builder);
                inEffect.isStrikeThrough = true;
                next(); // goto 2nd '~'
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