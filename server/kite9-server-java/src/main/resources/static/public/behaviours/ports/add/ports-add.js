import { hasLastSelected, parseInfo, isTerminator, isPort, getDependentElements } from "/public/bundles/api.js";
import { getMainSvg, getElementPageBBox } from '/public/bundles/screen.js';

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
	
	function connectedPort(terminator) {
		const info = parseInfo(terminator)
		const at = info['terminates-at']
		const end = getMainSvg().getElementById(at);
		if (isPort(end)) {
			return end;
		} else {
			return null;
		}
	}
	
	function selectElement(element, cm, event) {
		singleSelect(element);
		cm.destroy();
		cm.handle(event);
	}
	
	function highlight(port, active) {
		if (active) {
			port.classList.add("attention");
		} else {
			port.classList.remove("attention");		
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
				const portForElement = connectedPort(lastElement)
				if (portForElement) {
					cm.addControl(event, "/public/behaviours/ports/port.svg", 'Containing Port', 
						() => selectElement(portForElement, cm, event), 'Related', 
						{ 
							"onmouseover" : (e) => highlight(portForElement, true), 
							"onmouseout" : (e) => highlight(portForElement, false)
						});			
				} else {
					cm.addControl(event, "/public/behaviours/ports/port.svg", 'Add Port', () => addPort(elements));							
				}
			} else if (isPort(lastElement)) {
				const deps = getDependentElements([lastElement.getAttribute("id")]);
				deps.forEach(dep => {
					cm.addControl(event, "/public/behaviours/ports/add/link.svg", 'Link To Port', 
						() => selectElement(dep, cm, event), 'Related', 
						{ 
							"onmouseover" : (e) => highlight(dep, true), 
							"onmouseout" : (e) => highlight(dep, false)
						});		
					
					
				});
			} else {
				cm.addControl(event, "/public/behaviours/ports/port.svg", 'Add Port', () => addPort(elements));			
			}
		}
 	}
}


