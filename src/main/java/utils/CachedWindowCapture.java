package utils;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

public class CachedWindowCapture {
    public final int width, height;
    public final WinDef.HDC hdcWindow, hdcMemDC;
    public final WinDef.HBITMAP hBitmap;
    public final Memory buffer;
    public final WinGDI.BITMAPINFO bmi;

    public CachedWindowCapture(int width, int height, WinDef.HWND hWnd) {
        this.width = width;
        this.height = height;

        hdcWindow = User32.INSTANCE.GetDC(hWnd);
        hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);
        hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);
        GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);

        buffer = new Memory((long) width * height * 4);

        bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height; // Top-down bitmap
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;
    }

    void cleanup() {
        GDI32.INSTANCE.DeleteObject(hBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);
        User32.INSTANCE.ReleaseDC(null, hdcWindow);
    }
}