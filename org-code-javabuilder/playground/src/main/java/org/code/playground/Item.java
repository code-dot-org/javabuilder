package org.code.playground;

public class Item {
  
   /**
   * Creates an item that can be displayed in the Playground. An item simply consists of an image,
   * referenced by the name of the image file in the asset manager.
   *
   * @param filename the string name of the color (case insensitive).
   * @throws FileNotFoundException if the file specified is not the in the asset manager
   */
  public Item(String filename) throws FileNotFoundException;
  
  /**
  * Retrieve the filename for this item.
  *
  * @returns the filename in the asset manager for the image associated with this item.
  */
  public String getFilename();
}
