#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)

// Lighting Information
const int MAX_LIGHTS = 16;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 ambientLightIntensity;

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

// Shading Information
uniform float shininess;

varying vec2 fUV;
varying vec4 worldPos; // vertex position in world coordinates
varying mat3 mTBN;


vec4 phongColor(vec3 N, vec3 V){
    vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);
    for (int i = 0; i < numLights; i++) {
        float r = length(lightPosition[i] - worldPos.xyz);
        vec3 L = normalize(lightPosition[i] - worldPos.xyz);
        vec3 H = normalize(L + V);
        // calculate diffuse term
        vec4 Idiff = getDiffuseColor(fUV) * max(dot(N, L), 0.0);
        // calculate specular term
        vec4 Ispec = getSpecularColor(fUV) * pow(max(dot(N, H), 0.0), shininess);
        finalColor += vec4(lightIntensity[i], 0.0) * (Idiff + Ispec) / (r*r);
    }
    // calculate ambient term
    vec4 Iamb = getDiffuseColor(fUV);
    return (finalColor + vec4(ambientLightIntensity, 0.0) * Iamb) * exposure;
}


void main() {
    // Normal Vector (obtained from texture)
    vec3 NTemp1 = getNormalColor(fUV).xyz;
    vec3 NTemp2 = (2*NTemp1) - vec3(1,1,1);
    vec3 N = normalize(mTBN * NTemp2);
    
    // Viewing vector
    vec3 V = normalize(worldCam - worldPos.xyz);
    
    // Use phong shading with the normal vector obtained from texture
    gl_FragColor = phongColor(N, V);
}
