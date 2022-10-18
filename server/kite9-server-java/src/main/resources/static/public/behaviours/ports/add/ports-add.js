import { hasLastSelected, parseInfo, isTerminator, isPort, isConnected, getDependentElements, connectedElement, createUniqueId } from "/public/bundles/api.js";
import { getMainSvg } from '/public/bundles/screen.js';

/**
 * If you select a container, this adds the default port to the container (if allowed)
 * If you select a terminator, this also re-links the connection to the new port
 */
export function initPortsAddContextMenuCallback(command, containment, paletteFinder, selector) {
	
	if (selector == undefined) {
		selector = function() {
			const palettePort = paletteFinder(document.params['port-template-uri']);
			return Array.from(getMainSvg()
				.querySelectorAll("[id].selected"))
				.filter(e => {
					if (isTerminator(e)) {
						const connected = connectedElement(e, getMainSvg());
						if (isPort(connected)) {
							// already has a port
							return false;
						} else {
							// only allow containers that can contain the palette port
							return containment.canContain(palettePort, connected);
						}		 
					} else if (isConnected(e)) {
						return containment.canContain(palettePort, e);
					}
				});
		}
	}
	
	function addPorts(elements, contextMenu) {
		const portUri = document.params['port-template-uri'];
		elements.forEach(e => {
			if (isTerminator(e)) {

			} else if (isConnected(e)) {
				const newId = createUniqueId();
				command.push({
					"type": 'InsertUrl',
					"fragmentId": e.getAttribute("id"),
					"uriStr": portUri,
					"newId" : newId,
				});
			}
 		});
 		contextMenu.destroy();

 		command.perform();
	}
	
	/**
	 * Provides add port/select port option for the context menu
	 */
	return function(event, cm) {
		
		const elements = hasLastSelected(selector());
		if (elements.length > 0) {
			cm.addControl(event, "/public/behaviours/ports/port.svg", 'Add Port', () => addPorts(elements, cm));							
		}
 	}
}


