package com.kite9.k9server.command.xml.move;

import java.util.Collections;
import java.util.List;

import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.managers.IntegerRangeValue;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.CommandException;
import com.kite9.k9server.command.xml.AbstractADLCommand;

/**
 * Makes some space in a grid for new cells to be dropped in.
 * 
 * @author robmoffat
 *
 */
public class ADLMoveCells extends AbstractADLCommand {

	public Integer push, from;
	public Boolean horiz;
	public String fragmentId;
	public List<String> excludedIds = Collections.emptyList();
	
	
	public void doMove(ADLDom adl, int push) {
		checkProperties();
		
		ADLDocument doc = adl.getDocument();
		adl.ensureCssEngine(doc);
		Element container = findFragmentElement(doc, fragmentId);	
		int moved = 0;
		
		NodeList contents = container.getChildNodes();
		for (int i = 0; i < contents.getLength(); i++) {
			if (contents.item(i) instanceof StyledKite9XMLElement) {
				StyledKite9XMLElement el = (StyledKite9XMLElement) contents.item(i);
				
				String id = el.getAttribute("id");
				if (!excludedIds.contains(id)) {
					IntegerRangeValue yr = (IntegerRangeValue) el.getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY);
					IntegerRangeValue xr = (IntegerRangeValue) el.getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY);
					CSSStyleDeclaration sd = el.getStyle();
					
					if ((xr != null) && (yr != null)) {
						if (horiz) {
							if (xr.getFrom() >= from) {
								xr = new IntegerRangeValue(xr.getFrom() + push, xr.getTo() + push);
								moved++;
							} else if (xr.getTo() >= from) {
								xr = new IntegerRangeValue(xr.getFrom(), xr.getTo() + push);
								moved++;
							}
							
							sd.setProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY, xr.getCssText(), "");
							el.setComputedStyleMap("", null);  // clears cache
						} else {
							if (yr.getFrom() >= from) {
								yr = new IntegerRangeValue(yr.getFrom() + push, yr.getTo() + push);
								moved++;
							} else if (yr.getTo() >= from) {
								yr = new IntegerRangeValue(yr.getFrom(), yr.getTo() + push);
								moved++;
							}
							
							sd.setProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY, yr.getCssText(), "");
							el.setComputedStyleMap("", null);  // clears cache
						}
					}
				}
			}
		}
		LOG.info("Processed move from "+from+" push "+push+" horiz="+horiz+",moved="+moved);
	}

	protected void checkProperties() {
		ensureNotNull("fragmentId", fragmentId);
		ensureNotNull("from", from);
		ensureNotNull("push", push);
	}

	@Override
	public Mismatch applyCommand(ADLDom in) throws CommandException {
		doMove(in, push);
		return null;
	}

	@Override
	public Mismatch undoCommand(ADLDom in) throws CommandException {
		doMove(in, -push);
		return null;
	}
	
}
