package org.kite9.diagram.batik.model;

import java.util.Collections;
import java.util.Map;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.transform.SVGTransformer;
import org.kite9.diagram.batik.transform.TransformFactory;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.model.AbstractDOMDiagramElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.ContentTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents {@link DiagramElement}s that contain SVG that will need rendering, and the method to render them
 * (delegating to Painter and Transform implementations).
 *  
 * @author robmoffat
 *
 */
public abstract class AbstractBatikDiagramElement extends AbstractDOMDiagramElement {
	
	public AbstractBatikDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter p) {
		super(el, parent);
		this.p = p;
		this.p.setDiagramElement(this);
		this.ctx = ctx;
	}

	protected Painter p;
	protected Kite9BridgeContext ctx;
	
	private SVGTransformer transformer;
	
	protected void initialize() {
		this.transformer = TransformFactory.initializeTransformer(this);
	}
	
	protected abstract ContentTransform getDefaultTransform();

	protected double getCssDoubleValue(String prop) {
		Value v = getCSSStyleProperty(prop);
		return v.getFloatValue();
	}

	protected Element paintElementToDocument(Document d) {
		return transformer.postProcess(p, d);
	}

	
	protected Map<String, String> getParameters() {
		return Collections.emptyMap();
	}

	public ContentTransform getTransform() {
		ContentTransform t = null;
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.CONTENT_TRANSFORM);
		t = (ContentTransform) ev.getTheValue();

		if ((t == null) || (t==ContentTransform.DEFAULT)) {
			t = getDefaultTransform();
		}
		
		return t;
	}

	@Override
	protected Painter getPainter() {
		return p;
	}
	
}
