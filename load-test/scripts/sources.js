export const helloWorld = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"HelloWorld.java":{"text":"public class HelloWorld {\\n  public static void main(String[] args)  {\\n    System.out.println(\\"Hello World\\");\\n  }\\n}","isVisible":true}},"animations":{}}',
  },
  assetUrls: {},
});

export const throwsException = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"HelloWorld.java":{"text":"public class HelloWorld {\\n  public static void main(String[] args) throws Exception {\\n    System.out.println(\\"Hello World!\\");\\n    throw new Exception();\\n  }\\n}","isVisible":true}},"animations":{}}',
  },
  assetUrls: {},
});

export const neighborhood = JSON.stringify({
  sources: {
    "grid.txt":
      "[[{\"tileType\":2,\"value\":0,\"assetId\":287},{\"tileType\":1,\"assetId\":303,\"value\":5},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0}],[{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":0,\"assetId\":53,\"value\":0},{\"tileType\":0,\"assetId\":54,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":99,\"assetId\":303}],[{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0}],[{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":0,\"assetId\":12,\"value\":0},{\"tileType\":0,\"assetId\":13,\"value\":0}],[{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":0,\"assetId\":14,\"value\":0},{\"tileType\":0,\"assetId\":15,\"value\":0}],[{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":0,\"assetId\":16,\"value\":0},{\"tileType\":0,\"assetId\":17,\"value\":0}],[{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":0,\"assetId\":18,\"value\":0},{\"tileType\":0,\"assetId\":19,\"value\":0}],[{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0},{\"tileType\":1,\"value\":0}]]",
    "main.json":
      "{\"source\":{\"PainterTest.java\":{\"text\":\"import org.code.neighborhood.*;\\n\\npublic class PainterTest {\\n  public static void main(String[] args) {\\n    Painter p = new Painter(0, 0, \\\"east\\\", 1);\\n    p.paint(\\\"blue\\\");\\n  }\\n}\",\"isVisible\":true}},\"animations\":{}}"
  },
  "assetUrls":{}
});

export const scanner = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"MyScanner.java":{"text":"import java.util.Scanner;\\n\\npublic class MyScanner {\\n  public static void main(String[] args) {\\n    Scanner myScanner = new Scanner(System.in);\\n    System.out.println(\\"What\'s your name?\\");\\n    String response = myScanner.nextLine();\\n    System.out.println(\\"Hello \\" + response + \\"!\\");\\n  }\\n}\\n","isVisible":true}},"animations":{}}'
  }
});

export const theaterImageAndText = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"MyTheater.java":{"text":"import org.code.theater.Theater;\\nimport org.code.theater.Scene;\\nimport org.code.media.Image;\\nimport java.io.FileNotFoundException;\\n\\npublic class MyTheater {\\n  public static void main(String[] args) throws Exception {\\n    Theater.playScenes(createScene());\\n  }\\n\\n  public static Scene createScene() throws FileNotFoundException {\\n    Scene scene = new Scene();\\n    Image image = new Image(\\"dog.jpeg\\");\\n    scene.drawImage(image, 0, 0, 400);\\n    scene.drawText(\\"Hello World\\", 100, 100);\\n    double[] sound = {1.0, 0.0, 1.0, 0.0};\\n    scene.playSound(sound);\\n\\n    return scene;\\n  }\\n}","isVisible":true}},"animations":{}}'
  },
  assetUrls: {"dog.jpeg": 'https://studio.code.org/v3/assets/123456/dog.jpeg'}
});

export const theaterImageFilter = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"MyConcert.java":{"text":"import org.code.theater.*;\\n\\npublic class MyConcert {\\n  public static void main(String[] args) {\\n    Scene myScene = new Scene();\\n    myScene.drawImage(\\"demo10.jpeg\\", 0, 0, myScene.getWidth(), myScene.getHeight(), 0);\\n\\n\\t\\tGrayscaleImage grayImage = new GrayscaleImage(\\"demo10.jpeg\\");\\n\\t\\tgrayImage.grayscale();\\n\\t\\tmyScene.drawImage(grayImage, 0, 0, myScene.getWidth(), myScene.getHeight(), 0);\\n\\n    Theater.playScenes(myScene);\\n  }\\n}","isVisible":true},"GrayscaleImage.java":{"text":"import org.code.theater.*;\\nimport org.code.media.*;\\n\\npublic class GrayscaleImage extends Image {\\n  private Pixel[][] imagePixels;\\n\\n  public GrayscaleImage(String filename) {\\n    super(filename);\\n    imagePixels = ImageEffect.getPixels(this);\\n  }\\n\\n  public void grayscale() {\\n    for (int row = 0; row < imagePixels.length; row++) {\\n      for (int col = 0; col < imagePixels[0].length; col++) {\\n        Pixel currentPixel = imagePixels[row][col];\\n\\n        int average = (int)((currentPixel.getRed() + currentPixel.getGreen() + currentPixel.getBlue()) / 3);\\n\\n        currentPixel.setRed(average);\\n        currentPixel.setGreen(average);\\n        currentPixel.setBlue(average);\\n      }\\n    }\\n  }\\n}","isVisible":true},"ImageEffect.java":{"text":"import org.code.media.*;\\n\\npublic final class ImageEffect {\\n\\tpublic static Pixel[][] getPixels(Image theImage) {\\n    int width = theImage.getWidth();\\n    int height = theImage.getHeight();\\n    Pixel[][] pixelArray = new Pixel[height][width];\\n\\n    for (int row = 0; row < height; row++) {\\n      for (int col = 0; col < width; col++) {\\n        pixelArray[row][col] = theImage.getPixel(col, row);\\n      }\\n    }\\n\\n    return pixelArray;\\n  }\\n  \\n}","isVisible":true}},"animations":{}}'
  },
  assetUrls: {"demo10.jpeg": 'https://studio.code.org/v3/assets/123456/demo10.jpeg'}
});
