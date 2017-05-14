package org.kite9.diagram.visualization.orthogonalization;

import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Contains some utility methods that can be used by Orthogonalizers.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractOrthogonalizer implements Orthogonalizer, Logable {
	
	protected Kite9Log log = new Kite9Log(this);

	public String getPrefix() {
		return "ORTH";
	}

	public boolean isLoggingEnabled() {
		return false;
	}
}
