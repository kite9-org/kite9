import { getMainSvg, getElementPageBBox, currentTarget } from '../../../bundles/screen.js';
import { parseInfo, createUniqueId, getContainingDiagram, getExistingConnections, getKite9Target, getCommonContainer, isLink, getNextSiblingId, getAffordances, getParentElement } from '../../../bundles/api.js';
import { reverseDirection } from '../../../bundles/types.js';
import { getAutoConnectMode, AutoConnectMode } from './links-autoconnect-mode.js';
let link_to = undefined;
let link_d = undefined;
let draggingElement = undefined;
let templateUri = undefined;
export function initAutoConnectLinkerCallback(command, alignmentIdentifier) {
    function undoAlignment(e) {
        const alignOnly = alignmentIdentifier(e);
        const id = e.getAttribute("id");
        const parent = getParentElement(e);
        if (alignOnly) {
            command.push({
                type: 'Delete',
                fragmentId: parent.getAttribute("id"),
                base64Element: command.getAdl(id),
                beforeId: getNextSiblingId(e)
            });
            return false;
        }
        else {
            const direction = parseInfo(e)['direction'];
            command.push({
                type: 'ReplaceStyle',
                fragmentId: id,
                name: '--kite9-direction',
                from: direction
            });
            return true;
        }
    }
    function ensureNoDirectedLeavers(id, d1) {
        getExistingConnections(id).forEach(e => {
            const parsed = parseInfo(e);
            const d = parsed['direction'];
            const ids = parsed['link'];
            const reversed = ids[0] == id;
            const dUse = reversed ? reverseDirection(d1) : d1;
            if (d == dUse) {
                undoAlignment(e);
            }
        });
    }
    return function (linker) {
        if (link_to) {
            // create links between the selected object and the link_to one
            const id_from = draggingElement.getAttribute("id");
            const id_to = link_to.getAttribute("id");
            let existingLinks = getExistingConnections(id_from, id_to);
            ensureNoDirectedLeavers(id_from, link_d);
            const diagramId = getContainingDiagram(link_to).getAttribute("id");
            existingLinks = existingLinks.filter(e => undoAlignment(e));
            const mode = getAutoConnectMode();
            if ((existingLinks.length == 0) || (mode == AutoConnectMode.ON)) {
                // create a new link
                const linkId = createUniqueId();
                command.push({
                    fragmentId: diagramId,
                    type: 'InsertUrlWithChanges',
                    newId: linkId,
                    xpathToValue: {
                        "*[local-name()='from']/@reference": id_to,
                        "*[local-name()='to']/@reference": id_from
                    },
                    uriStr: templateUri,
                });
                command.push({
                    fragmentId: linkId,
                    type: 'ReplaceStyle',
                    name: '--kite9-direction',
                    to: link_d,
                });
            }
            else {
                const firstLink = existingLinks[0];
                const parsed = parseInfo(firstLink);
                const oldDirection = parsed['direction'];
                const ids = parsed['link'];
                const reversed = ids[0] == id_to;
                const direction = reversed ? link_d : reverseDirection(link_d);
                command.push({
                    fragmentId: firstLink.getAttribute("id"),
                    type: 'ReplaceStyle',
                    name: '--kite9-direction',
                    to: direction,
                    from: oldDirection
                });
                // moves it to the last in the list
                command.push({
                    type: 'Move',
                    from: diagramId,
                    fromBefore: getNextSiblingId(firstLink),
                    to: diagramId,
                    moveId: firstLink.getAttribute("id"),
                });
            }
            linker.removeDrawingLinks();
            link_to = null;
        }
    };
}
export function initAutoConnectMoveCallback(linker, linkFinder, linkTemplateSelector, selector = undefined, autoConnectWith = undefined) {
    const maxDistance = 100;
    function clearLink() {
        linker.removeDrawingLinks();
        link_to = null;
    }
    function updateLink(topos, frompos, link_d, e) {
        let fx, fy, tx, ty;
        const mx = topos.x + topos.width / 2;
        const my = topos.y + topos.height / 2;
        if (link_d == 'left') {
            fy = my;
            ty = my;
            fx = frompos.x;
            tx = topos.x + topos.width;
        }
        else if (link_d == 'right') {
            fy = my;
            ty = my;
            fx = frompos.x + frompos.width;
            tx = topos.x;
        }
        else if (link_d == 'up') {
            fx = mx;
            tx = mx;
            fy = frompos.y;
            ty = topos.y + topos.height;
        }
        else if (link_d == 'down') {
            fx = mx;
            tx = mx;
            fy = frompos.y + frompos.height;
            ty = topos.y;
        }
        else {
            return;
        }
        if (linker.get().length == 0) {
            const representation = linkFinder(templateUri);
            if (representation) {
                linker.start([e], representation);
            }
        }
        if (linker.get().length > 0) {
            linker.moveCoords(fx, fy, tx, ty);
        }
    }
    if (selector == undefined) {
        selector = function () {
            return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='autoconnect']"));
        };
    }
    if (autoConnectWith == undefined) {
        autoConnectWith = function (moving, inside, linkTo) {
            if (moving) {
                const ui = getAffordances(moving);
                if (!ui.includes("autoconnect")) {
                    // ok, we can try for a child to autoconnect to
                    const options = moving.querySelectorAll("[id][k9-ui~='autoconnect']");
                    if (options.length > 0) {
                        moving = options[0];
                    }
                    else {
                        return null;
                    }
                }
            }
            if (inside) {
                // check that we are allowed to auto-connect inside
                const target = getKite9Target(inside);
                const info = parseInfo(target);
                const layout = info.layout;
                if ((layout != null) && (layout != 'null')) {
                    return null;
                }
                if (linkTo) {
                    const commonContainer = getCommonContainer(inside, linkTo);
                    const commonInfo = parseInfo(commonContainer);
                    const commonLayout = commonInfo.layout;
                    if ((commonLayout != null) && (commonLayout != 'null')) {
                        return null;
                    }
                }
            }
            return moving;
        };
    }
    function getElementsInAxis(coords, horiz) {
        const out = Array.from(selector())
            .filter(e => {
            const { x, y, width, height } = getElementPageBBox(e);
            if (!horiz) {
                return ((y <= coords) && (y + height >= coords));
            }
            else {
                return ((x <= coords) && (x + width >= coords));
            }
        });
        return out;
    }
    /**
     * This function looks for stuff to connect to and shows links on screen to demonstrate this
     */
    return function (dragTargets, event, dropTargets) {
        function alreadyDragging(e) {
            if (dragTargets.indexOf(e) != -1) {
                return true;
            }
            if (e.parentNode == null) {
                return false;
            }
            else {
                return alreadyDragging(e.parentNode);
            }
        }
        function outside(a, b) {
            return ((a.x + a.width < b.x)
                || (a.x > b.x + b.width)
                || (a.y + a.height < b.y)
                || (a.y > b.y + b.height));
        }
        let cancelEarly = (dropTargets == undefined) || (dragTargets.length > 1)
            || (dropTargets.filter(dt => isLink(dt)).length > 0);
        if (!cancelEarly) {
            templateUri = linkTemplateSelector(dragTargets[0]);
            draggingElement = autoConnectWith(dragTargets[0], currentTarget(event));
            cancelEarly = draggingElement == null;
        }
        if (cancelEarly) {
            clearLink();
            return;
        }
        const pos = getElementPageBBox(draggingElement);
        const x = pos.x + (pos.width / 2);
        const y = pos.y + (pos.height / 2);
        let best = undefined;
        let best_dist = undefined;
        let best_d = undefined;
        getElementsInAxis(y, false).forEach(function (k) {
            if (!alreadyDragging(k)) {
                const v = getElementPageBBox(k);
                if (outside(pos, v) && (y <= v.y + v.height) && (y >= v.y)) {
                    // intersection on y position
                    let d, dist;
                    if (v.x + v.width < x) {
                        dist = pos.x - v.x - v.width;
                        d = 'right';
                    }
                    else if (v.x > x) {
                        dist = v.x - pos.x - pos.width;
                        d = 'left';
                    }
                    else {
                        dist = maxDistance + 1;
                        d = null;
                    }
                    if (best_dist) {
                        if (dist > best_dist) {
                            return;
                        }
                    }
                    best = k;
                    best_dist = dist;
                    best_d = d;
                }
            }
        });
        getElementsInAxis(x, true).forEach(function (k) {
            if (!alreadyDragging(k)) {
                const v = getElementPageBBox(k);
                if (outside(pos, v) && (x <= v.x + v.width) && (x >= v.x)) {
                    // intersection on x position
                    let d, dist;
                    if (v.y + v.height < y) {
                        dist = pos.y - v.y - v.height;
                        d = 'down';
                    }
                    else if (v.y > y) {
                        dist = v.y - pos.y - pos.height;
                        d = 'up';
                    }
                    else {
                        dist = maxDistance + 1;
                        d = null;
                    }
                    if (best_dist) {
                        if (dist > best_dist) {
                            return;
                        }
                    }
                    best = k;
                    best_dist = dist;
                    best_d = d;
                }
            }
        });
        if (best_dist > maxDistance) {
            best = undefined;
        }
        if (best === undefined) {
            clearLink();
        }
        else if (autoConnectWith(draggingElement, currentTarget(event), best) == null) {
            clearLink();
            //		} else if ((best === link_to) && link) {
            //			link_d = best_d;
            //			updateLink(pos, getElementPageBBox(best), link_d, draggingElement);
        }
        else {
            clearLink();
            link_to = best;
            link_d = best_d;
            updateLink(pos, getElementPageBBox(best), link_d, draggingElement);
        }
    };
}
