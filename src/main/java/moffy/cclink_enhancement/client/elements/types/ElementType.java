package moffy.cclink_enhancement.client.elements.types;

public enum ElementType {
    SCALABLE_ITEM("ScalableItem"),
    FORMATTED_TEXT("FormattedText"),
    ENTITY("Entity"),
    TEXTURE("Texture");

    private final String name;

    ElementType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
