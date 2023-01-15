import { describe, it, expect, getMonikaEventListener } from '../../bundles/monika.js';
import { getElementHTMLBBox, getMainSvg } from '../../bundles/screen.js';
import { isLastSelected, isSelected } from '../../behaviours/selectable/selectable.js';

export const selectableTest = describe("hello", async () => {

	it("select an element", async () => {
		const t1 = document.getElementById("t1");
		const area = getElementHTMLBBox(t1);
		const mainSvg = getMainSvg();
		const l = getMonikaEventListener("selectable", mainSvg);
		const me = new MouseEvent("click", {
			clientX : area.x + area.width / 2,
			clientY : area.y + area.height / 2
		});
		l(me);
		
		// make sure element is sslected
		expect(isSelected(t1)).toEqual(true);
		expect(isLastSelected(t1)).toEqual(true);
		
		l(me);
		
		// make sure element is unsslected
		expect(isSelected(t1)).toEqual(false);
		expect(isLastSelected(t1)).toEqual(false);
	
	});

});

