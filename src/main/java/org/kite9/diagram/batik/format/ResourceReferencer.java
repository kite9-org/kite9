package org.kite9.diagram.batik.format;

import java.io.InputStream;

import org.apache.batik.util.ParsedURL;

/**
 * Takes resource references (e.g. fonts, files, images, whatever) in the input 
 * document and produces a new output URL for them.  Different implementations 
 * will be needed dependent on context.
 * 
 * @author robmoffat
 *
 */
public interface ResourceReferencer {
	
	static interface Reference {
		
		String getUrl();
		
	}
	
	public Reference getReference(ParsedURL purl);

}
