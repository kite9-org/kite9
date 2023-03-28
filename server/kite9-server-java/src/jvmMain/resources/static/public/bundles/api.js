/**
 * Returns the collection of elements so long as one of them has the lastSelected class.
 */
export function hasLastSelected(e) {
    for (let i = 0; i < e.length; i++) {
        const item = e[i];
        if (item.classList.contains("lastSelected")) {
            return e;
        }
    }
    return [];
}
/**
 * Returns the element from the collection with the lastSelected class, or null.
 */
export function onlyLastSelected(e) {
    for (let i = 0; i < e.length; i++) {
        const item = e[i];
        if (item.classList.contains("lastSelected")) {
            return item;
        }
    }
    return null;
}
/**
 * Given a list of Ids, return elements that are dependent on them
 */
export function getDependentElements(ids) {
    return Array.from(document.querySelectorAll("div.main svg [id][k9-info*='link:']")).filter(e => {
        const parsed = parseInfo(e);
        const depIds = parsed['link'];
        const anyMatch = depIds
            .map(di => ids.indexOf(di) != -1)
            .reduce((a, b) => a || b, false);
        return anyMatch;
    });
}
/**
 * Returns a connection linking to id1 and (if provided) also id2.
 */
export function getExistingConnections(id1, id2 = undefined) {
    return Array.from(document.querySelectorAll("div.main svg [id][k9-info*='link:']")).filter(e => {
        const parsed = parseInfo(e);
        const ids = parsed['link'];
        if (ids) {
            if (id2) {
                return ((ids[0] == id1) && (ids[1] == id2)) ||
                    ((ids[1] == id1) && (ids[0] == id2));
            }
            else {
                return (ids[0] == id1) || (ids[1] == id1);
            }
        }
        return false;
    });
}
export function parseInfo(t) {
    if ((t != null) && (t.hasAttribute("k9-info"))) {
        const parts = t.getAttribute("k9-info").split(';');
        const out = {};
        parts.forEach(p => {
            p = p.trim();
            const colon = p.indexOf(":");
            if (colon > -1) {
                const name = p.substring(0, colon).trim();
                const value = p.substring(colon + 1).trim();
                if (value.startsWith("[") && value.endsWith("]")) {
                    const v2 = value.substring(1, value.length - 1)
                        .split(",")
                        .filter(s => s.length > 0)
                        .map(s => s.startsWith("'") ? s.substring(1, s.length - 1) : parseFloat(s.trim()));
                    out[name] = v2;
                }
                else if (value == 'null') {
                    out[name] = null;
                }
                else {
                    out[name] = value;
                }
            }
        });
        return out;
    }
    else {
        return {};
    }
}
export function getParentElement(elem) {
    let p = elem.parentElement;
    while ((p != null) && (!p.hasAttribute("k9-elem"))) {
        p = p.parentElement;
    }
    if (p instanceof SVGGraphicsElement) {
        return p;
    }
    else {
        return null;
    }
}
export function getParentElements(elements) {
    return [...new Set(Array.from(elements).flatMap(e => getParentElement(e)))];
}
export function getNextSiblingElement(elem) {
    const p = elem;
    const children = Array.from(p.parentElement.children).filter(e => e.hasAttribute("k9-elem"));
    const thisOne = children.indexOf(elem);
    if (thisOne < children.length - 1) {
        return children[thisOne + 1];
    }
    else {
        return null;
    }
}
export function getNextSiblingId(elem) {
    elem = getNextSiblingElement(elem);
    return elem == null ? null : elem.getAttribute("id");
}
export function getContainedChildIds(elem, criteria = () => true) {
    const out = [];
    function traverse(e) {
        for (const c of Array.from(e.children)) {
            if (c.hasAttribute("id") && c.hasAttribute("k9-elem")) {
                const id = c.getAttribute("id");
                if ((!out.includes(id)) && (criteria(c))) {
                    out.push(id);
                }
            }
            else {
                traverse(c);
            }
        }
    }
    traverse(elem);
    return out;
}
export function getContainingDiagram(elem) {
    if (elem == null) {
        return null;
    }
    const pcd = getContainingDiagram(elem.parentElement);
    if (pcd) {
        return pcd;
    }
    else if (elem.hasAttribute("k9-elem")) {
        return elem;
    }
}
export function transformToCss(a) {
    let out = '';
    if ((a.scaleX) && (a.scaleX != 1)) {
        out = out + "scaleX(" + a.scaleX + ") ";
    }
    if ((a.scaleX) && (a.scaleY != 1)) {
        out = out + "scaleY(" + a.scaleY + ") ";
    }
    if ((a.translateX) && (a.translateX != 0)) {
        out = out + "translateX(" + a.translateX + "px) ";
    }
    if ((a.translateY) && (a.translateY != 0)) {
        out = out + "translateY(" + a.translateY + "px) ";
    }
    if (a.matrix) {
        out = "matrix(" + a.matrix + ") ";
    }
    return out;
}
export function number(value) {
    if (value == null) {
        return null;
    }
    else {
        const out = Number(value.replace(/[a-z]+$/, ''));
        if (isNaN(out)) {
            return 0;
        }
        else {
            return out;
        }
    }
}
export function parseTransform(a) {
    const b = {
        translateX: 0,
        translateY: 0,
        scaleX: 1,
        scaleY: 1
    };
    if (a == null) {
        return b;
    }
    const arr = a.match(/(\w+\((-?\d*\.?\d*e?-?\d*[(px), ]*)+\))+/g);
    for (const i in arr) {
        const c = arr[i].match(/[\w.-]+/g);
        const name = c.shift();
        if (c.length > 1) {
            b[name] = c.map(e => number(e));
        }
        else {
            b[name] = number(c[0]);
        }
    }
    if (b.translate) {
        b.translateX = b.translate[0];
        b.translateY = b.translate[1];
        delete b.translate;
    }
    if (b.scale) {
        if (b.scale instanceof Array) {
            b.scaleX = b.scale[0];
            b.scaleY = b.scale[1];
        }
        else {
            b.scaleX = b.scale;
            b.scaleY = b.scale;
        }
        delete b.scale;
    }
    return b;
}
export function changeId(e, oldId, newId) {
    let descend = true;
    if (e.hasAttribute("id")) {
        const idAttr = e.getAttribute("id");
        const partIndex = idAttr.indexOf("@");
        if (partIndex > -1) {
            const id = idAttr.substring(0, partIndex);
            const part = idAttr.substring(partIndex + 1);
            if (id.indexOf(oldId) == 0) {
                newId = id.replace(oldId, newId);
                e.setAttribute("id", newId + "@" + part);
            }
            else if (oldId.indexOf(id) == 0) {
                // do nothing
            }
            else {
                descend = false;
            }
        }
        else {
            const id = idAttr;
            if (id == oldId) {
                e.setAttribute("id", newId);
            }
            else {
                descend = false;
            }
        }
    }
    if (descend) {
        let cntr = 0;
        for (let i = 0; i < e.children.length; i++) {
            const c = e.children[i];
            const cOld = c.getAttribute("id");
            const cNew = cOld == undefined ? newId : newId + "-" + (cntr++);
            changeId(c, cOld, cNew);
        }
    }
}
export function suffixIds(elements, suffix) {
    elements.forEach(e => {
        if (e.hasAttribute("id")) {
            const idAttr = e.getAttribute("id");
            const partIndex = idAttr.indexOf("@");
            if (partIndex > -1) {
                const id = idAttr.substring(0, partIndex);
                const part = idAttr.substring(partIndex + 1);
                e.setAttribute("id", id + suffix + "@" + part);
            }
            else {
                e.setAttribute("id", idAttr + suffix);
            }
        }
    });
}
export function handleTransformAsStyle(e) {
    if (e.hasAttribute('transform')) {
        const t = parseTransform(e.getAttribute('transform'));
        const css = transformToCss(t);
        if (e instanceof SVGGraphicsElement) {
            e.style.setProperty('transform', css, '');
        }
        else {
            e.setAttribute("style", "transform: " + css + ";");
        }
        e.removeAttribute('transform');
        return t;
    }
    return null;
}
let lastId = 1;
export function createUniqueId() {
    lastId++;
    while (document.querySelectorAll("[id='" + lastId + "']").length > 0) {
        lastId++;
    }
    return "" + lastId;
}
export function getKite9Target(v) {
    if (v instanceof SVGGraphicsElement) {
        if (v.hasAttribute("k9-elem") && v.hasAttribute("id")) {
            return v;
        }
        else {
            return getKite9Target(v.parentNode);
        }
    }
    else {
        return null;
    }
}
export function isTerminator(v) {
    if (v == null)
        return false;
    const att = v.getAttribute("k9-info");
    return (att == undefined ? "" : att).includes("terminates:");
}
export function isLink(v) {
    if (v == null)
        return false;
    const att = v.getAttribute("k9-info");
    return (att == undefined ? "" : att).includes("link:");
}
export function isPort(v) {
    if (v == null)
        return false;
    const att = v.getAttribute("k9-info");
    return (att == undefined ? "" : att).includes("port:");
}
export function isConnected(v) {
    if (v == null)
        return false;
    const att = v.getAttribute("k9-info");
    const typeSet = (att == undefined ? "" : att).includes("connected");
    const ui = v.getAttribute("k9-ui");
    const uiSet = (ui == undefined ? "" : ui).includes("connect");
    return uiSet && typeSet;
}
export function isDiagram(v) {
    if (v == null)
        return false;
    const att = v.getAttribute("k9-info");
    return (att == undefined ? "" : att).includes("rectangular: diagram");
}
export function isRectangular(v) {
    if (v == null)
        return false;
    const att = v.getAttribute("k9-info");
    return (att == undefined ? "" : att).includes("rectangular:");
}
export function isLabel(v) {
    if (v == null)
        return false;
    const att = v.getAttribute("k9-info");
    return (att == undefined ? "" : att).includes("rectangular: label");
}
export function isTemporary(e) {
    if (e == null)
        return false;
    if (e.hasAttribute("k9-info")) {
        const out = e.getAttribute("k9-info");
        if (out.includes("temporary: true;")) {
            return true;
        }
    }
    return false;
}
export function connectedElement(terminator, within) {
    const info = parseInfo(terminator);
    const at = info['terminates-at'];
    const end = within.getElementById(at);
    return end;
}
export function connectedElementOtherEnd(terminator, within) {
    const info = parseInfo(terminator);
    const parent = within.getElementById(info['terminates']);
    const otherTerminators = getContainerChildren(parent)
        .filter(c => c != terminator)
        .filter(c => isTerminator(c));
    if (otherTerminators.length != 1) {
        throw Error("Links should only have two terminators : " + parent.getAttribute('id'));
    }
    return connectedElement(otherTerminators[0], within);
}
export function getContainerChildren(container, ignore = []) {
    const allChildren = Array.from(container.querySelectorAll("[id][k9-info]"))
        .filter(e => getParentElement(e) == container)
        .filter(e => ignore.indexOf(e) == -1);
    return allChildren;
}
export function xmlDepth(a) {
    if (a == undefined) {
        return 0;
    }
    else {
        return 1 + xmlDepth(a.parentNode);
    }
}
export function getCommonContainer(a, b) {
    let aDepth = xmlDepth(a);
    let bDepth = xmlDepth(b);
    while (aDepth > bDepth) {
        aDepth--;
        a = a.parentNode;
    }
    while (bDepth > aDepth) {
        bDepth--;
        b = b.parentNode;
    }
    while (a != b) {
        a = a.parentNode;
        b = b.parentNode;
    }
    return a;
}
export function onlyUnique(value, index, self) {
    return self.indexOf(value) === index;
}
export function encodeADLElement(text) {
    return btoa(text);
}
export function addQueryParam(url, name, value) {
    if (url.indexOf("?") != -1) {
        return url + "&" + name + "=" + value;
    }
    else {
        return url + "?" + name + "=" + value;
    }
}
export function getAffordances(element) {
    const ui = element?.getAttribute("k9-ui")?.split(" ") ?? [];
    return ui;
}
export function getDocumentParam(p) {
    return document['params'][p];
}
export function setDocumentParam(p, a) {
    return document['params'][p] = a;
}
