package net.runelite.cache;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HeightMapLegend {

    private static final int LEGEND_WIDTH = 200;
    private static final int LEGEND_HEIGHT = 700;

    private static final float MAX_HEIGHT = 2048f;

    public static void main(String[] args) throws IOException {
        BufferedImage legendImage = createLegend();

        // Output legend image to a file
        File legendFile = new File("height_map_legend.png");
        ImageIO.write(legendImage, "png", legendFile);
        System.out.println("Legend image created: " + legendFile.getAbsolutePath());
    }

    public static BufferedImage createLegend() {
        BufferedImage image = new BufferedImage(LEGEND_WIDTH, LEGEND_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Define legend colors and labels
        Color[] colors = {Color.blue, Color.green, Color.yellow, Color.orange, Color.red};
        String[] labels = {"Low", "Mid-Low", "Medium", "Mid-High", "High"};

        // Clear background
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, LEGEND_WIDTH, LEGEND_HEIGHT);

        // Draw legend title
        g2d.setColor(Color.black);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Height Map Legend", 20, 30);

        // Draw color bands and labels
        int bandHeight = LEGEND_HEIGHT / colors.length;
        for (int i = 0; i < colors.length; i++) {
            g2d.setColor(colors[i]);
            g2d.fillRect(50, 50 + i * bandHeight, 50, bandHeight);
            g2d.setColor(Color.black);
            g2d.drawString(labels[i], 110, 50 + i * bandHeight + bandHeight / 2);
        }

        g2d.dispose();

        return image;
    }
}