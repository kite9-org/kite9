package org.kite9.diagram;

import org.junit.Before;
import org.junit.BeforeClass;
import org.kite9.diagram.common.HelpMethods;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;

public class AbstractTest extends HelpMethods {

    @BeforeClass
    public static void setLoggingFactory() {
        Kite9Log.Companion.setFactory(l -> new Kite9LogImpl(l));
    }


    static boolean firstRun = true;

    @Before
    public void setLogging() {
//		Kite9LogImpl.setLogging(Kite9Log.Destination.STREAM);
        if ("off".equals(System.getProperty("kite9.logging"))) {
            Kite9LogImpl.setLogging(Kite9Log.Destination.OFF);
        } else {
            // if we are running more than one test, then there's no point in logging.
            if (firstRun) {
                firstRun = false;
            } else {
                Kite9LogImpl.setLogging(Kite9Log.Destination.OFF);
            }
        }
    }
}
