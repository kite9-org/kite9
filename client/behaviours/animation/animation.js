import { parseTransform, number, handleTransformAsStyle } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.7'
import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.7'

/**
 * Somehow, someday, make these extensible
 */
const numeric = ['width', 'height', 'x', 'y', 'rx', 'ry'];
const editorClasses = ['selected', 'lastSelected'];

function reconcileTransform(fromValue, toValue, start, end) {
  var fromT = parseTransform(fromValue);
  var toT = parseTransform(toValue);
  for (var i in fromT) start[i] = fromT[i];
  for (var i in toT) end[i] = toT[i];
}

function reconcileStyles(fromElement, toElement, tl, start, end) {
  var toStyles = Array.from(toElement.style);
  var fromStyles = Array.from(fromElement.style);
  var toRemove = fromStyles.filter(a => -1 == toStyles.indexOf(a));

  toRemove.forEach(a => fromElement.style[a] = undefined);

  toStyles.forEach(a => {

    var fromValue = fromElement.style[a];
    var toValue = toElement.style[a];

    if (fromValue !== toValue) {
      if (numeric.indexOf(a) != -1) {
        end[a] = number(toValue);
        start[a] = number(fromValue);
      } else if (a == 'transform') {
        reconcileTransform(fromValue, toValue, start, end);
      } else {
        // just change text
        fromElement.style[a] = toValue;
      }
    }
  })
}

function reconcileClasses(fromElement, toElement, tl, start, end) {
  var toClasses = Array.from(toElement.classList);
  var fromClasses = Array.from(fromElement.classList);
  
  var toRemove = fromClasses.filter(a => -1 == toClasses.indexOf(a));
  var toAdd = toClasses.filter(a => -1 == toClasses.indexOf(a));
  toRemove = toRemove.filter(a => -1 == editorClasses.indexOf(a));
  
  toRemove.forEach(a => fromElement.classList.remove(a));
  toAdd.forEach(a => fromElement.classList.add(a));
}

function reconcileAttributes(fromElement, toElement, tl) {
  var toAtts = Array.from(toElement.attributes).map(a => a.name);
  var fromAtts = Array.from(fromElement.attributes).map(a => a.name);
  var toRemove = fromAtts.filter(a => -1 == toAtts.indexOf(a));

  var start = {
    delay: 0,
    duration: 0
  };
  var end = {
    duration: 1000
  };

  toRemove.forEach(a => fromElement.removeAttribute(a));
  toAtts.forEach(a => {
    var fromValue = fromElement.getAttribute(a);
    var toValue = toElement.getAttribute(a);

    if ((a.startsWith("xlink:")) && (fromValue !== toValue)) {
      fromElement.setAttributeNS("http://www.w3.org/1999/xlink", a.substring(a.indexOf(':')+1), toValue);
    } else if (fromValue == null) {
      fromElement.setAttribute(a, toValue);
    } else if (fromValue !== toValue) {
      if (numeric.indexOf(a) != -1) {
        end[a] = number(toValue);
        start[a] = number(fromValue);
      } else if (a == 'style') {
        if (fromElement.tagName != 'svg') {
          reconcileStyles(fromElement, toElement, tl, start, end);
        }
      } else if (a == 'class') {
        reconcileClasses(fromElement, toElement, tl, start, end); 
      } else if (a == 'd') {
        end[a] = toValue;
        start[a] = fromValue;
      } else {
        fromElement.setAttribute(a, toValue);
      }
    }

  });

  if (fromElement.style){
    tl.add({
      targets: fromElement,
      keyframes: [start, end],
    }, 0);
  }

}

function reconcileText(fromElement, toElement) {
  if ((fromElement.tagName == 'script') && ("adl:markup" != fromElement.getAttribute("id"))) {
    // we don't reconcile scripts as this means doing a lot of 
    // parsing / reloading js
    return;
  }
  
  if (fromElement.textContent != toElement.textContent) {
    fromElement.textContent = toElement.textContent;
  }
}

function getLocalTranslate(e) {
  const t = parseTransform(e.style.transform);
  return {
    x: t.translateX,
    y: t.translateY
  }
}

function getTotalTranslate(e) {
  if ((e == null) || (e.style == null)) {
    return { x: 0, y: 0 };
  }

  const t = getLocalTranslate(e);
  const pt = getTotalTranslate(e.parentElement);
  const out = {
    x: t.x + pt.x,
    y: t.y + pt.y
  }
  return out;
}

function removeNonElementContent(el) {
  var child = el.firstChild;
  var nextChild;

  while (child) {
      nextChild = child.nextSibling;
      if (child.nodeType == 3) {
          el.removeChild(child);
      }
      child = nextChild;
  }
}


function reconcileElement(inFrom, inTo, toDelete, tl) {
  const isMainSvg = (inFrom.tagName == 'svg');
  //console.log("Reconciling " + inFrom.tagName + ' with ' + inTo.tagName + " " + inFrom.getAttribute("id") + " " + inTo.getAttribute("id"))
  
  if (!isMainSvg) {
    handleTransformAsStyle(inFrom);
    handleTransformAsStyle(inTo);
  }

  if (inTo.childElementCount == 0) {
    reconcileText(inFrom, inTo);
  } else {
    var ti = 0;
    var fi = 0;
    
    removeNonElementContent(inFrom);
    removeNonElementContent(inTo);
    
    while (ti < inTo.childElementCount) {
      const toElement = inTo.children.item(ti);
      const fromElement = (fi < inFrom.childElementCount) ? inFrom.children.item(fi) : null;

      if (toElement.hasAttribute("id")) {
        // ideally, we need to merge
        const toId = toElement.getAttribute("id");
        const fromId = fromElement == null ? null : fromElement.getAttribute("id");
        const missingFrom = inFrom.ownerDocument.getElementById(toId);

        if (toId == fromId) {
          // to/from correspond
          reconcileElement(fromElement, toElement, toDelete, tl);
          fi++;
          ti++;
        } else if (missingFrom != null) {
          // from element has moved
          handleTransformAsStyle(missingFrom);
          const parentFromTranslate = getTotalTranslate(missingFrom.parentElement);
          const parentToTranslate = getTotalTranslate(inFrom);
          const localFromTranslate = getLocalTranslate(missingFrom);
          const newTranslate = {
            x: localFromTranslate.x + parentFromTranslate.x - parentToTranslate.x,
            y: localFromTranslate.y + parentFromTranslate.y - parentToTranslate.y
          };
          //console.log("from moving" + newTranslate);
          inFrom.insertBefore(missingFrom, fromElement);
          missingFrom.style.transform = "translateX(" + newTranslate.x + "px) translateY(" + newTranslate.y + "px)";
          reconcileElement(missingFrom, toElement, toDelete, tl);
          ti++;
          fi++;
        } else {
          // to element is new  
          //console.log("creating new element " + toElement.tagName)
          const newFromElement = document.createElementNS(toElement.namespaceURI, toElement.tagName);
          inFrom.insertBefore(newFromElement, fromElement);
          reconcileElement(newFromElement, toElement, toDelete, tl);
          fi++;
          ti++;
        }
      } else {
        // here, we have non-id elements, so we really just need 
        // to be sure there are the right number

        if ((fromElement == null) || (fromElement.tagName != toElement.tagName) || (fromElement.hasAttribute("id"))) {
          // treat as an insertion.
          //console.log("creating new element " + toElement.tagName)
          const newFromElement = document.createElementNS(toElement.namespaceURI, toElement.tagName);
          inFrom.insertBefore(newFromElement, fromElement);
          reconcileElement(newFromElement, toElement, toDelete, tl);
          fi++;
          ti++;
        } else {
          // assume it's the same element (tags match, after all)
          reconcileElement(fromElement, toElement, toDelete, tl);
          fi++;
          ti++;
        }
      }

    }

    while (fi < inFrom.childElementCount) {
      const fromElement = inFrom.children.item(fi);
      //console.log("removing " + fromElement);
      if (fromElement == toDelete) {
        toDelete.parentElement.removeChild(toDelete);
      } else  if (fromElement.tagName == 'g') {
        handleTransformAsStyle(fromElement);
        const totalTranslate = getTotalTranslate(fromElement);
        toDelete.appendChild(fromElement);
        fromElement.style.transform = "translateX(" + totalTranslate.x + "px) translateY(" + totalTranslate.y + "px) ";
      } else {
        toDelete.appendChild(fromElement);
      }
    }
  }
  
  if (!isMainSvg) {
    reconcileAttributes(inFrom, inTo, tl);
  }

}

/**
 * This handles the process of loading new SVG and animating between the old one and the new one.
 */
export function initTransitionAnimationCallback() {
  
 
  return function(changeTo, animationTimeline) {
    document.body.style.cursor = null;

    // this will store everything we'll eventually remove
    var svg = getMainSvg();
    var toDelete = svg.ownerDocument.createElementNS(svg.namespaceURI, "g");
    svg.appendChild(toDelete);
    toDelete.setAttribute('id', '_deleteGroup');

    reconcileElement(svg, changeTo, toDelete, animationTimeline);
  } 
  
}