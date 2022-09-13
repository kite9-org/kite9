import { numeric } from '/public/bundles/form.js'
import { getElementHTMLBBox } from '/public/bundles/screen.js'

/**
 * Common, used by other styleable controls.
 */
export function addNumericControl(overlay, cssAttribute, style, horiz, inverse, sx, sy, inheritedLength, boxMove = (e) => {}) {
	var val = style[cssAttribute];
	var length = inheritedLength;
	var placeholderText;
	if ((val) && val.endsWith("px")) {
		val = val.substring(0, val.length-2);
		length = parseFloat(val);
		placeholderText = "revert to default"
	} else {
		placeholderText = "default ("+inheritedLength.toFixed(1)+")"
	}
	
	const box = numeric(cssAttribute, val, {"min" : "0", "placeholder": placeholderText});
	const input = box.children[1];
	
	const sizer = overlay.createSizingArrow(sx, sy, length, horiz, inverse, (v) => {
		input.value = v;
		boxMove(v)
	});

	// event for when the size is changed in the context menu
	input.addEventListener("input", (e) => {
		if (input.value) {
			const mpx = Math.max(input.value,0);
			sizer(mpx);	
			boxMove(mpx)
		} else {
			sizer(inheritedLength);
			boxMove(inheritedLength);
		}
	})
	
	return box;
}
