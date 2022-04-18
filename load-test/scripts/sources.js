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

export const scanner = JSON.stringify({
  sources: {
    "main.json":
      '{"source":{"MyScanner.java":{"text":"import java.util.Scanner;\\n\\npublic class MyScanner {\\n  public static void main(String[] args) {\\n    Scanner myScanner = new Scanner(System.in);\\n    System.out.println(\\"What\'s your name?\\");\\n    String response = myScanner.nextLine();\\n    System.out.println(\\"Hello \\" + response + \\"!\\");\\n  }\\n}\\n","isVisible":true}},"animations":{}}'
  }
})