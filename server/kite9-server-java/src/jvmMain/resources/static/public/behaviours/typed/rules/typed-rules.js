import { intersects } from '../../../classes/containment/containment.js';
export function initTypedRulesContainsCallback() {
    return (element) => {
        if (element == undefined) {
            return [];
        }
        const attrValue = element.getAttribute('k9-contains');
        if (attrValue) {
            return attrValue.split(" ") || [];
        }
        return [];
    };
}
export function initTypedRulesTypeCallback() {
    return (element) => {
        if (element == undefined) {
            return '';
        }
        const attrValue = element.getAttribute('k9-type');
        return attrValue;
    };
}
/**
 * Allows inclusion where any of the elementTypes is contained in the containsTypes (also allows wildcard * on either side)
 */
export function initTypedRulesContainmentRuleCallback() {
    return function (elementTypes, containsTypes) {
        return intersects(elementTypes, containsTypes);
    };
}
/**
 * Creates a filter whereby two elements must have a certain combination of element types.
 */
export function initBiFilter(containment, childAllowedTypes, parentAllowedTypes) {
    const childSet = new Set(childAllowedTypes);
    const parentSet = new Set(parentAllowedTypes);
    return function (e1, e2) {
        const e1Types = e1 ? containment.getTypes(e1) : new Set();
        const e2Types = e2 ? containment.getTypes(e2) : new Set();
        const e1Ok = intersects(e1Types, childSet);
        const e2Ok = intersects(e2Types, parentSet);
        return e1Ok && e2Ok;
    };
}
