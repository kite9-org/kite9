import { hasLastSelected, parseInfo, isLink, isTerminator, isPort, getContainedChildren, getParentElement, getDependentElements, connectedPort } from "/public/bundles/api.js";
import { getMainSvg, getElementHTMLBBox, getElementPageBBox } from '/public/bundles/screen.js';

/**
 * If you select a terminator, allows you to select the link or port.
 * If you select a link, allows you to select the terminator
 * If you select a port, you can select the links.
 */
export function initLinksNavContextMenuCallback(singleSelect, selector) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id].selected")
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
		
		const lastElement = hasLastSelected(selector(), true);

		if (lastElement) {
			if (isTerminator(lastElement)) {				
				const link = getParentElement(lastElement)
				cm.addControl(event, "/public/behaviours/links/link.svg", 'Link', 
					() => selectElement(link, cm, event), 'Related', 
					{ 
						"onmouseover" : (e) => highlight(link, true), 
						"onmouseout" : (e) => highlight(link, false)
					});	
					
				
				const portForElement = connectedPort(lastElement, getMainSvg())
				if (portForElement) {
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


