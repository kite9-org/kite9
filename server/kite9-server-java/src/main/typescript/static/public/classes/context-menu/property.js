/**
 * Handles property functions where there might need to be pluggable behaviour.
 * A property is represented as a subset of the context menu for controlling a single property.
 */
export class Property {
    constructor(name) {
        this.formCallbacks = [];
        this.setCallbacks = [];
        this.name = name;
    }
    formCallback(cb) {
        this.formCallbacks.push(cb);
    }
    setCallback(cb) {
        // nb: additions are added to front of array
        this.setCallbacks.unshift(cb);
    }
    /**
     * Adds controls to the contextMenu for editing the property.
     */
    populateForm(contextEvent, contextMenu, selectedElements) {
        this.formCallbacks.forEach(fc => fc(this, contextEvent, contextMenu, selectedElements));
    }
    /**
     * Use this as the callback for "ok" buttons on the property to ensure all callbacks
     * are done.
     */
    setProperty(contextEvent, formEvent, contextMenu, selectedElements) {
        this.setCallbacks.forEach(fc => fc(this, contextEvent, formEvent, contextMenu, selectedElements));
        contextMenu.destroy();
    }
}
