import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

public class Park {

 private static HashMap<String, ClickableImage> clickables = new HashMap<>();
 private static HashMap<String, Image> images = new HashMap<>();

    static void addClickableImage(ClickableImage image, float x, float y) {
       String id = UUID.randomUUID().toString();
       image.setUUID(id);
       clickables.put(id, image);

       String tempURL = image.getTempUrl();
       // Hacked messaging through the console :)
       System.out.println("${" +
               "\"action\": \"addclickable\"," +
               "\"url\":\"" + tempURL + "\"," +
               "\"id\": \"" + id + "\"," +
               "\"x\": " + x + "," + "\"y\": " + y + "}");
    }

    static void removeClickableImage(ClickableImage image) {
       clickables.remove(image.getUUID());
       System.out.println("${" +
            "\"action\": \"removeclickable\"," +
            "\"id\": \"" + image.getUUID() + "\"}");
    }

    static void addImage(Image image, float x, float y) {
       String id = UUID.randomUUID().toString();
       image.setUUID(id);
       images.put(id, image);

       String tempURL = image.getTempUrl();

       System.out.println("${" +
               "\"action\": \"addimg\"," +
               "\"url\":\"" + tempURL + "\"," +
               "\"id\": \"" + image.getUUID() + "\"," +
               "\"x\": " + x + "," + "\"y\": " + y + "}");
    }

    static void removeImage(Image image) {
       images.remove(image.getUUID());
       System.out.println("${" +
               "\"action\": \"removeimg\"," +
               "\"id\": \"" + image.getUUID() + "\"}");
    }

    static void run() {
       Scanner myScanner = new Scanner(System.in);
       while (true) {
          if (myScanner.hasNext()) {
             String line = myScanner.nextLine();
             if (line.charAt(0) == '$') {
                ClickableImage ci = clickables.get(line.substring(1));
                if (ci != null) {
                   ci.onClick();
                } else {
                   System.out.println("id not found");
                }
             }
          }
       }
    }
}
