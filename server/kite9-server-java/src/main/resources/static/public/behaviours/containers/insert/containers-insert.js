import { hasLastSelected, getKite9Target, createUniqueId, changeId } from '/public/bundles/api.js'
import { getMainSvg, getSVGCoords, getElementPageBBox, currentTarget } from '/public/bundles/screen.js'
import { getBefore } from '/public/bundles/ordering.js'
import { getElementUri} from '/public/classes/palette/palette.js';


function defaultInsertSelector() {
	return getMainSvg().querySelectorAll('[k9-contains].selected:not([k9-contains=""])');
}

function defaultInsertableSelector(palettePanel) {
	return palettePanel.querySelectorAll("[id][k9-palette]");	
}

/**
 * Provides functionality so that when the user clicks on a 
 * palette element it is inserted into the document.
 */
export function initInsertPaletteCallback(command, containment, insertableSelector, insertSelector) {
	
	if (insertableSelector == undefined) {
		insertableSelector = defaultInsertableSelector;
	}
	
	if (insertSelector == undefined) {
		insertSelector = defaultInsertSelector;
	}
	
	return function(palette, palettePanel) {

		function createInsertStep(e, drop, newId, beforeId) {			
			return {
				"type": 'InsertUrl',
				"fragmentId": e.getAttribute('id'),
				"uriStr": getElementUri(drop, palettePanel),
				"beforeId" : beforeId,
				"newId": newId
			}
		}
		
		function handleInsert(event, selectedElements) {
			const paletteElement = palette.get().querySelector("[id].mouseover");
			const ownBBox = getElementPageBBox(paletteElement);
			event.droppingElements = [];
			Array.from(selectedElements)				
				.map(e => {
					const newId = createUniqueId();
					e.classList.remove("selected");
					const parentBBox = getElementPageBBox(e);

					// create the new svg element
					const clone = paletteElement.cloneNode(true);
					const pId = paletteElement.getAttribute("id");
					changeId(clone, pId, newId);
					
					// insert it in the correct place in the svg.
					const before = getBefore(e, palette.getOpenEvent(), []);
					if (before == undefined) {
						e.appendChild(clone)
					} else {
						e.insertBefore(clone, before);
					}
					
					if (e == getKite9Target(currentTarget(palette.getOpenEvent()))) {
						// set the position so it appears at the location of the pointer event
						const coords = getSVGCoords(palette.getOpenEvent());
						clone.style.transform = "translateX("+(coords.x - parentBBox.x - (ownBBox.width / 2))+"px) translateY("+(coords.y - parentBBox.y - (ownBBox.height / 2))+"px)";
					} else {
						// set so that it's in the middle of the container.
						clone.style.transform = "translateX("+((parentBBox.width / 2) - (ownBBox.width /2))+"px) translateY("+((parentBBox.height / 2) - (ownBBox.height / 2 ))+"px)";						
					}
					
					// update the adl
					return createInsertStep(e, paletteElement, newId, 
						(before == undefined ? undefined : before.getAttribute("id")));
				})
				.forEach(c => command.push(c));
			
			palette.destroy();	
			
			command.perform();
			event.stopPropagation();			
		}
		
		function mouseUp(event) {
			if (palette.getCurrentAction() == 'insert') {
				const selectedElements = insertSelector();
				handleInsert(event, selectedElements);
			}
		}
	
		insertableSelector(palettePanel).forEach(function(v) {
	    	v.removeEventListener("mouseup", mouseUp);
	    	v.addEventListener("mouseup", mouseUp);
		})
	}
}

export function initInsertDragLocator() {
	
	return function(event) {
		return event.droppingElements ? event.droppingElements : [];
	}
	
}
	
/**
 * Adds insert option into context menu
 */
export function initInsertContextMenuCallback(palette, containment, selector) {
	
	if (selector == undefined) {
		selector = defaultInsertSelector;
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElements = hasLastSelected(selector());
		
		if (selectedElements.length > 0) {
			const allowed = containment.canInsert(selectedElements);
			if (allowed) {
				// console.log("Allowing insert with types: "+allowedTypes);
				contextMenu.addControl(event, "/public/behaviours/containers/insert/insert.svg", "Insert",
					function(e2, selector) {
						contextMenu.destroy();
						// primes this based on the screen.
						currentTarget(event);
						palette.open(
							 event, 
							 (e) => containment.canContain(e, Array.from(selectedElements)), 
							"insert");
					});
			}
		}
	}
}



