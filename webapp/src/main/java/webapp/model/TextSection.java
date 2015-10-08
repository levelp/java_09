package webapp.model;

/**
 * User: gkislin
 * Date: 20.06.2014
 */
public class TextSection extends Section<String> {
    public static final TextSection EMPTY = new TextSection("");
    static final long serialVersionUID = 1L;

    public TextSection() {
    }

    public TextSection(String... content) {
        super(content);
    }
}
