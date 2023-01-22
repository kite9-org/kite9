/**
 * This handles moving a block from one place to another on the diagram, via drag and drop.
 * You can't drop into an element unless it has 
 */
import { parseInfo, isTerminator, isPort, isLink, isConnected, getParentElement, getAffordances, connectedElementOtherEnd, onlyUnique, connectedElement, getKite9Target } from "../../../bundles/api.js";
import { getSVGCoords, getMainSvg, currentTargets } from '../../../bundles/screen.js'
import { ElementFilter } from "../../../bundles/types.js";
import { Command } from "../../../classes/command/command.js";
import { Containment } from "../../../classes/containment/containment.js";
import { DropCallback, DropLocatorCallback, MoveCallback } from "../../../classes/dragger/dragger.js";

/**
 * Keeps track of any links we've animated moving.
 */
let moveLinks = [];

const SVG_PATH_REGEX = /[MLQTCSAZ][^MLQTCSAZ]*/gi;

export function initTerminatorDropCallback(
	command : Command,
	containment: Containment, 
	filter: ElementFilter = isTerminator) : DropCallback {
	
	return function(dragState, _evt, dropTargets) {
		const relevantState = dragState
			.filter(si => filter(si.dragTarget));
			
		const dragTargets = relevantState
			.map(s => s.dragTarget)

		const validDropTargets = dropTargets
			.map(t => {
				if (isTerminator(t)) {
					return connectedElement(t, getMainSvg());
				} else {
					return t;
				}
			})
			.filter(t => containment.canContainAll(dragTargets, t))
			.filter(onlyUnique);
			
		if (validDropTargets.length == 1) {
			Array.from(dragTargets).forEach(dt => {
				if (isTerminator(dt)) {
					command.push(  {
						type: 'ReplaceAttr',
						fragmentId: dt.getAttribute('id'),
						name: 'reference',
						to: dropTargets[0].getAttribute('id'),
						from: dt.getAttribute('reference')
					});
				}	
			});
		} 
	}
}

export function initTerminatorDropLocatorCallback(containment: Containment) : DropLocatorCallback {
	
	return function (dragTargets, e) {		
		if (isTerminator(dragTargets[0])) {
			const otherEnd = connectedElementOtherEnd(dragTargets[0], getMainSvg());

			const containerDropTargets = currentTargets(e)
				.map(t => getKite9Target(t))
				.filter(t => isConnected(t) || isPort(t))
				.filter(t => containment.canContainAll(dragTargets, t))
				.filter(t => t != otherEnd)
				.filter(t => !(isPort(otherEnd) && getParentElement(otherEnd) == t));
			
			if (containerDropTargets.length > 0) {
				return containerDropTargets[0];
			}
		}

		return null;
	}
}


/**
 * This shows the user where links will go.
 */
export function initTerminatorMoveCallback() : MoveCallback {

	return function(dragTargets, evt) {
		
		if (evt) {
			dragTargets.forEach(dt => {
				if (isTerminator(dt)) {
					const debug = parseInfo(dt);
					
					const id = debug.terminates;
					const linkElem = document.getElementById(id);
					
					const paths = linkElem.querySelectorAll("[k9-animate=link]");
					Array.from(paths).forEach(path => {
						
						const d = path.getAttribute("d");
						
						if (moveLinks.indexOf(path) == -1) {
							moveLinks.push(path);
							path['oldPath'] = d;
						}
						
						path.setAttributeNS(null, 'pointer-events', 'none');
						
						const coords = getSVGCoords(evt);
						const commands = d.match(SVG_PATH_REGEX);
						let from, to;
						if (debug.end == 'from') {
							from = 'M'+coords.x+" "+coords.y; 
							to = commands[commands.length-1];
						} else {
							to = 'L'+coords.x+" "+coords.y; 
							from = commands[0];
						}
						
						path.setAttribute("d", from+" "+to);			
					});
				}
			});
		} else {
			// no event means reset
			moveLinks.forEach(path => {
				path.setAttribute("d", path['oldPath']);
				delete path['oldPath'];
			});
			
			moveLinks = [];
		}
	}	
}




