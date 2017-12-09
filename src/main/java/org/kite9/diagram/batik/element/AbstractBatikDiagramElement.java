package org.kite9.diagram.batik.element;

import java.util.HashMap;
import java.util.Map;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.batik.HasSVGGraphics;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Painter;
import org.kite9.diagram.batik.templater.ValueReplacingProcessor;
import org.kite9.diagram.batik.templater.ValueReplacingProcessor.ValueReplacer;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.xml.AbstractStyleableXMLElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents {@link DiagramElement}s that contain SVG that will need rendering, and the method to render them
 * (delegating to a Painter implementation).
 *  
 * @author robmoffat
 *
 */
public abstract class AbstractBatikDiagramElement extends AbstractDOMDiagramElement implements HasSVGGraphics {
	
	public AbstractBatikDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter<?> p) {
		super(el, parent, ctx);
		this.p = p;
	}

	protected Painter<?> p;
	protected double padding[] = new double[4];
	protected double margin[] = new double[4];
	
	protected void initialize() {
		initializeDirectionalCssValues(theElement, padding, CSSConstants.KITE9_CSS_PADDING_PROPERTY_PREFIX);
		initializeDirectionalCssValues(theElement, margin, CSSConstants.KITE9_CSS_MARGIN_PROPERTY_PREFIX);
	}
	
	public double getMargin(Direction d) {
		ensureInitialized();
		return margin[d.ordinal()];
	}

	public double getPadding(Direction d) {
		ensureInitialized();
		return padding[d.ordinal()];
	}

	protected static void initializeDirectionalCssValues(CSSStylableElement el, double[] vals, String prefix) {
		vals[Direction.UP.ordinal()] = getCssDoubleValue(el, prefix+CSSConstants.TOP);
		vals[Direction.DOWN.ordinal()] = getCssDoubleValue(el, prefix+CSSConstants.BOTTOM);
		vals[Direction.LEFT.ordinal()] = getCssDoubleValue(el, prefix+CSSConstants.LEFT);
		vals[Direction.RIGHT.ordinal()] = getCssDoubleValue(el, prefix+CSSConstants.RIGHT);	
	}

	private static double getCssDoubleValue(CSSStylableElement el,String prop) {
		Value v = AbstractStyleableXMLElement.getCSSStyleProperty(el, prop);
		return v.getFloatValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Element output(Document d) {
		ensureInitialized();
		preProcess(theElement);
		Element out = ((Painter<DiagramElement>)p).output(d, theElement, this);
		postProcess(out);
		return out;
	}


	/**
	 * Perform pre-processing, such as value replacements.
	 */
	protected void preProcess(StyledKite9SVGElement theElement) {
		preProcessPlaceholders(theElement, getReplacementMap(theElement));
	}

	
	protected Map<String, String> getReplacementMap(@SuppressWarnings("unused") StyledKite9SVGElement theElement) {
		return new HashMap<>();
	}

	/**
	 * Performs any necessary post-processing, such as translation.
	 */
	protected abstract void postProcess(Element out);
	
	/**
	 * This is likely to be temporary, and can only be used in containers and decals since 
	 * leaf elements need to know what their XML looks like in order to size themselves.
	 * Use this in preProcess.
	 */
	protected void preProcessPlaceholders(Element child, Map<String, String> replacements) {
		ValueReplacer valueReplacer = new ValueReplacer() {
			
			@Override
			public String getReplacementValue(String in) {
				if (replacements.containsKey(in)) {
					return replacements.get(in);
				} else {
					return in;
				}
			}
		};
		
		new ValueReplacingProcessor(valueReplacer).processContents(child);
	}
	
	protected CostedDimension getSizeBasedOnPadding() {
		double left = getPadding(Direction.LEFT);
		double right = getPadding(Direction.RIGHT);
		double up = getPadding(Direction.UP);
		double down = getPadding(Direction.DOWN);
		return new CostedDimension(left + right, up + down, CostedDimension.UNBOUNDED);
	}

}
