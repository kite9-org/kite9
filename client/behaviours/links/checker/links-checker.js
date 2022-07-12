import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.9'
import { parseInfo, getParentElement, getNextSiblingId, getContainedChildren } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.9'

/**
 * This is added to drag-and-drop to make sure that any time we move an object
 * that we check it's links.  Specifically, you can't have a link to a parent object.
 */
export function initLinksCheckerDropCallback(command, selector) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-info~='link:']");
		}
	}
	
	return function(dragState, evt, dropTargets) {
		var movingIds = [];
		dragState.forEach(s => {
			const c = getContainedChildren(s.dragTarget);
			movingIds = movingIds.concat(c);
			movingIds.push(s.dragTarget.getAttribute("id"));
		});
		
		var droppingIds = [];
		dropTargets.forEach(d => {
			do {
				droppingIds.push(d.getAttribute("id"));
				d = getParentElement(d);
			} while (d != undefined);
		});
				
		Array.from(selector()).forEach(link => {
			const info = parseInfo(link);
			const linkEnds = info['link'];
			
			const from = linkEnds[0];
			const to = linkEnds[1];

			if ((movingIds.includes(from) && droppingIds.includes(to)) ||
				(movingIds.includes(to) && droppingIds.includes(from))) {
			
				var id = link.getAttribute("id");
				var parent = getParentElement(link);
				var parentId = parent == undefined ? undefined : parent.getAttribute("id");
				
				command.push({
					fragmentId: parentId,
					type: 'Delete',
					base64Element: command.getAdl(id),
					beforeId: getNextSiblingId(link) 
				});
			}
		});
	}
	
}