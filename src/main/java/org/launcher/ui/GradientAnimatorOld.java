package org.launcher.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.nio.IntBuffer;

public class GradientAnimatorOld extends ImageView {
    private final int TOP_COLOR;
    private final int BOTTOM_COLOR;
    private static final int GRADIENT_SIZE = 40;

    private final int width;
    private final int height;

    private final int[] pixels;
    private final double[] borders;
    private final int[] gradientColors;

    private double time;

    public GradientAnimatorOld(int width, int height, int TOP_COLOR, int BOTTOM_COLOR) {
        this.width = width;
        this.height = height;
        this.TOP_COLOR = TOP_COLOR;
        this.BOTTOM_COLOR = BOTTOM_COLOR;
        pixels = new int[width * height];
        borders = new double[width];
        gradientColors = createGradient(
        );

        PixelBuffer<IntBuffer> pixelBuffer =
                new PixelBuffer<>(
                        width,
                        height,
                        IntBuffer.wrap(pixels),
                        PixelFormat.getIntArgbPreInstance()
                );

        WritableImage image = new WritableImage(pixelBuffer);
        setImage(image);

        AnimationTimer timer = new AnimationTimer() {
            private long last = -1;

            @Override
            public void handle(long now) {
                if (last != -1) {
                    time += (now - last) / 1_000_000_000.0;
                }
                last = now;

                calculateBorders();
                render();

                pixelBuffer.updateBuffer(b -> null);
            }
        };

        timer.start();
    }

    private void calculateBorders() {
        double center = height * 0.5;

        for (int x = 0; x < width; x++) {
            double a1 = 100 + 20 * Math.sin(time * 0.1);
            double a2 = 50 + 10 * Math.sin(time * 0.17);
            double a3 = 25 + 5 * Math.sin(time * 0.23);

            borders[x] =
                    center
                            + a1 * Math.sin(x * 0.002 + time * 0.15)
                            + a2 * Math.sin(x * 0.004 + time * 0.25)
                            + a3 * Math.sin(x * 0.008 + time * 0.4);
        }
    }

    private void render() {
        int index = 0;

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++, index++) {

                int border = (int) borders[x];

                int gradientStart =
                        border - GRADIENT_SIZE / 2;

                int gradientEnd =
                        gradientStart + GRADIENT_SIZE;

                if (y < gradientStart) {
                    pixels[index] = TOP_COLOR;
                }
                else if (y >= gradientEnd) {
                    pixels[index] = BOTTOM_COLOR;
                }
                else {
                    pixels[index] =
                            gradientColors[y - gradientStart];
                }
            }
        }
    }

    private int[] createGradient() {
        int[] gradient = new int[GradientAnimatorOld.GRADIENT_SIZE];

        int a1 = (TOP_COLOR >>> 24) & 0xFF;
        int r1 = (TOP_COLOR >>> 16) & 0xFF;
        int g1 = (TOP_COLOR >>> 8) & 0xFF;
        int b1 = TOP_COLOR & 0xFF;

        int a2 = (BOTTOM_COLOR >>> 24) & 0xFF;
        int r2 = (BOTTOM_COLOR >>> 16) & 0xFF;
        int g2 = (BOTTOM_COLOR >>> 8) & 0xFF;
        int b2 = BOTTOM_COLOR & 0xFF;

        for (int i = 0; i < GradientAnimatorOld.GRADIENT_SIZE; i++) {
            double t = (double) i / (GradientAnimatorOld.GRADIENT_SIZE - 1);

            int a = (int) (a1 + (a2 - a1) * t);
            int r = (int) (r1 + (r2 - r1) * t);
            int g = (int) (g1 + (g2 - g1) * t);
            int b = (int) (b1 + (b2 - b1) * t);

            gradient[i] =
                    (a << 24)
                            | (r << 16)
                            | (g << 8)
                            | b;
        }

        return gradient;
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000
                | (r << 16)
                | (g << 8)
                | b;
    }
}
