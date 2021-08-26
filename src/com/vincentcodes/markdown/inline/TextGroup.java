package com.vincentcodes.markdown.inline;

/**
 * Sample TextGroup are:
 * <pre>
 * // For the text: "asd*as**d*asd**123*\n*123**123"
 * [{TextGroup value:'asd'}, {TextGroup em value:'as'}, {TextGroup strong em value:'d'}, 
 * {TextGroup strong value:'asd'}, {TextGroup value:'123* '}, {TextGroup em value:'123*'}, 
 * {TextGroup value:'123'}]
 * </pre>
 */
public class TextGroup {
    // I can create some properties class and a List<Prop> to make it scale better
    public boolean isStrong = false;
    public boolean isEmphasis = false;
    public boolean isStrikeThrough = false;
    
    public boolean isCode = false;
    public boolean isLink = false;
    public boolean isImage = false;

    public String value;
    
    public String desc; // for link / image
    public String url; // for link / image

    public void resetStyles(){
        this.isStrong = false;
        this.isEmphasis = false;
        this.isStrikeThrough = false;
        this.isCode = false;
        this.isLink = false;
        this.isImage = false;
    }

    public TextGroup copy(){
        TextGroup newGroup = new TextGroup();
        newGroup.isStrong = this.isStrong;
        newGroup.isEmphasis = this.isEmphasis;
        newGroup.isStrikeThrough = this.isStrikeThrough;
        newGroup.isCode = this.isCode;
        newGroup.isLink = this.isLink;
        newGroup.isImage = this.isImage;
        newGroup.value = this.value;
        return newGroup;
    }

    public String toString(){
        return TextGroup.toString(this);
    }

    public static String toString(TextGroup group){
        StringBuilder str = new StringBuilder("{TextGroup ");
        if(group.isStrong) str.append("strong ");
        if(group.isEmphasis) str.append("em ");
        if(group.isStrikeThrough) str.append("strike ");
        if(group.isCode) str.append("code ");
        if(group.isLink) str.append("link ");
        if(group.isImage) str.append("image ");
        if(group.desc != null)
            str.append("desc:'" + group.desc + "' ");
        if(group.url != null)
            str.append("url:'" + group.url + "' ");
        str.append("value:'" + group.value + "'}");
        return str.toString();
    }
}
