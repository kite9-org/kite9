package org.kite9.framework.dom;

public interface CSSConstants {
	
	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	public static final String TOP = "top";
	public static final String BOTTOM = "bottom";

	public static final String KITE9_CSS_PROPERTY_PREFIX = "kite9-";

	public static final String ELEMENT_TYPE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"type";
	public static final String ELEMENT_SIZING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"sizing";
	public static final String ELEMENT_USAGE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"usage";
	
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
	public static final String MARKER_REFERENCE = KITE9_CSS_PROPERTY_PREFIX+"marker-reference";
	
	// controls which side connections will go into a connected
	public static final String CONNECTIONS_PROPERTY = KITE9_CSS_PROPERTY_PREFIX+"connections";
	
	// for aligning content within a container
	public static final String VERTICAL_ALIGNMENT = KITE9_CSS_PROPERTY_PREFIX+"vertical-align";
	public static final String HORIZONTAL_ALIGNMENT = KITE9_CSS_PROPERTY_PREFIX+"horizontal-align";
	
	// Length settings
	public static final String LINK_INSET = KITE9_CSS_PROPERTY_PREFIX+"link-inset";
	public static final String LINK_GUTTER = KITE9_CSS_PROPERTY_PREFIX+"link-gutter";
	public static final String LINK_MINIMUM_LENGTH = KITE9_CSS_PROPERTY_PREFIX+"link-minimum-length";
	
}
