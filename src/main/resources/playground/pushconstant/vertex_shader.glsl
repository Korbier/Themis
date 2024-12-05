#version 450

const vec3 positions[3] = vec3[3](
    vec3(-0.5f, -0.5f, 0.0f),
    vec3( 0.5f, -0.5f, 0.0f),
    vec3( 0.0f,  0.5f, 0.0f)
);

layout(push_constant) uniform pushConstant {
    layout( offset = 0 ) vec3 position;
};

void main()
{
    gl_Position = vec4( position + positions[gl_VertexIndex], 1.0f);
}