package com.github.davidmoten.latex;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public final class Renderer {

    private Renderer() {
        // prevent instantiation
    }

    public enum Format {
        PNG("png");

        private final String formatName;

        private Format(String formatName) {
            this.formatName = formatName;
        }

        public String formatName() {
            return formatName;
        }
    }

    public static void renderPng(String latex, OutputStream out) {
        render(latex, out, Format.PNG, "image/png", 20, Color.white, Color.black, 5);
    }

    public static byte[] renderPng(String latex) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        renderPng(latex, bytes);
        return bytes.toByteArray();
    }

    public static void render(String latex, OutputStream out, Format format, String mimeType,
            int size, Color background, Color foreground, int margin) {
        TeXFormula formula = new TeXFormula(latex);
        TeXIcon icon = formula.new TeXIconBuilder() //
                .setStyle(TeXConstants.STYLE_DISPLAY) //
                .setSize(size) //
                .build();
        icon.setInsets(new Insets(margin, margin, margin, margin));
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(background);
        g.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
        JLabel jl = new JLabel();
        jl.setForeground(foreground);
        icon.paintIcon(jl, g, 0, 0);
        try {
            ImageIO.write(image, format.formatName(), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
