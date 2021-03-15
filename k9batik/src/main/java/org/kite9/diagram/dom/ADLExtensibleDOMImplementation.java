package org.kite9.diagram.dom;

import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.RGBColorValue;
import org.apache.batik.css.engine.value.svg.MarkerManager;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.managers.*;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.End;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.*;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Extends the SVG DOM Implementation by adding Kite9 Namespace support, and
 * handing for Kite9 CSS definitions.
 * 
 * @author robmoffat
 *
 */
public class ADLExtensibleDOMImplementation extends CachingSVGDOMImplementation {
	
	public static final boolean USE_GENERIC_XML_ELEMENT = true;	


	public ADLExtensibleDOMImplementation() {
		this(Cache.NO_CACHE);
	}
	
	public ADLExtensibleDOMImplementation(Cache cache) {
		super(cache);

		// PADDING CSS
		registerCustomCSSShorthandManager(new FourDirectionalShorthandManager(CSSConstants.PADDING_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.PADDING_LEFT_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.PADDING_RIGHT_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.PADDING_TOP_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.PADDING_BOTTOM_PROPERTY));

		// MARGIN CSS
		// We are using Kite9 margin here to differentiate from the one in regular CSS (not sure if this is a good idea)
		registerCustomCSSShorthandManager(new FourDirectionalShorthandManager(CSSConstants.MARGIN_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.MARGIN_LEFT_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.MARGIN_RIGHT_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.MARGIN_TOP_PROPERTY));
		registerCustomCSSValueManager(new PaddingLengthManager(CSSConstants.MARGIN_BOTTOM_PROPERTY));
		
		
		// ELEMENT TYPE / LAYOUT CONTROL
		registerCustomCSSValueManager(new EnumManager(CSSConstants.ELEMENT_TYPE_PROPERTY, DiagramElementType.class, DiagramElementType.UNSPECIFIED, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.ELEMENT_USAGE_PROPERTY, RectangularElementUsage.class, RectangularElementUsage.REGULAR, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.LAYOUT_PROPERTY, Layout.class, null, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.CONTENT_TRANSFORM, ContentTransform.class, ContentTransform.NORMAL, false));
		
		// SIZING
		registerCustomCSSValueManager(new EnumManager(CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY, DiagramElementSizing.class, DiagramElementSizing.MINIMIZE, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY, DiagramElementSizing.class, DiagramElementSizing.MINIMIZE, false));
		registerCustomCSSShorthandManager(new SizingShorthandManager());
		
		// GRIDS
		registerCustomCSSValueManager(new IntegerRangeManager(CSSConstants.GRID_OCCUPIES_X_PROPERTY));
		registerCustomCSSValueManager(new IntegerRangeManager(CSSConstants.GRID_OCCUPIES_Y_PROPERTY));
		registerCustomCSSValueManager(new GridSizeManager(CSSConstants.GRID_ROWS_PROPERTY));
		registerCustomCSSValueManager(new GridSizeManager(CSSConstants.GRID_COLUMNS_PROPERTY));
		registerCustomCSSShorthandManager(new GridSizeShorthandManager());
		registerCustomCSSShorthandManager(new OccupiesShorthandManager());
		
		// CONNECTION TRAVERSAL
		registerCustomCSSShorthandManager(new TraversalShorthandManager());
		registerCustomCSSValueManager(new EnumManager(CSSConstants.TRAVERSAL_BOTTOM_PROPERTY, BorderTraversal.class, BorderTraversal.LEAVING, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.TRAVERSAL_RIGHT_PROPERTY, BorderTraversal.class, BorderTraversal.LEAVING, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.TRAVERSAL_LEFT_PROPERTY, BorderTraversal.class, BorderTraversal.LEAVING, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.TRAVERSAL_TOP_PROPERTY, BorderTraversal.class, BorderTraversal.LEAVING, false));
		
		// CONNECTION SIDES
		registerCustomCSSValueManager(new EnumManager(CSSConstants.CONNECTIONS_PROPERTY, ConnectionsSeparation.class, ConnectionsSeparation.SAME_SIDE, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.ARRIVAL_SIDE, Direction.class, null, false));
		
		// ALIGNMENT
		registerCustomCSSValueManager(new EnumManager(CSSConstants.VERTICAL_ALIGNMENT, VerticalAlignment.class, VerticalAlignment.CENTER, false));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.HORIZONTAL_ALIGNMENT, HorizontalAlignment.class, HorizontalAlignment.CENTER, false));
		
		registerCustomCSSShorthandManager(new FourDirectionalShorthandManager(CSSConstants.CONNECTION_ALIGN_PROPERTY));
		registerCustomCSSValueManager(new ConnectionAlignmentLengthManager(CSSConstants.CONNECTION_ALIGN_LEFT_PROPERTY));
		registerCustomCSSValueManager(new ConnectionAlignmentLengthManager(CSSConstants.CONNECTION_ALIGN_RIGHT_PROPERTY));
		registerCustomCSSValueManager(new ConnectionAlignmentLengthManager(CSSConstants.CONNECTION_ALIGN_TOP_PROPERTY));
		registerCustomCSSValueManager(new ConnectionAlignmentLengthManager(CSSConstants.CONNECTION_ALIGN_BOTTOM_PROPERTY));
		
		// LINK DIRECTION
		registerCustomCSSValueManager(new EnumManager(CSSConstants.CONNECTION_DIRECTION, Direction.class, null, true));

		// LINK LENGTHS
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.LINK_INSET, 0f));
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.LINK_MINIMUM_LENGTH, 0f));
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.LINK_GUTTER, 0f));
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.LINK_CORNER_RADIUS, 0f));
		
		// LABELS
		registerCustomCSSValueManager(new EnumManager(CSSConstants.LABEL_PLACEMENT, LabelPlacement.class, LabelPlacement.BOTTOM_RIGHT, false));
		
		// LINK DYNAMICS
		registerCustomCSSValueManager(new XPathManager(CSSConstants.LINK_FROM_XPATH, "./*[@k9-elem='from']/@reference", true));
		registerCustomCSSValueManager(new XPathManager(CSSConstants.LINK_TO_XPATH, "./*[@k9-elem='to']/@reference", true));
		registerCustomCSSValueManager(new EnumManager(CSSConstants.LINK_END, End.class, null, false));
		
		// TERMINATORS
		registerCustomCSSValueManager(new MarkerManager(CSSConstants.MARKER_START_REFERENCE));
		registerCustomCSSValueManager(new MarkerManager(CSSConstants.MARKER_END_REFERENCE));
		registerCustomCSSValueManager(new LinkLengthManager(CSSConstants.MARKER_RESERVE, 0f));

		// RECTANGLE SIZING
		registerCustomCSSShorthandManager(new SizeShorthandManager(CSSConstants.RECT_MINIMUM_WIDTH, CSSConstants.RECT_MINIMUM_HEIGHT, CSSConstants.RECT_MINIMUM_SIZE));
		registerCustomCSSValueManager(new WidthHeightManager(CSSConstants.RECT_MINIMUM_WIDTH, 0f, false));
		registerCustomCSSValueManager(new WidthHeightManager(CSSConstants.RECT_MINIMUM_HEIGHT, 0f, false));
		
		// TEXT BOUNDS
		registerCustomCSSShorthandManager(new SizeShorthandManager(CSSConstants.TEXT_BOUNDS_WIDTH, CSSConstants.TEXT_BOUNDS_HEIGHT, CSSConstants.TEXT_BOUNDS));
		registerCustomCSSValueManager(new WidthHeightManager(CSSConstants.TEXT_BOUNDS_WIDTH, 0f, true));
		registerCustomCSSValueManager(new WidthHeightManager(CSSConstants.TEXT_BOUNDS_HEIGHT, 0f, true));
	}

	public static final RGBColorValue NO_COLOR = new RGBColorValue(
			new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, -1),
			new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, -1),
			new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, -1));



}
