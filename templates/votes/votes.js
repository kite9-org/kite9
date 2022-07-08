import { contextMenu, command, metadata } from '/github/kite9-org/kite9/templates/editor/editor.js?v=v0.2'
import { initVoteContextMenuCallback } from '/github/kite9-org/kite9/behaviours/voting/vote/vote.js?v=v0.2' 
import { once } from '/github/kite9-org/kite9/bundles/ensure.js?v=v0.2'

once(() => contextMenu.add(initVoteContextMenuCallback(command, metadata)));