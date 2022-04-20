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
