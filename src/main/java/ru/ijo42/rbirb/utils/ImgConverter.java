package ru.ijo42.rbirb.utils;

import lombok.extern.slf4j.Slf4j;
import ru.ijo42.rbirb.model.PicType;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Slf4j
public class ImgConverter {

    public static final String FORMAT = "png";

    public static boolean isAnimated(File f) {
        ImageReader is = ImageIO.getImageReadersBySuffix("GIF").next();
        try (ImageInputStream iis = ImageIO.createImageInputStream(f)) {
            is.setInput(iis);
            return is.getNumImages(true) > 1;
        } catch (IOException ex) {
            log.warn("ImgConverter::isAnimated " + ex.getLocalizedMessage());
        }
        return false;
    }

    public static boolean convert(File tmp, Path destination, PicType type) {
        switch (type) {
            case GIF -> {
                return convertAnimated(tmp, destination);
            }
            case PNG -> {
                return convertStatic(tmp, destination);
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public static boolean convertStatic(File tmp, Path destination) {
        try {
            boolean isConvert = convert(tmp, destination.toFile());
            if (tmp.exists())
                tmp.delete();
            return isConvert;
        } catch (Exception ex) {
            log.warn("ImgConverter::convertStatic " + ex.getLocalizedMessage());
            if (tmp.exists())
                tmp.delete();
            if (destination.toFile().exists())
                destination.toFile().delete();
            return false;
        }
    }

    public static boolean convertAnimated(File tmp, Path destination) {
        try {
            Files.move(tmp.toPath(), destination);
            return true;
        } catch (IOException ex) {
            log.warn("ImgConverter::convertAnimated " + ex.getLocalizedMessage());
            if (tmp.exists())
                tmp.delete();
            if (destination.toFile().exists())
                destination.toFile().delete();
            return false;
        }
    }

    public static boolean convert(File inputImagePath,
                                  File outputImagePath) {
        try (FileInputStream inputStream = new FileInputStream(inputImagePath);
             FileOutputStream outputStream = new FileOutputStream(outputImagePath)) {
            BufferedImage inputImage = ImageIO.read(inputStream);
            return ImageIO.write(inputImage, FORMAT, outputStream);
        } catch (Exception ex) {
            // ex.printStackTrace();
            log.warn("ImgConverter::convert " + ex.getLocalizedMessage());
            if (inputImagePath.exists())
                inputImagePath.delete();
            if (outputImagePath.exists())
                outputImagePath.delete();
            return false;
        }
    }
}
