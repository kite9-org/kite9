package org.kite9.framework.common;

/**
 * Thrown whenever there is a problem creating the kite9 item.
 * This is to do with input data not matching expectations, and 
 * should lead the user back to the understanding of what the issue is.
 * 
 * @author moffatr
 *
 */
public class Kite9ProcessingException extends RuntimeException {

    /**
     * These prevent us from creating traces with multiple {@link Kite9ProcessingException}s in them.
     */
    private static String correctMessage(String arg0, Throwable arg1) {
		return arg1 instanceof Kite9ProcessingException ? arg0 + "\n"+arg1.getMessage() : arg0;
	}

	protected static Throwable correctThrowable(Throwable arg1) {
		return arg1 instanceof Kite9ProcessingException ? arg1.getCause() : arg1;
	}
    
    public Kite9ProcessingException(String arg0, Throwable arg1) {
    	super(correctMessage(arg0, arg1), correctThrowable(arg1));
    }

    public Kite9ProcessingException(String arg0) {
    	super(arg0);
    }

    public Kite9ProcessingException(Throwable arg0) {
    	super(correctThrowable(arg0));
    }
    
}
