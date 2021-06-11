package org.code.theater;

public enum Instrument {
   PIANO, BASS
}

public static class Theater {
   /**
    * Returns the width of the theater canvas. Right now this will always be
    * 400 pixels.
    */
   public static double getWidth() {
   }
   
   /**
    * Returns the height of the theater canvas. Right now this will always be
    * 400 pixels.
    */
   public static double getHeight() {
   }
   
   /**
    * Plays the array of samples provided.
    * 
    * @param sound an array of samples to play.
    */
   public static void playSound(double[] sound) {
   }

   /**
    * Plays the sound referenced by the file name.
    * 
    * @param filename the file to play in the asset manager.
    * @throws FileNotFoundException if the file can't be found in the project.
    */
   public static void playSound(String filename) throws FileNotFoundException {
   }

   /**
    * Plays a note with the selected instrument.
    * 
    * @param instrument the instrument to play.
    * @param note       the note to play. 60 represents middle C on a piano.
    * @param seconds    length of the note. Implementer's note: Behind the scenes,
    *                   this should just be implemented using an array of loopable
    *                   sounds (Mike can generate these).
    */
   public static void playNote(Instrument instrument, int note, double seconds) {
   }

   /**
    * Wait the provided number of seconds before performing the next draw or play
    * command.
    * 
    * @param seconds The number of seconds to wait. This can be a fraction of a
    *                second, but the smallest value can be .1 seconds.
    */
   public static void wait(double seconds) {
   }

   /**
    * Clear everything, starting from the beginning and emptying the canvas.
    */
   public static void reset() {
   }

   /**
    * Draw an image on the canvas at the given location, expanded or shunk to fit
    * the width and height provided
    * 
    * @param file     the name of the file in the asset manager
    * @param x        the left side of the image in the canvas
    * @param y        the top of the image in the canvas
    * @param width    the width to draw the image on the canvas
    * @param height   the height to draw the image on the canvas
    * @param rotation the amount to rotate the image
    * @throws FileNotFoundException if the file can't be found in the project.
    */
   public static void drawImage(String filename, double x, double y, double width, double height, double rotation)
         throws FileNotFoundException {
   }

   /**
    * Draw an image on the canvas at the given location, expanded or shunk to fit
    * the width and height provided
    * 
    * @param img      the Image object to draw on the canvas
    * @param x        the left side of the image in the canvas
    * @param y        the top of the image in the canvas
    * @param width    the width to draw the image on the canvas
    * @param height   the height to draw the image on the canvas
    * @param rotation the amount to rotate the image
    */
   public static void drawImage(Image img, double x, double y, double width, double height, double rotation) {
   }

   /**
    * Draws text on the image.
    * 
    * @param text   the text to draw
    * @param x      the distance from the left side of the image to draw the text.
    * @param y      the distance from the top of the image to draw the text.
    * @param color  the color to draw the text, using any CSS color string (e.g. #234 or green)
    * @param font   the name of the font to draw the text in
    * @param height the height of the text in pixels.
    */
   public static void drawText(String text, double x, double y, String color, String font, double height) {
   }

   /**
    * Draw a line on the cavas.
    * 
    * @param startX the beginning X coordinate of the line.
    * @param startY the beginning Y coordinate of the line.
    * @param endX   the end X coordinate of the line.
    * @param endY   the end Y coordinate of the line.
    */
   public static void drawLine(double startX, double startY, double endX, double endY) {
   }

   /**
    * Draw a regular polygon on the canvas.
    * 
    * @param x     the center X coordinate of the polygon
    * @param y     the center Y coordinate of the polygon
    * @param sides the number of sides of the polygon
    * @param size  the distance from the center to each point on the polyon
    */
   public static void drawPolygon(double x, double y, int sides, double size) {
   }

   /**
    * Draw as a shape by connecting the points provided.
    * 
    * @param points an array of numbers representing the points. For instance, a
    *               triangle could be represented as [x1, y1, x2, y2, x3, y3].
    * @param close  whether to close the shape. If this is set to true, the last
    *               point and the first point will be connected by a line, and if a
    *               fill color is set, the shape will be filled with that color.
    */
   public static void drawShape(double[] points, boolean close) {
   }

   /**
    * Draws an ellipse (an oval or a circle) on the canvas.
    * 
    * @param x      the left side of the ellipse.
    * @param y      the top of the ellipse
    * @param width  the width of the ellipse
    * @param height the height of the ellipse
    */
   public static void drawEllipse(double x, double y, double width, double height) {
   }

   /**
    * Draws a rectangle on the canvas.
    * 
    * @param x      the left side of the rectangle.
    * @param y      the top of the rectangle.
    * @param width  the width of the rectangle.
    * @param height the height of the rectangle.
    */
   public static void drawRectangle(double x, double y, double width, double height) {
   }

   /**
    * Sets the thickness of lines drawn
    * 
    * @param width
    */
   public static void setStrokeWidth(double width) {
   }

   /**
    * Sets the color of lines drawn.
    * 
    * @param CSS color string 
    */
   public static void setStrokeColor(String color) {
   }

   /**
    * Sets the fill color for all shapes drawn
    * 
    * @param color CSS color string to fill any shape. Defaults to "none", and 
    *              setting it to "none" ensures that any shapes are not filled.
    */
   public static void setFillColor(String color) {
   }

   /**
    * Plays the script.
    */
   public static void play() {
   }
}
