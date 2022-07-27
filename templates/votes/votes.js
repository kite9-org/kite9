import { contextMenu, command, metadata } from '/github/kite9-org/kite9/templates/editor/editor.js?v=v0.11'
import { initVoteContextMenuCallback } from '/public/behaviours/voting/vote/vote.js' 
import { once } from '/public/bundles/ensure.js'

once(() => contextMenu.add(initVoteContextMenuCallback(command, metadata)));