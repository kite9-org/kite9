package org.kite9.diagram.dom.model;

import org.jetbrains.annotations.NotNull;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.BatikLeafPainter;
import org.kite9.diagram.dom.painter.*;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.common.elements.factory.DiagramElementFactory;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.diagram.model.style.RectangularElementUsage;
import org.w3c.dom.Element;

public abstract class AbstractDiagramElementFactory<X> implements DiagramElementFactory<X> {

    public AbstractDiagramElementFactory() {
        super();
    }

    protected ElementContext context;

    public void setElementContext(ElementContext c) {
        this.context = c;
    }

    protected AbstractDOMDiagramElement instantiateDiagramElement(DiagramElement parent, Element el, DiagramElementType lt, RectangularElementUsage usage) {
        if ((parent == null) && (lt != DiagramElementType.DIAGRAM)) {
            // all elements apart from diagram must have a parent
            return null;
        }

        switch (lt) {
            case DIAGRAM:
                if (parent != null) {
                    throw new Kite9XMLProcessingException("Can't nest type 'diagram' @ " + getId(el), el);
                }
                return new DiagramImpl(el, context, getContainerPainter(el), ContentTransform.POSITION);
            case CONTAINER:
                switch (usage) {
                    case LABEL:
                        return new LabelContainerImpl(el, parent, context, getContainerPainter(el), ContentTransform.POSITION);
                    case REGULAR:
                        return new ConnectedContainerImpl(el, parent, context, getContainerPainter(el), ContentTransform.POSITION);
                    case DECAL:
                    default:
                        // need to extend slideables to handle this
                        // return new DecalContainerImpl(el, parent, context, new SVGContainerRectangularPainter(el, context), ContentTransform.POSITION);
                        throw new Kite9XMLProcessingException("Decal containers not supported yet: @" + getId(el), el);
                }
            case TEXT:
                switch (usage) {
                    case LABEL:
                        return new LabelLeafImpl(el, parent, context, getTextPainter(el), ContentTransform.CROP);
                    case DECAL:
                        return new DecalLeafImpl(el, parent, context, getTextPainter(el), ContentTransform.RESCALE);
                    case REGULAR:
                    default:
                        return new ConnectedLeafImpl(el, parent, context, getTextPainter(el), ContentTransform.CROP);
                }
            case SVG:
                switch (usage) {
                    case LABEL:
                        return new LabelLeafImpl(el, parent, context, getLeafPainter(el), ContentTransform.CROP);
                    case DECAL:
                        return new DecalLeafImpl(el, parent, context, getLeafPainter(el), ContentTransform.POSITION);
                    case REGULAR:
                    default:
                        return new ConnectedLeafImpl(el, parent, context, getLeafPainter(el), ContentTransform.CROP);
                }
            case LINK:
                return new ConnectionImpl(el, parent, context, new DirectSVGGroupPainter(el), ContentTransform.POSITION);
            case LINK_END:
                return new TerminatorImpl(el, parent, context, new DirectSVGGroupPainter(el), ContentTransform.POSITION);
            case NONE:
                return null;
            case UNSPECIFIED:
            default:
                throw new Kite9XMLProcessingException("Don't know how to process element: " + el + "(" + el.getTagName() + ") with type " + lt + " and usage " + usage + " and parent " + parent, el);
        }
    }

    protected abstract Painter getContainerPainter(Element el);

    protected abstract LeafPainter getLeafPainter(Element el);

    protected abstract LeafPainter getTextPainter(Element el);

    private String getId(Element el) {
        return el.getAttribute("id");
    }

}
