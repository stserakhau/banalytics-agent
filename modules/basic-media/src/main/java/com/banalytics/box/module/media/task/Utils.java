package com.banalytics.box.module.media.task;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

public class Utils {
    private static final Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();

    public static void saveFrameToFile(Frame frame, File file) throws IOException {
        BufferedImage img = java2DFrameConverter.convert(frame);
        int targetWidth = 200;

        double scale = Math.abs(frame.imageWidth - targetWidth) / (double) Math.max(frame.imageWidth, targetWidth);

        int targetHeight = (int) ((1-scale) * frame.imageHeight);
        img = resizeImage(img, targetWidth, targetHeight);

        ImageWriter writer = null;
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
        if (iter.hasNext()) {
            writer = iter.next();
        } else {
            throw new IOException("");
        }

        // Prepare output file
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
            writer.setOutput(ios);
            JPEGImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
            iwparam.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            iwparam.setCompressionQuality(0.8f);
            writer.write(null, new IIOImage(img, null, null), iwparam);
            ios.flush();
        } finally {
            writer.dispose();
        }
//        ImageIO.write(img, "jpg", file);
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }
}
