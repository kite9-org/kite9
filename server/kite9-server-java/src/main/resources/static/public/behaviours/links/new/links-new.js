import { hasLastSelected, getKite9Target, createUniqueId, changeId } from '/public/bundles/api.js'
import { getMainSvg, getSVGCoords, getElementPageBBox, currentTarget } from '/public/bundles/screen.js'
import { getElementUri } from '/public/classes/palette/palette.js';

function defaultInsertableSelector(palettePanel) {
	return palettePanel.querySelectorAll("[id][k9-palette]");	
}

function defaultInsertSelector() {
	return getMainSvg().querySelectorAll('[k9-contains].selected:not([k9-contains=""])');
}

/**
 * Provides functionality so that when the user clicks on a 
 * palette element it is inserted into the document.
 */
export function initNewLinkPaletteCallback(dragger, insertableSelector) {
	
	if (insertableSelector == undefined) {
		insertableSelector = defaultInsertableSelector;
	}
	
	return function(palette, palettePanel) {
	
		function getPaletteElement(event) {
			var choices = Array.from(insertableSelector(palettePanel));
			var target = getKite9Target(currentTarget(event));
			while ((!choices.includes(target)) && (target != null)) {
				target = target.parentElement;
			}
			
			return target;
		}
		
		function handleDrag(event) {
			const paletteElement = getPaletteElement(event);
			
			// create a new copy of the palette element
			const newId = createUniqueId();
			const droppingElement = paletteElement.cloneNode(true);
			changeId(droppingElement, droppingElement.getAttribute("id"), newId);
			
			droppingElement.setAttribute("autoconnect", "new");
						
			// place it in the same position on the main svg 
			getMainSvg().appendChild(droppingElement);
			const mousePos = getSVGCoords(event);
			const boundBox = getElementPageBBox(droppingElement);
			const nx = mousePos.x - (boundBox.width / 2);
			const ny = mousePos.y - (boundBox.height / 2);
			droppingElement.setAttribute("transform", "translateX("+nx+"px) translateY("+ny+"px)")
			droppingElement.classList.remove("selected");
			
			palette.destroy();	
			const map = new Map();
			const uri = getElementUri(paletteElement, palettePanel);
			map.set(droppingElement, uri);
			dragger.beginAdd(map, event);
			dragger.grab(event);

			event.stopPropagation();			
		}
	
		insertableSelector(palettePanel).forEach(function(v) {
	    	v.removeEventListener("mousedown", handleDrag);
	    	v.addEventListener("mousedown", handleDrag);
	    	
	    	v.removeEventListener("touchstart", handleDrag);
	    	v.addEventListener("touchstart", handleDrag);
		})
	}
}




