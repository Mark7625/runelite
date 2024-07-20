package net.runelite.cache;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HeightMapLegend {

    private static final int LEGEND_WIDTH = 400; // Adjust as needed
    private static final int LEGEND_HEIGHT = 150; // Adjust as needed
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

        // Define the number of gradient steps
        int numSteps = 100; // Number of gradient steps for smooth transition
        Color[] gradientColors = new Color[numSteps];

        // Generate gradient colors from blue to green to brown
        for (int i = 0; i < numSteps; i++) {
            float normalizedHeight = (float) i / (numSteps - 1); // Normalize to [0, 1]
            gradientColors[i] = getColorFromHeight(normalizedHeight);
        }

        // Clear background
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, LEGEND_WIDTH, LEGEND_HEIGHT);

        // Draw color bands with outlines
        int bandWidth = LEGEND_WIDTH / numSteps;
        for (int i = 0; i < numSteps; i++) {
            // Calculate box position
            int x = i * bandWidth;
            int y = 20;
            int width = bandWidth;
            int height = LEGEND_HEIGHT - 40; // Adjust to leave space for labels

            // Draw color box
            g2d.setColor(gradientColors[i]);
            g2d.fillRect(x, y, width, height);

            // Draw black outline
            g2d.setColor(Color.black);
            g2d.drawRect(x, y, width, height);
        }

        // Draw legend title
        g2d.setColor(Color.black);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Height Map Legend", 20, 15);

        // Draw horizontal labels at the bottom
        String[] labels = {"Low", "Mid-Low", "Medium", "Mid-High", "High"};
        int numLabels = labels.length;
        int labelSpacing = (LEGEND_WIDTH - 100) / (numLabels - 1); // Space out labels evenly horizontally

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics metrics = g2d.getFontMetrics();
        for (int i = 0; i < numLabels; i++) {
            String label = labels[i];
            // Calculate label position
            int x = 50 + i * labelSpacing - metrics.stringWidth(label) / 2;
            int y = LEGEND_HEIGHT - 5; // Position labels near the bottom

            // Draw label
            g2d.setColor(Color.black);
            g2d.drawString(label, x, y);
        }

        g2d.dispose();

        return image;
    }



    // Map normalized height (0 to 1) to the color
    private static Color getColorFromHeight(float normalizedHeight) {
        if (normalizedHeight <= 0.5) {
            // Interpolate between blue and green
            float ratio = normalizedHeight * 2; // Scale from 0 to 1
            return interpolate(Color.BLUE, Color.GREEN, ratio);
        } else {
            // Interpolate between green and brown
            float ratio = (normalizedHeight - 0.5f) * 2; // Scale from 0 to 1
            return interpolate(Color.GREEN, new Color(139, 69, 19), ratio);
        }
    }


    private static final int BOX_WIDTH = 50; // Width of each color box
    private static final int BOX_MARGIN = 10; // Margin between color boxes and text

    // Interpolation method
    private static Color interpolate(Color c1, Color c2, float ratio) {
        int red = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int green = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int blue = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);

        return new Color(red, green, blue);
    }
}