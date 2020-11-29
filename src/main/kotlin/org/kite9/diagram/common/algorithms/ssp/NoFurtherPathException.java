package org.kite9.diagram.common.algorithms.ssp;

public class NoFurtherPathException extends Exception {

	private static final long serialVersionUID = 1676726302630889939L;

	public NoFurtherPathException() {
		super();
	}

	public NoFurtherPathException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoFurtherPathException(String message) {
		super(message);
	}

	public NoFurtherPathException(Throwable cause) {
		super(cause);
	}

}
