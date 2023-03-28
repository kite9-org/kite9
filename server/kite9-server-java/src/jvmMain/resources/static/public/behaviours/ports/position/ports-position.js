import { getMainSvg } from '../../../bundles/screen.js';
import { fieldset, select, numeric, hidden, formObject } from '../../../bundles/form.js';
export const portsPositionIcon = '/public/behaviours/ports/port.svg';
const PORT_POSITION = '--kite9-port-position';
const props = [PORT_POSITION];
const positionRegex = /^([0-9\-.]+)(px|%)$/;
function parsePosition(str) {
    if (str) {
        const matches = str.match(positionRegex);
        if (matches.length > 0) {
            return {
                amount: matches[1],
                unit: matches[2]
            };
        }
    }
    return {
        amount: '',
        unit: '%'
    };
}
export function initPortsSelector(doc = getMainSvg()) {
    return () => {
        return Array.from(doc.querySelectorAll("[id][k9-ui~=port].selected"));
    };
}
export function initPortsPositionBuildControls() {
    return function (_selectedElement, style) {
        const position = style[PORT_POSITION];
        const { amount, unit } = parsePosition(position);
        return [fieldset('Port Position', [
                numeric('amount', amount, { name: undefined, id: 'amount' }),
                select('unit', unit, { name: undefined, id: 'unit' }, ['%', 'px']),
                hidden(PORT_POSITION, style[PORT_POSITION])
            ])];
    };
}
export const initPortsPositionChangeEvent = () => {
    return () => {
        // update hidden field
        const form = formObject();
        const amount = form.querySelector('#amount').value;
        const unit = form.querySelector('#unit').value;
        const pos = form.querySelector('#' + PORT_POSITION);
        if (amount) {
            pos.value = amount + unit;
        }
        else {
            pos.value = undefined;
        }
    };
};
