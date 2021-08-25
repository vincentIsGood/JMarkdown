package com.vincentcodes.markdown.inline;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Example: "<em>asd<strong>fgh</em>hjk</strong>" creates
 * <p>
 * TextNode.groups[TextGroup, TextGroup, TextGroup]
 * <p>
 * // Props:       em       ; em,strong; strong
 */
public class TextNode {
    public List<TextGroup> groups;

    public TextNode(){
        groups = new ArrayList<>();
    }

    public void add(TextGroup group){
        groups.add(group);
    }

    public String toString(){
        return groups.toString();
    }
}
