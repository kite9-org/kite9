package org.kite9.diagram.dom.processors.xpath;

import org.kite9.diagram.dom.processors.XMLProcessor;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class AbstractProcessor implements XMLProcessor {

	public AbstractProcessor() {
		super();
	}
	
	protected abstract boolean isAppending();

	public final Node processContents(Node n, Node inside) {
		//System.out.println("Process Contents : "+this.getClass()+ "    "+n.getLocalName()+"  inside "+inside);
		if (n instanceof Element) {
			Element out = processTag((Element) n);
			checkDoAppend(inside, out);
			
			NodeList contents = n.getChildNodes();
			mergeTextNodes(contents);
			processContents(contents, out);
			return out;
		} else if (n instanceof Text) {
			Text out = processText((Text) n);
			checkDoAppend(inside, out);
			return out;
		} else if (n instanceof Document){
			NodeList contents = n.getChildNodes();
			processContents(contents, inside);
			return inside;
		} else if (n instanceof Comment) {
			Comment out = processComment((Comment) n);
			checkDoAppend(inside, out);
			return out;
		} else {
			throw new UnsupportedOperationException("Don't know how to handle "+n);
		}
	}

	private void checkDoAppend(Node inside, Node out) {
		if (isAppending() && (inside!=null) && (out != null)) {
			//System.out.println("Appending "+n);
			inside.appendChild(out);
		}
	}
	
	protected abstract Element processTag(Element n);

	protected abstract Text processText(Text n);
	
	protected abstract Comment processComment(Comment c);

	protected void processContents(NodeList contents, Node inside) {
		for (int i = 0; i < contents.getLength(); i++) {
			Node n = contents.item(i);
			processContents(n, inside);
		}
	}
	
	protected void mergeTextNodes(NodeList nodeList) {
		Text lastTextNode = null;
		int i = 0;
		while (i < nodeList.getLength()) {
			Node n = nodeList.item(i);
			if (n instanceof Text) {
				if (lastTextNode != null) {
					lastTextNode.setData(lastTextNode.getData() + ((Text)n).getData());
					n.getParentNode().removeChild(n);
				} else {
					i++;
				}
			} else {
				lastTextNode = null;
				i++;
			}
		}
	}

}