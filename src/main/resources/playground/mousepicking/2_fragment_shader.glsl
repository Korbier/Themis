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

/******* 1 - Mouse selection ******************/
layout ( std140, set = 1, binding = 0 ) readonly buffer Storage {
    vec4 identifier;
} selection;

/******* PUSHCONSTANT - Instance Data ******************/
layout(push_constant) uniform PushConstant {
    layout( offset = 64 ) vec4 identifier;
} instance;


void main() {
    if ( instance.identifier == selection.identifier ) {
        outFragColor = vec4( 1.0, 1.0, 1.0, 1.0 );
    } else {
        outFragColor = instance.identifier;
    }
}
