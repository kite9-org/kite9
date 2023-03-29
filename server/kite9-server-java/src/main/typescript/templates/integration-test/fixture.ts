// things we are going to use across many tests

import { Area } from "../../bundles/types.js";

export const box = document.getElementById("b1");
export const box2 = document.getElementById("b1");
export const container = document.getElementById("bigbox");
export const diagram = document.getElementById("dia");
export const link = document.getElementById("link1");
export const terminator = document.getElementById("link2-from");
export const text = document.getElementById("t1");
export const port = document.getElementById("p1");
export const label = document.getElementById("l1");
export const grid = document.getElementById("biggrid"); 
export const cell = document.getElementById("2-0"); 
export const temporary = document.getElementById("biggrid-g-1-1");

export function mouseEvent(element: Element, name: string, init: MouseEventInit = {}, xo = 10, yo = 10) {
	const area = element.getBoundingClientRect();
	const me = mousePositionEvent(area, name, init, xo, yo);
	return me;
}

export function mousePositionEvent(area: Area, name: string, init: MouseEventInit = {}, xo = 10, yo = 10) {
	init.clientX = area.x + xo + window.pageXOffset,
	init.clientY = area.y + yo + window.pageYOffset;
	const me = new MouseEvent(name, init);
	return me;
}

