package com.vincentcodes.markdown.renderer;

import java.util.ArrayDeque;
import java.util.Deque;

import com.vincentcodes.markdown.inline.TextGroup;
import com.vincentcodes.markdown.inline.TextNode;

public class HtmlRenderer implements Renderer {
    private StringBuilder builder = new StringBuilder();
    private TextGroup currentStyle = new TextGroup();

    private boolean includeStyleTag = true;

    public HtmlRenderer(){}

    public HtmlRenderer(boolean includeStyleTag){
        this.includeStyleTag = includeStyleTag;
    }

    public String getRenderedHtml(){
        return builder.toString();
    }

    /**
     * Any differences toward what we have now means something changed.
     * Using this mechanism, we can add / close the tags based on the
     * current state. (eg. !Bold -> Bold, Bold -> !Bold)
     * @param group
     */
    private void addStyleTags(TextGroup group){
        if(group.isStrong == !currentStyle.isStrong){
            if(!currentStyle.isStrong){
                builder.append("<strong>");
                currentStyle.isStrong = !currentStyle.isStrong;
            }
        }
        if(group.isEmphasis == !currentStyle.isEmphasis){
            if(!currentStyle.isEmphasis){
                builder.append("<em>");
                currentStyle.isEmphasis = !currentStyle.isEmphasis;
            }
        }
        if(group.isStrikeThrough == !currentStyle.isStrikeThrough){
            if(!currentStyle.isStrikeThrough){
                builder.append("<s>");
                currentStyle.isStrikeThrough = !currentStyle.isStrikeThrough;
            }
        }
        if(group.isCode == !currentStyle.isCode){
            if(!currentStyle.isCode){
                builder.append("<code>");
                currentStyle.isCode = !currentStyle.isCode;
            }
        }
        if(group.isLink == !currentStyle.isLink){
            if(!currentStyle.isLink){
                builder.append("<a href='").append(group.url).append("'>");
                currentStyle.isLink = !currentStyle.isLink;
            }
        }
        if(group.isImage == !currentStyle.isImage){
            if(!currentStyle.isImage){
                builder.append("<img src='").append(group.url).append("' alt='").append(group.desc).append("'>");
            }
        }
    }
    private void closeStyleTags(TextGroup group){
        if(group.isStrong == !currentStyle.isStrong){
            if(currentStyle.isStrong){
                builder.append("</strong>");
                currentStyle.isStrong = !currentStyle.isStrong;
            }
        }
        if(group.isEmphasis == !currentStyle.isEmphasis){
            if(currentStyle.isEmphasis){
                builder.append("</em>");
                currentStyle.isEmphasis = !currentStyle.isEmphasis;
            }
        }
        if(group.isStrikeThrough == !currentStyle.isStrikeThrough){
            if(currentStyle.isStrikeThrough){
                builder.append("</s>");
                currentStyle.isStrikeThrough = !currentStyle.isStrikeThrough;
            }
        }
        if(group.isCode == !currentStyle.isCode){
            if(currentStyle.isCode){
                builder.append("</code>");
                currentStyle.isCode = !currentStyle.isCode;
            }
        }
        if(group.isLink == !currentStyle.isLink){
            if(currentStyle.isLink){
                builder.append("</a>");
                currentStyle.isLink = !currentStyle.isLink;
            }
        }
    }
    /**
     * Appends texts directly into StringBuilder
     * @param texts
     */
    private void renderInnerText(TextNode texts){
        for(TextGroup group : texts.groups){
            closeStyleTags(group);
            addStyleTags(group);
            if(group.value != null)
                builder.append(group.value.replace("<", "&lt;").replaceAll("\n", "<br>"));
            if(group.isLink)
                builder.append(group.desc);
        }
        closeStyleTags(new TextGroup()); // reset style with an empty style
    }
    
    
    // init
    @Override
    public void body(){
        builder.append("<html>");
        if(includeStyleTag){
            builder.append("<style>");
            builder.append("/* Pre-defined Markdown Styles */");
            builder.append("/* Style provided by https://github.com/sindresorhus/github-markdown-css */");
            builder.append(getPreDefinedCss());
            builder.append("</style>");
        }
        builder.append("<body class='markdown-body'>");
    }

    // close
    @Override
    public void done(){
        builder.append("</body></html>");
        currentStyle = new TextGroup();
    }

    // Horizontal line
    @Override
    public void hr(){
        builder.append("<hr>");
    }

    // Headings
    @Override
    public void h1(TextNode texts){
        builder.append("<h1>");
        renderInnerText(texts);
        builder.append("</h1>");
    }
    @Override
    public void h2(TextNode texts){
        builder.append("<h2>");
        renderInnerText(texts);
        builder.append("</h2>");
    }
    @Override
    public void h3(TextNode texts){
        builder.append("<h3>");
        renderInnerText(texts);
        builder.append("</h3>");
    }
    @Override
    public void h4(TextNode texts){
        builder.append("<h4>");
        renderInnerText(texts);
        builder.append("</h4>");
    }
    @Override
    public void h5(TextNode texts){
        builder.append("<h5>");
        renderInnerText(texts);
        builder.append("</h5>");
    }
    @Override
    public void h6(TextNode texts){
        builder.append("<h6>");
        renderInnerText(texts);
        builder.append("</h6>");
    }

    // Normal texts
    @Override
    public void p(TextNode texts){
        builder.append("<div class='para'>");
        renderInnerText(texts);
        builder.append("</div>");
    }
    @Override
    public void append(String text){
        builder.append("<div class='appended-text'>");
        builder.append(text);
        builder.append("</div>");
    }

    
    /**
     * Tools to create your own list (optional)
     */
    private Deque<Integer> unclosedLi = new ArrayDeque<>();
    // ordered list
    public void ol() {
        if(builder.substring(builder.length()-5, builder.length()).equals("</li>")){
            builder.delete(builder.length()-5, builder.length()); // delete 4 chars
            unclosedLi.push(1);
        }
        builder.append("<ol>");
    }
    // unordered list
    public void ul() {
        if(builder.substring(builder.length()-5, builder.length()).equals("</li>")){
            builder.delete(builder.length()-5, builder.length()); // delete 4 chars
            unclosedLi.push(1);
        }
        builder.append("<ul>");
    }
    public void li(TextNode text) {
        builder.append("<li>");
        renderInnerText(text);
        builder.append("</li>");
    }
    public void endol() {
        builder.append("</ol>");
        if(unclosedLi.size() > 0){
            builder.append("</li>");
            unclosedLi.pop();
        }
    }
    public void endul() {
        builder.append("</ul>");
        if(unclosedLi.size() > 0){
            builder.append("</li>");
            unclosedLi.pop();
        }
    }

    // Table
    @Override
    public void table(TextNode[] headings){
        builder.append("<table>");
        builder.append("<tr>");
        for(TextNode node : headings){
            builder.append("<th>");
            renderInnerText(node);
            builder.append("</th>");
        }
        builder.append("</tr>");
    }
    @Override
    public void tr(TextNode[] rowEntries){
        builder.append("<tr>");
        for(TextNode node : rowEntries){
            builder.append("<td>");
            renderInnerText(node);
            builder.append("</td>");
        }
        builder.append("</tr>");
    }
    @Override
    public void endTable(){
        builder.append("</table>");
    }

    @Override
    public void codeblock(String text, String lang){
        builder.append("<pre><code class='language-").append(lang).append("'>");
        builder.append(text.replace("<", "&lt;"));
        builder.append("</code></pre>");
    }

    /**
     * Github css wants nested <blockquote>s
     */
    private int previousLevel = 1;
    @Override
    public void startBlockQuote(){
        previousLevel = 1;
        builder.append("<blockquote>");
    }
    @Override
    public void blockquote(TextNode text, int level){
        if(level > previousLevel){
            builder.append("<blockquote>");
            previousLevel = level;
        }else if(level < previousLevel){
            for(int i = 0; i < (previousLevel - level); i++)
                builder.append("</blockquote>");
            previousLevel = level;
        }
        builder.append("<p style='--lvl:").append(level).append("'>");
        renderInnerText(text);
        builder.append("</p>");
    }
    @Override
    public void endBlockQuote(){
        // at least once because previousLevel starts with 1
        for(int i = 0; i < previousLevel; i++)
            builder.append("</blockquote>");
    }

    /**
     * Style is provided by https://github.com/sindresorhus/github-markdown-css
     */
    private String getPreDefinedCss(){
        StringBuilder builder = new StringBuilder();
        builder.append(".markdown-body {    font-family: Lato,Helvetica Neue,Helvetica,sans-serif;}.markdown-body h1,.markdown-body h2,.markdown-body h3,.markdown-body h4,.markdown-body h5,.markdown-body h6 {  margin-top: 0;  margin-bottom: 0;}");
        builder.append(".markdown-body h1 {  font-size: 32px;}.markdown-body h1,.markdown-body h2 {  font-weight: 600;}.markdown-body h2 {  font-size: 24px;}.markdown-body h3 {  font-size: 20px;}.markdown-body h3,.markdown-body h4 {  font-weight: 600;}");
        builder.append(".markdown-body h4 {  font-size: 16px;}.markdown-body h5 {  font-size: 14px;}.markdown-body h5,.markdown-body h6 {  font-weight: 600;}.markdown-body h6 {  font-size: 12px;}.markdown-body p {  margin-top: 0;  margin-bottom: 10px;}");
        builder.append(".markdown-body blockquote {  margin: 0;}.markdown-body ol,.markdown-body ul {  padding-left: 0;  margin-top: 0;  margin-bottom: 0;}.markdown-body ol ol,.markdown-body ul ol {  list-style-type: lower-roman;}.markdown-body ol ol ol,");
        builder.append(".markdown-body ol ul ol,.markdown-body ul ol ol,.markdown-body ul ul ol {  list-style-type: lower-alpha;}.markdown-body dd {  margin-left: 0;}.markdown-body code,.markdown-body pre {  font-family: SFMono-Regular,Consolas,Liberation Mono,Menlo,monospace;  font-size: 12px;}");
        builder.append(".markdown-body pre {  margin-top: 0;  margin-bottom: 0;}");
        builder.append(".markdown-body blockquote,.markdown-body details,.markdown-body dl,.markdown-body ol,.markdown-body p,.markdown-body pre,.markdown-body table,.markdown-body ul {  margin-top: 0;  margin-bottom: 16px;}");
        builder.append(".markdown-body hr {  height: .25em;  padding: 0;  margin: 24px 0;  background-color: #e1e4e8;  border: 0;}.markdown-body blockquote {  padding: 0 1em;  color: #6a737d;  border-left: .25em solid #dfe2e5;}.markdown-body blockquote>:first-child {  margin-top: 0;}");
        builder.append(".markdown-body blockquote>:last-child {  margin-bottom: 0;}.markdown-body h1,.markdown-body h2,.markdown-body h3,.markdown-body h4,.markdown-body h5,.markdown-body h6 {  margin-top: 24px;  margin-bottom: 16px;  font-weight: 600;  line-height: 1.25;}");
        builder.append(".markdown-body h1 {  font-size: 2em;}.markdown-body h1,.markdown-body h2 {  padding-bottom: .3em;  border-bottom: 1px solid #eaecef;}.markdown-body h2 {  font-size: 1.5em;}.markdown-body h3 {  font-size: 1.25em;}.markdown-body h4 {  font-size: 1em;}.markdown-body h5 {  font-size: .875em;}");
        builder.append(".markdown-body h6 {  font-size: .85em;  color: #6a737d;}.markdown-body ol,.markdown-body ul {  padding-left: 2em;}.markdown-body ol ol,.markdown-body ol ul,.markdown-body ul ol,.markdown-body ul ul {  margin-top: 0;  margin-bottom: 0;}.markdown-body li {  word-wrap: break-all;}");
        builder.append(".markdown-body li>p {  margin-top: 16px;}.markdown-body li+li {  margin-top: .25em;}.markdown-body table {  display: block;  width: 100%;  overflow: auto;}.markdown-body table th {  font-weight: 600;}.markdown-body table td,.markdown-body table th {  padding: 6px 13px;  border: 1px solid #dfe2e5;}");
        builder.append(".markdown-body table tr {  background-color: #fff;  border-top: 1px solid #c6cbd1;}.markdown-body table tr:nth-child(2n) {  background-color: #f6f8fa;}.markdown-body img {  max-width: 100%;  box-sizing: initial;  background-color: #fff;}.markdown-body img[align=right] {  padding-left: 20px;}");
        builder.append(".markdown-body img[align=left] {  padding-right: 20px;}.markdown-body code {  padding: .2em .4em;  margin: 0;  font-size: 85%;  background-color: rgba(27,31,35,.05);  border-radius: 3px;}.markdown-body pre {  word-wrap: normal;}");
        builder.append(".markdown-body pre>code {  padding: 0;  margin: 0;  font-size: 100%;  word-break: normal;  white-space: pre;  background: transparent;  border: 0;}.markdown-body .highlight {  margin-bottom: 16px;}.markdown-body .highlight pre {  margin-bottom: 0;  word-break: normal;}");
        builder.append(".markdown-body .highlight pre,.markdown-body pre {  padding: 16px;  overflow: auto;  font-size: 85%;  line-height: 1.45;  background-color: #f6f8fa;  border-radius: 3px;}.markdown-body pre code {  display: inline;  max-width: auto;  padding: 0;  margin: 0;  overflow: visible;  line-height: inherit;  word-wrap: normal;  background-color: initial;  border: 0;}");
        builder.append(".para {margin-bottom: 1rem;}");
        return builder.toString();
    }

}