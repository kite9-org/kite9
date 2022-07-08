import { getContainingDiagram, createUniqueId, getExistingConnections, parseInfo, hasLastSelected, reverseDirection } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.2'
import { getMainSvg, getElementPageBBox } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.2'


export function initAlignContextMenuCallback(command, templateUri, selector) {
	
	
	
	/**
	 * Aligns the two elements
	 */
	function createAlignStep(from, to, direction, steps ,linkId) {
		
		const conns = getExistingConnections(from.getAttribute("id"), to.getAttribute("id"));
		var toUseId = null;
		var existingDirection;
		
		// tidy up any existing connections between these elements.
		conns.forEach(c => {
			const alignOnly = c.classList.contains("kite9-align");
			const id = c.getAttribute("id");
			
			if (alignOnly) {
				// remove the old alignment
				steps.push({
					type: 'Delete',
					fragmentId: id,
					base64Element: command.getAdl(id)
				});
			} else {
				const debug = parseInfo(c);
				existingDirection = debug.direction;

				if (existingDirection != 'null') {
					steps.push({
						fragmentId: c.getAttribute("id"),
						type: 'ReplaceAttr',
						name: 'drawDirection',
						from: existingDirection,
						to: null
					})	
				}
				
				if (toUseId == null) {
					toUseId = c.getAttribute("id");
					// check to see if we need to reverse the align
					const parsed = parseInfo(c);
					const ids = parsed['link'];
					const reversed = ids[0] == to.getAttribute("id");
					direction = reversed ? reverseDirection(direction) : direction;
				}
			}
		})
		
		if (toUseId == null) {
			// create an align element
			steps.push({
				fragmentId: getContainingDiagram(from).getAttribute("id"),
				type: 'InsertUrlLink',
				newId: linkId,
				fromId: from.getAttribute("id"),
				toId: to.getAttribute("id"),
				uriStr: templateUri,
				deep: true
			});
		} else {
			linkId = toUseId;
		}
				
		steps.push({
			fragmentId: linkId,
			type: 'ReplaceAttr',
			name: 'drawDirection',
			from: existingDirection,
			to: direction
		});
		
		return linkId;
	}

	function performAlign(cm, horiz) {
		var selectedElements = Array.from(selector());
		
		selectedElements.sort((a, b) => {
			var apos = getElementPageBBox(a);
			var bpos = getElementPageBBox(b);
			
			if (horiz) {
				var d = (apos.x + (apos.width / 2)) - ( bpos.x + (bpos.width / 2))
			} else {
				var d = (apos.y + (apos.height / 2)) - ( bpos.y + (bpos.height / 2))
			}
			
			return d;
		});
		
		var steps = [];
		const linkId = createUniqueId();
		
		for (var i = 0; i < selectedElements.length-1; i++) {
			var from = selectedElements[i];
			var to = selectedElements[i+1];
			createAlignStep(from, to, horiz ? "RIGHT" : "DOWN", steps, linkId+"-"+i);
		}
		
		cm.destroy();
		command.pushAllAndPerform(steps);
	}
		
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui~='align'].selected");
		}
	}
	
	if (templateUri == undefined) {
		templateUri = document.params['align-template-uri'];
	}
	
	/**
	 * Provides an align option for the context menu
	 */
	return function(event, cm) {
		
		const e = hasLastSelected(selector());
		
		if (e.length > 1) {
			cm.addControl(event, "/public/behaviours/links/align/align-horiz.svg", "Horizontal Align",() => performAlign(cm, true));
			cm.addControl(event, "/public/behaviours/links/align/align-vert.svg", "Vertical Align",() => performAlign(cm, false));
		}
	}
}


