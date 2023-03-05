import { getMainSvg } from '../../bundles/screen.js'
import { getExistingConnections, parseInfo } from '../../bundles/api.js'
import { Linker } from '../../classes/linker/linker.js';
import { Direction, ElementFilter, Point, Selector } from '../../bundles/types.js';
import { Command } from '../../classes/command/command.js';
import { StateItem } from '../../classes/dragger/dragger.js';
import { addNamedEventListener } from '../../bundles/monika.js';
import { parseStyle } from '../../bundles/css.js';

export type LinkDirection = Direction | undefined

export const LINKER_MOVE = "linker-move";
export const LINKER_END = "linker-end";

export function reverseDirection(d: LinkDirection): LinkDirection {
	switch (d) {
		case "left":
			return "right";
		case "up":
			return "down";
		case "down":
			return "up";
		case "right":
			return "left";
		case undefined:
			return undefined;
	}
}

/**
 * Contains the functionality for linking drawing links between selected elements 
 * and a target.
 * 
 * There are some basic expectations about the way links work embedded in this behaviour:
 * 
 * 1.  <from> and <to> elements are present, and have reference="" to indicate where they link to.\
 * 2.  Labels are embedded within <from> and <to>.
 * 3.  Align links (links that the editor can remove) have the class "align".  
 * 
 * All of these are in any case defaults that can be overridden.
 */
export function initLinkable(linker: Linker, selector: Selector = undefined) {

	function move(event: Event) {
		linker.move(event);
	}

	function end(event: Event) {
		linker.end(event);
	}

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-elem]"));
		}
	}

	window.addEventListener('DOMContentLoaded', function() {
		selector().forEach(function(v) {
			addNamedEventListener(v, "mousemove", LINKER_MOVE, move);
			addNamedEventListener(v, "touchmove", LINKER_MOVE, move, { passive: false });
			addNamedEventListener(v, "mouseup", LINKER_END, end);
			addNamedEventListener(v, "touchend", LINKER_END, end);
		})
	})
}


export function initLinkerDropCallback(_command: Command, linker: Linker) {

	return function(_dragState: StateItem[], event: Event) {
		linker.end(event, false);  // this false means that changes don't get performed immediately
	}

}

/**
 * This is for updating links when they are being dragged.
 */
export function updateLink(e:Element, from: Point, to: Point) {
	e.querySelectorAll("[k9-animate=link]").forEach(f => {
		f.setAttribute("d", "M" + from.x + " " + from.y + "L" + to.x + " " + to.y);
	});
}



/**
 * All of this allows us to identify links on the diagram which are just for the 
 * purposes of alignment.  These can be deleted / changed when needed.
 */
export type AlignmentIdentifier = ElementFilter

export type AlignmentInfo = {
	element: Element,
	direction: LinkDirection
}

export type AlignmentCollector = (id1: string, id2: string) => AlignmentInfo[]

export function initAlignmentCollector(ai: AlignmentIdentifier)  : AlignmentCollector {
	
	return function getAlignElementsAndDirections(id1: string, id2: string) {
		return getExistingConnections(id1, id2)
			.filter(ai)
			.map(e => {
				const parsed = parseInfo(e);
				const d = parsed['direction'];
				const ids = parsed['link'];
				const reversed = ids[0] == id2;
				return {
					element: e,
					direction: reversed ? reverseDirection(d) : d
				} as AlignmentInfo
			});
		
	}
}


export function getDirection(e: Element): LinkDirection {
	if (e == null) {
		return undefined;
	} else {
		const info = parseInfo(e);
		const l = info['direction'];
		return l;
	}
}

export function getStyleDirection(e1: Element) : LinkDirection {
	const style = parseStyle(e1.getAttribute("style"));
	const d = style['--kite9-direction'] as LinkDirection;
	return d;
}


/** 
 * Call this to tell autoconnect we're dealing with a new element
 */
export function setAutoconnectNew(e: Element) {
	e.setAttribute("autoconnect", "new");
}

export function isAutoconnectNew(e: Element) {
	return e.getAttribute("autoconnect") == 'new';
}