package org.code.playground;

import java.io.FileNotFoundException;

public abstract class ClickableImage extends ImageItem {

    /**
     * Sets a sound to play when the user clicks on this item.
     * 
     * @param filename the name of the sound file from the asset manager to play
     * @throws FileNotFoundException when the sound file cannot be found.
     */
    public void setClickSound(String filename) throws FileNotFoundException;

    /**
     * Called when this item is clicked. Implement this method in a subclass to
     * control what happens when the user clicks on the item.
     */
    public abstract void onClick();
}
