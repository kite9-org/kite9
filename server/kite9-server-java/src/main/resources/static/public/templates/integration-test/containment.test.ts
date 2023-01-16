import { describe, it, expect } from '../../bundles/monika.js';
import { containment } from '../editor/editor.js'

type Rules<X> = {
	box: X,
	bigBox: X,
	diagram: X,
	link: X,
	terminator: X,
	text: X,
	port: X,
	label: X
}

export const containmentTest = describe("Containment Tests", async () => {

	const box = document.getElementById("b1");
	const bigBox = document.getElementById("bigbox");
	const diagram = document.getElementById("dia");
	const link = document.getElementById("link1");
	const terminator = document.getElementById("link2-from");
	const text = document.getElementById("t1");
	const port = document.getElementById("p1");
	const label = document.getElementById("l1");
	
	function createRule(box: boolean, bigBox: boolean, diagram: boolean, link: boolean, terminator: boolean, text: boolean, port: boolean, label: boolean) : Rules<boolean> {
		return {
			box,
			bigBox,
			diagram,
			link,
			terminator,
			text,
			port, 
			label
		}
	}
	
	const containers : Rules<Element> =  {
		box,
		bigBox,
		diagram,
		link,
		terminator,
		text,
		port,
		label
	}
	
	const containerRules = {
		box: createRule(false, false, false, false, true, false, true, false),
		bigBox: createRule(true, true, false, false, true, true, true, true),
		diagram: createRule(true, true, false, false, false, true, false, true),
		link: createRule(false, false, false, false, false, false, false, false),
		terminator: createRule(false, false, false, false, false, false, false, true),
		text: createRule(false, false, false, false, false, false, false, false)
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

