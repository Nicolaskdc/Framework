#version 120

// RenderCamera Input
uniform mat4 mViewProjection;

// RenderObject Input
uniform mat4 mWorld;

// RenderMesh Input
attribute vec4 vPosition; // Sem (POSITION 0)
attribute vec2 vUV; // Sem (TEXCOORD 0)

uniform float time;

const vec3 texture_scales = vec3(1.0, 2.0, 3.0);
const vec3 scroll_speeds = vec3(1.0, 1.0, 1.0);

varying vec2 fUV;
varying vec3 fPos;
const float PI = 3.14159265359;
const float FRACTIONAL_D = 0.2;


void main() {
    vec2 h = fUV * 100;
    
    vec2 s1 = fUV * texture_scales.x;
    vec2 s2 = fUV * texture_scales.y;
    vec2 s3 = fUV * texture_scales.z;
    
    vec2 t1 = vec2(s1.x, s1.y + (scroll_speeds.x*time));
    vec2 t2 = vec2(s2.x, s2.y + (scroll_speeds.y*time));
    vec2 t3 = vec2(s3.x, s3.y + (scroll_speeds.z*time));
    
    vec2 n1 = vec2(mod(t1.x,1.0), mod(t1.y,1.0));
    vec2 n2 = vec2(mod(t2.x,1.0), mod(t2.y,1.0));
    vec2 n3 = vec2(mod(t3.x,1.0), mod(t3.y,1.0));
    
    float radius = length(vPosition.xyz);
    
    fUV = vUV;
    fPos = vPosition.xyz*(1+((mod(h.x,1.0)+mod(h.y,1.0))/2)*FRACTIONAL_D);
    gl_Position = mViewProjection * (mWorld * vec4(fPos,1));
}