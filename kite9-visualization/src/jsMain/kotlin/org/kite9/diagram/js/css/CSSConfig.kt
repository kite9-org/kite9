import kotlinx.browser.window
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.js.css.CSSProperty
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.End
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.*

@JsExport
@JsName("initCSS")
fun init() {

    val css = window.asDynamic().CSS as CSSRegistry

    inline fun <reified T : Enum<T>> syntax(): String {
        return enumValues<T>().map { it.name.lowercase().replace("_", "-") }.reduceIndexed {
                i,
                out,
                s ->
            if (i == 0) {
                s
            } else {
                out + " | " + s
            }
        }
    }

    fun <E : Enum<E>> lower(e: Enum<E>): String {
        return e.name.lowercase().replace("_", "-")
    }

    fun register(prop: CSSProperty) {
        css.registerProperty(prop)
    }

    // PADDING CSS
    register(CSSProperty(CSSConstants.PADDING_PROPERTY, "<length>", "0px", false))
    register(CSSProperty(CSSConstants.PADDING_LEFT_PROPERTY, "<length> | none", "none", false))
    register(CSSProperty(CSSConstants.PADDING_RIGHT_PROPERTY, "<length> | none", "none", false))
    register(CSSProperty(CSSConstants.PADDING_BOTTOM_PROPERTY, "<length> | none", "none", false))
    register(CSSProperty(CSSConstants.PADDING_TOP_PROPERTY, "<length> | none", "none", false))

    // MARGIN CSS
    // We are using Kite9 margin here to differentiate from the one in regular CSS (not sure if this
    // is a good idea)
    register(CSSProperty(CSSConstants.MARGIN_PROPERTY, "<length>", "0px", false))
    register(CSSProperty(CSSConstants.MARGIN_LEFT_PROPERTY, "<length> | none", "none", false))
    register(CSSProperty(CSSConstants.MARGIN_RIGHT_PROPERTY, "<length> | none", "none", false))
    register(CSSProperty(CSSConstants.MARGIN_TOP_PROPERTY, "<length> | none", "none", false))
    register(CSSProperty(CSSConstants.MARGIN_BOTTOM_PROPERTY, "<length> | none", "none", false))

    // ELEMENT TYPE / LAYOUT CONTROL
    register(
            CSSProperty(
                    CSSConstants.ELEMENT_TYPE_PROPERTY,
                    syntax<DiagramElementType>(),
                    lower(DiagramElementType.UNSPECIFIED),
                    false
            )
    )
    register(
            CSSProperty(
                    CSSConstants.ELEMENT_USAGE_PROPERTY,
                    syntax<RectangularElementUsage>(),
                    lower(RectangularElementUsage.REGULAR),
                    false
            )
    )
    register(CSSProperty(CSSConstants.LAYOUT_PROPERTY, syntax<Layout>() + " | none", "none", false))
    register(
            CSSProperty(
                    CSSConstants.CONTENT_TRANSFORM,
                    syntax<ContentTransform>(),
                    lower(ContentTransform.NORMAL),
                    false
            )
    )

    // SIZING
    register(
            CSSProperty(
                    CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY,
                    syntax<DiagramElementSizing>(),
                    lower(DiagramElementSizing.MINIMIZE),
                    false
            )
    )
    register(
            CSSProperty(
                    CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY,
                    syntax<DiagramElementSizing>(),
                    lower(DiagramElementSizing.MINIMIZE),
                    false
            )
    )
    // registerCustomCSSShorthandManager(SizingShorthandManager())

    // GRIDS
    register(CSSProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY, "<integer>+", "0", false))
    register(CSSProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY, "<integer>+", "0", false))
    register(CSSProperty(CSSConstants.GRID_ROWS_PROPERTY, "<integer>", "0", false))
    register(CSSProperty(CSSConstants.GRID_COLUMNS_PROPERTY, "<integer>", "0", false))
    //    registerCustomCSSShorthandManager(GridSizeShorthandManager())
    //    registerCustomCSSShorthandManager(OccupiesShorthandManager())

    // CONNECTION TRAVERSAL
    register(
            CSSProperty(
                    CSSConstants.TRAVERSAL_PROPERTY,
                    syntax<BorderTraversal>(),
                    lower(BorderTraversal.LEAVING),
                    false
            )
    )
    register(
            CSSProperty(
                    CSSConstants.TRAVERSAL_BOTTOM_PROPERTY,
                    syntax<BorderTraversal>() + " | none",
                    "none",
                    false
            )
    )
    register(
            CSSProperty(
                    CSSConstants.TRAVERSAL_LEFT_PROPERTY,
                    syntax<BorderTraversal>() + " | none",
                    "none",
                    false
            )
    )
    register(
            CSSProperty(
                    CSSConstants.TRAVERSAL_TOP_PROPERTY,
                    syntax<BorderTraversal>() + " | none",
                    "none",
                    false
            )
    )
    register(
            CSSProperty(
                    CSSConstants.TRAVERSAL_RIGHT_PROPERTY,
                    syntax<BorderTraversal>() + " | none",
                    "none",
                    false
            )
    )

    // CONNECTION SIDES
    register(
            CSSProperty(
                    CSSConstants.CONNECTIONS_PROPERTY,
                    syntax<ConnectionsSeparation>(),
                    lower(ConnectionsSeparation.SAME_SIDE),
                    false
            )
    )

    // ALIGNMENT
    register(
            CSSProperty(
                    CSSConstants.VERTICAL_ALIGNMENT,
                    syntax<VerticalAlignment>(),
                    lower(VerticalAlignment.CENTER),
                    false
            )
    )
    register(
            CSSProperty(
                    CSSConstants.HORIZONTAL_ALIGNMENT,
                    syntax<HorizontalAlignment>(),
                    lower(HorizontalAlignment.CENTER),
                    false
            )
    )

    register(CSSProperty(CSSConstants.VERTICAL_ALIGN_POSITION, "<length-percentage>", "50%", false))
    register(
            CSSProperty(CSSConstants.HORIZONTAL_ALIGN_POSITION, "<length-percentage>", "50%", false)
    )

    // LINK DIRECTION
    register(CSSProperty(CSSConstants.DIRECTION, syntax<Direction>() + " | none", "none", true))

    // LINK LENGTHS
    register(CSSProperty(CSSConstants.LINK_INSET, "<length>", "0", false))
    register(CSSProperty(CSSConstants.LINK_MINIMUM_LENGTH, "<length>", "0", false))
    register(CSSProperty(CSSConstants.LINK_GUTTER, "<length>", "0", false))
    register(CSSProperty(CSSConstants.LINK_CORNER_RADIUS, "<length>", "0", false))

    // LINK DYNAMICS
    register(
            CSSProperty(
                    CSSConstants.LINK_FROM_XPATH,
                    "*",
                    "\"descendant::*[@k9-elem='from']/@reference\'",
                    true
            )
    )
    register(
            CSSProperty(
                    CSSConstants.LINK_TO_XPATH,
                    "*",
                    "\"descendant::*[@k9-elem='to']/@reference\"",
                    true
            )
    )
    register(CSSProperty(CSSConstants.LINK_END, syntax<End>() + " | none", "none", false))

    // TERMINATORS
    register(CSSProperty(CSSConstants.MARKER_RESERVE, "<length>", "0", false))

    // RECTANGLE SIZING
    register(CSSProperty(CSSConstants.RECT_MINIMUM_SIZE, "<length>+", "0 0", false))
    register(CSSProperty(CSSConstants.RECT_MINIMUM_WIDTH, "<length> | none", "none", false))
    register(CSSProperty(CSSConstants.RECT_MINIMUM_HEIGHT, "<length> | none", "none", false))

    // TEXT BOUNDS
    register(CSSProperty(CSSConstants.TEXT_BOUNDS_SIZE, "<length>+", "10000px 10000px", true))
    register(CSSProperty(CSSConstants.TEXT_BOUNDS_WIDTH, "<length>", "10000px", true))
    register(CSSProperty(CSSConstants.TEXT_BOUNDS_HEIGHT, "<length>", "10000px", true))

    // PORT SPECIFIC
    register(CSSProperty(CSSConstants.PORT_POSITION, "<length-percentage>", "50%", false))
}
