import { fieldset, select } from '../../../bundles/form.js' 
import { addNumericControl, BuildControlsCallback } from '../styleable.js'
import { getMainSvg, getElementPageBBox } from '../../../bundles/screen.js';


export const textIcon = '/public/behaviours/styleable/text/text.svg';

export function textSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=font][k9-ui~=edit].selected"));
}

const decorationOptions = [ '', 'underline', 'line-through'];

const textAlignOptions = [ '', 'start', 'middle', 'end'];

const props = [
	'--kite9-text-bounds-width',
	'--kite9-text-bounds-height',
	'text-decoration',
	'text-align'
]

export function initTextBuildControls() : BuildControlsCallback {
	return function(selectedElement, style, overlay) {
		const bbox = getElementPageBBox(selectedElement);
		return [ fieldset('Text', [
			addNumericControl(overlay, props[0], style, true, false, bbox.x, bbox.y, bbox.width),
			addNumericControl(overlay, props[1], style, false, false, bbox.x, bbox.y, bbox.height),
			select(props[2], style[props[2]], {}, decorationOptions),
			select(props[3], style[props[3]], {}, textAlignOptions)
		]) ];
	}
}