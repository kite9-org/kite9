package org.kite9.diagram.adl;

import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.model.position.Direction;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class BasicSocket extends AbstractMutableXMLElement {

	private static final long serialVersionUID = 3578883565482903409L;

	@Override
	public String toString() {
		return "Port: "+getID();
	}

	public BasicSocket(String id, Document doc, Direction arrivalSide, String pos) {
		super(id+"-port", "socket", doc);
		setAttribute("style", CSSConstants.DIRECTION+": "+arrivalSide+"; "+CSSConstants.PORT_POSITION+": "+pos+";");
	}

	public BasicSocket(String id, Direction arrivalSide, String pos) {
		this(id, TESTING_DOCUMENT,arrivalSide, pos);
	}

	@Override
	protected Node newNode() {
		return new BasicSocket(createID(), (Document) ownerDocument, Direction.UP, "50%");
	}

	public String getXMLId() {
		return getAttribute("id");
	}
}
