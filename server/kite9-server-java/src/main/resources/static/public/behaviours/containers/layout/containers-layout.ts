import { hasLastSelected, onlyLastSelected, isConnected, parseInfo } from '../../../bundles/api.js'
import { getSVGCoords, getElementPageBBox, getMainSvg } from '../../../bundles/screen.js'
import { drawBar, clearBar } from '../../../bundles/ordering.js'
import { svg } from '../../../bundles/screen.js'
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { FormCallback, Property, SetCallback } from '../../../classes/context-menu/property.js';
import { Command } from '../../../classes/command/command.js';
import { PaletteSelector, Selector } from '../../../bundles/types.js';
import { MoveCallback } from '../../../classes/dragger/dragger.js';
import { Palette, PaletteCallback } from '../../../classes/palette/palette.js';

function getLayout(e: Element) {
	if (e == null) {
		return 'none';
	} else {
		const info = parseInfo(e);
		const l = info['layout'];
		return ((l == 'null') || (l == undefined)) ? undefined : l.toLowerCase();
	}
}

function getLayoutImage(layout: string) {
	return "/public/behaviours/containers/layout/" + layout + ".svg";
}

function drawLayout(event: Event, cm: ContextMenu, layout : string, selected = undefined) {
	if (layout == undefined) {
		layout = "none";
	}

	const out = cm.addControl(event, getLayoutImage(layout),
		"Layout (" + layout + ")",
		undefined);

	const img = out.children[0] as HTMLImageElement;
	img.style.borderRadius = "0px";

	if (selected == layout) {
		img.setAttribute("class", "selected");
	}

	return img;
}

const LAYOUTS = ["none", "right", "down", "horizontal", "vertical", "left", "up"];


export function initContainerLayoutPropertyFormCallback() : FormCallback {

	return function(propertyOwner: Property, contextEvent: Event, contextMenu: ContextMenu, selectedElements: Element[]) {
		const ls = onlyLastSelected(selectedElements);
		const layout = getLayout(ls);

		LAYOUTS.forEach(s => {
			const img2 = drawLayout(contextEvent, contextMenu, s, layout);
			if (layout != s) {
				img2.setAttribute("title", s);
				img2.addEventListener("click", (formEvent) => propertyOwner.setProperty(contextEvent, formEvent, contextMenu, selectedElements));
			}
		});
	}
}

export function initContainerLayoutPropertySetCallback(command: Command) : SetCallback {

	return function(propertyOwner: Property, contextEvent: Event, formEvent: Event, contextMenu: ContextMenu, selectedElements: Element[]) {

		const layout = (formEvent.currentTarget as Element).getAttribute("title");
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

export function initLayoutContextMenuCallback(layoutProperty: Property, selector: Selector) : ContextMenuCallback {

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=layout].selected"));
		}
	}

	/**
	 * Provides a layout option for the context menu
	 */
	return function(event: Event, contextMenu: ContextMenu) {

		const e = hasLastSelected(selector());

		if (e.length > 0) {
			const ls = onlyLastSelected(e);
			const layout = getLayout(ls);
			const img = drawLayout(event, contextMenu, layout);

			img.addEventListener("click", formEvent => {
				contextMenu.clear();
				layoutProperty.populateForm(formEvent, contextMenu, e);
			});
		}
	};
}

export function initContainerLayoutMoveCallback() : MoveCallback {

	function updateBar(event: Event, inside: Element, horiz: boolean) {
		let fx: number, fy: number, tx: number, ty: number;

		const { x, y } = getSVGCoords(event);

		const contain = getElementPageBBox(inside);

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

	return function(dragTargets: Element[], event: Event, dropTargets: Element[]) {
		if (dragTargets.filter(dt => isConnected(dt)).length == 0) {
			// not dragging a connected, so we don't need a layout indicator
			return;
		}

		if (dropTargets) {
			const connectedDropTargets = dropTargets.filter(dt => isConnected(dt));

			if ((connectedDropTargets.length == 1)) {
				const dropInto = connectedDropTargets[0];
				const layout = getLayout(dropInto);
				if ((layout == 'up') || (layout == 'down') || (layout == "vertical")) {
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


function removeLayoutIndicator(e: Element) {
	const indicator = e.querySelector(INDICATOR_SELECTOR);
	if (indicator) {
		e.removeChild(indicator);
	}
}

function ensureLayoutIndicator(e: Element, layout: string) {
	let indicator = e.querySelector(INDICATOR_SELECTOR);
	if ((indicator != null) && (indicator.getAttribute("layout") != layout)) {
		e.removeChild(indicator);
	} else if (indicator != null) {
		return;
	}

	indicator = svg('g', {
		'class': 'k9-layout',
		'k9-highlight': 'stroke',
		'layout': layout,
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

export function initLayoutIndicator(selector: Selector) {


	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=layout]"));
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

export function initLayoutIndicatorPaletteCallback(selector: PaletteSelector) : PaletteCallback {

	if (selector == undefined) {
		selector = function(e) {
			return Array.from(e.querySelectorAll("[id][k9-ui~=layout]"));
		}
	}

	return function(p: Palette, palettePanel: Element) {
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