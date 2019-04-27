package com.seeks.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {
    public static BufferedImage toBufferedImage(ImageIcon imageIcon) {
        // This code ensures that all the pixels in the image are loaded
        Image image = imageIcon.getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        //boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
	       /* if (hasAlpha) {
	         transparency = Transparency.BITMASK;
	         }*/

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            //int type = BufferedImage.TYPE_3BYTE_BGR;//by wang
	        /*if (hasAlpha) {
	         type = BufferedImage.TYPE_INT_ARGB;
	         }*/
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }
    public static String convertRGBToHex(int r, int g, int b) {
        String rFString, rSString, gFString, gSString,
                bFString, bSString, result;
        int red, green, blue;
        int rred, rgreen, rblue;
        red = r / 16;
        rred = r % 16;
        if (red == 10) rFString = "A";
        else if (red == 11) rFString = "B";
        else if (red == 12) rFString = "C";
        else if (red == 13) rFString = "D";
        else if (red == 14) rFString = "E";
        else if (red == 15) rFString = "F";
        else rFString = String.valueOf(red);

        if (rred == 10) rSString = "A";
        else if (rred == 11) rSString = "B";
        else if (rred == 12) rSString = "C";
        else if (rred == 13) rSString = "D";
        else if (rred == 14) rSString = "E";
        else if (rred == 15) rSString = "F";
        else rSString = String.valueOf(rred);

        rFString = rFString + rSString;

        green = g / 16;
        rgreen = g % 16;

        if (green == 10) gFString = "A";
        else if (green == 11) gFString = "B";
        else if (green == 12) gFString = "C";
        else if (green == 13) gFString = "D";
        else if (green == 14) gFString = "E";
        else if (green == 15) gFString = "F";
        else gFString = String.valueOf(green);

        if (rgreen == 10) gSString = "A";
        else if (rgreen == 11) gSString = "B";
        else if (rgreen == 12) gSString = "C";
        else if (rgreen == 13) gSString = "D";
        else if (rgreen == 14) gSString = "E";
        else if (rgreen == 15) gSString = "F";
        else gSString = String.valueOf(rgreen);

        gFString = gFString + gSString;

        blue = b / 16;
        rblue = b % 16;

        if (blue == 10) bFString = "A";
        else if (blue == 11) bFString = "B";
        else if (blue == 12) bFString = "C";
        else if (blue == 13) bFString = "D";
        else if (blue == 14) bFString = "E";
        else if (blue == 15) bFString = "F";
        else bFString = String.valueOf(blue);

        if (rblue == 10) bSString = "A";
        else if (rblue == 11) bSString = "B";
        else if (rblue == 12) bSString = "C";
        else if (rblue == 13) bSString = "D";
        else if (rblue == 14) bSString = "E";
        else if (rblue == 15) bSString = "F";
        else bSString = String.valueOf(rblue);
        bFString = bFString + bSString;
        result = rFString + gFString + bFString;
        return result;

    }
}
