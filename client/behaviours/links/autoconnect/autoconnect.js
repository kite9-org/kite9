import { getMainSvg, getElementPageBBox, currentTarget } from '/github/kite9-org/kite9/bundles/screen.js?v=v0.2'
import { parseInfo, createUniqueId, getContainingDiagram, reverseDirection, getExistingConnections, getKite9Target, getCommonContainer, isLink, getNextSiblingId } from '/github/kite9-org/kite9/bundles/api.js?v=v0.2'

var link = null;
var link_to = undefined;
var link_d = undefined;
var draggingElement = undefined;
var templateUri = undefined;


export function initAutoConnectTemplateSelector(alignTemplateUriCallback, linkTemplateUriCallback) {

	return function(element) {
		const alignLink = (element != null) && (!element.hasAttribute("autoconnect"));
		return alignLink ? alignTemplateUriCallback() : linkTemplateUriCallback();
	}
}

export function initAutoConnectLinkerCallback(command) {
	
	function undoAlignment(command, e) {
		const alignOnly = e.classList.contains("kite9-align");
		const id =  e.getAttribute("id");
		if (alignOnly) {
			command.push({
				type: 'Delete',
				fragmentId: id,
				base64Element: command.getAdl(id)
			});
		} else {
			command.push({
				type: 'ReplaceAttr',
				fragmentId: id,
				name: 'direction',
				from: e.getAttribute('direction')
			})
			
			return true;
		}		
	}
	
	function ensureNoDirectedLeavers(id, d1) {
		getExistingConnections(id).forEach(e => {
			const parsed = parseInfo(e);
			const d = parsed['direction'];
			const ids = parsed['link'];
			const reversed = ids[0] == id;
			const dUse = reversed ? reverseDirection(d1) : d1;
			
			if (d==dUse) {
				undoAlignment(command, e);
			} 
		});
	}
		
	return function(linker, event) {		
		if (link_to) {
			// create links between the selected object and the link_to one
			var id_from = draggingElement.getAttribute("id");
			var id_to = link_to.getAttribute("id");
			var existingLinks = getExistingConnections(id_from, id_to);

			ensureNoDirectedLeavers(id_from, link_d);
			const diagramId = getContainingDiagram(link_to).getAttribute("id");
			
			existingLinks = existingLinks.filter(e => undoAlignment(command, e));
			
			if (existingLinks.length == 0) {
				// create a new link
				const linkId = createUniqueId();
				command.push({
					fragmentId: diagramId,
					type: 'InsertUrlLink',
					newId: linkId,
					fromId: id_to, 
					toId: id_from,
					uriStr: templateUri,	
				});
				
				command.push({
					fragmentId: linkId,
					type: 'ReplaceAttr',
					name: 'drawDirection',
					to: link_d,
				});
			} else {
				const firstLink = existingLinks[0];
				const parsed = parseInfo(firstLink);
				const ids = parsed['link'];
				const reversed = ids[0] == id_to;
				const direction = reversed ? link_d : reverseDirection(link_d);
				command.push({
					fragmentId: firstLink.getAttribute("id"),
					type: 'ReplaceAttr',
					name: 'drawDirection',
					to: direction,
					from: firstLink.getAttribute('drawDirection')
				});
				
				// moves it to the last in the list
				command.push({
					type: 'Move',
					from: diagramId,
					fromBefore: getNextSiblingId(firstLink),
					to: diagramId,
					moveId: firstLink.getAttribute("id"),
				})
			}	
			
			linker.clear();
			link = null;
			link_to = null;
		}
	}
}

export function initAutoConnectMoveCallback(linker, linkFinder, linkTemplateSelector, selector, autoConnectWith) {
	
	var maxDistance = 100;
	var width, height;
	
	function clearLink() {
		linker.removeDrawingLinks();
		link = null;
	}

	function updateLink(topos, frompos, link_d, e) {
	    var fx, fy, tx, ty;
	    const mx = topos.x + topos.width / 2;
	    const my = topos.y + topos.height / 2;
	    
	    if (link_d == 'LEFT') {
	        fy = my;
	        ty = my;
	        fx = frompos.x;
	        tx = topos.x + topos.width;
	    } else if (link_d == 'RIGHT') {
	        fy = my;
	        ty = my;
	        fx = frompos.x + frompos.width;  
	        tx = topos.x;
	    } else if (link_d == 'UP') {
	    	fx = mx;
	    	tx = mx;
	    	fy = frompos.y;
	    	ty = topos.y + topos.height;
	    } else {
	    	fx = mx;
	    	tx = mx;
	    	fy = frompos.y + frompos.height;
	    	ty = topos.y;
	    }
	   
	    
	    
		if (link == null) {
			var representation = linkFinder(templateUri);
			if (representation) {
				linker.start([e], representation);
				link = linker.get()[0];
			}
		}
		
		if (link != null) {
			linker.moveCoords(fx,fy,tx,ty);
		}
	}
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui~='autoconnect']");
		}
	}
	
	if (autoConnectWith == undefined) {
		autoConnectWith = function(moving, inside, linkTo) {
			
			if (moving) {
				var ui = moving.getAttribute("k9-ui");
				ui == undefined ? "" : ui;
				
				if (!ui.includes("autoconnect")) {
					
					// ok, we can try for a child to autoconnect to
					var options = moving.querySelectorAll("[id][k9-ui~='autoconnect']");
					if (options.length > 0) {
						moving = options[0];
					} else {
						return null;
					}
					
				}
			}
			
			if (inside) {
				// check that we are allowed to auto-connect inside
				const target = getKite9Target(inside);
				const info = parseInfo(target);
				const layout = info.layout;
				if ((layout != null) && (layout != 'null')) {
					return null;
				}
				
				
				if (linkTo) {
					const commonContainer = getCommonContainer(inside, linkTo);
					const commonInfo = parseInfo(commonContainer);
					const commonLayout = commonInfo.layout;
					if ((commonLayout != null) && (commonLayout != 'null')) {
						return null;
					}
				}
			}
		
			return moving;
		}
	}
	
	function getElementsInAxis(coords, horiz) {
		
		const out = Array.from(selector())
			.filter(e => {
				var {x, y, width, height} = getElementPageBBox(e);
				
				if (!horiz) {
					return ((y <= coords) && (y+height >= coords));
				} else {
					return ((x <= coords) && (x+width >= coords));
				}
			});
		
		return out;
	}
	
    /**
	 * This function looks for stuff to connect to and shows links on screen to demonstrate this
	 */
	return function(dragTargets, event, dropTargets) {
		
		function alreadyDragging(e) {
			if (dragTargets.indexOf(e) != -1) {
				return true;
			} 
			
			if (e.parentNode == null) {
				return false;
			} else {
				return alreadyDragging(e.parentNode);
			}			
		}
		
		function outside(a, b) {
			return ((a.x + a.width < b.x) 
					|| (a.x > b.x + b.width)
					|| (a.y + a.height < b.y) 
					|| (a.y > b.y + b.height));
		}
		
		
		var cancelEarly = (dropTargets == undefined) || (dragTargets.length > 1)
			|| (dropTargets.filter(dt => isLink(dt)).length > 0);
			
		if (!cancelEarly) {
			templateUri = linkTemplateSelector(dragTargets[0]);
			draggingElement = autoConnectWith(dragTargets[0], currentTarget(event));
			cancelEarly = draggingElement == null;
		}
		
		if (cancelEarly) {
			clearLink();
			link_to = undefined;
			return;
		}

		var pos = getElementPageBBox(draggingElement);
		
		var x = pos.x + (pos.width / 2);
		var y = pos.y + (pos.height /2);

		var best = undefined;
		var best_dist = undefined;
		var best_d  = undefined;
		
		getElementsInAxis(y, false).forEach(function(k, c) {
			if (!alreadyDragging(k)) {
				var v = getElementPageBBox(k);
				
				if (outside(pos, v) && (y <= v.y + v.height) && (y >= v.y)) {
					// intersection on y position
					var d, dist;
					if (v.x + v.width < x) {
						dist = pos.x - v.x - v.width;
						d = 'RIGHT';
					} else if (v.x > x) {
						dist = v.x - pos.x - pos.width;
						d = 'LEFT';
					} else {
						dist = maxDistance +1;
						d = null;
					}
								
					if (best_dist) {
						if (dist > best_dist) {
							return;
						}
					}
						
					best = k;
					best_dist = dist;
					best_d = d;
				}
			}
		});
			
		getElementsInAxis(x, true).forEach(function(k, c) {
			if (!alreadyDragging(k)) {
				var v = getElementPageBBox(k);

				if (outside(pos, v) && (x <= v.x+v.width) && (x >= v.x)) {
					// intersection on x position
					var d, dist;
					if (v.y + v.height < y) {
						dist = pos.y - v.y - v.height;
						d = 'DOWN';
					} else if (v.y > y) {
						dist = v.y - pos.y - pos.height;
						d = 'UP';
					} else {
						dist = maxDistance +1;
						d = null;
					}
					
					if (best_dist) {
						if (dist > best_dist) {
							return;
						}
					}
						
						
					best = k;
					best_dist = dist;
					best_d = d;
				}
			}
		});

		
		if (best_dist > maxDistance){
			best = undefined;
		}
				
		if (best === undefined) {
			clearLink();
			link_to = undefined;
		} else if (autoConnectWith(draggingElement, currentTarget(event), best) == null) {
			clearLink();
			link_to = undefined;
		} else if (best === link_to) {
			link_d = best_d;
			updateLink(pos, getElementPageBBox(best), link_d, draggingElement);	
		} else {
			clearLink();
			link_to = best;
			link_d = best_d;
			updateLink(pos, getElementPageBBox(best), link_d, draggingElement);
		}
	}

}
