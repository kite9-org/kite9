package org.kite9.framework.serialization;

public interface CSSConstants {

	public static final String ELEMENT_TYPE_PROPERTY = "type";
	public static final String ELEMENT_SIZING_PROPERTY = "sizing";
	
	public static final String LAYOUT_PROPERTY = "layout";			// for containers, to decide how to layout their contents
	public static final String GRID_OCCUPIES_X_PROPERTY = "occupies-x";			// for containers, to decide how to layout their contents
	public static final String GRID_OCCUPIES_Y_PROPERTY = "occupies-y";			// for containers, to decide how to layout their contents
	public static final String GRID_ROWS_PROPERTY = "grid-rows";			// for containers, to decide how to layout their contents
	public static final String GRID_COLUMNS_PROPERTY = "grid-columns";			// for containers, to decide how to layout their contents
	
	public static final String PADDING_BOTTOM_PROPERTY = "padding-bottom";
	public static final String PADDING_TOP_PROPERTY = "padding-top";
	public static final String PADDING_RIGHT_PROPERTY = "padding-right";
	public static final String PADDING_LEFT_PROPERTY = "padding-left";
	
	public static final String BOX_SHADOW_X_OFFSET_PROPERTY = "box-shadow-x-offset";
	public static final String BOX_SHADOW_Y_OFFSET_PROPERTY = "box-shadow-y-offset";
	public static final String BOX_SHADOW_OPACITY_PROPERTY = "box-shadow-opacity";
	public static final String BOX_SHADOW_COLOR_PROPERTY = "box-shadow-color";
		
	// deciding whether an edge can cross a container border.
	public static final String TRAVERSAL_BOTTOM_PROPERTY = "traversal-bottom";
	public static final String TRAVERSAL_TOP_PROPERTY = "traversal-top";
	public static final String TRAVERSAL_RIGHT_PROPERTY = "traversal-right";
	public static final String TRAVERSAL_LEFT_PROPERTY = "traversal-left";
	
	// for importing SVG content into an element
	public static final String TEMPLATE = "template";
}
