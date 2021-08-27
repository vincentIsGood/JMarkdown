# JMarkdown

A Java markdown library is created with an objective to provide an easy interface to convert Markdown syntax to other syntax. A simple Markdown to Html renderer is provided. Hope everyone can find it useful.

Since this the MarkdownParser is still in development, there could be bugs.

## Usage

Compile your java project by including this JMarkdown library in your project's classpath.

## How to use `HtmlRenderer` or `OoxmlWordRenderer`

Some tests are written inside `src/com/vincentcodes/tests/markdown/`, you may make reference to those.

Within TemplateParsingTest.java
```java
public class TemplateParsingTest {
    public static void main(String[] args) throws IOException{
        try(FileInputStream fis = new FileInputStream("Template.md")){
            MarkdownParser parser = new MarkdownParser(new HtmlRenderer());
            parser.parse(new String(fis.readAllBytes()));

            HtmlRenderer renderer = (HtmlRenderer)parser.getRenderer();
            System.out.println(renderer.getRenderedHtml());
        }
    }
}
```

Styles made not be the finest, you may customize it by extending or modifying `OoxmlWordRenderer` class itself.

Within TemplateParsingToWordTest.java
```java
public class TemplateParsingToWordTest {
    public static void main(String[] args) throws IOException{
        try(FileInputStream fis = new FileInputStream("Template.md")){
            MarkdownParser parser = new MarkdownParser(new OoxmlWordRenderer(new File("out.docx"), new File("TemplateWithThemes.docx")));
            parser.parse(new String(fis.readAllBytes()));
        }
    }
}
```

## Creating your own Renderer

You can create your own Renderer to convert Markdown to languages. First, you need to `implement` the `Renderer` interface.

```java
public class HtmlRenderer implements Renderer {
    ...
}
```

After that, let's take a look inside the `Renderer` interface. There are multiple methods like `body()`, `h1(TextNode)`, etc. These functions are called by the parser when it starts parsing (by calling the `parse()` method). 

For example, when the parser encounters a heading `#`, `h1(TextNode)` is called with the `TextNode` object. `TextNode` stores information of a group of texts `TextGroup` each text group has information on the style of the text it stores. Feel free to use `TextNode.toString()` to observe what they stores.

```java
public interface Renderer {

    /**
     * Init
     */
    void body();

    /**
     * Close
     */
    void done();

    // Headings
    void h1(TextNode texts);
    void h2(TextNode texts);
    ...
}
```

After that you can include it into your code.

```java
MarkdownParser parser = new MarkdownParser(new CustomRenderer());
```

## Upcoming plans

- A command line util will be implemented for markdown to html conversion.
- Markdown to OpenXML renderer will be implemented