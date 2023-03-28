import { describe, it, expect, getNamedEventListener } from '../../bundles/monika.js';
import { getMainSvg } from '../../bundles/screen.js';
import { isLastSelected, isSelected } from '../../behaviours/selectable/selectable.js';
import { mouseEvent } from './fixture.js';
export const selectableTest = describe("Selectable Tests", async () => {
    it("select an element", async () => {
        const t1 = document.getElementById("t1");
        const mainSvg = getMainSvg();
        const l = getNamedEventListener("selectable", mainSvg);
        const me = mouseEvent(t1, "click");
        l(me);
        // make sure element is sslected
        expect(isSelected(t1)).toEqual(true);
        expect(isLastSelected(t1)).toEqual(true);
        l(me);
        // make sure element is unsslected
        expect(isSelected(t1)).toEqual(false);
        expect(isLastSelected(t1)).toEqual(false);
    });
});
