import { getMainSvg } from '../../../bundles/screen.js'
import { fieldset, select, numeric, hidden, formObject } from '../../../bundles/form.js' 
import { BuildControlsCallback, InitChangeEvent } from '../../styleable/styleable.js'
import { Selector } from '../../../bundles/types.js'

export const portsPositionIcon = '/public/behaviours/ports/port.svg'

const props = [ '--kite9-port-position' ]

const positionRegex = /^([0-9\-.]+)(px|%)$/

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

export function initPortsSelector(doc = getMainSvg()) : Selector {
	return () => {
		return Array.from(doc.querySelectorAll("[id][k9-ui~=port].selected"));
	}
}

export function initPortsPositionBuildControls() : BuildControlsCallback {
	return function(selectedElement, style) {
		const position = style[props[1]];
		const { amount, unit } = parsePosition(position);
		
		return [ fieldset('Port Position', [
			numeric('amount', amount, {name: undefined, id: 'amount'}),
			select('unit', unit, {name: undefined, id: 'unit'}, [ '%', 'px' ]),
			hidden(props[1], style[props[1]])
		]) ];
	}
}


export const initPortsPositionChangeEvent: InitChangeEvent = () => {
	return () => {
		// update hidden field
		const form = formObject('enum');
		const amount = (form.querySelector('#amount') as HTMLFormElement).value;
		const unit = (form.querySelector('#unit') as HTMLFormElement).value;
		const pos = (form.querySelector('#'+props[1])  as HTMLFormElement);
		if (amount) {
			pos.value = amount+unit;
		} else {
			pos.value = undefined;
		}
	}
}