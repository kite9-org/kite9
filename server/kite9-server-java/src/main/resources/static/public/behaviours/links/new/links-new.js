import { hasLastSelected, getKite9Target, createUniqueId, changeId } from '/public/bundles/api.js'
import { getMainSvg, getSVGCoords, getElementPageBBox, currentTarget } from '/public/bundles/screen.js'

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
		
		function getElementUri(e) {
			var paletteId = palettePanel.getAttribute("id");
			var id = e.getAttribute("id");
			return palettePanel.getAttribute("k9-palette-uri")+"?format=adl#"+id.substring(0, id.length - paletteId.length);	
		}
		
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
			const uri = getElementUri(paletteElement);
			map.set(droppingElement, uri);
			dragger.beginAdd(map, event);
			dragger.grab(event);

			event.stopPropagation();			
		}
	
		/**
		 * If the user holds the mouse down for 1/2 a second, we activate drag mode
		 */
		function mouseDown(event) {
			if (palette.getCurrentAction() == 'new-link') {
				handleDrag(event);
			}
		}
	
		insertableSelector(palettePanel).forEach(function(v) {
	    	v.removeEventListener("mousedown", mouseDown);
	    	v.addEventListener("mousedown", mouseDown);
	    	
	    	v.removeEventListener("touchstart", mouseDown);
	    	v.addEventListener("touchstart", mouseDown);
		})
	}
}
	
/**
 * Adds insert option into context menu
 */
export function initNewLinkContextMenuCallback(palette, containment, selector) {
	
	
	if (selector == undefined) {
		selector = defaultInsertSelector;
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const selectedElements = hasLastSelected(selector());
		
		if (selectedElements.length > 0) {
			contextMenu.addControl(event, "/github/kite9-org/kite9/client/behaviours/links/new/new-link.svg", "Linked Insert",
				function(e2, selector) {
					contextMenu.destroy();
					// primes this based on the screen.
					currentTarget(event);
					palette.open(
						 event, 
						 (e) => containment.canContain(e, Array.from(selectedElements)),
						"new-link");
				});
		}
	}
}



