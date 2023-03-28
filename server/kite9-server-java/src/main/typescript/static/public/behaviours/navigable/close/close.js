import { icon } from '../../../bundles/form.js';
let closeUrl;
let navigator;
let metadata;
let editor = false;
export const closeMetadataCallback = (md) => {
    metadata = md;
    closeUrl = metadata['close'];
    updateClose();
};
function updateClose() {
    if (navigator) {
        const existing = navigator.querySelector("#_close");
        let newClose;
        if (!editor) {
            const image = '/public/behaviours/navigable/close/viewer.svg';
            newClose = icon('_close', 'Close Viewer', image, function () {
                window.location.href = closeUrl;
            });
        }
        else {
            const hasCommits = (metadata['committing'] != undefined) && (metadata['committing'] != '0');
            const canClose = (!hasCommits) && (closeUrl);
            const image = hasCommits ? '/public/behaviours/navigable/close/waiting.svg' :
                (canClose ? '/public/behaviours/navigable/close/cloud.svg' :
                    '/public/behaviours/navigable/close/cloud-minus.svg');
            const pop = canClose ? 'Close Editor' : (hasCommits ? 'Changes Pending: ' + metadata['committing'] : "Can't Save Here");
            newClose = icon('_close', pop, image, function () {
                if (canClose) {
                    window.location.href = closeUrl;
                }
            });
        }
        if (existing == undefined) {
            navigator.appendChild(newClose);
        }
        else {
            navigator.replaceChild(newClose, existing);
        }
    }
}
export function initCloseInstrumentationCallback(isEditor) {
    editor = isEditor;
    return function (nav) {
        navigator = nav;
        updateClose();
    };
}
