import { getContainerChildren, isCell, isTemporary, parseInfo } from '../../bundles/api.js'
import { Command } from '../../classes/command/command.js';

export interface Ordinals extends Array<number> {
	max: number, 
	min: number
}

export function getOrdinal(index: number, ordinals: Ordinals) : number {
	
	if (index < ordinals.min) {
		return ordinals[ordinals.min] - (index + ordinals.min);
	} else if (index >= ordinals.max) {
		return ordinals[ordinals.max] + (index - ordinals.max);
	} else {
		let carry = 0;
		while (ordinals[index] == undefined) {
			index--;
			carry++;
		}
		return ordinals[index]+carry;
	}
}

export function nextOrdinal(o: number, ordinals: Ordinals) {
	const index = ordinals.indexOf(o);
	if (index == -1) {
		if (o < ordinals.min) {
			return ordinals.min;
		} else if (o > ordinals.max) {
			return o + 1;
		}
	}
	return getOrdinal(index+1, ordinals);
}

export function getOrdinals(container: Element) : {xOrdinals: Ordinals, yOrdinals: Ordinals } {
	const xOrdinals = [] as Ordinals;
	xOrdinals.max = Number.MIN_SAFE_INTEGER;
	xOrdinals.min = Number.MAX_SAFE_INTEGER;
	
	const yOrdinals = [] as Ordinals;
	yOrdinals.max = Number.MIN_SAFE_INTEGER;
	yOrdinals.min = Number.MAX_SAFE_INTEGER;
	
	getContainerChildren(container)
		.forEach(e => {
		const details = parseInfo(e);
		if ((details != null) && (details['position']) && details['grid-x']) {
			const position = details['position'];
			const gridX = details['grid-x'];
			const gridY = details['grid-y'];
			
			xOrdinals[gridX[0]] = position[0];
			xOrdinals[gridX[1]-1] = position[1];
			yOrdinals[gridY[0]] = position[2];
			yOrdinals[gridY[1]-1] = position[3];
			
			xOrdinals.max = Math.max(xOrdinals.max, gridX[1]-1);
			xOrdinals.min = Math.min(xOrdinals.min, gridX[0]);
			yOrdinals.max = Math.max(yOrdinals.max, gridY[1]-1);
			yOrdinals.min = Math.min(yOrdinals.min, gridY[0]);
		}
	});

	return {
		xOrdinals: xOrdinals,
		yOrdinals: yOrdinals,
	}
}

/**
 * Used for moving cells in a grid down/left.
 */
export function pushCells(command: Command, container: Element, from: number, horiz: boolean, push: number, ignore: Element[]) {
	const movableCells = getContainerChildren(container, ignore)
		.filter(c => isCell(c))
		.filter(c => !isTemporary(c))

	movableCells.forEach(cell => {
		const info = parseInfo(cell);
		const position = info['position'];
		const styleField = horiz ? '--kite9-occupies-x': '--kite9-occupies-y';
		const [f, t] = horiz ? [ position[0], position[1]] : [position[2], position[3]];
		if (f >= from) {
			command.push({
				type: 'ReplaceStyle',
				fragmentId:  cell.getAttribute("id"),
				name: styleField,
				from: `${f} ${t}`,
				to: `${f+push} ${t+push}`
			});
		} else if (t >= from) {
			command.push({
				type: 'ReplaceStyle',
				fragmentId:  cell.getAttribute("id"),
				name: styleField,
				from: `${f} ${t}`,
				to: `${f} ${t+push}`
			});
		}
	})
	
}