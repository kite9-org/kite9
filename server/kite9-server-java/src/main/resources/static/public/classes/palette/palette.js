import { suffixIds, addQueryParam } from '/public/bundles/api.js'
import { icon } from '/public/bundles/form.js'

/**
 * Provides functionality for populating/ showing/ hiding a palette.  
 */
export class Palette {

  constructor(id, uriList) {
    this.callbacks = [];
    this.paletteMap = [];
    this.expanded = {};
    this.updateCallbacks = [];

    var done = [];

    uriList = uriList == undefined ? [] : uriList;

    for (var i = 0; i < uriList.length; i += 2) {
      const uri = uriList[i];
      const selector = uriList[(i + 1)];

      if (!done.includes(uri)) {
        this.paletteMap.push({
          number: i / 2,
          uri: uri,
          selector: selector
        });
        done.push(uri);
      }
    }

    this.id = (id == undefined) ? "_palette" : id;

    var cssId = 'palette';
    if (!document.getElementById(cssId)) {
      var head = document.getElementsByTagName('head')[0];
      var link = document.createElement('link');
      link.id = cssId;
      link.rel = 'stylesheet';
      link.type = 'text/css';
      link.href = '/public/classes/palette/palette.css';
      link.media = 'all';
      head.appendChild(link);
    }

    var darken = document.getElementById("_darken");
    if (!darken) {
      darken = document.createElement("div");
      darken.setAttribute("id", "_darken");
      darken.setAttribute("class", "darken");
      document.querySelector("body").appendChild(darken);
      darken.style.display = 'none';
    }

    var palette = document.getElementById(this.id);
    if (!palette) {
      // create palette
      palette = document.createElement("div");
      palette.setAttribute("id", this.id);
      palette.setAttribute("class", "palette indicators-on");
      document.querySelector("body").appendChild(palette);

      // create area for control buttons
      var control = document.createElement("div");
      control.setAttribute("class", "control");
      palette.appendChild(control);

      // create concertina area
      var concertina = document.createElement("div");
      concertina.setAttribute("class", "concertina");
      palette.appendChild(concertina);

      this.paletteMap.forEach(p => this.loadPalette(p, concertina));
    }
  }

  loadPalette(p, concertina) {
    var id = "_palette-" + p.number;
    var item = document.createElement("div");
    item.setAttribute("k9-palette", p.selector);
    item.setAttribute("class", "palette-item");
    item.setAttribute("k9-palette-uri", p.uri);
    item.setAttribute("id", id);
    concertina.appendChild(item);

    // create loading indicator
    var loading = document.createElement("img");
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
      var parser = new DOMParser();
      return parser.parseFromString(text, "image/svg+xml");
    })
    .then(doc => {
      // set new ids
      removeScripts(doc);
      const diagramElements = doc.querySelectorAll("[k9-elem][id]");
      suffixIds(diagramElements, id);
      item.appendChild(doc.documentElement);
      item.removeChild(loading);

      this.callbacks.forEach(cb => {
        cb(this, item);
      })

      var evt = document.createEvent('Event');
      evt.initEvent('DOMContentLoaded', false, false);
      window.dispatchEvent(evt);
    })
    .catch(e => {
      alert("Problem loading palette: " + e);
    })
  }


  add(cb) {
    this.callbacks.push(cb);
  }
  
  addUpdate(cb) {
	this.updateCallbacks.push(cb);
  }

  getId() {
    return this.id;
  }

  get(event) {
    return document.getElementById(this.id);
  }

  getOpenEvent() {
    return this.openEvent;
  }
  
  getOpenPanel() {
	return this.expanded[this.getCurrentSelector()];
  }

  getCurrentSelector() {
    return this.currentSelector;
  }

  getCurrentAction() {
    return this.currentAction;
  }

  open(event, selectorFunction, actionFunction) {
    this.openEvent = event;
    this.currentSelector = selectorFunction;
    this.currentAction = actionFunction;
    const _this = this;

    var darken = document.getElementById("_darken");
    var palette = document.getElementById(this.id);
    var concertina = palette.querySelector("div.concertina");
    var control = palette.querySelector("div.control");

    // hide palettes without the selector
    var toShow = [];

    palette.querySelectorAll("div.palette-item")
      .forEach(e => {
        if (selectorFunction(e)) {
          e.style.display = 'block';
          toShow.push(e);

          // highlight selectable items on the palette
          const diagramElements = e.querySelectorAll("[k9-palette][id]");
          diagramElements.forEach(de => {
            if (selectorFunction(de)) {
              de.style.cursor = 'grab';
              de.classList.remove('inactive');
            } else {
              de.classList.add('inactive');
              de.style.cursor = 'not-allowed';
            }
          })

        } else {
          e.style.display = 'none';
        }
      });

    // keep track of which palette we are showing
    var expandInfo = this.expanded;
    if (expandInfo[selectorFunction] == undefined) {
      expandInfo[selectorFunction] = toShow[0];
    };

    // remove old control buttons
    while (control.firstChild) {
      control.removeChild(control.firstChild);
    }

    // add cancel button
    var cancel = icon('palette-cancel', 'Close',  "/public/classes/palette/cancel.svg", (event) => this.destroy());
    control.appendChild(cancel);

    var paletteWidth = 100, paletteHeight = 100, width, height;
    var selectedDot;

    function expandPanel(e, dot) {
      const expanded = expandInfo[selectorFunction];
      expanded.style.maxHeight = "0px";
      e.style.maxHeight = height + "px";
      expandInfo[selectorFunction] = e;
      control.querySelectorAll("img").forEach(e => e.classList.remove("selected"));
      if (dot != null) {
        dot.classList.add("selected");
      }
      _this.updateCallbacks.forEach(cb => cb());
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
      const icon = diagram == undefined ? d  : diagram.getAttribute("icon");
      return icon  ? icon : d;
    }


    // display new control buttons and size the overall thing
    toShow.forEach((e) => {
      e.style.maxHeight = "0px";
      e.style.visibility = 'show';
      e.style.display = 'block';
      var svg = e.querySelector(":first-child");
      if (!(svg.tagName.toLowerCase() == 'img')) {
        paletteWidth = Math.max(svg.width.baseVal.valueInSpecifiedUnits, paletteWidth);
        paletteHeight = Math.max(svg.height.baseVal.valueInSpecifiedUnits, paletteHeight);
      }

      if (toShow.length > 1) {
        var dot = icon('', getTitle(e), getIcon(e), (event) => expandPanel(e, dot));
        dot.classList.remove("hint--bottom");
        dot.classList.add("hint--right");
        
        if (e == expandInfo[selectorFunction]) {
          selectedDot = dot;
        }
        control.appendChild(dot);
      }
    });

    // ensure the palette appears in the centre of the screen
    width = Math.min(paletteWidth + 30, window.innerWidth - 100);
    height = Math.min(paletteHeight + 30, window.innerHeight - 100);

    palette.style.marginTop = (-height / 2) + "px";
    palette.style.marginLeft = (-width / 2) + "px";
    concertina.style.width = (width) + "px";
    concertina.style.height = (height) + "px";
    palette.style.visibility = 'visible';
    darken.style.display = 'block';

    expandPanel(expandInfo[selectorFunction], selectedDot);

    return palette;
  }

  destroy() {
    this.currentAction = undefined;
    var palette = document.getElementById(this.id);
    var darken = document.getElementById("_darken");
    palette.style.visibility = 'hidden';
    darken.style.display = 'none';
    this.updateCallbacks.forEach(cb => cb());
  }
}


/**
 * These are not needed for palettes, because when you pull something off a palette, it will use the
 * main documents stylesheet.  Also, these interact with the main svg area, breaking the styling.
 */
function removeScripts(doc) {
  doc.querySelectorAll("script,style,defs").forEach(n => n.parentElement.removeChild(n));
}

export function initPaletteHoverableAllowed(palette) {

  return function(v) {
    const currentSelector = palette.getCurrentSelector();
    return currentSelector(v);
  }

}

/**
 * For the purposes of referencing the ADL of an element on a palette
 */
export function getElementUri(e, palettePanel) {
	var paletteId = palettePanel.getAttribute("id");
	var id = e.getAttribute("id");
	return addQueryParam(palettePanel.getAttribute("k9-palette-uri"), "format", "adl")+"#"+ id.substring(0, id.length - paletteId.length);	
}