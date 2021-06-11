package org.code.theater;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

// Writer to generate a gif from a set of images.
// Adapted from https://memorynotfound.com/generate-gif-image-java-delay-infinite-loop-example/
public class GifWriter {
  private ImageWriter writer;
  private ImageWriteParam params;
  private ImageOutputStream imageOutputStream;

  public GifWriter(ByteArrayOutputStream out) throws IOException {
    this.imageOutputStream = ImageIO.createImageOutputStream(out);
    this.writer = ImageIO.getImageWritersBySuffix("gif").next();
    this.params = writer.getDefaultWriteParam();

    this.writer.setOutput(this.imageOutputStream);
    this.writer.prepareWriteSequence(null);
  }

  public void writeToGif(BufferedImage img, int delay) throws IOException {
    this.writer.writeToSequence(
        new IIOImage(img, null, getMetadataForFrame(delay, img.getType())), params);
  }

  public void close() throws IOException {
    this.writer.endWriteSequence();
    this.imageOutputStream.flush();
  }

  private IIOMetadata getMetadataForFrame(int delay, int imageType) throws IIOInvalidTreeException {
    IIOMetadata metadata =
        this.writer.getDefaultImageMetadata(
            ImageTypeSpecifier.createFromBufferedImageType(imageType), params);
    String metaFormatName = metadata.getNativeMetadataFormatName();
    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

    IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
    graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
    graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
    graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
    graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));
    graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

    metadata.setFromTree(metaFormatName, root);
    return metadata;
  }

  private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
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
}
