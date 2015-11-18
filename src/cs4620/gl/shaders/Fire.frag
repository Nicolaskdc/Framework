#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv) // samples fire.png
// vec4 getNormalColor(vec2 uv)  // samples noise.png

uniform float time;

const vec3 texture_scales = vec3(1.0, 2.0, 3.0);
const vec3 scroll_speeds = vec3(1.0, 1.0, 1.0)*2;

varying vec2 fUV;
varying vec3 fPos;

void main() {
    // TODO#PPA2 SOLUTION START
    vec2 s1 = fUV * texture_scales.x;
    vec2 s2 = fUV * texture_scales.y;
    vec2 s3 = fUV * texture_scales.z;
    
    vec2 t1 = vec2(s1.x, s1.y - (scroll_speeds.x*time));
    vec2 t2 = vec2(s2.x, s2.y - (scroll_speeds.y*time));
    vec2 t3 = vec2(s3.x, s3.y - (scroll_speeds.z*time));
    
    vec2 n1 = vec2(mod(t1.x,1.0), mod(t1.y,1.0));
    vec2 n2 = vec2(mod(t2.x,1.0), mod(t2.y,1.0));
    vec2 n3 = vec2(mod(t3.x,1.0), mod(t3.y,1.0));
    
    vec4 tex1 = getNormalColor(n1);
    vec4 tex2 = getNormalColor(n2);
    vec4 tex3 = getNormalColor(n3);
    
    vec2 texAv = (tex1.xy + tex2.xy + tex3.xy) / 3.0;
    
    gl_FragColor = (getDiffuseColor(texAv)+vec4(0,0,(1-fPos.y)*0.1,0))*(1-fPos.y-(mod(time/10,1.0)));
    // SOLUTION END
}