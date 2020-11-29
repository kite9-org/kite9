package org.kite9.diagram.common.algorithms.ssp;

/**
 * This is caused when there is an out of memory error, or a limit on the size of the ssp
 * state gets too big.
 * 
 * @author robmoffat
 *
 */
public class SSPTooLargeException extends RuntimeException {

	private static final long serialVersionUID = 1676726302630889939L;

	public SSPTooLargeException() {
		super();
	}

	public SSPTooLargeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SSPTooLargeException(String message) {
		super(message);
	}

	public SSPTooLargeException(Throwable cause) {
		super(cause);
	}

}
