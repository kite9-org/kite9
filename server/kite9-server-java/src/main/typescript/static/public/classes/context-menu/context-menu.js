import { getHtmlCoords } from '../../bundles/screen.js';
import { ensureCss } from '../../bundles/ensure.js';
import { icon, fieldset, form, DEFAULT_FORM_ID } from '../../bundles/form.js';
import { number } from '../../bundles/api.js';
/**
 * Provides functionality for populating the context menu.  Takes a number of callbacks
 * that provide functionality when the user asks the context menu to appear.
 */
export class ContextMenu {
    constructor() {
        this.menu = undefined;
        this.moving = undefined;
        this.callbacks = [];
        this.offsetCoords = undefined;
        this.eventCoords = undefined;
        ensureCss('/public/classes/context-menu/context-menu.css');
        this.eventCoords = undefined;
        document.addEventListener("mouseup", () => this.moving = undefined);
        document.addEventListener("mousemove", (e) => this.move(e));
        document.addEventListener("touchend", () => this.moving = undefined);
        document.addEventListener("touchmove", (e) => this.move(e), { passive: false });
    }
    move(e) {
        if (this.moving) {
            const ec = getHtmlCoords(e);
            this.moving.style.left = (ec.x + this.offsetCoords.x) + "px";
            this.moving.style.top = (ec.y + this.offsetCoords.y) + "px";
            e.stopPropagation();
            e.preventDefault();
            return false;
        }
    }
    start(e) {
        if (e.target == this.menu) {
            this.eventCoords = getHtmlCoords(e);
            this.offsetCoords = {
                x: number(this.menu.style.left) - this.eventCoords.x,
                y: number(this.menu.style.top) - this.eventCoords.y
            };
            this.moving = this.menu;
        }
    }
    end() {
        // used to close the context menu, wasn't helpful.
    }
    add(cb) {
        this.callbacks.push(cb);
    }
    /**
     * Creates the context menu within the main svg element,
     * positioning it relative to the event that created it.
     */
    get(event) {
        let theForm = document.querySelector("#" + DEFAULT_FORM_ID);
        if (theForm) {
            return theForm;
        }
        else {
            const ctxMenu = document.createElement("div");
            ctxMenu.setAttribute("id", "contextMenu");
            ctxMenu.setAttribute("class", "contextMenu");
            ctxMenu.setAttribute("draggable", "false");
            theForm = form([], DEFAULT_FORM_ID);
            ctxMenu.appendChild(theForm);
            const coords = getHtmlCoords(event);
            coords.x += 15;
            coords.y -= 20;
            ctxMenu.style.left = coords.x + "px";
            ctxMenu.style.top = coords.y + "px";
            ctxMenu.addEventListener("mousedown", (e) => this.start(e));
            ctxMenu.addEventListener("mouseup", () => this.end());
            ctxMenu.addEventListener("touchstart", (e) => this.start(e));
            ctxMenu.addEventListener("touchend", () => this.end());
            document.querySelector("body").appendChild(ctxMenu);
            this.menu = ctxMenu;
            return theForm;
        }
    }
    /**
     * Call this when the user clicks on an element that might need a context menu.
     */
    handle(event) {
        this.callbacks.forEach(cb => cb(event, this));
    }
    /**
     * Removes the context menu from the screen.
     */
    destroy() {
        const ctxMenu = document.querySelector("#contextMenu");
        if (ctxMenu) {
            ctxMenu.parentElement.removeChild(ctxMenu);
        }
    }
    fieldset(event, set) {
        const htmlElement = this.get(event);
        let fs = document.getElementById("#contextMenu-" + set);
        if (!fs) {
            fs = fieldset(set, [], { 'id': "#contextMenu-" + set });
            htmlElement.appendChild(fs);
        }
        return fs;
    }
    /**
     * Short-hand way of adding a single control to the context menu
     */
    addControl(event, imageUrl, title, clickListener = () => { }, set = "Actions", imageAtts = {}) {
        const fs = this.fieldset(event, set);
        const out = icon('_cm-' + title, title, imageUrl, clickListener, imageAtts);
        fs.appendChild(out);
        return out;
    }
    /**
     * Removes all the content from the context menu form
     */
    clear() {
        const theForm = document.querySelector('#' + DEFAULT_FORM_ID);
        Array.from(theForm.children).forEach(e => {
            theForm.removeChild(e);
        });
    }
}
