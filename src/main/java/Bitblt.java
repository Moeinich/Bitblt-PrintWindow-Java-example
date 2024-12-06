import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import utils.CachedWindowCapture;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bitblt {
    private final Map<WinDef.HWND, CachedWindowCapture> cache = new ConcurrentHashMap<>();

    public BufferedImage captureWindow(WinDef.HWND hWnd) {
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
        GDI32.INSTANCE.BitBlt(cachedCapture.hdcMemDC, 0, 0, width, height, cachedCapture.hdcWindow, 0, 0, 0x00CC0020); // SRCCOPY

        // Retrieve bitmap data
        GDI32.INSTANCE.GetDIBits(cachedCapture.hdcWindow, cachedCapture.hBitmap, 0, height,
                cachedCapture.buffer, cachedCapture.bmi, WinGDI.DIB_RGB_COLORS);

        // Create and return BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, cachedCapture.buffer.getIntArray(0, width * height), 0, width);

        return image;
    }
}
