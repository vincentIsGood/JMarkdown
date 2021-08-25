package com.vincentcodes.markdown.renderer;

import com.vincentcodes.markdown.inline.TextGroup;
import com.vincentcodes.markdown.inline.TextNode;

public class HtmlRenderer implements Renderer {
    private StringBuilder builder = new StringBuilder();
    private TextGroup currentStyle = new TextGroup();

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
                builder.append("<img href='").append(group.url).append("' alt='").append(group.desc).append("'>");
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
                builder.append(group.value);
            if(group.isLink)
                builder.append(group.desc);
        }
        closeStyleTags(new TextGroup()); // reset style with an empty style
    }
    
    
    // init
    @Override
    public void body(){
        builder.append("<html><body>");
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
        builder.append("<div>");
        renderInnerText(texts);
        builder.append("</div>");
    }
    @Override
    public void append(String text){
        builder.append("<div>");
        builder.append(text);
        builder.append("</div>");
    }

    // Ordered list (ol)
    @Override
    public void ol(TextNode[] texts){
        builder.append("<ol>");
        for(TextNode node : texts){
            builder.append("<li>");
            renderInnerText(node);
            builder.append("</li>");
        }
        builder.append("</ol>");
    }
    // Unordered list (ul)
    @Override
    public void ul(TextNode[] texts){
        builder.append("<ul>");
        for(TextNode node : texts){
            builder.append("<li>");
            renderInnerText(node);
            builder.append("</li>");
        }
        builder.append("</ul>");
    }

    // Table
    @Override
    public void table(TextNode[] headings){
        builder.append("<table>");
        tr(headings);
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
        builder.append(text);
        builder.append("</code></pre>");
    }

    @Override
    public void startBlockQuote(){
        builder.append("<div class='block-quote'>");
    }
    @Override
    public void blockquote(TextNode text, int level){
        // Crude css is provided
        builder.append("<div style='--lvl:").append(level).append("'>");
        renderInnerText(text);
        builder.append("</div>");
    }
    @Override
    public void endBlockQuote(){
        builder.append("</div>");
    }

}
