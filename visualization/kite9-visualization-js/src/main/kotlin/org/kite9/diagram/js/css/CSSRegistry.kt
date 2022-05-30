import org.kite9.diagram.js.bridge.PropValue
import org.kite9.diagram.js.css.CSSNumericValue
import org.kite9.diagram.js.css.CSSProperty

external interface CSSRegistry {

    fun registerProperty(p: CSSProperty)
    fun cm(s: String): CSSNumericValue
    fun mm(s: String): CSSNumericValue
    fun pt(s: String): CSSNumericValue
    fun pc(s: String): CSSNumericValue
    fun em(s: String): CSSNumericValue

}