package org.kite9.framework.logging;

@SuppressWarnings("serial")
public class LogicException extends RuntimeException {

    public LogicException() {
	super();
    }

    public LogicException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }
    
    public LogicException(String arg0) {
	super(arg0);
    }

    public LogicException(Throwable arg0) {
	super(arg0);
    }

}
