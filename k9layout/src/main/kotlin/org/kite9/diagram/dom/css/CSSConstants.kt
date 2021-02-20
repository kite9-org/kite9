package org.kite9.diagram.dom.css

interface CSSConstants {
    companion object {
        const val LEFT = "left"
        const val RIGHT = "right"
        const val TOP = "top"
        const val BOTTOM = "bottom"
        const val KITE9_CSS_PROPERTY_PREFIX = "kite9-"
        const val ELEMENT_TYPE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "type"
        const val ELEMENT_USAGE_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "usage"
        const val CONTENT_TRANSFORM = KITE9_CSS_PROPERTY_PREFIX + "transform"
        const val ELEMENT_SIZING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "sizing"
        const val ELEMENT_HORIZONTAL_SIZING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "horizontal-sizing"
        const val ELEMENT_VERTICAL_SIZING_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "vertical-sizing"
        const val LAYOUT_PROPERTY =
            KITE9_CSS_PROPERTY_PREFIX + "layout" // for containers, to decide how to layout their contents
        const val LABEL_PLACEMENT = KITE9_CSS_PROPERTY_PREFIX + "label-placement"

        // grid property
        const val GRID_OCCUPIES_X_PROPERTY =
            KITE9_CSS_PROPERTY_PREFIX + "occupies-x" // for containers, to decide how to layout their contents
        const val GRID_OCCUPIES_Y_PROPERTY =
            KITE9_CSS_PROPERTY_PREFIX + "occupies-y" // for containers, to decide how to layout their contents
        const val GRID_ROWS_PROPERTY =
            KITE9_CSS_PROPERTY_PREFIX + "grid-rows" // for containers, to decide how to layout their contents
        const val GRID_COLUMNS_PROPERTY =
            KITE9_CSS_PROPERTY_PREFIX + "grid-columns" // for containers, to decide how to layout their contents
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

        // for importing SVG content into an element
        const val TEMPLATE = KITE9_CSS_PROPERTY_PREFIX + "template"

        // for referencing <defs> within CSS
        const val MARKER_START_REFERENCE = KITE9_CSS_PROPERTY_PREFIX + "marker-start-reference"
        const val MARKER_END_REFERENCE = KITE9_CSS_PROPERTY_PREFIX + "marker-end-reference"
        const val MARKER_RESERVE = KITE9_CSS_PROPERTY_PREFIX + "marker-reserve"

        // controls which side connections will go into a connected
        const val CONNECTIONS_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "connections"
        const val ARRIVAL_SIDE = KITE9_CSS_PROPERTY_PREFIX + "arrival-side"

        // direction a connection goes in
        const val CONNECTION_DIRECTION = KITE9_CSS_PROPERTY_PREFIX + "direction"

        // controls whether we try and align connections along the mid-point of the connected.
        const val CONNECTION_ALIGN_PROPERTY = KITE9_CSS_PROPERTY_PREFIX + "connection-align"
        const val CONNECTION_ALIGN_PROPERTY_PREFIX = KITE9_CSS_PROPERTY_PREFIX + "connection-align-"
        const val CONNECTION_ALIGN_BOTTOM_PROPERTY = CONNECTION_ALIGN_PROPERTY_PREFIX + BOTTOM
        const val CONNECTION_ALIGN_TOP_PROPERTY = CONNECTION_ALIGN_PROPERTY_PREFIX + TOP
        const val CONNECTION_ALIGN_RIGHT_PROPERTY = CONNECTION_ALIGN_PROPERTY_PREFIX + RIGHT
        const val CONNECTION_ALIGN_LEFT_PROPERTY = CONNECTION_ALIGN_PROPERTY_PREFIX + LEFT

        // for aligning content within a container
        const val VERTICAL_ALIGNMENT = KITE9_CSS_PROPERTY_PREFIX + "vertical-align"
        const val HORIZONTAL_ALIGNMENT = KITE9_CSS_PROPERTY_PREFIX + "horizontal-align"

        // Length settings
        const val LINK_INSET = KITE9_CSS_PROPERTY_PREFIX + "link-inset"
        const val LINK_GUTTER = KITE9_CSS_PROPERTY_PREFIX + "link-gutter"
        const val LINK_MINIMUM_LENGTH = KITE9_CSS_PROPERTY_PREFIX + "link-minimum-length"
        const val LINK_CORNER_RADIUS = KITE9_CSS_PROPERTY_PREFIX + "link-corner-radius"

        // Rectangular Sizing 
        const val RECT_MINIMUM_SIZE = KITE9_CSS_PROPERTY_PREFIX + "min-size"
        const val RECT_MINIMUM_WIDTH = KITE9_CSS_PROPERTY_PREFIX + "min-width"
        const val RECT_MINIMUM_HEIGHT = KITE9_CSS_PROPERTY_PREFIX + "min-height"

        // Scripting
        const val SCRIPT = KITE9_CSS_PROPERTY_PREFIX + "script"

        // for link dynamics
        const val LINK_FROM_XPATH = KITE9_CSS_PROPERTY_PREFIX + "link-from-xpath"
        const val LINK_TO_XPATH = KITE9_CSS_PROPERTY_PREFIX + "link-to-xpath"
        const val LINK_END = KITE9_CSS_PROPERTY_PREFIX + "link-end"

        // text bounding
        const val TEXT_BOUNDS_WIDTH = KITE9_CSS_PROPERTY_PREFIX + "text-bounds-width"
        const val TEXT_BOUNDS_HEIGHT = KITE9_CSS_PROPERTY_PREFIX + "text-bounds-height"
        const val TEXT_BOUNDS = KITE9_CSS_PROPERTY_PREFIX + "text-bounds"
    }
}