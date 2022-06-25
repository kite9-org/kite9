import { parseInfo, getContainingDiagram, hasLastSelected, getParentElement } from '/public/bundles/api.js';
import { getMainSvg, currentTarget } from '/public/bundles/screen.js';
import { getAlignElementsAndDirections } from '/public/behaviours/links/linkable.js'
import { icon } from '/public/bundles/form.js';

/** 
 * Keeps track of the URI of the element we are using for new links 
 */
var templateUri = document.params['link-template-uri'];

function defaultLinkableSelector(palettePanel) {
	return palettePanel.querySelectorAll("[id][k9-palette~=link]");	
}

export function getLinkTemplateUri() {
	return templateUri;
}

export function initLinkPaletteCallback(selector) {
	
	if (selector == undefined) {
		selector = defaultLinkableSelector;
	}
	
	
	return function(palette, palettePanel) {

		function getElementUri(e) {
			var paletteId = palettePanel.getAttribute("id");
			var id = e.getAttribute("id");
			return palettePanel.getAttribute("k9-palette-uri")+"?format=adl#"+id.substring(0, id.length - paletteId.length);	
		}
		
		function click(elem, event) {
			if (palette.getCurrentAction() == 'link') {
				templateUri = getElementUri(elem);
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


/**
 * Given a URI, returns the element itself, which we can use as the template
 */
export function initLinkFinder() {
	
	return function(uri) {
		const options = Array.from(document.querySelectorAll("div.palette-item"))
			.filter(pDiv => uri.startsWith(pDiv.getAttribute("k9-palette-uri")))
			.map(pDiv => {
				const paletteId = pDiv.getAttribute("id");
				const elementId = uri.substr(uri.lastIndexOf("#")+1) + paletteId;
				return pDiv.querySelector('#'+elementId);
			});
		
		return options[0];
	}
}


export function initLinkContextMenuCallback(command, linker, selector, linkFinder) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui~='connect'].selected");
		}
	}
	
	if (linkFinder == undefined) {
		linkFinder = initLinkFinder();
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const elements = hasLastSelected(selector());
		
		if (elements.length > 0) {
			contextMenu.addControl(event, "/public/behaviours/links/link/link.svg",
				"Draw Link", e => {
					contextMenu.destroy();
					linker.start(Array.from(elements), linkFinder(templateUri));
				});
		}
	};
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

/**
 * This is called when the user finishes doing a link operation, 
 * which will end up creating the link.
 */
export function initLinkLinkerCallback(command) {
	
	return function(linker, evt, perform=true) {
		const linkTarget = linker.getLinkTarget(currentTarget(evt));
		
		if (linkTarget == null) {
			linker.removeDrawingLinks();
		} else {
			linker.move(evt);
			const diagramId = getContainingDiagram(linkTarget).getAttribute("id");
			const linkTargetId = linkTarget.getAttribute("id");
			linker.get().forEach(e => {
				var fromId = e.getAttribute("temp-from");
				var aligns = getAlignElementsAndDirections(fromId, linkTargetId);
				var linkId = e.getAttribute("id");
				
				command.push({
					type: "InsertUrlLink",
					fragmentId: diagramId,
					uriStr: templateUri,
					fromId: fromId,
					toId: linkTargetId,
					newId: linkId
				});
				
				/*
				 * If there is an align element, remove it and set the draw direction.
				 */
				
				if (aligns.length == 1) {
					var { element, direction } = aligns[0];
					const id = element.getAttribute("id");
					command.push({
						type: 'Delete',
						fragmentId: getParentElement(element).getAttribute('id'),
						base64Element: command.getAdl(id)
					});
					command.push({
						type: 'ReplaceAttr',
						fragmentId: linkId,
						name: 'drawDirection',
						to: direction
					})
				}
				
			});
			
      if (perform) {
			  command.perform();
      }
			linker.clear();
		}
	};
	
}

