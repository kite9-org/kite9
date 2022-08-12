




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