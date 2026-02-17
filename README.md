&nbsp;&nbsp;&nbsp;&nbsp;
   <A HREF="http://theintraclinic.com">
  <IMG SRC="https://github.com/stomarver/dot-ruby/blob/main/IntraClinic.png?raw=true">
</A>
&nbsp;&nbsp;&nbsp;&nbsp;
   <A HREF="https://discord.gg/AQtjxJDatu">
  <IMG SRC="https://github.com/stomarver/dot-ruby/blob/main/DotRuby.png?raw=true">
</A>

***
| Controls | Debug |
|----------|-------|
| <table><tr><th>Function</th><th>Keys</th></tr><tr><td>Fullscreen</td><td>F4 or Ctrl + Enter</td></tr><tr><td>Screenshot</td><td>F12 or Ctrl + P</td></tr><tr><td>Movement</td><td>Arrows or Edge-Scroll</td></tr><tr><td>Rotating</td><td>Q/E or Scroll</td></tr><tr><td>Reset</td><td>R</td></tr></table> | <table><tr><th>Function</th><th>Keys</th></tr><tr><td>Chunk Borders</td><td>B</td></tr><tr><td>Info</td><td>F8</td></tr><tr><td>Virtual/Real 3D</td><td>F7</td></tr></td></tr><tr><td>Sun Yaw</td><td>Y/U</td></tr></table></table> |

***
 ### Runtime
 - Target Java version: **17**

***
  ### Features
 - ~~You can bomb up a cobble blocks by clicking near of it.~~ (implemented in future)
 - You can write a text from term**o**nal (when you launch game from it)
 - Screenshots saving in
     - win [USER\Picures\SwordsGame\] 
     - linux [~/Pictures/SwordsGame/]
 - Working Discord RPC!
 - Weird Graphics

***
  ### Known Issues
- All...

_...project urgently needs REAL Java developers who understand LWJGL, OpenGL, Groovy (for writing extensible modding syntax), and how the overall structure and logic of Multiplayer RTS should look.
Currently, the project is being written by an incompetent vibe coder who only knows how to create textures and models, can set the basic concept of the game, and... that's about it... (pls help us)_


***
### Architecture (client/server/shared)
- `SwordsGame.client.*` — rendering, input, window lifecycle, HUD/UI, local client integrations.
- `SwordsGame.server.*` — world simulation, terrain/chunks, sun/environment, RTS gameplay templates, server-side UI composition.
- `SwordsGame.shared.*` — protocol/data contracts used by both sides (for future multiplayer transport).

Current server->client UI example in code:
- `SwordsGame.server.ui.ServerUiComposer` builds `UiFrameState`/`UiPanelState`.
- `SwordsGame.client.core.Base` and `Debug` consume this state and render it through HUD.