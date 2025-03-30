## Description 
Themis is a Vulkan rendering engine written in Java.
The viewer is a sample application built with this engine.

# Screenshots
### Sphere (all lights on / With tangent,bitangent and normal vectors / Spot light only)
![](documentation/sphere/001.png)
![](documentation/sphere/002.png)
![](documentation/sphere/003.png)

### Shark (Color material / Texture material) ([The model on https://sketchfab.com/](https://sketchfab.com/3d-models/anthro-shark-6e9d487cbce94dd58a4591fec7aff8f9))
![](./documentation/shark/001.png)
![](./documentation/shark/003.png)

### Wall (Without normal mapping / Widh normal mapping)
![](documentation/wall/001.png)
![](documentation/wall/002.png)

## Features
### ✨ Done

* Renderer
  * Resource loading
    * Model
    * Texture
    * Font
    * Shader source
  * Light casters
    * Directional
    * Point
    * Spot
  * Mouse picking
  * Material system
  * Post processing render pass
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
    * Toggle directional light
    * Toggle point light
    * Toggle spot light
    * Toggle Normal mapping
  * Mouse control
  * Material
    * Color material
    * Texture material + Normal mapping
  
* Other
  * Native executable building with GraalVM Native Image
