import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import utils.CachedWindowCapture;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrintWindow {
    private final Map<WinDef.HWND, CachedWindowCapture> cache = new ConcurrentHashMap<>();

    public BufferedImage captureWindowUsingPrintWindow(WinDef.HWND hWnd) {
        WinDef.RECT bounds = new WinDef.RECT();
        User32.INSTANCE.GetClientRect(hWnd, bounds);

        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;

        CachedWindowCapture cachedCapture = cache.get(hWnd);
        if (cachedCapture == null) {
            cachedCapture = new CachedWindowCapture(width, height, hWnd);
            cache.put(hWnd, cachedCapture);
        }

        // Use cached HDC and HBITMAP
        boolean success = User32.INSTANCE.PrintWindow(hWnd, cachedCapture.hdcMemDC, 0x00000002); //PW_RENDERFULLCONTENT
        if (!success) {
            System.err.println("PrintWindow failed.");
            return null;
        }

        // Retrieve bitmap data
        GDI32.INSTANCE.GetDIBits(cachedCapture.hdcWindow, cachedCapture.hBitmap, 0, height,
                cachedCapture.buffer, cachedCapture.bmi, WinGDI.DIB_RGB_COLORS);

        // Create an array to hold pixel data
        int[] pixels = cachedCapture.buffer.getIntArray(0, width * height);

        // Create a new array for adjusted pixels
        int[] adjustedPixels = new int[width * height]; // The width remains the same after adjustments

        // Fill the leftmost column with the specified color and shift the rows.
        int fillColor = 0xFF1B1817; // ARGB format for the color #1B1817

        for (int y = 0; y < height; y++) {
            // Fill the first pixel of the row
            adjustedPixels[y * width] = fillColor;

            // Copy pixels from the original row, skipping the last column
            System.arraycopy(pixels, y * width, adjustedPixels, y * width + 1, width - 1);
        }

        // Create the final BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return image;
    }
}
