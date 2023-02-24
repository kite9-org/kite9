import { describe, it, expect } from '../../bundles/monika.js';
import { containment } from '../editor/editor.js'
import { box, container, diagram, link,terminator, text, port, label, grid, cell, temporary } from './fixture.js';

type Rules<X> = {
	box: X,
	container: X,
	diagram: X,
	link: X,
	terminator: X,
	text: X,
	port: X,
	label: X,
	grid: X, 
	cell: X,
	temporary: X
}

export const containmentTest = describe("Containment Tests", async () => {
		
	const containers : Rules<Element> =  {
		box,
		container,
		diagram,
		link,
		terminator,
		text,
		port,
		label,
		grid,
		cell,
		temporary
	}
	
	function createRule(box: boolean, container: boolean, diagram: boolean, link: boolean, terminator: boolean, text: boolean, port: boolean, label: boolean, grid: boolean, cell: boolean, temporary: boolean) : Rules<boolean> {
		return {
			box,
			container,
			diagram,
			link,
			terminator,
			text,
			port, 
			label,
			grid,
			cell,
			temporary
		}
	}
	
	const ALL_FALSE = createRule(false, false, false, false, false, false, false, false, false, false, false);
	
	const containerRules : Rules<Rules<boolean>> = {
		box: createRule(false, false, false, false, true, false, true, false, false, false, false),
		container: createRule(true, true, false, false, true, true, true, true, true, false, false),
		diagram: createRule(true, true, false, false, false, true, false, true, true, false, false),
		link: ALL_FALSE,
		terminator: createRule(false, false, false, false, false, false, false, true, false, false, false),
		text: createRule(false, false, false, false, false, false, false, false, false, false, false),
		port: createRule(false, false, false, false, true, false, false, false, false, false, false),
		label: createRule(false, false, false, false, false, false, false, false, false, false, false),
		grid: createRule(false, false, false, false, true, false, true, true, false, true, false),
		cell: createRule(true, true, false, false, false, true, false, true, true, false, false),
		temporary: ALL_FALSE,
	}
	
	function singleTest(parent: string, child: string) : void {
		const parentElem = containers[parent];
		const childElem = containers[child];
		const allowed = containerRules[parent][child];
		if (allowed) {
			it (`${parent} can contain ${child}`, async() => {
				expect(containment.allowed([childElem], parentElem)).toEqual([childElem]);
			});
		} else {
			it (`${parent} can't contain ${child}`, async() => {
				expect(containment.allowed([childElem], parentElem)).toEqual([]);
			});
		}
	}
	
	Object.keys(containers).forEach(k => {
		Object.keys(containers).forEach(l => {
			singleTest(k, l);
		});
	})

});

