import kotlinx.browser.window
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.js.css.CSSProperty

@JsName("initCSS")
fun init() {

    val css = window.asDynamic().CSS as CSSRegistry
    css.registerProperty( CSSProperty(CSSConstants.PADDING_LEFT_PROPERTY, "<length>", "0px", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_RIGHT_PROPERTY, "<length>", "0px", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_BOTTOM_PROPERTY, "<length>", "0px", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_TOP_PROPERTY, "<length>", "0px", false));

}