package org.code.playground;

import java.io.FileNotFoundException;

import org.code.media.Image;

public abstract class ClickableImage extends Image {

    /**
     * Sets a sound to play when the user clicks on this image.
     * 
     * @param filename the name of the sound file from the asset manager to play
     * @throws FileNotFoundException when the sound file cannot be found.
     */
    public void setClickSound(String filename) throws FileNotFoundException;

    /**
     * Called when this image is clicked. Implement this method in a subclass to
     * control what happens when the user clicks on the image.
     */
    public abstract void onClick();
}