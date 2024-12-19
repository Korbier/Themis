#version 450

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texture;
layout(location = 3) in vec3 tangent;
layout(location = 4) in vec3 bitangent;

/******* 0 - Global Data ******************/
layout(std140, set = 0, binding = 0) uniform Global {
    mat4 projection;
    mat4 view;
    mat4 projectionInv;
    mat4 viewInv;
    vec4 camera;
    vec2 resolution;
    uint utime;
} global;


/******* PUSH - Instance Data ******************/
layout(push_constant) uniform pushConstant {
    layout( offset = 0 ) mat4 matrix;
} instance;

void main()
{
    gl_Position = global.projection * global.view * instance.matrix * vec4(position, 1.0f);
}