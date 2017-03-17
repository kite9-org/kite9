package org.kite9.framework.common;

/**
 * Thrown whenever there is a problem creating the kite9 item.
 * 
 * @author moffatr
 *
 */
public class Kite9ProcessingException extends RuntimeException {

    private static final long serialVersionUID = -1338453592786354494L;

    public Kite9ProcessingException() {
	super();
    }

    public Kite9ProcessingException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }

    public Kite9ProcessingException(String arg0) {
	super(arg0);
    }

    public Kite9ProcessingException(Throwable arg0) {
	super(arg0);
    }

}
