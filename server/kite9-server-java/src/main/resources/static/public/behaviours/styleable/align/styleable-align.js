import { getMainSvg } from '/public/bundles/screen.js'
import { isConnected } from '/public/bundles/api.js'
import { fieldset, select } from '/public/bundles/form.js' 


const props = [	
	'--kite9-horizontal-align',
	'--kite9-vertical-align'
 ]

export const horiz = [ 'left', 'center', 'right' ];
export const vert = [ 'top', 'center', 'bottom' ];

export const alignIcon = '/public/behaviours/styleable/align/align.svg';

export function alignSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=align].selected"))
		.filter(e => isConnected(e));
}

export function initAlignBuildControls() {
	return function(selectedElement, style) {
		return [ fieldset(name, [
			select(props[0], style[props[0]], {}, [ '', ...horiz ]) ,
			select(props[1], style[props[1]], {}, [ '', ...vert ])
		]) ];
	}
}
