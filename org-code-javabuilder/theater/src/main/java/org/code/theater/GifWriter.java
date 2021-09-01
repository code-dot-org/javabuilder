package org.code.theater;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalServerRuntimeError;

/**
 * Writer to generate a gif from a set of images. The gif will be stored in the given
 * ByteArrayOutputStream.
 */
class GifWriter {
  private ImageWriter writer;
  private ImageWriteParam params;
  private ImageOutputStream imageOutputStream;

  public static class Factory {
    public GifWriter createGifWriter(ByteArrayOutputStream out) {
      return new GifWriter(out);
    }
  }

  GifWriter(ByteArrayOutputStream out) {
    try {
      this.imageOutputStream = ImageIO.createImageOutputStream(out);
      this.writer = ImageIO.getImageWritersBySuffix("gif").next();
      this.params = writer.getDefaultWriteParam();
      this.writer.setOutput(this.imageOutputStream);
      IIOMetadata streamData = this.getMetadataForGif();
      this.writer.prepareWriteSequence(streamData);
    } catch (IOException e) {
      throw new InternalServerRuntimeError(
          InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e.getCause());
    }
  }

  /**
   * Write the given image as the next frame of the gif.
   *
   * @param img BufferedImage
   * @param delay delay in milliseconds after this frame of the gif.
   */
  public void writeToGif(BufferedImage img, int delay) {
    try {
      this.writer.writeToSequence(
          new IIOImage(img, null, getMetadataForFrame(delay, img.getType())), this.params);

    } catch (IOException e) {
      throw new InternalServerRuntimeError(
          InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e.getCause());
    }
  }

  /**
   * Close the gif stream and flush any remaining bytes. Any updates after close will throw an
   * exception because the writer has been closed.
   */
  public void close() {
    try {
      this.writer.endWriteSequence();
      this.imageOutputStream.flush();
    } catch (IOException e) {
      throw new InternalServerRuntimeError(
          InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e.getCause());
    }
  }

  /**
   * Get the metadata for the next frame with the given delay and image type. See Gif metadata
   * specification here:
   * https://javadoc.scijava.org/Java7/javax/imageio/metadata/doc-files/gif_metadata.html
   *
   * @param delay int delay in milliseconds after this frame
   * @param imageType int
   * @return IIOMetadata for the next frame
   */
  private IIOMetadata getMetadataForFrame(int delay, int imageType) {
    IIOMetadata metadata =
        this.writer.getDefaultImageMetadata(
            ImageTypeSpecifier.createFromBufferedImageType(imageType), params);
    String metaFormatName = metadata.getNativeMetadataFormatName();
    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

    IIOMetadataNode graphicsControlExtensionNode = this.getNode(root, "GraphicControlExtension");
    graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
    graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
    graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
    // delay is expected in hundredths of a second, divide milliseconds by 10
    graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));
    graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

    try {
      metadata.setFromTree(metaFormatName, root);
    } catch (IIOInvalidTreeException e) {
      throw new InternalServerRuntimeError(
          InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e.getCause());
    }
    return metadata;
  }

  /**
   * Find metadata node with the given name from the rootNode
   *
   * @param rootNode
   * @param nodeName
   * @return Requested IIOMetadataNode
   */
  private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
    int nNodes = rootNode.getLength();
    for (int i = 0; i < nNodes; i++) {
      if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
        return (IIOMetadataNode) rootNode.item(i);
      }
    }
    IIOMetadataNode node = new IIOMetadataNode(nodeName);
    rootNode.appendChild(node);
    return (node);
  }

  /**
   * Get overall metadata for the gif. Sets the width and height to be 400 px.
   *
   * @return IIOMetadata
   */
  private IIOMetadata getMetadataForGif() {
    IIOMetadata streamData = this.writer.getDefaultStreamMetadata(this.params);
    String metaFormatName = streamData.getNativeMetadataFormatName();
    IIOMetadataNode root = (IIOMetadataNode) streamData.getAsTree(metaFormatName);
    IIOMetadataNode screenDescriptor = this.getNode(root, "LogicalScreenDescriptor");
    screenDescriptor.setAttribute("logicalScreenWidth", "400");
    screenDescriptor.setAttribute("logicalScreenHeight", "400");
    try {
      streamData.setFromTree(metaFormatName, root);
    } catch (IIOInvalidTreeException e) {
      throw new InternalServerRuntimeError(
          InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e.getCause());
    }
    return streamData;
  }
}
