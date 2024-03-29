package org.kite9.diagram.dom.css

interface CSSConstants {
    companion object {

        const val LEFT = "left"
        const val RIGHT = "right"
        const val TOP = "top"
        const val BOTTOM = "bottom"
        const val WIDTH = "width"
        const val HEIGHT = "height"
        const val SIZE = "size"

        private const val KITE9_CSS_PROPERTY_PREFIX = "--kite9-"

        const val ELEMENT_TYPE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "type"
        const val ELEMENT_USAGE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "usage"
        const val CONTENT_TRANSFORM = KITE9_CSS_PROPERTY_PREFIX + "transform"
        const val ELEMENT_SIZING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "sizing"
        const val ELEMENT_HORIZONTAL_SIZING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "horizontal-sizing"
        const val ELEMENT_VERTICAL_SIZING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "vertical-sizing"

        // for containers, to decide how to layout their contents
        const val LAYOUT_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "layout"

        // grid property (part of layout)
        const val GRID_OCCUPIES_X_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "occupies-x"
        const val GRID_OCCUPIES_Y_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "occupies-y"
        const val GRID_ROWS_PROPERTY =  KITE9_CSS_PROPERTY_PREFIX + "grid-rows"
        const val GRID_COLUMNS_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "grid-columns"
        const val GRID_SIZE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "grid-size"
        const val GRID_OCCUPIES_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "occupies"

        // margin property
        const val KITE9_CSS_MARGIN_PROPERTY_PREFIX = KITE9_CSS_PROPERTY_PREFIX + "margin-"
        const val MARGIN_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "margin"
        const val MARGIN_BOTTOM_PROPERTY = KITE9_CSS_MARGIN_PROPERTY_PREFIX + BOTTOM
        const val MARGIN_TOP_PROPERTY = KITE9_CSS_MARGIN_PROPERTY_PREFIX + TOP
        const val MARGIN_RIGHT_PROPERTY = KITE9_CSS_MARGIN_PROPERTY_PREFIX + RIGHT
        const val MARGIN_LEFT_PROPERTY = KITE9_CSS_MARGIN_PROPERTY_PREFIX + LEFT

        // padding property
        const val KITE9_CSS_PADDING_PROPERTY_PREFIX = KITE9_CSS_PROPERTY_PREFIX + "padding-"
        const val PADDING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "padding"
        const val PADDING_BOTTOM_PROPERTY = KITE9_CSS_PADDING_PROPERTY_PREFIX + BOTTOM
        const val PADDING_TOP_PROPERTY = KITE9_CSS_PADDING_PROPERTY_PREFIX + TOP
        const val PADDING_RIGHT_PROPERTY = KITE9_CSS_PADDING_PROPERTY_PREFIX + RIGHT
        const val PADDING_LEFT_PROPERTY = KITE9_CSS_PADDING_PROPERTY_PREFIX + LEFT

        // deciding whether an edge can cross a container border.
        const val KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX = KITE9_CSS_PROPERTY_PREFIX + "traversal-"
        const val TRAVERSAL_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "traversal"
        const val TRAVERSAL_BOTTOM_PROPERTY = KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX + BOTTOM
        const val TRAVERSAL_TOP_PROPERTY = KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX + TOP
        const val TRAVERSAL_RIGHT_PROPERTY = KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX + RIGHT
        const val TRAVERSAL_LEFT_PROPERTY = KITE9_CSS_TRAVERSAL_PROPERTY_PREFIX + LEFT

        // controls which side connections will go into a connected
        const val CONNECTIONS_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "connections"

        // direction a connection goes in (used by ports, links, terminators)
        const val DIRECTION = KITE9_CSS_PROPERTY_PREFIX + "direction"
        const val PORT_POSITION = KITE9_CSS_PROPERTY_PREFIX + "port-position"

        // distance between edge of connection and terminator it joins to (space for marker)
        const val MARKER_RESERVE = KITE9_CSS_PROPERTY_PREFIX + "marker-reserve"

        // controls whether we try and align connections along the mid-point of the connected.
        const val VERTICAL_ALIGN_POSITION = KITE9_CSS_PROPERTY_PREFIX + "vertical-align-position"
        const val HORIZONTAL_ALIGN_POSITION = KITE9_CSS_PROPERTY_PREFIX + "horizontal-align-position"

        // for aligning content within a container
        const val VERTICAL_ALIGNMENT = KITE9_CSS_PROPERTY_PREFIX + "vertical-align"
        const val HORIZONTAL_ALIGNMENT = KITE9_CSS_PROPERTY_PREFIX + "horizontal-align"

        // Length settings
        const val LINK_INSET = KITE9_CSS_PROPERTY_PREFIX + "link-inset"
        const val LINK_GUTTER = KITE9_CSS_PROPERTY_PREFIX + "link-gutter"
        const val LINK_MINIMUM_LENGTH = KITE9_CSS_PROPERTY_PREFIX + "link-minimum-length"
        const val LINK_CORNER_RADIUS = KITE9_CSS_PROPERTY_PREFIX + "link-corner-radius"

        // Rectangular Sizing 
        const val RECT_MINIMUM_SIZE = KITE9_CSS_PROPERTY_PREFIX + "min-" + SIZE
        const val RECT_MINIMUM_WIDTH = KITE9_CSS_PROPERTY_PREFIX + "min-" + WIDTH
        const val RECT_MINIMUM_HEIGHT = KITE9_CSS_PROPERTY_PREFIX + "min-" + HEIGHT

        // for link dynamics
        const val LINK_FROM_XPATH = KITE9_CSS_PROPERTY_PREFIX + "link-from-xpath"
        const val LINK_TO_XPATH = KITE9_CSS_PROPERTY_PREFIX + "link-to-xpath"
        const val LINK_END = KITE9_CSS_PROPERTY_PREFIX + "link-end"

        // text bounding
        const val TEXT_BOUNDS_SIZE = KITE9_CSS_PROPERTY_PREFIX + "text-bounds-" + SIZE
        const val TEXT_BOUNDS_WIDTH = KITE9_CSS_PROPERTY_PREFIX + "text-bounds-" + WIDTH
        const val TEXT_BOUNDS_HEIGHT = KITE9_CSS_PROPERTY_PREFIX + "text-bounds-" + HEIGHT
        const val TEXT_ALIGN = "text-align"

    }
}