import { parseInfo, getContainingDiagram, hasLastSelected, getParentElement } from '/public/bundles/api.js'
import { getMainSvg, currentTarget } from '/public/bundles/screen.js'
import { getAlignElementsAndDirections, initLinkFinder } from '/public/behaviours/links/linkable.js'
import { icon } from '/public/bundles/form.js'
import { getElementUri} from '/public/classes/palette/palette.js';


function defaultLinkableSelector(palettePanel) {
	return palettePanel.querySelectorAll("[id][k9-palette~=link]");	
}

export function getLinkTemplateUri() {
	return document.params['link-template-uri'];
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
					linker.start(Array.from(elements), linkFinder(getLinkTemplateUri()));
				});
		}
	};
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
					type: "InsertUrlWithChanges",
					fragmentId: diagramId,
					uriStr: getLinkTemplateUri(),
					xpathToValue: {
						"*[local-name()='from']/@reference": fromId,
						"*[local-name()='to']/@reference": linkTargetId
					},
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

