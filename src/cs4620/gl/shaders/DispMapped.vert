#version 120

// Note: We multiply a vector with a matrix from the left side (M * v)!
// mProj * mView * mWorld * pos

// RenderCamera Input
uniform mat4 mViewProjection;
uniform float dispMagnitude;

// RenderObject Input
uniform mat4 mWorld;
uniform mat3 mWorldIT;

// RenderMesh Input
attribute vec4 vPosition; // Sem (POSITION 0)
attribute vec3 vNormal; // Sem (NORMAL 0)
attribute vec2 vUV; // Sem (TEXCOORD 0)

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world-space coordinates

void main() {
    // Calculate Point In World Space
    vec4 norm = getNormalColor(vUV);
    float displAmount = dispMagnitude*(norm.x + norm.y + norm.z)/3;
    vec4 newPosition = vPosition + displAmount*(vec4 (vNormal, 0));
    worldPos = mWorld * newPosition;
    // Calculate Projected Point
    gl_Position = mViewProjection * worldPos;
    
    // We have to use the inverse transpose of the world transformation matrix for the normal
    fN = normalize((mWorldIT * vNormal).xyz);
    fUV = vUV;
}
