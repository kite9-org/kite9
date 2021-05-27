package com.kite9.k9server.adl.holder.pipeline;

import com.kite9.k9server.adl.holder.meta.MetaRead;
import org.kite9.diagram.logging.Kite9ProcessingException;

import com.kite9.k9server.adl.format.media.DiagramFileFormat;
import com.kite9.k9server.adl.format.media.DiagramFormat;

/**
 * Third point in the pipeline: ADL is now converted to it's output format.
 * 
 * @author robmoffat
 *
 */
public interface ADLOutput<X extends DiagramFormat> extends XMLBase, MetaRead {
	
	/**
	 * Returns the format used to create this output.
	 */
	public X getFormat();
	
	byte[] getAsBytes();
	
	/**
	 * Return as UTF-8 string, unless this is a binary format {@link DiagramFileFormat}.isBinaryFormat();
	 */
	String getAsString() throws Kite9ProcessingException;
	
	/**
	 * Creator of this.
	 */
	public ADLDom originatingADLDom();
}
