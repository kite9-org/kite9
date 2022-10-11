import { hasLastSelected, isConnected, parseInfo } from '/public/bundles/api.js'
import { getSVGCoords, getElementPageBBox, getMainSvg } from '/public/bundles/screen.js'
import { drawBar, clearBar } from  '/public/bundles/ordering.js'
import { svg } from '/public/bundles/screen.js'

function getLayout(e) {
	if (e==null) {
		return 'none';
	} else {
		const info = parseInfo(e);
		const l = info['layout'];
		return ((l == 'null') || (l == undefined)) ? undefined : l.toLowerCase();
	}
}

function getLayoutImage(layout) {
	return "/public/behaviours/containers/layout/" + layout + ".svg";
}

function drawLayout(event, cm, layout, selected) {
	if (layout == undefined) {
		layout = "none";
	}
	
	var out = cm.addControl(event, getLayoutImage(layout),
			 "Layout (" + layout + ")",
			 undefined);
	
	var img = out.children[0];
	img.style.borderRadius = "0px";

	if (selected == layout) {
		img.setAttribute("class", "selected");
	}

	return img;
}

const LAYOUTS = [ "none", "right", "down", "horizontal", "vertical", "left", "up" ];


export function initContainerLayoutPropertyFormCallback() {
	
	return function(propertyOwner, contextEvent, contextMenu, selectedElements) {
		const ls = hasLastSelected(selectedElements, true);
		const layout = getLayout(ls);
	
		LAYOUTS.forEach(s => {
			var img2 = drawLayout(event, contextMenu, s, layout);
			if (layout != s) {
				img2.setAttribute("title", s);
				img2.addEventListener("click", (formEvent) => propertyOwner.setProperty(contextEvent, formEvent, contextMenu, selectedElements));
			} 
		});
	}	
}

export function initContainerLayoutPropertySetCallback(command) {
	
	return function(propertyOwner, contextEvent, formEvent, contextMenu, selectedElements) {
		
		const layout = formEvent.currentTarget.getAttribute("title");
		selectedElements.forEach(e => {
			
			const existing = getLayout(e);
			const id = e.getAttribute("id");
			
			if (LAYOUTS.includes(layout)) {
				command.push({
					fragmentId: id,
					type: 'ReplaceStyle',
					name: '--kite9-layout',
					to: layout == 'none' ? null : layout,
					from: existing
				});
			}
		});

	}
}

export function initLayoutContextMenuCallback(layoutProperty, selector) {
	
	if (selector == undefined) {
		selector = function () {
			return getMainSvg().querySelectorAll("[id][k9-ui~=layout].selected");
		}
	}
	
	/**
	 * Provides a layout option for the context menu
	 */
	return function (event, contextMenu) {

		const e = hasLastSelected(selector());

		if (e.length> 0) {
			const ls = hasLastSelected(e, true);
			const layout = getLayout(ls);
			var img = drawLayout(event, contextMenu, layout);

			img.addEventListener("click", formEvent => {
				contextMenu.clear();
				layoutProperty.populateForm(formEvent, contextMenu, e);
			});
		}
	};	
}

export function initContainerLayoutMoveCallback() {

	function updateBar(event, inside, horiz) {
		var fx, fy, tx, ty;

		var { x, y } = getSVGCoords(event);

		var contain = getElementPageBBox(inside);

		if (horiz) {
			fx = 0;
			tx = contain.width;
			fy = y - contain.y;
			ty = y - contain.y;
		} else {
			fx = x - contain.x;
			tx = x - contain.x;
			fy = 0;
			ty = contain.height;
		}
		drawBar(fx, fy, tx, ty, inside);
	}
	
	return function (dragTargets, event, dropTargets, barDirectionOverrideHoriz) {
		if (dropTargets) {
			var connectedDropTargets = dropTargets.filter(dt => isConnected(dt));
			var connectedDragTargets = dragTargets.filter(dt => isConnected(dt));
			
			if ((connectedDropTargets.length == 1) && (connectedDragTargets.length > 0)) {
				const dropInto = connectedDropTargets[0];
				const layout = getLayout(dropInto);
				if (barDirectionOverrideHoriz != undefined) {
					updateBar(event, dropInto, barDirectionOverrideHoriz);
					return;
				} else if ((layout == 'up') || (layout == 'down') || (layout == "vertical")) {
					// draw the horizontal bar
					updateBar(event, dropInto, true);
					return;
				} else if ((layout == 'left') || (layout == 'right') || (layout == 'horizontal')) {
					updateBar(event, dropInto, false);
					return;
				} 
			}
		}
		
		clearBar();

	}

}

const INDICATOR_SELECTOR = ":scope > g.k9-layout";


function removeLayoutIndicator(e) {
	var indicator = e.querySelector(INDICATOR_SELECTOR);
	if (indicator) {
		indicator.removeChild(existingIndicator);
	}
}

function ensureLayoutIndicator(e, layout) {		
	var indicator = e.querySelector(INDICATOR_SELECTOR);
	if ((indicator != null) && (indicator.getAttribute("layout")!=layout)) {
		e.removeChild(indicator);
	} else if (indicator != null) {
		return;
	} 
	
	indicator = svg('g', {
		'class' : 'k9-layout',
		'k9-highlight' : 'stroke',
		'layout' : layout,
	}, [ 
		svg('image', {
			x: 5,
			y: 5,
			width: 15,
			height: 15,
			href: getLayoutImage(layout)
		}) 
	]);
	
	e.appendChild(indicator)
}

export function initLayoutIndicator(selector) {
	
	
	if (selector == undefined) {
		selector = function() { 
			return getMainSvg().querySelectorAll("[id][k9-ui~=layout]"); 
		}
	}
	
	window.addEventListener('DOMContentLoaded', function() {
		selector().forEach(function(v) {
			const layout = getLayout(v);
						
			if (layout) {
				ensureLayoutIndicator(v, layout)
 			} else {
				removeLayoutIndicator(v);
			}
		})
	})
}

export function initLayoutIndicatorPaletteCallback(selector) {
	
	if (selector == undefined) {
		selector = function(e) {
	 		return e.querySelectorAll("[id][k9-ui~=layout]"); 
	 	}
	}
	
	return function(palettePanel) {
		if (palettePanel) {
			selector(palettePanel).forEach(function(v) {
				const layout = getLayout(v);
							
				if (layout) {
					ensureLayoutIndicator(v, layout)
	 			} else {
					removeLayoutIndicator(v);
				}
			})
		}
	}
}