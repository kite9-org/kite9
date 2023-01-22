import { describe, it, expect, getNamedEventListener } from '../../bundles/monika.js';
import { getElementHTMLBBox, getElementPageBBox, getMainSvg } from '../../bundles/screen.js';
import { isLastSelected, isSelected, singleSelect } from '../../behaviours/selectable/selectable.js';
import { box, box2, container, diagram, link,terminator, text, port, label, mousePositionEvent } from './fixture.js';
import { mouseEvent } from './fixture.js';
import { DRAGABLE_END_EVENT, DRAGABLE_MOVE_EVENT, DRAGABLE_START_EVENT } from '../../behaviours/dragable/dragable.js';
import { Area, sharedArea } from '../../bundles/types.js';
import { command, dragger } from '../editor/editor.js';
import { Command, CommandCallback } from '../../classes/command/command.js';

type Rule = {
 drag: string, 
 over: string[],
 drops: string[]
}

function allowed(drag: string, over: string) : Rule {
	return {
		drag,
		over: [over],
		drops: [over]
	}
}

function denied(drag: string, over: string) : Rule {
	return {
		drag,
		over: [over],
		drops: []
	}
}

let _commandCallbacks : CommandCallback[];

function replaceCommandStuff(command: Command) {
	_commandCallbacks = [ ...command.callbacks ];
	command.callbacks.length = 0;
}

export const dragableTest = describe("Dragable Tests", async () => {

	const elements = {
		box, box2, container, diagram, link,terminator, text, port, label
	}
	
	const rules = [
		allowed('box', 'container'),
//		allowed('box', 'diagram'),
//		denied('box', 'box2'),
//		denied('box', 'box2'),
//		allowed('terminator', 'box'),
//		allowed('terminator', 'container')		
	];
	
	let delay = 10;
	
	function doDelay(f: () => void) {
		setTimeout(f, delay);
		delay +=10;
	}
	
	replaceCommandStuff(command);
	
	rules.forEach(r => {
		const dragging = elements[r.drag];
		singleSelect([ dragging ]);
		
		const dropArea : Area = r.over
				.map(n => elements[n])
				.map(e => getElementHTMLBBox(e) as Area)
				.reduce(sharedArea);
		
		const mouseDown = mouseEvent(dragging, 'mousedown');
		const mouseDrag1 = mouseEvent(dragging, 'mousemove', { buttons: 1}, 20, 20);
		const mouseDrag2 = mousePositionEvent(dropArea, 'mousemove');
		const mouseUp = mousePositionEvent(dropArea, "mouseup");

		const start = getNamedEventListener(DRAGABLE_START_EVENT, dragging);
		const move = getNamedEventListener(DRAGABLE_MOVE_EVENT, getMainSvg());
		const end = getNamedEventListener(DRAGABLE_END_EVENT, document);
		
		doDelay(() => start(mouseDown));
		doDelay(() => move(mouseDrag1));
		doDelay(() => move(mouseDrag2));
		doDelay(() => end(mouseUp));
		doDelay(() => dragger.endMove(true));

	});
	
});

