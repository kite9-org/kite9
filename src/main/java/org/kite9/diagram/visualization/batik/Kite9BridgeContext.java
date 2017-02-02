package org.kite9.diagram.visualization.batik;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.script.InterpreterPool;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.Element;


public final class Kite9BridgeContext extends SVG12BridgeContext  {
//	private final ADLDocument ownerDocument;


	public Kite9BridgeContext(UserAgent userAgent, DocumentLoader loader) {
		super(userAgent, loader);
	}

	public Kite9BridgeContext(UserAgent userAgent, InterpreterPool interpreterPool, DocumentLoader documentLoader) {
		super(userAgent, interpreterPool, documentLoader);
	}

	public Kite9BridgeContext(UserAgent userAgent) {
		super(userAgent);
	}

	public boolean isInteractive() {
		return false;
	}

	public boolean isDynamic() {
		return false;
	}
	
	@Override
	public Bridge getBridge(Element element) {
		if (element instanceof XMLElement) {
			DiagramElement de = ((XMLElement)element).getDiagramElement();
			return new Kite9DiagramBridge();
			 
//			throw new Kite9ProcessingException();
			
		} else {
			return super.getBridge(element);
		}
	}
	
	
}