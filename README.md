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
| <table><tr><th>Function</th><th>Keys</th></tr><tr><td>Fullscreen</td><td>F4 or Ctrl + Enter</td></tr><tr><td>Screenshot</td><td>F12 or Ctrl + P</td></tr><tr><td>Movement</td><td>Arrows or Edge-Scroll</td></tr><tr><td>Rotating</td><td>Q/E or Scroll</td></tr><tr><td>Reset</td><td>R</td></tr></table> | <table><tr><th>Function</th><th>Keys</th></tr><tr><td>Chunk Borders</td><td>B</td></tr><tr><td>Info</td><td>F8</td></tr><tr><td>Sun Yaw</td><td>Y/U</td></tr></table> |

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
- *Terrible optimization at maximum camera distance*
- *Incorrect positioning of block outlines relative to the cursor*
- *Disgusting code structure and complete lack of understanding of Java/LWJGL by the lead developer*


***
### Build Troubleshooting
- If you see `Could not find or load main class org.gradle.wrapper.GradleWrapperMain`, verify the wrapper jar exists in `gradle/wrapper/gradle-wrapper.jar` and re-checkout the branch (the file is committed in this repo).
- If you see `Unsupported class file major version 69`, run Gradle with JDK 21 explicitly:
  - `JAVA_HOME=$HOME/.local/share/mise/installs/java/21 PATH=$HOME/.local/share/mise/installs/java/21/bin:$PATH ./gradlew compileJava`
- Project bytecode target is Java 8 compatibility layer (configured in `build.gradle`).
