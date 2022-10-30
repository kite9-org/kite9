import { isLink, isTerminator, isPort, getContainedChildren, getParentElement, getDependentElements, connectedElement } from "../../../bundles/api.js";
import { getMainSvg, getElementHTMLBBox, getElementPageBBox } from '../../../bundles/screen.js';
import { Selector } from "../../../bundles/types.js";
import { ContextMenuCallback } from "../../../classes/context-menu/context-menu.js";

/**
 * If you select a terminator, allows you to select the link or port.
 * If you select a link, allows you to select the terminator
 * If you select a port, you can select the links.
 */
export function initLinksNavContextMenuCallback(
	singleSelect = false, 
	selector: Selector = undefined) : ContextMenuCallback {
	
	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id].selected"))
		}
	}
	
	function selectElement(element, cm, event) {
		singleSelect(element);
		const { x, y, width, height } = getElementHTMLBBox(element);
		const event2 = new Event('mouseup');
		event2.pageX = x + (width / 2);
		event2.pageY = y + (height / 2);
		cm.destroy();
		cm.handle(event2);
		element.classList.remove("attention");		
	}
	
	function highlight(element, active) {
		if (active) {
			element.classList.add("attention");
		} else {
			element.classList.remove("attention");		
		}
	}
	
	function leftToRightSort(a, b) {
		const aPos = getElementPageBBox(a);
		const bPos = getElementPageBBox(b);
		return (aPos.x + (aPos.width / 2)) - (bPos.x + (bPos.width / 2));
	}

	return function(event, cm) {
		
		const lastElement = onlyLastSelected(selector());

		if (lastElement) {
			if (isTerminator(lastElement)) {				
				const link = getParentElement(lastElement)
				cm.addControl(event, "/public/behaviours/links/link.svg", 'Link', 
					() => selectElement(link, cm, event), 'Related', 
					{ 
						"onmouseover" : (e) => highlight(link, true), 
						"onmouseout" : (e) => highlight(link, false)
					});	
					
				
				const portForElement = connectedElement(lastElement, getMainSvg())
				if (isPort(portForElement)) {
					cm.addControl(event, "/public/behaviours/links/nav/port.svg", 'Containing Port', 
						() => selectElement(portForElement, cm, event), 'Related', 
						{ 
							"onmouseover" : (e) => highlight(portForElement, true), 
							"onmouseout" : (e) => highlight(portForElement, false)
						});		
				}		
							
			} else if (isLink(lastElement)) {
				const deps = getContainedChildren(lastElement, e => isTerminator(e))
					.map(id => getMainSvg().getElementById(id))
					.sort(leftToRightSort);
								
				deps.forEach(element => {
					cm.addControl(event, "/public/behaviours/links/nav/ends.svg", 'Link', 
						() => selectElement(element, cm, event), 'Related', 
						{ 
							"onmouseover" : (e) => highlight(element, true), 
							"onmouseout" : (e) => highlight(element, false)
						});	
				})
			} else if (isPort(lastElement)) {
				const deps = getDependentElements([lastElement.getAttribute("id")])
					.sort(leftToRightSort);

				deps.forEach(dep => {
					cm.addControl(event, "/public/behaviours/links/link.svg", 'Link To Port', 
						() => selectElement(dep, cm, event), 'Related', 
						{ 
							"onmouseover" : (e) => highlight(dep, true), 
							"onmouseout" : (e) => highlight(dep, false)
						});		
					
					
				});
			} 
		}
 	}
}


