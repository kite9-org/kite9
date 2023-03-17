import { describe, it, expect, getNamedEventListener } from '../../bundles/monika.js';
import { getElementHTMLBBox, getElementPageBBox, getMainSvg } from '../../bundles/screen.js';
import { isLastSelected, isSelected, singleSelect } from '../../behaviours/selectable/selectable.js';
import { box, box2, container, diagram, link,terminator, text, port, label, mousePositionEvent } from './fixture.js';
import { mouseEvent } from './fixture.js';
import { DRAGABLE_END_EVENT, DRAGABLE_MOVE_EVENT, DRAGABLE_START_EVENT } from '../../behaviours/dragable/dragable.js';
import { Area, sharedArea } from '../../bundles/types.js';
import { command, dragger, transition } from '../editor/editor.js';
import { Command, CommandCallback, Update } from '../../classes/command/command.js';
import { LINKER_END, LINKER_MOVE } from '../../behaviours/links/linkable.js';

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

function initResetAndCheckCommandCallback() : CommandCallback {
	
	const original = getMainSvg().outerHTML;
	const parser = new DOMParser();
	
	const expected = [{
		from: "dia",
		fromBefore: "bigbox",
		moveId: "b1",
		to: "bigbox",
		toBefore: "b2",
		type: "Move"
	}]
	
	return (u: Update) => {
		expect(u.commands).toEqual(expected)
		const doc = parser.parseFromString(original, "image/svg+xml");
		transition.change(doc);
	};

}

function replaceCommandStuff(command: Command, ) {
	_commandCallbacks = [ ...command.callbacks ];
	command.callbacks.length = 0;
	
	command.add(initResetAndCheckCommandCallback());
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
	
	function doDelay(f: ()=> void) : Promise<void> {
		const out = new Promise<void>((res) => {
			return setTimeout(() => {
				f();
				res();
			}, 100);
		});
		
		return out;
	}
	
	replaceCommandStuff(command);
	
	const r = rules[0];
	
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
		const dragMove = getNamedEventListener(DRAGABLE_MOVE_EVENT, getMainSvg());
		const linkMove = getNamedEventListener(LINKER_MOVE, dragging);
		const dragEnd = getNamedEventListener(DRAGABLE_END_EVENT, document);
		const linkEnd =  getNamedEventListener(LINKER_END, dragging);
		
		
		return Promise.resolve()
			.then(() => doDelay(() => start(mouseDown)))
			.then(() => doDelay(() => { linkMove(mouseDrag1); dragMove(mouseDrag1);  }))
			.then(() => doDelay(() => { linkMove(mouseDrag2); dragMove(mouseDrag2);  }))
			.then(() => doDelay(() => { dragEnd(mouseUp); linkEnd(mouseUp) }))
			.then(() => doDelay(() => dragger.endMove(true)));
	
});

