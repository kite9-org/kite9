package org.kite9.diagram;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.kite9.diagram.common.HelpMethods;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;

public class AbstractTest extends HelpMethods {

    @BeforeAll
    public static void setLoggingFactory() {
        Kite9Log.Companion.setFactory(l -> new Kite9LogImpl(l));
    }

    static boolean firstRun = true;

    @BeforeEach
    public void setLogging() {
        if ("off".equals(System.getProperty("kite9.logging"))) {
            Kite9LogImpl.setLogging(Kite9Log.Destination.OFF);
        } else {
            if (firstRun) {
                Kite9LogImpl.setLogging(Kite9Log.Destination.STREAM);
                firstRun = false;
            } else {
                // if we are running more than one test, then there's no point in logging.
                Kite9LogImpl.setLogging(Kite9Log.Destination.OFF);
            }
        }
    }
}
