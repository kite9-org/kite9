import { ensureCss } from '../../bundles/ensure.js';
export class Modal {
    constructor(id) {
        this.id = id;
        ensureCss('/public/classes/modal/modal.css');
        let darken = document.getElementById("_darken");
        if (!darken) {
            darken = document.createElement("div");
            darken.setAttribute("id", "_darken");
            darken.setAttribute("class", "darken");
            document.querySelector("body").appendChild(darken);
            darken.style.display = 'none';
        }
        let modal = document.getElementById(this.id);
        if (!modal) {
            // create modal
            modal = document.createElement("div");
            modal.setAttribute("id", this.id);
            modal.setAttribute("class", "modal");
            document.querySelector("body").appendChild(modal);
            // create content area
            const content = document.createElement("div");
            content.setAttribute("class", "content");
            modal.appendChild(content);
        }
    }
    getId() {
        return this.id;
    }
    getContent() {
        return document.getElementById(this.id).querySelector("div.content");
    }
    /**
     * Removes all the content
     */
    clear() {
        const content = this.getContent();
        Array.from(content.children).forEach(e => {
            content.removeChild(e);
        });
    }
    open() {
        const darken = document.getElementById("_darken");
        const modal = document.getElementById(this.id);
        modal.style.visibility = 'visible';
        darken.style.display = 'block';
        return modal;
    }
    destroy() {
        const modal = document.getElementById(this.id);
        const darken = document.getElementById("_darken");
        modal.style.visibility = 'hidden';
        darken.style.display = 'none';
    }
}
