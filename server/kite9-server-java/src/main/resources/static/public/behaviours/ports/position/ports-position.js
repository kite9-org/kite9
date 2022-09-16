import { getMainSvg } from '/public/bundles/screen.js'
import { fieldset, select } from '/public/bundles/form.js' 

export const portsPositionIcon = '/public/behaviours/ports/position/position.svg'

const portSideOptions = [ 'top', 'left', 'bottom', 'right' ]

const props = [ '--kite9-port-side' ]

export function portsSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=port].selected"));
}

export function initPortsPositionBuildControls() {
	return function(selectedElement, style) {
		return [ fieldset('Port Poisition', [
			select(props[0], style[props[0]], {}, [ '', ...portSideOptions ]) ,
		]) ];
	}
}