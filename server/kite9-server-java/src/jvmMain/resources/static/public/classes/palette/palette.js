import { suffixIds, addQueryParam } from '../../bundles/api.js';
import { icon } from '../../bundles/form.js';
import { ensureCss } from '../../bundles/ensure.js';
/**
 * Provides functionality for populating/ showing/ hiding a palette.
 */
export class Palette {
    constructor(id, uriList) {
        this.loadCallbacks = [];
        this.paletteMap = [];
        this.expanded = null;
        this.revealCallbacks = [];
        const done = [];
        uriList = uriList == undefined ? [] : uriList;
        uriList.forEach((uri, i) => {
            if (!done.includes(uri)) {
                this.paletteMap.push({
                    number: i,
                    uri: uri,
                });
                done.push(uri);
            }
        });
        this.id = (id == undefined) ? "_palette" : id;
        ensureCss('/public/classes/palette/palette.css');
        let darken = document.getElementById("_darken");
        if (!darken) {
            darken = document.createElement("div");
            darken.setAttribute("id", "_darken");
            darken.setAttribute("class", "darken");
            document.querySelector("body").appendChild(darken);
            darken.style.display = 'none';
        }
        let palette = document.getElementById(this.id);
        if (!palette) {
            // create palette
            palette = document.createElement("div");
            palette.setAttribute("id", this.id);
            palette.setAttribute("class", "palette indicators-on");
            document.querySelector("body").appendChild(palette);
            // create area for control buttons
            const control = document.createElement("div");
            control.setAttribute("class", "control");
            palette.appendChild(control);
            // create concertina area
            const concertina = document.createElement("div");
            concertina.setAttribute("class", "concertina");
            palette.appendChild(concertina);
            this.paletteMap.forEach(p => this.loadPalette(p, concertina));
        }
    }
    loadPalette(p, concertina) {
        const id = "_palette-" + p.number;
        const item = document.createElement("div");
        item.setAttribute("class", "palette-item");
        item.setAttribute("k9-palette-uri", p.uri);
        item.setAttribute("id", id);
        concertina.appendChild(item);
        // create loading indicator
        const loading = document.createElement("img");
        loading.setAttribute("src", "/public/classes/palette/loading.svg");
        item.appendChild(loading);
        // populate it
        fetch(p.uri, {
            credentials: 'include',
            method: 'GET',
            headers: {
                "Accept": "image/svg+xml"
            }
        })
            .then(response => {
            if (!response.ok) {
                return response.json().then(j => {
                    loading.setAttribute("src", "/public/classes/palette/missing.svg");
                    throw new Error(j.message);
                });
            }
            return response;
        })
            .then(response => response.text())
            .then(text => {
            console.log("Loaded " + p.uri);
            const parser = new DOMParser();
            return parser.parseFromString(text, "image/svg+xml");
        })
            .then(doc => {
            // set new ids
            removeScripts(doc);
            const diagramElements = Array.from(doc.querySelectorAll("[k9-elem][id]"));
            suffixIds(diagramElements, id);
            item.appendChild(doc.documentElement);
            item.removeChild(loading);
            this.loadCallbacks.forEach(cb => {
                cb(this, item);
            });
            // hover styling
            diagramElements.forEach(de2 => {
                const de = de2;
                de.style.cursor = 'grab';
                de.classList.remove('inactive');
            });
            const evt = document.createEvent('Event');
            evt.initEvent('DOMContentLoaded', false, false);
            window.dispatchEvent(evt);
        })
            .catch(e => {
            alert("Problem loading palette: " + e);
        });
    }
    addLoad(cb) {
        this.loadCallbacks.push(cb);
    }
    addReveal(cb) {
        this.revealCallbacks.push(cb);
    }
    getId() {
        return this.id;
    }
    get() {
        return document.getElementById(this.id);
    }
    getOpenEvent() {
        return this.openEvent;
    }
    getOpenPanel() {
        return this.expanded;
    }
    open(event) {
        this.openEvent = event;
        const darken = document.getElementById("_darken");
        const palette = document.getElementById(this.id);
        const concertina = palette.querySelector("div.concertina");
        const control = palette.querySelector("div.control");
        // remove old control buttons
        while (control.firstChild) {
            control.removeChild(control.firstChild);
        }
        // add cancel button
        const cancel = icon('palette-cancel', 'Close', "/public/classes/palette/cancel.svg", () => this.destroy());
        control.appendChild(cancel);
        let paletteWidth = 100, paletteHeight = 100;
        let selectedDot;
        function expandPanel(p, e, dot) {
            if (p.expanded) {
                p.expanded.style.maxHeight = "0px";
            }
            e.style.maxHeight = height + "px";
            p.expanded = e;
            control.querySelectorAll("img").forEach(e => e.classList.remove("selected"));
            if (dot != null) {
                dot.children[0].classList.add("selected");
            }
            p.revealCallbacks.forEach(cb => cb(e));
        }
        function getTitle(palette) {
            const d = "Untitled Palette";
            const diagram = palette.querySelector("g[k9-elem=diagram]");
            const title = diagram == undefined ? d : diagram.getAttribute("title");
            return title ? title : d;
        }
        function getIcon(palette) {
            const d = "/public/classes/palette/dot.svg";
            const diagram = palette.querySelector("g[k9-elem=diagram]");
            const icon = diagram == undefined ? d : diagram.getAttribute("icon");
            return icon ? icon : d;
        }
        // display new control buttons and size the overall thing
        Array.from(palette.querySelectorAll("div.palette-item")).forEach((e, i) => {
            const elem = e;
            elem.style.maxHeight = "0px";
            elem.style.visibility = 'show';
            elem.style.display = 'block';
            const svg = e.querySelector(":first-child");
            if (!(svg.tagName.toLowerCase() == 'img')) {
                paletteWidth = Math.max(svg.width.baseVal.valueInSpecifiedUnits, paletteWidth);
                paletteHeight = Math.max(svg.height.baseVal.valueInSpecifiedUnits, paletteHeight);
            }
            const dot = icon('', getTitle(e), getIcon(e), () => expandPanel(this, elem, dot));
            dot.classList.remove("hint--bottom");
            dot.classList.add("hint--right");
            if ((e == this.expanded) || ((this.expanded == null) && i == 0)) {
                this.expanded = elem;
                selectedDot = dot;
            }
            control.appendChild(dot);
        });
        // ensure the palette appears in the centre of the screen
        const width = Math.min(paletteWidth + 30, window.innerWidth - 100);
        const height = Math.min(paletteHeight + 30, window.innerHeight - 100);
        palette.style.marginTop = (-height / 2) + "px";
        palette.style.marginLeft = (-width / 2) + "px";
        concertina.style.width = (width) + "px";
        concertina.style.height = (height) + "px";
        palette.style.visibility = 'visible';
        darken.style.display = 'block';
        expandPanel(this, this.expanded, selectedDot);
        return palette;
    }
    destroy() {
        const palette = document.getElementById(this.id);
        const darken = document.getElementById("_darken");
        palette.style.visibility = 'hidden';
        darken.style.display = 'none';
        this.revealCallbacks.forEach(cb => cb());
    }
}
/**
 * These are not needed for palettes, because when you pull something off a palette, it will use the
 * main documents stylesheet.  Also, these interact with the main svg area, breaking the styling.
 */
function removeScripts(doc) {
    doc.querySelectorAll("script,style,defs").forEach(n => n.parentElement.removeChild(n));
}
/**
 * For the purposes of referencing the ADL of an element on a palette
 */
export function getElementUri(e, palettePanel) {
    const paletteId = palettePanel.getAttribute("id");
    const id = e.getAttribute("id");
    return addQueryParam(palettePanel.getAttribute("k9-palette-uri"), "format", "adl") + "#" + id.substring(0, id.length - paletteId.length);
}
