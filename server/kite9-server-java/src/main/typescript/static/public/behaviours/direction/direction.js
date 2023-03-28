import { getMainSvg } from '../../bundles/screen.js';
import { parseInfo, getContainingDiagram, getNextSiblingId, isTerminator, onlyLastSelected, isLink, getParentElement } from '../../bundles/api.js';
import { rotateAntiClockwise, rotateClockwise } from '../../bundles/types.js';
import { reverseDirection } from '../../bundles/types.js';
import { parseStyle } from '../../bundles/css.js';
function defaultSelector() {
    return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=direction].selected"));
}
function getStyleDirection(e1) {
    const style = parseStyle(e1.getAttribute("style"));
    const d = style['--kite9-direction'];
    return d;
}
export function initOptionalDirectionContextMenuCallback(command, isOptional = () => true, selector = defaultSelector) {
    function setDirections(es, direction, contextMenu) {
        contextMenu.destroy();
        es.forEach(e1 => {
            const toChange = isTerminator(e1) ? getParentElement(e1) : e1;
            const id = toChange.getAttribute("id");
            const oldDirection = getStyleDirection(toChange);
            let relativeDirection;
            let reverse = false;
            if (e1 != toChange) {
                // deal with terminator
                const termInfo = parseInfo(e1);
                reverse = termInfo.end == 'from';
            }
            switch (direction) {
                case 'cw':
                    relativeDirection = rotateClockwise(oldDirection);
                    break;
                case 'acw':
                    relativeDirection = rotateAntiClockwise(oldDirection);
                    break;
                case '180':
                    relativeDirection = reverseDirection(oldDirection);
                    break;
                default:
                    relativeDirection = reverse ? reverseDirection(direction) : direction;
            }
            command.push({
                fragmentId: id,
                type: 'ReplaceStyle',
                name: '--kite9-direction',
                to: relativeDirection,
                from: oldDirection
            });
            if (isLink(toChange)) {
                const diagramId = getContainingDiagram(toChange).getAttribute("id");
                // when setting a link direction, we move it to the 
                // end of the diagram to give it precedence over other 
                // links
                command.push({
                    type: 'Move',
                    from: diagramId,
                    fromBefore: getNextSiblingId(toChange),
                    moveId: id,
                    to: diagramId
                });
            }
        });
        command.perform();
    }
    function drawDirectionImage(event, cm, text, icon, selected = undefined, cb, set = "Actions") {
        let title, src;
        if (text != undefined) {
            title = "Direction (" + text + ")";
            src = "/public/behaviours/direction/" + icon + ".svg";
        }
        else {
            title = "No Fixed Direction (undirected)";
            src = "/public/behaviours/direction/undirected.svg";
        }
        const a = cm.addControl(event, src, title, cb, set);
        const img = a.children[0];
        if (selected == text) {
            img.setAttribute("class", "selected");
        }
        return img;
    }
    /**
     * Provides a link option for the context menu
     */
    return function (event, contextMenu) {
        const e = onlyLastSelected(selector());
        let link, contradicting = false, direction, reverse = false;
        if (e) {
            if (isTerminator(e)) {
                link = getParentElement(e);
                const debugLink = parseInfo(link);
                const debugTerm = parseInfo(e);
                direction = debugLink.direction;
                contradicting = debugLink.contradicting == "yes";
                reverse = debugTerm.end == 'from';
            }
            else {
                const debug = parseInfo(e);
                direction = debug.direction;
                if (isLink(e)) {
                    contradicting = debug.contradicting == "yes";
                    reverse = contradicting ? false : (debug.direction == 'left' || debug.direction == 'up');
                }
            }
            const d2 = reverse ? reverseDirection(direction) : direction;
            const img = drawDirectionImage(event, contextMenu, d2, d2, undefined, () => {
                contextMenu.clear();
                if (isOptional(e)) {
                    drawDirectionImage(event, contextMenu, null, null, d2, () => setDirections(selector(), null, contextMenu), "No Direction");
                }
                ["up", "down", "left", "right"].forEach((s) => {
                    drawDirectionImage(event, contextMenu, s, s, d2, () => setDirections(selector(), s, contextMenu), "Fixed Direction");
                });
                if (d2) {
                    drawDirectionImage(event, contextMenu, "Turn Clockwise", 'turn-cw', d2, () => setDirections(selector(), 'cw', contextMenu), "Rotation");
                    drawDirectionImage(event, contextMenu, "Turn Anti-Clockwise", 'turn-acw', d2, () => setDirections(selector(), 'acw', contextMenu), "Rotation");
                    drawDirectionImage(event, contextMenu, "Turn 180", 'turn-180', d2, () => setDirections(selector(), '180', contextMenu), "Rotation");
                }
            });
            if (contradicting) {
                img.style.backgroundColor = "#ff5956";
            }
        }
    };
}
