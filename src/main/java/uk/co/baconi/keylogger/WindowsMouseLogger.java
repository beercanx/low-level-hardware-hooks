package uk.co.baconi.keylogger;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinBase.SYSTEMTIME;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;
import com.sun.jna.platform.win32.WinUser.MOUSEINPUT;
import com.sun.jna.platform.win32.WinUser.MSG;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/* http://msdn.microsoft.com/en-us/library/ms644967(VS.85).aspx - KBDLLHOOKSTRUCT revealed */
/* http://msdn.microsoft.com/en-us/library/dd375731(VS.85).aspx - vkCodes Explained */
/* KBDLLHOOKSTRUCT.time is time in milliseconds from the start of the system */

public class WindowsMouseLogger {
    private static volatile boolean quit;
    private static HHOOK hhk;
    private static LowLevelMouseProc mouseHook;
    private static final String logFileName = "MouseHookLog.csv";

    private static final int WM_LBUTTONDBLCLK = 515; // Left Mouse Button Double Click
    private static final int WM_LBUTTONDOWN = 513; // Left Mouse Button Released
    private static final int WM_LBUTTONUP = 514; // Left Mouse Button Single Click
    private static final int WM_RBUTTONDBLCLK = 518; // Right Mouse Button Double Click
    private static final int WM_RBUTTONDOWN = 516; // Right Mouse Button Released
    private static final int WM_RBUTTONUP = 517; // Right Mouse Button Single Click
    private static final int WM_MBUTTONDBLCLK = 521; // Middle Mouse Button Double Click
    private static final int WM_MBUTTONDOWN = 519; // Middle Mouse Button Released
    private static final int WM_MBUTTONUP = 520; // Middle Mouse Button Single Click
    private static final int WM_NCLBUTTONDBLCLK = 163; // Left Mouse Button Double Click - Non-Client Area
    private static final int WM_NCLBUTTONDOWN = 161; // Left Mouse Button Released - Non-Client Area
    private static final int WM_NCLBUTTONUP = 162; // Left Mouse Button Single Click - Non-Client Area
    private static final int WM_NCMBUTTONDBLCLK = 169; // Middle Mouse Button Double Click - Non-Client Area
    private static final int WM_NCMBUTTONDOWN = 167; // Middle Mouse Button Released - Non-Client Area
    private static final int WM_NCMBUTTONUP = 168; // Middle Mouse Button Single Click - Non-Client Area
    private static final int WM_NCRBUTTONDBLCLK = 166; // Right Mouse Button Double Click - Non-Client Area
    private static final int WM_NCRBUTTONDOWN = 164; // Right Mouse Button Released - Non-Client Area
    private static final int WM_NCRBUTTONUP = 165; // Right Mouse Button Single Click - Non-Client Area
    private static final int WM_NCXBUTTONDBLCLK = 173; // First or Second X Mouse Button Double Click - Non-Client Area
    private static final int WM_NCXBUTTONDOWN = 171; // First or Second X Mouse Button Released - Non-Client Area
    private static final int WM_NCXBUTTONUP = 172; // First or Second X Mouse Button Single Click - Non-Client Area
    private static final int WM_XBUTTONDBLCLK = 525; // First or Second X Mouse Button Double Click
    private static final int WM_XBUTTONDOWN = 523; // First or Second X Mouse Button Released
    private static final int WM_XBUTTONUP = 524; // First or Second X Mouse Button Single Click
    private static final int WM_MOUSEHWHEEL = 526; // Horizontal Scroll Wheel Rotated
    private static final int WM_MOUSEWHEEL = 522; // Scroll Wheel Rotated

    public static void main(final String[] args) {
        if (!Platform.isWindows()) {
            throw new UnsupportedOperationException("Not supported on this platform.");
        }

        final User32 lib = User32.INSTANCE;
        final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);

        if (!(new File(logFileName).exists())) {
            appendToLogFile("wParam,mousePointX,mousePointY,mouseData,flags,time,dwExtraInfo,Year-Month-Day,Hour:Minute:Second:Millisecond");
        }

        mouseHook = (nCode, wParam, info) -> {
            if (nCode >= 0) {
                // switch (wParam.intValue()) {
                // case WM_MOUSEHWHEEL:
                // case WM_MOUSEWHEEL:
                // case WM_LBUTTONDOWN:
                // case WM_RBUTTONDOWN:
                // case WM_MBUTTONDOWN:
                // case WM_XBUTTONDOWN:
                // case WM_NCLBUTTONDOWN:
                // case WM_NCMBUTTONDOWN:
                // case WM_NCRBUTTONDOWN:
                // case WM_NCXBUTTONDOWN:
                // appendToLogFile(wParam.intValue() + "," + info.dx + "," + info.dy + "," + info.mouseData
                // + "," + info.dwFlags + "," + info.time + "," + info.dwExtraInfo + ","
                // + getSystemTime());
                // default:
                // break;
                // }
                appendToLogFile(wParam.intValue() + "," + info.dx + "," + info.dy + "," + info.mouseData + ","
                        + info.dwFlags + "," + info.time + "," + info.dwExtraInfo + "," + getSystemTime());
            }
            return lib.CallNextHookEx(hhk, nCode, wParam, new WinDef.LPARAM(Pointer.nativeValue(info.getPointer())));
        };

        hhk = lib.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseHook, hMod, 0);

        new Thread() {
            @Override
            public void run() {
                while (!quit) {
                    try {
                        Thread.sleep(10);
                    } catch (final Exception e) {
                    }
                }
                System.err.println("unhook and exit");
                lib.UnhookWindowsHookEx(hhk);
                System.exit(0);
            }
        }

                .start();

        // This bit never returns from GetMessage

        int result;
        final MSG msg = new MSG();

        while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
            if (result == -1) {
                System.err.println("error in get message");
                break;
            } else {
                System.err.println("got message");
                lib.TranslateMessage(msg);
                lib.DispatchMessage(msg);
            }
        }
        lib.UnhookWindowsHookEx(hhk);
    }

    private static String getSystemTime() {
        final SYSTEMTIME tTH = new SYSTEMTIME();
        Kernel32.INSTANCE.GetSystemTime(tTH);
        return "" + tTH.wYear + "-" + tTH.wMonth + "-" + tTH.wDay + "," + tTH.wHour + ":" + tTH.wMinute + ":"
                + tTH.wSecond + ":" + tTH.wMilliseconds;
    }

    private static void appendToLogFile(final String logEntry) {
        System.out.println(logEntry);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(logFileName, true));
            bw.write(logEntry);
            bw.newLine();
            bw.flush();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (final IOException ioe2) { /* Do Nothing */
                }
            }
        }
    }

    /*
     * ############################# New Mouse Hooking Code #############################
     */

    private interface LowLevelMouseProc extends HOOKPROC {
        LRESULT callback(int nCode, WPARAM wParam, MOUSEINPUT lParam);
    }

    // private interface LowLevelMouseProc extends HOOKPROC {
    // LRESULT callback(int nCode, WPARAM wParam, MSLLHOOKSTRUCT lParam);
    // }
    //
    // public class Point extends Structure {
    // public class ByReference extends Point implements Structure.ByReference {
    // };
    //
    // public NativeLong x;
    // public NativeLong y;
    //
    // @Override
    // protected List getFieldOrder() {
    // return FIELD_LIST;
    // }
    // }
    //
    // public static class MSLLHOOKSTRUCT extends Structure {
    // public static class ByReference extends MSLLHOOKSTRUCT implements Structure.ByReference {
    // };
    //
    // public POINT pt;
    // public int mouseData;
    // public int flags;
    // public int time;
    // public ULONG_PTR dwExtraInfo;
    //
    // @Override
    // protected List getFieldOrder() {
    // return FIELD_LIST;
    // }
    // }
    //
    // /**
    // * Not Needed!
    // */
    // public static class MOUSEHOOKSTRUCT extends Structure {
    // public static class ByReference extends MOUSEHOOKSTRUCT implements Structure.ByReference {
    // };
    //
    // public POINT pt;
    // public HWND hwnd;
    // public int wHitTestCode;
    // public ULONG_PTR dwExtraInfo;
    //
    // @Override
    // protected List getFieldOrder() {
    // return FIELD_LIST;
    // }
    // }
}
