const theaterImageAndTextSources = {
  "main.json":
    '{"source":{"MyTheater.java":{"text":"import org.code.theater.Theater;\\nimport org.code.theater.Scene;\\nimport org.code.media.Image;\\nimport java.io.FileNotFoundException;\\n\\npublic class MyTheater {\\n  public static void main(String[] args) throws Exception {\\n    Theater.playScenes(createScene());\\n  }\\n\\n  public static Scene createScene() throws FileNotFoundException {\\n    Scene scene = new Scene();\\n    Image image = new Image(\\"dog.jpeg\\");\\n    scene.drawImage(image, 0, 0, 400);\\n    scene.drawText(\\"Hello World\\", 100, 100);\\n    double[] sound = {1.0, 0.0, 1.0, 0.0};\\n    scene.playSound(sound);\\n\\n    return scene;\\n  }\\n}","isVisible":true}},"animations":{}}'
};

export const helloWorld = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"HelloWorld.java":{"text":"public class HelloWorld {\\n  public static void main(String[] args)  {\\n    System.out.println(\\"Hello World\\");\\n  }\\n}","isVisible":true}},"animations":{}}',
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
  sources: theaterImageAndTextSources,
  assetUrls: {"dog.jpeg": 'https://studio.code.org/v3/assets/123456/dog.jpeg'}
});

export const theaterRuntimeFileNotFound = JSON.stringify({
  sources: theaterImageAndTextSources,
  assetUrls: {"unknownCatImage.jpeg": 'https://studio.code.org/v3/assets/123456/unknownCatImage.jpeg'}
});

export const compilationError = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"HelloWorld.java":{"text":"public class HelloWorld {","isVisible":true}},"animations":{}}'
  },
  assetUrls: {}
});

export const blockedClassError = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"HelloWorld.java":{"text":"import java.lang.Thread;\\npublic class HelloWorld {\\n  public static void main(String[] args) {\\n    Thread thread = new Thread();\\n  }\\n}","isVisible":true}},"animations":{}}'
  }
});

export const blankProject = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"MyClass.java":{"text":"","isVisible":true}},"animations":{}}'
  }
});
