#version 450

layout (location = 0) out vec2 outTextCoord;

/**
Here you can see how we are able to draw a triangle even if we are not passing the coordinates to the shader.
We use the gl_VertexIndex builtin variable that will have the number of the vertex that we are drawing.
In our case it will have the values 0, 1 and 3. You can check that for the first vertex, index 0, we will get (-1, 1, 0, 1) as the position coordinates
and (0, 0) as the texture coordinates. For the second vertex we will get (3, 1, 0, 1) as the position coordinates and (2, 0) as the texture coordinates.
For the third vertex we will get (-1, -3, 0, 1) as the position coordinates and (0, 2) as the texture coordinates.
**/
void main()
{
    outTextCoord = vec2((gl_VertexIndex << 1) & 2, gl_VertexIndex & 2);
    gl_Position  =  vec4(outTextCoord.x * 2.0f - 1.0f, outTextCoord.y * -2.0f + 1.0f, 0.0f, 1.0f);
}