import { hasLastSelected, parseInfo, isTerminator, isPort, isConnected, getDependentElements, connectedElement, createUniqueId, getAffordances } from "/public/bundles/api.js";
import { getMainSvg, closestSide, getElementPageBBox } from '/public/bundles/screen.js';
import { parseStyle } from '/public/bundles/css.js'

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
	
	/**
	 * For a given set of elements, some will be containers, some will be terminators.
	 * THese need to be dealt with separately.  containers will all just create a port
	 * within the container.  terminators need to be grouped by the container they are on, and
	 * the side of the container they are on.  
	 */
	function addPorts(elements, contextMenu) {
		const portUri = document.params['port-template-uri'];
		const palettePort = paletteFinder(portUri);
		const setPortSide = getAffordances(palettePort).includes("port")
		const portStyle = parseStyle(palettePort.getAttribute("style"))
		
		function newPort(newId, insideId) {
			command.push({
				"type": 'InsertUrl',
				"fragmentId": insideId,
				"uriStr": portUri,
				"newId" : newId,
			});
		}
		
		elements.filter(e => isConnected(e))
			.forEach(e => {
				const newId = createUniqueId();
				newPort(newId, e.getAttribute("id"));
			});
		
		const groups = {}
		const sides = {}
		
		elements.filter(e => isTerminator(e))
			.forEach(e => {
				const coords = getElementPageBBox(e);
				const pos = { x: coords.x + coords.width / 2, y: coords.y + coords.height / 2 };
				const container = connectedElement(e, getMainSvg());
				const side = closestSide(container, pos);
				const containerId = container.getAttribute("id");
				
				if (!groups[containerId]) {
					groups[containerId] = [ e ];
				} else {
					groups[containerId].push(e);
				}
				
				sides[containerId] = side;
			});
			
		for (const [key, value] of Object.entries(groups)) {
			const insideId = key;
			const newId = createUniqueId();
			newPort(newId, insideId);
			
			if (setPortSide) {
				const side = sides[key]
				command.push({
					fragmentId: newId,
					type: 'ReplaceStyle',
					name: '--kite9-port-side',
					to: side,
					from: portStyle['--kite9-port-side']
				})
			}

			// point the terminator at the new port
			value.forEach(t => {
				command.push({
					"fragmentId": t.getAttribute("id"),
					"type": 'ReplaceAttr',
					"to": newId,
					"name": "reference",
					"from": insideId,
				})
			})

		}
		
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


