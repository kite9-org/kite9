import { hasLastSelected, parseInfo, isTerminator, isPort, getDependentElements, connectedPort } from "/public/bundles/api.js";
import { getMainSvg } from '/public/bundles/screen.js';

/**
 * If you select a container, this adds the default port to the container (if allowed)
 * If you select a terminator, this also re-links the connection to the new port
 * If the terminator is alredy on the port, it just selects the port.
 */
export function initPortsAddContextMenuCallback(command, singleSelect, selector) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id].selected")
		}
	}
	
	/**
	 * Provides add port/select port option for the context menu
	 */
	return function(event, cm) {
		
		const elements = hasLastSelected(selector());
		const lastElement = hasLastSelected(selector(), true);
		if (lastElement) {
			if (isTerminator(lastElement)) {
				const portForElement = connectedPort(lastElement, getMainSvg())
				if (!portForElement) {
					cm.addControl(event, "/public/behaviours/ports/port.svg", 'Add New Port For Terminator', () => addPort(elements));							
				}
			} else {
				cm.addControl(event, "/public/behaviours/ports/port.svg", 'Add Port', () => addPort(elements));			
			}
		}
 	}
}


