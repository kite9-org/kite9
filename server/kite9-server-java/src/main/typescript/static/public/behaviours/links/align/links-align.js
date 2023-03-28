import { getContainingDiagram, createUniqueId, getExistingConnections, parseInfo, hasLastSelected, getDocumentParam, getParentElement } from '../../../bundles/api.js';
import { getMainSvg, getElementPageBBox } from '../../../bundles/screen.js';
import { reverseDirection } from '../../../bundles/types.js';
export function initAlignContextMenuCallback(command, alignmentIndentifier, templateUri = undefined, selector = undefined) {
    /**
     * Aligns the two elements
     */
    function createAlignStep(from, to, direction, steps, linkId) {
        const conns = getExistingConnections(from.getAttribute("id"), to.getAttribute("id"));
        let toUseId = null;
        let existingDirection;
        // tidy up any existing connections between these elements.
        conns.forEach(c => {
            const alignOnly = alignmentIndentifier(c);
            const id = c.getAttribute("id");
            if (alignOnly) {
                // remove the old alignment
                steps.push({
                    type: 'Delete',
                    fragmentId: getParentElement(c).getAttribute("id"),
                    base64Element: command.getAdl(id)
                });
            }
            else {
                const debug = parseInfo(c);
                existingDirection = debug.direction;
                if (existingDirection != 'null') {
                    steps.push({
                        fragmentId: c.getAttribute("id"),
                        type: 'ReplaceStyle',
                        name: '--kite-direction',
                        from: existingDirection,
                        to: null
                    });
                }
                if (toUseId == null) {
                    toUseId = c.getAttribute("id");
                    // check to see if we need to reverse the align
                    const parsed = parseInfo(c);
                    const ids = parsed['link'];
                    const reversed = ids[0] == to.getAttribute("id");
                    direction = reversed ? reverseDirection(direction) : direction;
                }
            }
        });
        if (toUseId == null) {
            // create an align element
            steps.push({
                fragmentId: getContainingDiagram(from).getAttribute("id"),
                type: 'InsertUrlWithChanges',
                newId: linkId,
                xpathToValue: {
                    "*[local-name()='from']/@reference": from.getAttribute("id"),
                    "*[local-name()='to']/@reference": to.getAttribute("id")
                },
                uriStr: templateUri,
            });
        }
        else {
            linkId = toUseId;
        }
        steps.push({
            fragmentId: linkId,
            type: 'ReplaceStyle',
            name: '--kite9-direction',
            from: existingDirection,
            to: direction
        });
        return linkId;
    }
    function performAlign(cm, horiz) {
        const selectedElements = selector();
        selectedElements.sort((a, b) => {
            const apos = getElementPageBBox(a);
            const bpos = getElementPageBBox(b);
            if (horiz) {
                return (apos.x + (apos.width / 2)) - (bpos.x + (bpos.width / 2));
            }
            else {
                return (apos.y + (apos.height / 2)) - (bpos.y + (bpos.height / 2));
            }
        });
        const steps = [];
        const linkId = createUniqueId();
        for (let i = 0; i < selectedElements.length - 1; i++) {
            const from = selectedElements[i];
            const to = selectedElements[i + 1];
            createAlignStep(from, to, horiz ? "right" : "down", steps, linkId + "-" + i);
        }
        cm.destroy();
        command.pushAllAndPerform(steps);
    }
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='align'].selected"));
        };
    }
    if (templateUri == undefined) {
        templateUri = getDocumentParam('align-template-uri');
    }
    /**
     * Provides an align option for the context menu
     */
    return function (event, cm) {
        const e = hasLastSelected(selector());
        if (e.length > 1) {
            cm.addControl(event, "/public/behaviours/links/align/align-horiz.svg", "Horizontal Align", () => performAlign(cm, true));
            cm.addControl(event, "/public/behaviours/links/align/align-vert.svg", "Vertical Align", () => performAlign(cm, false));
        }
    };
}
