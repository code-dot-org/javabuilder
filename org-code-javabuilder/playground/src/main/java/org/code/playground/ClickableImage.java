package org.code.playground;

import java.io.FileNotFoundException;

public abstract class ClickableImage extends ImageItem {
    /**
     * Called when this item is clicked. Implement this method in a subclass to
     * control what happens when the user clicks on the item.
     */
    public abstract void onClick();
}
