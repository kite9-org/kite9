package org.kite9.diagram.batik.model;

import kotlin.jvm.JvmClassMappingKt;
import org.kite9.diagram.batik.painter.BatikLeafPainter;
import org.kite9.diagram.batik.painter.BatikTextPainter;
import org.kite9.diagram.common.elements.factory.TemporaryConnectedRectangular;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.model.AbstractDiagramElementFactory;
import org.kite9.diagram.dom.model.TemporaryConnectedRectangularImpl;
import org.kite9.diagram.dom.painter.*;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.diagram.model.style.RectangularElementUsage;
import org.w3c.dom.Element;

import java.util.Arrays;

public class BatikDiagramElementFactory extends AbstractDiagramElementFactory<Element> {

	public BatikDiagramElementFactory(ElementContext ctx) {
		super(false);
		setContext(ctx);
	}


	private static <X> X getCssStyleEnumProperty(String prop, Element e, ElementContext ec, Class<X> enumIn) {
		String s = ec.getCssStyleStringProperty(prop, e);
		if (s==null) {
			return null;
		} else {
			return Arrays.stream(enumIn.getEnumConstants())
					.filter(f -> ((Enum) f).name().toLowerCase().replace("_", "-").equals(s.trim()))
					.findFirst()
					.orElse(null);
		}
	}

	/**
	 * Produces the diagram element for the underlying XML.
	 */
	public DiagramElement createDiagramElement(Element x, DiagramElement parent) {
		DiagramElementType type = getCssStyleEnumProperty(CSSConstants.ELEMENT_TYPE_PROPERTY, x, getContext(), DiagramElementType.class);
		RectangularElementUsage usage = getCssStyleEnumProperty(CSSConstants.ELEMENT_USAGE_PROPERTY, x, getContext(), RectangularElementUsage.class);

		if ((type == null) || (usage == null)) {
			return null;
		}

		DiagramElement out = instantiateDiagramElement(parent, x, type, usage);

		if (out != null) {
			if (parent != null) {
				getContext().addChild(parent, out);
			}

			getContext().register(x, out);
		}

		return out;
	}

	@Override
	protected LeafPainter getTextPainter(Element el) {
		return new BatikTextPainter(el, getContext());
	}

	@Override
	protected BatikLeafPainter getLeafPainter(Element el) {
		return new BatikLeafPainter(el, getContext());
	}

	@Override
	protected SVGContainerRectangularPainter getContainerPainter(Element el) {
		return new SVGContainerRectangularPainter(el, getContext());
	}

	@Override
	public TemporaryConnectedRectangular createTemporaryConnected(DiagramElement parent, String idSuffix) {
		return new TemporaryConnectedRectangularImpl(parent, idSuffix, new SVGRectPainter("grid-temporary"));
	}

	@Override
	protected Painter getDirectPainter(Element el) {
		return new DirectSVGGroupPainter(el);
	}
}
