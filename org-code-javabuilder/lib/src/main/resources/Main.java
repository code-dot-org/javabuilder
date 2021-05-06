public class Main extends ClickableImage {
    public void onClick() {
        Park.removeClickableImage(this);
    }

    public Main(String url) {
        super(url);
    }
    public static void main(String[] args) {
        Main foo = new Main("https://placekitten.com/256/256");
        Park.addClickableImage(foo, 0, 0);

        Park.run();
    }
}