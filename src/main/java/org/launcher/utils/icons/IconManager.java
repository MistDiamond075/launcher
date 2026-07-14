package org.launcher.utils.icons;

import jnr.ffi.*;
import jnr.ffi.Runtime;
import org.launcher.utils.jnr.lib.Gdi32;
import org.launcher.utils.jnr.lib.Kernel32;
import org.launcher.utils.jnr.lib.Shell32;
import org.launcher.utils.jnr.lib.User32;
import org.launcher.utils.jnr.struct.BITMAP;
import org.launcher.utils.jnr.struct.ICONINFO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class IconManager {
    private static Pointer extractHicon(String path){
        jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getSystemRuntime();

        Pointer icons = Memory.allocateDirect(runtime, Long.BYTES);
        icons.putAddress(0, 0);

        int result = User32.INSTANCE.PrivateExtractIconsW(
                path,
                0,      // первая иконка
                128,    // желаемая ширина
                128,    // желаемая высота
                icons,
                null,
                1,
                0
        );
//        System.out.println("path=" + path +", extractIconW=" + Shell32.INSTANCE.ExtractIconW(null,path,0)+ ", err=" + Kernel32.INSTANCE.GetLastError());
//        System.out.println("path=" + path +", extractIconExW=" + result+ ", err=" + Kernel32.INSTANCE.GetLastError());
//        System.out.println("large address="+icons.address());
//        System.out.println("large address from 0="+Long.toHexString(large.getAddress(0)));
        if (result == 1) {
            return  icons.getPointer(0);
        }
        return null;
    }

    public static String make(String path){
        Pointer hIcon = extractHicon(path);
        if(hIcon == null){
            return null;
        }
        try {
            Dimension size = getIconSize(hIcon);
            System.out.println(size.width + " x " + size.height);
            DibSection dib = DibSection.createDibSection(size.width, size.height);
            System.out.println(dib.width + " x " + dib.height);
            try {
                drawIcon(dib, hIcon);
                BufferedImage image = convertToBufferedImage(dib);

                String ico_path = UUID.randomUUID() + ".png";
                ImageIO.write(image, "png", new File(ico_path));
                return ico_path;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                dispose(dib);
            }
        } finally {
            User32.INSTANCE.DestroyIcon(hIcon);
        }
    }

    private static void drawIcon(DibSection dib, Pointer hIcon) {
        boolean success = User32.INSTANCE.DrawIconEx(
                dib.hdc,
                0,
                0,
                hIcon,
                dib.width,
                dib.height,
                0,
                null,
                User32.DI_NORMAL
        );
        System.out.println("DrawIconEx=" + success+ ", err=" + Kernel32.INSTANCE.GetLastError());
        if (!success) {
            throw new IllegalStateException(
                    "DrawIconEx failed. Error = " + Kernel32.INSTANCE.GetLastError()
            );
        }
    }

    private static BufferedImage convertToBufferedImage(DibSection dib) {
        BufferedImage image = new BufferedImage(
                dib.width,
                dib.height,
                BufferedImage.TYPE_INT_ARGB
        );

        int[] dst = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        //int[] src = dib.bits.getIntArray(0, dib.width * dib.height);

        for (int i = 0; i < dst.length; i++) {
            int bgra = dib.bits.getInt(i * 4L);

            int b =  bgra        & 0xFF;
            int g = (bgra >> 8)  & 0xFF;
            int r = (bgra >> 16) & 0xFF;
            int a = (bgra >> 24) & 0xFF;

            dst[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        return image;
    }

    private static void dispose(DibSection dib) {
        if (dib == null) {
            return;
        }
        Gdi32.INSTANCE.SelectObject(dib.hdc, dib.oldBitmap);
        Gdi32.INSTANCE.DeleteObject(dib.hBitmap);
        Gdi32.INSTANCE.DeleteDC(dib.hdc);
    }

    private static Dimension getIconSize(Pointer hIcon) {
        ICONINFO iconInfo = new ICONINFO(Runtime.getSystemRuntime());
        System.out.println("GetIconInfo=" + User32.INSTANCE.GetIconInfo(hIcon, iconInfo) + ", err=" + Kernel32.INSTANCE.GetLastError());
        if (!User32.INSTANCE.GetIconInfo(hIcon, iconInfo)) {
            throw new IllegalStateException(
                    "GetIconInfo failed. Error = " + Kernel32.INSTANCE.GetLastError()
            );
        }

        try {
            BITMAP bitmap = new BITMAP(Runtime.getSystemRuntime());
            int size = Struct.size(bitmap);
            Pointer mem = Memory.allocateDirect(
                    Runtime.getSystemRuntime(),
                    size
            );
            Pointer hBitmap = iconInfo.hbmColor.get() != null
                    ? iconInfo.hbmColor.get()
                    : iconInfo.hbmMask.get();
            int result = Gdi32.INSTANCE.GetObjectW(
                    hBitmap,
                    size,
                    mem
            );
            System.out.println("result=" + result);
            System.out.println("First 32 bits:");
            for (int i = 0; i < 32; i += 4) {
                System.out.printf("%08X%n", mem.getInt(i));
            }
            System.out.println();
            System.out.println("bmWidth="+mem.getInt(4));
            System.out.println("bmHeight="+mem.getInt(8));
            System.out.println("bitmap size="+Struct.size(bitmap));
            System.out.println("iconinfo size="+Struct.size(iconInfo));
            System.out.println("bmWidth=" + bitmap.bmWidth.get());
            System.out.println("bmHeight=" + bitmap.bmHeight.get());
            System.out.println("bmBitsPixel=" + bitmap.bmBitsPixel.get());
            System.out.println("GetObjectW=" + result+ ", err=" + Kernel32.INSTANCE.GetLastError());
            if (result == 0) {
                throw new IllegalStateException(
                        "GetObject failed. Error = " + Kernel32.INSTANCE.GetLastError()
                );
            }


            int width = mem.getInt(4);
            int height = mem.getInt(8);
            if (iconInfo.hbmColor.get() == null) {
                height /= 2;
            }

            return new Dimension(width, height);
        } finally {
            if (iconInfo.hbmColor.get() != null) {
                Gdi32.INSTANCE.DeleteObject(iconInfo.hbmColor.get());
            }

            if (iconInfo.hbmMask.get() != null) {
                Gdi32.INSTANCE.DeleteObject(iconInfo.hbmMask.get());
            }
        }
    }
}
