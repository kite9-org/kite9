import kotlinx.browser.window
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.js.css.CSSProperty
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.End
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.*

@JsName("initCSS")
fun init() {

    val css = window.asDynamic().CSS as CSSRegistry

    inline fun <reified T : Enum<T>> syntax(): String {
        return enumValues<T>()
            .map { it.name.toLowerCase().replace("_","-") }
            .reduceIndexed { i, out, s ->
                if (i == 0) {
                    s;
                } else {
                    out + " | " + s
                }
            }
    }

    fun <E: Enum<E>> lower(e: Enum<E>) : String {
        return e.name.toLowerCase().replace("_","-") ;
    }

    // PADDING CSS
    css.registerProperty( CSSProperty(CSSConstants.PADDING_PROPERTY, "<length>", "0px", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_LEFT_PROPERTY, "<length> | none", "none", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_RIGHT_PROPERTY, "<length> | none", "none", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_BOTTOM_PROPERTY, "<length> | none", "none", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_TOP_PROPERTY, "<length> | none", "none", false));

    // MARGIN CSS
    // We are using Kite9 margin here to differentiate from the one in regular CSS (not sure if this is a good idea)
    css.registerProperty( CSSProperty(CSSConstants.MARGIN_PROPERTY, "<length>", "0px", false));
    css.registerProperty( CSSProperty(CSSConstants.MARGIN_LEFT_PROPERTY, "<length> | none", "none", false));
    css.registerProperty( CSSProperty(CSSConstants.MARGIN_RIGHT_PROPERTY,"<length> | none", "none", false));
    css.registerProperty( CSSProperty(CSSConstants.MARGIN_TOP_PROPERTY,"<length> | none", "none", false));
    css.registerProperty( CSSProperty(CSSConstants.MARGIN_BOTTOM_PROPERTY,"<length> | none", "none", false));

    // ELEMENT TYPE / LAYOUT CONTROL
    css.registerProperty( CSSProperty(CSSConstants.ELEMENT_TYPE_PROPERTY, syntax<DiagramElementType>(), lower(DiagramElementType.UNSPECIFIED), false))
    css.registerProperty( CSSProperty(CSSConstants.ELEMENT_USAGE_PROPERTY, syntax<RectangularElementUsage>(), lower(RectangularElementUsage.REGULAR), false))
    css.registerProperty( CSSProperty(CSSConstants.LAYOUT_PROPERTY, syntax<Layout>()+" | none", "none", false))
    css.registerProperty( CSSProperty(CSSConstants.CONTENT_TRANSFORM,syntax<ContentTransform>(), lower(ContentTransform.NORMAL), false))

    // SIZING
    css.registerProperty( CSSProperty(CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY,syntax<DiagramElementSizing>(), lower(DiagramElementSizing.MINIMIZE), false))
    css.registerProperty( CSSProperty(CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY,syntax<DiagramElementSizing>(), lower(DiagramElementSizing.MINIMIZE), false))
    //registerCustomCSSShorthandManager(SizingShorthandManager())

    // GRIDS
    css.registerProperty( CSSProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY, "<integer>+", "0", false))
    css.registerProperty( CSSProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY, "<integer>+" , "0", false))
    css.registerProperty( CSSProperty(CSSConstants.GRID_ROWS_PROPERTY, "<integer>", "0", false))
    css.registerProperty( CSSProperty(CSSConstants.GRID_COLUMNS_PROPERTY, "<integer>", "0", false))
//    registerCustomCSSShorthandManager(GridSizeShorthandManager())
//    registerCustomCSSShorthandManager(OccupiesShorthandManager())

    // CONNECTION TRAVERSAL
    css.registerProperty( CSSProperty(CSSConstants.TRAVERSAL_PROPERTY,syntax<BorderTraversal>(), lower(BorderTraversal.LEAVING), false))
    css.registerProperty( CSSProperty(CSSConstants.TRAVERSAL_BOTTOM_PROPERTY,syntax<BorderTraversal>()+" | none", "none", false))
    css.registerProperty( CSSProperty(CSSConstants.TRAVERSAL_LEFT_PROPERTY,syntax<BorderTraversal>()+" | none", "none", false))
    css.registerProperty( CSSProperty(CSSConstants.TRAVERSAL_TOP_PROPERTY,syntax<BorderTraversal>()+" | none", "none", false))
    css.registerProperty( CSSProperty(CSSConstants.TRAVERSAL_RIGHT_PROPERTY,syntax<BorderTraversal>()+" | none", "none", false))


    // CONNECTION SIDES
    css.registerProperty( CSSProperty(CSSConstants.CONNECTIONS_PROPERTY,syntax<ConnectionsSeparation>(), lower(ConnectionsSeparation.SAME_SIDE), false))
    css.registerProperty( CSSProperty(CSSConstants.ARRIVAL_SIDE,syntax<Direction>()+" | none","none", false))

    // ALIGNMENT
    css.registerProperty( CSSProperty(CSSConstants.VERTICAL_ALIGNMENT,syntax<VerticalAlignment>(), lower(VerticalAlignment.CENTER), false))
    css.registerProperty( CSSProperty(CSSConstants.HORIZONTAL_ALIGNMENT,syntax<HorizontalAlignment>(), lower(HorizontalAlignment.CENTER), false))

    css.registerProperty( CSSProperty(CSSConstants.VERTICAL_ALIGN_POSITION,"<length-percentage>", "50%", false))
    css.registerProperty( CSSProperty(CSSConstants.HORIZONTAL_ALIGN_POSITION,"<length-percentage>", "50%", false))

    // LINK DIRECTION
    css.registerProperty( CSSProperty(CSSConstants.CONNECTION_DIRECTION,syntax<Direction>()+" | none","none", true))

    // LINK LENGTHS
    css.registerProperty( CSSProperty(CSSConstants.LINK_INSET,"<length>","0", false))
    css.registerProperty( CSSProperty(CSSConstants.LINK_MINIMUM_LENGTH,"<length>","0", false))
    css.registerProperty( CSSProperty(CSSConstants.LINK_GUTTER,"<length>","0", false))
    css.registerProperty( CSSProperty(CSSConstants.LINK_CORNER_RADIUS,"<length>","0", false))


    // LABELS
    css.registerProperty( CSSProperty(CSSConstants.LABEL_PLACEMENT,syntax<LabelPlacement>(),lower(LabelPlacement.BOTTOM_RIGHT), false))


    // LINK DYNAMICS
    css.registerProperty( CSSProperty(CSSConstants.LINK_FROM_XPATH,"*",
            "\"./*[@k9-elem='from']/@reference\"", true))
    css.registerProperty( CSSProperty(CSSConstants.LINK_TO_XPATH, "*",
        "\"./*[@k9-elem='to']/@reference\"", true))
    css.registerProperty( CSSProperty(CSSConstants.LINK_END,syntax<End>()+" | none", "none", false))

    // TERMINATORS
    css.registerProperty( CSSProperty(CSSConstants.MARKER_RESERVE, "<length>", "0", false))

    // RECTANGLE SIZING
//    registerCustomCSSShorthandManager(
//        SizeShorthandManager(
//            CSSConstants.RECT_MINIMUM_WIDTH,
//            CSSConstants.RECT_MINIMUM_HEIGHT,
//            CSSConstants.RECT_MINIMUM_SIZE
//        )
//    )
    css.registerProperty( CSSProperty(CSSConstants.RECT_MINIMUM_WIDTH, "<length>", "0", false))
    css.registerProperty( CSSProperty(CSSConstants.RECT_MINIMUM_HEIGHT, "<length>", "0", false))

    // TEXT BOUNDS
//    registerCustomCSSShorthandManager(
//        SizeShorthandManager(
//            CSSConstants.TEXT_BOUNDS_WIDTH,
//            CSSConstants.TEXT_BOUNDS_HEIGHT,
//            CSSConstants.TEXT_BOUNDS
//        )
//    )
    css.registerProperty( CSSProperty(CSSConstants.TEXT_BOUNDS_WIDTH, "<length>", "10000px", true))
    css.registerProperty( CSSProperty(CSSConstants.TEXT_BOUNDS_HEIGHT, "<length>", "10000px", true))

    css.registerProperty(CSSProperty(CSSConstants.PORT_SIDE, syntax<PortSide>(), lower(PortSide.BOTTOM), inherits = false))
    css.registerProperty(CSSProperty(CSSConstants.PORT_POSITION, "<length-percentage>", "50%", false))

}


