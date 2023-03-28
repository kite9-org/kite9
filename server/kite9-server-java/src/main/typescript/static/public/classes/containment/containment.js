/**
 * Handles drag and drop rules, as well as surround/contain and insert/replace.
 */
export class Containment {
    constructor() {
        this.containmentRuleCallbacks = [];
        this.typeCallbacks = [];
        this.containsCallbacks = [];
    }
    addContainmentRuleCallback(cb) {
        this.containmentRuleCallbacks.push(cb);
    }
    addTypeCallback(cb) {
        this.typeCallbacks.push(cb);
    }
    addContainsCallback(cb) {
        this.containsCallbacks.push(cb);
    }
    getTypes(element) {
        return new Set(this.typeCallbacks
            .map(cb => cb(element))
            .filter(r => r != undefined));
    }
    getContainsTypes(element) {
        return new Set(this.containsCallbacks
            .flatMap(cb => cb(element)));
    }
    contains(elementTypes, containsTypes) {
        return this.containmentRuleCallbacks.map(cb => cb(elementTypes, containsTypes))
            .reduce((a, b) => a && b);
    }
    /**
     * This is the main function.  elements is the set of elements that will be reduced and returned.
     */
    allowed(elements, parent) {
        const parentTypes = this.getContainsTypes(parent);
        return elements.filter((e, i) => this.contains(this.getTypes(e), parentTypes));
    }
    /**
     * Helper function
     */
    canContainAny(element, parent) {
        return this.allowed(Array.isArray(element) ? element : [element], parent).length == 1;
    }
    /**
     * Helper function
     */
    canContainAll(element, parent) {
        const needed = Array.isArray(element) ? element.length : 1;
        return this.allowed(Array.isArray(element) ? element : [element], parent).length == needed;
    }
    canReplace(newElement, oldElement) {
        const eqSet = (xs, ys) => xs.size === ys.size &&
            [...xs].every((x) => ys.has(x));
        return eqSet(this.getTypes(newElement), this.getTypes(oldElement));
    }
}
/**
 * Useful for constructing ContainmentRuleCallbacks
 */
export function intersects(set1, set2) {
    if (set1.has("*")) {
        return set2.size > 0;
    }
    else if (set2.has("*")) {
        return set1.size > 0;
    }
    else {
        for (const elem of set2) {
            if (set1.has(elem)) {
                return true;
            }
        }
    }
}
