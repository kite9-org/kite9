import { describe, it, expect } from '../../bundles/monika.js';
import { containment } from '../editor/editor.js'

type Rules<X> = {
	box: X,
	bigBox: X,
	diagram: X,
	link: X,
	terminator: X,
	text: X,
	port: X
}

export const containmentTest = describe("hello", async () => {

	const box = document.getElementById("b1");
	const bigBox = document.getElementById("bigbox");
	const diagram = document.getElementById("dia");
	const link = document.getElementById("link1");
	const terminator = document.getElementById("link2-from");
	const text = document.getElementById("t1");
	const port = document.getElementById("p1");
	
	function createRule(box: boolean, bigBox: boolean, diagram: boolean, link: boolean, terminator: boolean, text: boolean, port: boolean) : Rules<boolean> {
		return {
			box,
			bigBox,
			diagram,
			link,
			terminator,
			text,
			port
		}
	}
	
	const containers : Rules<Element> =  {
		box,
		bigBox,
		diagram,
		link,
		terminator,
		text,
		port
	}
	
	const containerRules = {
		box: createRule(false, false, false, false, false, false, true),
		bigBox: createRule(true, true, false, false, true, true, true),
		diagram: createRule(true, true, false, true, true, true, false),
		link: createRule(true, true, false, false, false, false, false),
		terminator: createRule(false, false, false, false, false, false, false),
		text: createRule(false, false, false, false, false, false, false)
	}
	
	Object.keys(containers).forEach(k => {
		const elem1 = containers[k];
		const rules = containerRules[k];
		
		Object.keys(rules).forEach(l => {
			const allowed = rules[l];
			const elem2 = containers[l];
			if (allowed) {
				it (`${k} can contain ${l}`, async() => {
					expect(containment.allowed([elem2], [elem1])).toEqual([elem2]);
				});
			} else {
				it (`${k} can't contain ${l}`, async() => {
					expect(containment.allowed([elem2], [elem1])).toEqual([]);
				});
			}
		});
	})

});

