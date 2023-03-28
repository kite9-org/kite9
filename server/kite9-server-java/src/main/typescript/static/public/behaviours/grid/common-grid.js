import { getContainerChildren, isTemporary, parseInfo } from '../../bundles/api.js';
export function getOrdinal(index, ordinals) {
    if (index < ordinals.min) {
        return ordinals[ordinals.min] - (index + ordinals.min);
    }
    else if (index >= ordinals.max) {
        return ordinals[ordinals.max] + (index - ordinals.max);
    }
    else {
        let carry = 0;
        while (ordinals[index] == undefined) {
            index--;
            carry++;
        }
        return ordinals[index] + carry;
    }
}
export function nextNthOrdinal(o, ordinals, c) {
    if (c == 0) {
        return o;
    }
    else if (c == 1) {
        return nextOrdinal(o, ordinals);
    }
    else {
        return nextNthOrdinal(nextOrdinal(o, ordinals), ordinals, c - 1);
    }
}
export function nextOrdinal(o, ordinals) {
    const index = ordinals.indexOf(o);
    if (index == -1) {
        if (o < ordinals.min) {
            return ordinals.min;
        }
        else if (o > ordinals.max) {
            return o + 1;
        }
    }
    return getOrdinal(index + 1, ordinals);
}
export function getOrdinals(container) {
    const xOrdinals = [];
    xOrdinals.max = Number.MIN_SAFE_INTEGER;
    xOrdinals.min = Number.MAX_SAFE_INTEGER;
    const yOrdinals = [];
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
            xOrdinals[gridX[1] - 1] = position[1];
            yOrdinals[gridY[0]] = position[2];
            yOrdinals[gridY[1] - 1] = position[3];
            xOrdinals.max = Math.max(xOrdinals.max, gridX[1] - 1);
            xOrdinals.min = Math.min(xOrdinals.min, gridX[0]);
            yOrdinals.max = Math.max(yOrdinals.max, gridY[1] - 1);
            yOrdinals.min = Math.min(yOrdinals.min, gridY[0]);
        }
    });
    return {
        xOrdinals: xOrdinals,
        yOrdinals: yOrdinals,
    };
}
/**
 * Used for moving cells in a grid down/left.
 */
export function pushCells(command, container, from, horiz, push, ignore) {
    const movableCells = getContainerChildren(container, ignore)
        .filter(c => isCell(c))
        .filter(c => !isTemporary(c));
    movableCells.forEach(cell => {
        const info = parseInfo(cell);
        const position = info['position'];
        const styleField = horiz ? '--kite9-occupies-x' : '--kite9-occupies-y';
        const [f, t] = horiz ? [position[0], position[1]] : [position[2], position[3]];
        if (f >= from) {
            command.push({
                type: 'ReplaceStyle',
                fragmentId: cell.getAttribute("id"),
                name: styleField,
                from: `${f} ${t}`,
                to: `${f + push} ${t + push}`
            });
        }
        else if (t >= from) {
            command.push({
                type: 'ReplaceStyle',
                fragmentId: cell.getAttribute("id"),
                name: styleField,
                from: `${f} ${t}`,
                to: `${f} ${t + push}`
            });
        }
    });
}
export function isCell(e) {
    if (e == null)
        return false;
    if (e.hasAttribute("k9-info")) {
        const out = e.getAttribute("k9-info");
        if (out.includes("grid-x")) {
            return true;
        }
    }
    return false;
}
export function isGridLayout(e) {
    if (e == null)
        return false;
    if (e.hasAttribute("k9-info")) {
        const out = e.getAttribute("k9-info");
        if (out.includes("layout: grid;")) {
            return true;
        }
    }
    return false;
}
