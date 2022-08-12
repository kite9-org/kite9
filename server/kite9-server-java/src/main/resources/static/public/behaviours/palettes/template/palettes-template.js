import { hasLastSelected } from '/public/bundles/api.js'
import { getElementUri } from '/public/classes/palette/palette.js';


/**
 * Highlights default template items in the palettes.
 */
export function initPaletteUpdateDefaults(palette, linkFinder) {
	
	
	return function() {
		palette.get().querySelectorAll("[default]").forEach(e => e.removeAttribute("default"));
		const map = document.params;
	
		for(var k in map) {
			if (Object.prototype.hasOwnProperty.call(map, k)) {
				if (k.endsWith("-template-uri")) {
					const v = map[k];
					const elem = linkFinder(v);
					if (elem) {
						elem.setAttribute("default", "true");
					}
				}
			}
		}
	}
}


/**
 * Allows the user to click to make this the default element
 */
export function initSetDefaultContextMenuCallback(palette, paramName, description, linkFinder, selector) {
	
    /**
	 * Provides a contain option for the context menu
	 */
	return function(event, contextMenu) {
		const palettePanel = palette.getOpenPanel();		
	    const lastSelectedElement = hasLastSelected(selector(palettePanel), true);
		
		if (lastSelectedElement) {
			const id = getElementUri(lastSelectedElement, palettePanel);
			const currentSelectionId = document.params[paramName];
			const alreadySelected = linkFinder(currentSelectionId);
			const active = lastSelectedElement == alreadySelected;
			const title = active ? "Default "+description : "Make default "+description;
			const ctrl = contextMenu.addControl(event, "/public/behaviours/palettes/template/linkmenu.svg", title,
				function(e2, selector) {
					contextMenu.destroy();
					document.params[paramName] = id;
					initPaletteUpdateDefaults(palette, linkFinder);
					event.stopPropagation();
					palette.destroy();
				});
					
			var img = ctrl.children[0];
			
			if (active) {
				img.setAttribute("class", "selected");
			}			
		}
	}
}






export function initLinkPaletteCallback(selector) {
	
	if (selector == undefined) {
		selector = defaultLinkableSelector;
	}
	
	
	return function(palette, palettePanel) {
		
		function click(elem, event) {
			if (palette.getCurrentAction() == 'link') {
				templateUri = getElementUri(elem, palettePanel);
				palette.destroy();		
				event.stopPropagation();
			}
		}
	
		selector(palettePanel).forEach(function(v) {
	    	v.removeEventListener("click", (e) => click(v, e));
	    	v.addEventListener("click", (e) => click(v, e));
	    
	    	if (templateUri == undefined) {
	    		var id = v.getAttribute("id");
	    		templateUri = getElementUri(v);
	    	}
		})
		
	}
}


export function initLinkInstrumentationCallback(palette) {
	
	return function(nav) {
		const name = 'linkmenu';
		const allowedTypes = [ 'link' ];
		var b =  nav.querySelector("_link");
		if (b == undefined) {
			nav.appendChild(icon('_link', "Link Style", 
					'/public/behaviours/links/link/linkmenu.svg',
						(evt) => palette.open(evt, 
						(e) => {
							const p = e.getAttribute("k9-palette");
							return p == null ? false : p.split(" ").includes("link");	
						},
						"link")));
		}
	}	
}