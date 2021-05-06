import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Image {
    private String uuid;
    private String originalUrl;

    public void setUUID(String uuid){
        this.uuid=uuid;
    }

    public String getUUID() {
        return uuid;
    }

    public String getTempUrl() {
        return toString();
    }

    BufferedImage image;

    public Image(String url) {
        originalUrl = url;
        try {
            image = ImageIO.read(
                    new URL(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Image(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    void encode(OutputStream stream, String name) throws IOException {
        ImageIO.write(image, name, stream);
    }

    public String toString() {
        if (originalUrl != "") {
            return originalUrl;
        }

        try {
            Path path = Paths.get("www");
            Path temp = Files.createTempFile(path, "temp", ".bmp");
            OutputStream out = Files.newOutputStream(temp);
            encode(out, temp.toString());
            out.close();
            return temp.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}