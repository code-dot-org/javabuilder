public abstract class ClickableImage extends Image {

    public ClickableImage(String url) {
        super(url);
    }

    public abstract void onClick();
}