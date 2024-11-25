# Themis

## Todo list

### in progress

In progress : 🔲 Done : ✅

* Engine
  * ✅ Window
  * 🔲 Input 
  * ✅ Configuration loading
  * ✅ Configuration
    * ✅ application.name
    * ✅ application.version
    * ✅ engine.name
    * ✅ engine.version
    * ✅ window.width
    * ✅ window.height
    * ✅ window.resizable
    * ✅ window.maximized
### to do
* Engine
    * 🔲 Main Structure
* Scene
    * Configuration
      * scene.projection.fov
      * scene.projection.znear
      * scene.projection.zfar
* Renderer
  * Configuration
    * renderer.vsync
    * renderer.msaa.enabled
    * renderer.msaa.value
### done
    



## Engine

    Engine
        -> configuration : Configuration
        -> window : Window
        -> input : Input
        -> renderer : Renderer
        -> gamestate : GameState

## Model

    Mesh
        -> vertices : Vertex []
        -> indices : int []
        -> material : Material

    Instance
        -> parent : Model (
        -> transformation : Matrix4f

    Model
        -> meshes : Mesh []
        -> instances : Instance []

## Light

    Light (abstract)
        -> enabled : boolean
        -> ambient : Vector3f
        -> diffuse : Vector3f
        -> specular : Vector3f

    DirectionalLight : Light
        -> direction : Vector3f

    Attenuation
        -> data : Vector4f

    PointLight : Light
        -> position : Vector3f
        -> attenuation : Attenuation

    SpotLight : Light
        -> position : Vector3f
        -> direction : Vector3f
        -> attenuation : Attenuation
        -> innerCutOf : float
        -> outerCutOf : float

## Scene

    Camera

    Projection

    Scene
        -> actors : Instance []
        -> lDirectional : DirectionalLight
        -> lPoint : PointLight []
        -> lSpot : SpotLight []
        -> camera : Camera
        -> projection : Projection
