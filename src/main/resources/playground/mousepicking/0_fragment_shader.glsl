#version 450

layout(location = 0) out uvec4 outIdentifier;

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

/******* PUSHCONSTANT - Instance Data ******************/
layout(push_constant) uniform PushConstant {
    layout( offset = 64 ) uvec4 identifier;
} instance;

void main() {
    outIdentifier = instance.identifier;
}
