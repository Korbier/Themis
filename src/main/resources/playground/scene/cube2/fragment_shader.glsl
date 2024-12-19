#version 450

layout(location = 0) out vec4 outFragColor;

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

/******* 1 - Material ******************/
layout(std140, set = 1, binding = 0) uniform Material {
    vec4 color;
} material;

void main() {
    outFragColor = material.color;
}
