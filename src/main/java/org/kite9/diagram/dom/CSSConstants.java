package org.kite9.diagram.dom;

public interface CSSConstants {
	
	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	public static final String TOP = "top";
	public static final String BOTTOM = "bottom";

	public static final String KITE9_CSS_PROPERTY_PREFIX = "kite9-";

	public static final String ELEMENT_TYPE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"type";
	public static final String ELEMENT_SIZING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"sizing";
	public static final String ELEMENT_USAGE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"usage";
	public static final String CONTENT_TRANSFORM = KITE9_CSS_PROPERTY_PREFIX+"transform";
	
	public static final String LAYOUT_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"layout";			// for containers, to decide how to layout their contents
	
	// grid property
	public static final String GRID_OCCUPIES_X_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"occupies-x";			// for containers, to decide how to layout their contents
	public static final String GRID_OCCUPIES_Y_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"occupies-y";			// for containers, to decide how to layout their contents
	public static final String GRID_ROWS_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"grid-rows";			// for containers, to decide how to layout their contents
	public static final String GRID_COLUMNS_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"grid-columns";			// for containers, to decide how to layout their contents
	public static final String GRID_SIZE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"grid-size";
	public static final String GRID_OCCUPIES_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"occupies";
	
	// margin property
	public static final String KITE9_CSS_MARGIN_PROPERTY_PREFIX = KITE9_CSS_PROPERTY_PREFIX+"margin-";
	public static final String MARGIN_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"margin";
	public static final String MARGIN_BOTTOM_PROPERTY = KITE9_CSS_MARGIN_PROPERTY_PREFIX+BOTTOM;
	public static final String MARGIN_TOP_PROPERTY = KITE9_CSS_MARGIN_PROPERTY_PREFIX+TOP;
	public static final String MARGIN_RIGHT_PROPERTY = KITE9_CSS_MARGIN_PROPERTY_PREFIX+RIGHT;
	public static final String MARGIN_LEFT_PROPERTY = KITE9_CSS_MARGIN_PROPERTY_PREFIX+LEFT;
	
	// padding property
	public static final String KITE9_CSS_PADDING_PROPERTY_PREFIX = KITE9_CSS_PROPERTY_PREFIX+"padding-";
	public static final String PADDING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"padding";
	public static final String PADDING_BOTTOM_PROPERTY = KITE9_CSS_PADDING_PROPERTY_PREFIX+BOTTOM;
	public static final String PADDING_TOP_PROPERTY = KITE9_CSS_PADDING_PROPERTY_PREFIX+TOP;
	public static final String PADDING_RIGHT_PROPERTY = KITE9_CSS_PADDING_PROPERTY_PREFIX+RIGHT;
	public static final String PADDING_LEFT_PROPERTY = KITE9_CSS_PADDING_PROPERTY_PREFIX+LEFT;
		
	// deciding whether an edge can cross a container border.
	public static final String KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX = KITE9_CSS_PROPERTY_PREFIX+"traversal-";
	public static final String TRAVERSAL_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"traversal";
	public static final String TRAVERSAL_BOTTOM_PROPERTY = KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX+BOTTOM;
	public static final String TRAVERSAL_TOP_PROPERTY = KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX+TOP;
	public static final String TRAVERSAL_RIGHT_PROPERTY = KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX+RIGHT;
	public static final String TRAVERSAL_LEFT_PROPERTY = KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX+LEFT;	
	
	// for importing SVG content into an element
	public static final String TEMPLATE = KITE9_CSS_PROPERTY_PREFIX+"template";
	
	// for referencing <defs> within CSS
	public static final String MARKER_START_REFERENCE = KITE9_CSS_PROPERTY_PREFIX+"marker-start-reference";
	public static final String MARKER_END_REFERENCE = KITE9_CSS_PROPERTY_PREFIX+"marker-end-reference";
	public static final String MARKER_RESERVE = KITE9_CSS_PROPERTY_PREFIX+"marker-reserve";
	
	// controls which side connections will go into a connected
	public static final String CONNECTIONS_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"connections";
	
	// controls whether we try and align connections along the mid-point of the connected.
	public static final String CONNECTION_ALIGN_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"connection-align";
//	public static final String CONNECTION_VERTICAL_ALIGN_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"connection-vertical-align";
//	public static final String CONNECTION_HORIZONTAL_ALIGN_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"connection-horizontal-align";
	public static final String CONNECTION_ALIGN_PROPERTY_PREFIX = KITE9_CSS_PROPERTY_PREFIX+"connection-align-";
	public static final String CONNECTION_ALIGN_BOTTOM_PROPERTY = CONNECTION_ALIGN_PROPERTY_PREFIX+BOTTOM;
	public static final String CONNECTION_ALIGN_TOP_PROPERTY = CONNECTION_ALIGN_PROPERTY_PREFIX+TOP;
	public static final String CONNECTION_ALIGN_RIGHT_PROPERTY = CONNECTION_ALIGN_PROPERTY_PREFIX+RIGHT;
	public static final String CONNECTION_ALIGN_LEFT_PROPERTY = CONNECTION_ALIGN_PROPERTY_PREFIX+LEFT;	
	
	// for aligning content within a container
	public static final String VERTICAL_ALIGNMENT = KITE9_CSS_PROPERTY_PREFIX+"vertical-align";
	public static final String HORIZONTAL_ALIGNMENT = KITE9_CSS_PROPERTY_PREFIX+"horizontal-align";
	
	// Length settings
	public static final String LINK_INSET = KITE9_CSS_PROPERTY_PREFIX+"link-inset";
	public static final String LINK_GUTTER = KITE9_CSS_PROPERTY_PREFIX+"link-gutter";
	public static final String LINK_MINIMUM_LENGTH = KITE9_CSS_PROPERTY_PREFIX+"link-minimum-length";
	public static final String LINK_CORNER_RADIUS = KITE9_CSS_PROPERTY_PREFIX+"link-corner-radius";
	
	// Rectangular Sizing 
	public static final String RECT_MINIMUM_SIZE = KITE9_CSS_PROPERTY_PREFIX+"min-size";
	public static final String RECT_MINIMUM_WIDTH = KITE9_CSS_PROPERTY_PREFIX+"min-width";
	public static final String RECT_MINIMUM_HEIGHT = KITE9_CSS_PROPERTY_PREFIX+"min-height";
	
	
}
