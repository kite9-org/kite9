import { getMainSvg } from '../../../bundles/screen.js'
import { fieldset, select, numeric, hidden, formObject } from '../../../bundles/form.js' 
import { extractFormValues } from '/public/behaviours/styleable/styleable.js'

export const portsPositionIcon = '/public/behaviours/ports/port.svg'

const props = [ '--kite9-port-position' ]

const positionRegex = /^([0-9\-\.]+)(px|\%)$/

function parsePosition(str) {
	if (str) {
		const matches = str.match(positionRegex);
		if (matches.length > 0) {
			return {
				amount: matches[1],
				unit: matches[2]
			}		
		} 
	}

	return {
		amount: '',
		unit: '%'
	}
}

export function portsSelector(doc = getMainSvg()) {
	return Array.from(doc.querySelectorAll("[id][k9-ui~=port].selected"));
}

export function initPortsPositionBuildControls() {
	return function(selectedElement, style) {
		const position = style[props[1]];
		const { amount, unit } = parsePosition(position);
		
		return [ fieldset('Port Position', [
			numeric('amount', amount, {name: undefined, id: 'amount'}),
			select('unit', unit, {name: undefined, id: 'unit'}, [ '%', 'px' ]),
			hidden(props[1], style[props[1]], {})
		]) ];
	}
}


export function initPortsPositionChangeEvent(selectedElement, svgStyle) {
	return e => {
		// update hidden field
		const form = formObject('enum');
		const side = form.querySelector('#'+props[0]);
		const amount = form.querySelector('#amount').value;
		const unit = form.querySelector('#unit').value;
		const pos = form.querySelector('#'+props[1]);
		if (amount) {
			pos.value = amount+unit;
		} else {
			pos.value = undefined;
		}
	}
}