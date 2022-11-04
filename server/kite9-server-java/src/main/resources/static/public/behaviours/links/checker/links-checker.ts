import { getMainSvg } from '../../../bundles/screen.js'
import { parseInfo, getParentElement, getNextSiblingId, getContainedChildIds } from '../../../bundles/api.js'
import { DropCallback } from '../../../classes/dragger/dragger.js';
import { Command } from '../../../classes/command/command.js';
import { Selector } from '../../../bundles/types.js';

/**
 * This is added to drag-and-drop to make sure that any time we move an object
 * that we check it's links.  Specifically, you can't have a link to a parent object.
 */
export function initLinksCheckerDropCallback(
	command: Command, 
	selector: Selector = undefined) : DropCallback {
	
	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-info~='link:']"));
		}
	}
	
	return function(dragState, evt, dropTargets) {
		let movingIds = [];
		dragState.forEach(s => {
			const c = getContainedChildIds(s.dragTarget);
			movingIds = movingIds.concat(c);
			movingIds.push(s.dragTarget.getAttribute("id"));
		});
		
		const droppingIds = [];
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
			
				const id = link.getAttribute("id");
				const parent = getParentElement(link);
				const parentId = parent == undefined ? undefined : parent.getAttribute("id");
				
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