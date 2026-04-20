package uk.co.baconi.keylogger.framework.factories;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import uk.co.baconi.keylogger.framework.impl.WindowsKeyLogger;
import uk.co.baconi.keylogger.framework.impl.x11.X11KeyLogger;
import uk.co.baconi.keylogger.framework.interfaces.KeyLogger;

public class KeyLoggerFactoryTest extends FactoryTestSuite<KeyLogger, WindowsKeyLogger, X11KeyLogger> {

    public KeyLoggerFactoryTest() {
        super(KeyLoggerFactory.INSTANCE, WindowsKeyLogger.class, X11KeyLogger.class);
    }

    @Test
    public void shouldBeAbleToGetInstance() {
        assertThat(KeyLoggerFactory.INSTANCE, is(equalTo(KeyLoggerFactory.getInstance())));
    }
}
