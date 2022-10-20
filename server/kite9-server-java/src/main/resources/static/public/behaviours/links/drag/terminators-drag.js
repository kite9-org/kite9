/**
 * This handles moving a block from one place to another on the diagram, via drag and drop.
 * You can't drop into an element unless it has 
 */
import { parseInfo, isTerminator, getKite9Target, isConnected, isPort, isLink, getParentElement, getAffordances, connectedElementOtherEnd } from "/public/bundles/api.js";
import { getSVGCoords, getElementPageBBox, getMainSvg } from '/public/bundles/screen.js'
import { getBeforeId } from '/public/bundles/ordering.js'

/**
 * Keeps track of any links we've animated moving.
 */
var moveLinks = [];

const SVG_PATH_REGEX = /[MLQTCSAZ][^MLQTCSAZ]*/gi;

export function initTerminatorDropCallback(command) {
	
	return function(dragState, evt, dropTargets) {
		if (dropTargets.length == 1) {
			const dragTargets = dragState.map(s => s.dragTarget);
			Array.from(dragTargets).forEach(dt => {
				if (isTerminator(dt)) {
					const dropId = 
					command.push(  {
						type: 'ReplaceAttr',
						fragmentId: dt.getAttribute('id'),
						name: 'reference',
						to: dropTargets[0].getAttribute('id'),
						from: dt.getAttribute('reference')
					});
				}	
			});
			return true;
		} 
	}
}

export function initTerminatorDropLocatorFunction() {
	
	return function (dragTarget, dropTarget) {
		
		if (dropTarget == null) {
			return false;
		}
		
		if (dragTarget==dropTarget) {
			return false;
		}
				
		if (isTerminator(dragTarget)) {
			if (isLink(dropTarget)) {
				return false;
			}

			
			if (!getAffordances(dropTarget).includes("connect")) {
				return false;
			}
			
			const otherEnd = connectedElementOtherEnd(dragTarget, getMainSvg());
			
			if (otherEnd == dropTarget) {
				// linking to itself
				return false;
			}
			
			if (isPort(dropTarget)) {
				if (getParentElement(dropTarget) == dragTarget) {
					return false;
				}
			}
			
			if (isPort(otherEnd)) {
				if (getParentElement(otherEnd) == dropTarget) {
					return false;
				}
			}
		}


		return true;
		
		
	}
	
}


/**
 * This shows the user where links will go.
 */
export function initTerminatorMoveCallback() {

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
							path.oldPath = d;
						}
						
						path.setAttributeNS(null, 'pointer-events', 'none');
						
						const coords = getSVGCoords(evt);
						const commands = d.match(SVG_PATH_REGEX);
						var from, to;
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
				path.setAttribute("d", path.oldPath);
				delete path.oldPath;
			});
			
			moveLinks = [];
		}
	}	
}




