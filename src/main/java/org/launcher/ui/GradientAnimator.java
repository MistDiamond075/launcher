package org.launcher.ui;

import javafx.animation.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.nio.IntBuffer;
import java.util.Arrays;

public class GradientAnimator extends ImageView {
    private final int TOP_COLOR;
    private final int BOTTOM_COLOR;
    private static final int POINT_COUNT = 6;

    private final double[] pointY = new double[POINT_COUNT];

    private final int width;
    private final int height;

    private final int[] pixels;
    private final double[] borders;
    private final double[] velocity = new double[POINT_COUNT];
    private final double[] acceleration = new double[POINT_COUNT];

    private double time;

    public GradientAnimator(int width, int height, int TOP_COLOR, int BOTTOM_COLOR) {
        this.width = width;
        this.height = height;
        this.TOP_COLOR = TOP_COLOR;
        this.BOTTOM_COLOR = BOTTOM_COLOR;
        pixels = new int[width * height];
        borders = new double[width];
        double center = height * 0.5;

        for (int i = 0; i < POINT_COUNT; i++) {
            pointY[i] = center;
            velocity[i] = 0;
            acceleration[i] = 0;
        }
        Arrays.fill(pointY, center);

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

        for (int i = 0; i < POINT_COUNT; i++) {

            acceleration[i] += (Math.random() - 0.5) * 0.003;
            acceleration[i] = clamp(acceleration[i], -0.05, 0.05);

            velocity[i] += acceleration[i];
            velocity[i] *= 0.985;

            pointY[i] += velocity[i];
            if (pointY[i] < center - 150) {
                pointY[i] = center - 150;
                velocity[i] *= -0.5;
            }

            if (pointY[i] > center + 150) {
                pointY[i] = center + 150;
                velocity[i] *= -0.5;
            }
        }

        double step = (double)(width - 1) / (POINT_COUNT - 1);

        for (int x = 0; x < width; x++) {
            double gx = x / step;
            int i = (int)gx;
            double t = gx - i;

            int i0 = Math.max(0, i - 1);
            int i1 = Math.min(POINT_COUNT - 1, i);
            int i2 = Math.min(POINT_COUNT - 1, i + 1);
            int i3 = Math.min(POINT_COUNT - 1, i + 2);

            borders[x] = catmullRom(
                    pointY[i0],
                    pointY[i1],
                    pointY[i2],
                    pointY[i3],
                    t
            );
        }
    }

    private static double clamp(double x, double min, double max) {
        return Math.max(min, Math.min(max, x));
    }

    private static double catmullRom(
            double p0,
            double p1,
            double p2,
            double p3,
            double t) {

        double t2 = t * t;
        double t3 = t2 * t;

        return 0.5 * (
                2 * p1
                        + (-p0 + p2) * t
                        + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2
                        + (-p0 + 3 * p1 - 3 * p2 + p3) * t3
        );
    }

    private void render() {
        double center = height * 0.5;

        int index = 0;

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++, index++) {

                double offset = borders[x] - center;

                double yy = y - offset;

                double t = yy / (height - 1);

                t = Math.max(0.0, Math.min(1.0, t));

                pixels[index] = lerp(TOP_COLOR, BOTTOM_COLOR, t);
            }
        }
    }

    private static int lerp(int c1, int c2, double t) {

        int a1 = (c1 >>> 24) & 255;
        int r1 = (c1 >>> 16) & 255;
        int g1 = (c1 >>> 8) & 255;
        int b1 = c1 & 255;

        int a2 = (c2 >>> 24) & 255;
        int r2 = (c2 >>> 16) & 255;
        int g2 = (c2 >>> 8) & 255;
        int b2 = c2 & 255;

        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return (a << 24)
                | (r << 16)
                | (g << 8)
                | b;
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000
                | (r << 16)
                | (g << 8)
                | b;
    }
}
