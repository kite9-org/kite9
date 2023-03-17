import { isLink, isTerminator, isPort, getContainedChildIds, getParentElement, getDependentElements, connectedElement, onlyLastSelected, isConnected } from "../../../bundles/api.js";
import { getMainSvg, getElementHTMLBBox, getElementPageBBox, maxArea } from '../../../bundles/screen.js';
import { Selector } from "../../../bundles/types.js";
import { ContextMenu, ContextMenuCallback } from "../../../classes/context-menu/context-menu.js";

/**
 * If you select a terminator, allows you to select the link or port.
 * If you select a link, allows you to select the terminator
 * If you select a port, you can select the links.
 * If you select a connected, allows you to select links
 */
export function initLinksNavContextMenuCallback(
	singleSelect: (e: Element[], within?: Element) => void,
	selector: Selector = undefined): ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id].selected"))
		}
	}
	
	function selectElements(elements: Element[], cm: ContextMenu) {
		singleSelect(elements);
		const { x, y, width, height } = maxArea(elements.map(e => getElementHTMLBBox(e)));
		const event2 = new MouseEvent("mouseup", {
			clientX: x + width,
			clientY: y + height,
			bubbles: true,
			cancelable: true,
			view: window
		});

		cm.destroy();
		cm.handle(event2);
		elements.forEach(e => e.classList.remove("attention"));
	}

	function highlight(elements: Element[], active: boolean) {
		elements.forEach(element => {
			if (active) {
				element.classList.add("attention");
			} else {
				element.classList.remove("attention");
			}
		});
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
					() => selectElements([link], cm), 'Related',
					{
						"onmouseover": () => highlight([link], true),
						"onmouseout": () => highlight([link], false)
					});

				const portForElement = connectedElement(lastElement, getMainSvg())
				if (isPort(portForElement)) {
					cm.addControl(event, "/public/behaviours/links/nav/port.svg", 'Containing Port',
						() => selectElements([portForElement], cm), 'Related',
						{
							"onmouseover": () => highlight([portForElement], true),
							"onmouseout": () => highlight([portForElement], false)
						});
				}

			} else if (isLink(lastElement)) {
				const deps = getContainedChildIds(lastElement, e => isTerminator(e))
					.map(id => getMainSvg().getElementById(id))
					.sort(leftToRightSort);

				deps.forEach(element => {
					cm.addControl(event, "/public/behaviours/links/nav/ends.svg", 'Terminator',
						() => selectElements([element], cm), 'Related',
						{
							"onmouseover": () => highlight([element], true),
							"onmouseout": () => highlight([element], false)
						});
				})
			} else if (isPort(lastElement) || isConnected(lastElement)) {
				const deps = getDependentElements([lastElement.getAttribute("id")])
					.sort(leftToRightSort);

				deps.forEach(dep => {
					cm.addControl(event, "/public/behaviours/links/link.svg", 'Link',
						() => selectElements([dep], cm), 'Related',
						{
							"onmouseover": () => highlight([dep], true),
							"onmouseout": () => highlight([dep], false)
						});
				});
				
				if (deps.length > 1) {
					cm.addControl(event, "/public/behaviours/links/link.svg", 'Link',
						() => selectElements(deps, cm), 'Related',
						{
							"onmouseover": () => highlight(deps, true),
							"onmouseout": () => highlight(deps, false)
						});
					
				}
			} 
		}
	}
}


