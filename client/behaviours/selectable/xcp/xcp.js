import { hasLastSelected, encodeADLElement, getParentElement, getNextSiblingId, createUniqueId } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.7'
import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.7'
import { getBeforeId } from '/github/kite9-org/kite9/client/bundles/ordering.js?v=v0.7'

/**
 * Handles cut, copy, paste.
 * 
 * We put the following in the cut-buffer:
 * - adl xml
 * - element ids
 * - k9-palette info.
 * 
 * NB:  have given up on using the browser clipboard support as it didn't work very well.
 * 
 */
export function initXCPContextMenuCallback(command, metadata, containment, cutSelector, copySelector, pasteSelector) {
	
	var clipboard = [];
	
	if (cutSelector == undefined) {
		cutSelector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui~='delete'].selected")
		}
	}
	
	if (copySelector == undefined) {
		copySelector = function() {
			return getMainSvg().querySelectorAll("[id].selected")
		}
	}
	
	if (pasteSelector == undefined) {
		pasteSelector = function(buffer) {
			const elements = buffer.flatMap(e => e.element);
			const parents = Array.from(getMainSvg().querySelectorAll("[id][k9-info].selected"));
			if (parents.length > 0) {
				return containment.allowed(elements, parents);
			} else {
				return [];
			}
		}
	}
	
	function getElementClipInfo(e, adl) {
		var id = e.getAttribute("id");
		return {
			id: id,
			element: e,
			adl: adl
		};
	}
		
	function performCopy(copy, cut) {
		var steps = [];
		var pasteData = [];
		copy.forEach(e => {
			const id = e.getAttribute("id");
			const adl = command.getAdl(id);
			
			if (cut.indexOf(e) > -1) {
				steps.push({
					fragmentId: getParentElement(e).getAttribute('id'), 
					beforeId: getNextSiblingId(e),
					type: 'Delete',
					base64Element: adl
				});
			}
			
			pasteData.push(getElementClipInfo(e, adl));
		})
		
		clipboard = pasteData;
		
		if (steps.length > 0) {
			command.pushAllAndPerform(steps);
		}
	}
	
	function performPaste(event, destinations) {
		var steps = []
		clipboard.forEach(e =>  {
			destinations.forEach(d => {
				if (containment.canContain(e.element, d)) {
					const beforeId = getBeforeId(d, event, []);
					steps.push({
						"type": 'InsertXML',
						"fragmentId": d.getAttribute('id'),
						"base64Element": e.adl,
						"beforeId" : beforeId,
						"newId": createUniqueId()
					});
				}
			});
		});
				
		if (steps.length > 0) {
			command.pushAllAndPerform(steps);
		}
	}
	

	function onCut() {
		performCopy(Array.from(copySelector()), Array.from(cutSelector()));
	}
	
	function onCopy() {
		performCopy(Array.from(copySelector()), []);
	}

	function onPaste() {
		performPaste(undefined, Array.from(copySelector()));
	}

	document.removeEventListener("cut", onCut);
	document.removeEventListener("copy", onCopy);
	document.removeEventListener("paste", onPaste);

	document.addEventListener("cut", onCut);
	document.addEventListener("copy", onCopy);
	document.addEventListener("paste", onPaste);

		
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const copyElements = hasLastSelected(copySelector());
		const cutElements = hasLastSelected(cutSelector());
		const pasteElements = pasteSelector(clipboard);
		
		if (copyElements.length > 0) {
		
			if (cutElements.length > 0) {
				contextMenu.addControl(event, "/public/behaviours/selectable/xcp/cut.svg", "Cut", (e2, selector) => {
					contextMenu.destroy();
					performCopy(Array.from(copyElements), Array.from(cutElements));
				});
			}
			
			contextMenu.addControl(event, "/public/behaviours/selectable/xcp/copy.svg", "Copy", (e2, selector) => {
				contextMenu.destroy();
				performCopy(Array.from(copyElements), []);
			});
		}
		
		if (pasteElements.length > 0) {
			contextMenu.addControl(event, "/public/behaviours/selectable/xcp/paste.svg", "Paste", (e2, selector) => {
				contextMenu.destroy();
				performPaste(event, Array.from(copyElements));
			});
		}		
			
	}


}

