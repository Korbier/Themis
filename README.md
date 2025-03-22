## Description 
Themis is a Vulkan rendering engine written in Java.
The viewer is a sample application built with this engine.

## Features

### ✨ Done

* Renderer
  * Resource loading
    * Model
    * Texture
    * Font (Standard and Signed Distance Field Fonts)
    * Shader source
  * Light casters
    * Directional
    * Point
    * Spot
  * Material
    * Basic color material (Phong lighting)
  * Mouse picking
  * Post processing
    * TBN display
  * Immediate Mode UI
    * Button
    * Toggle button
    * Panel
    * Label
  
* Viewer
  * Sample scene
  * Key mapping
  * User interface
    * Toggle TBN
    * Toggle main light

* Other
  * Native executable building with GraalVM Native Image
 
### 📝 Todo

* Renderer
  * Material
    * Basic texture material (Phong lighting)
    * PBR material
  * Post processing
    * Grid Display
    
* Viewer
  * Mouse picking

# Screenshots
### Default view
 
![](./documentation/screenshots/001.png)

### With TBN vectors

![](./documentation/screenshots/002.png)

### UI Panel & Toggle button
 
![](./documentation/screenshots/003.png)

 
 
