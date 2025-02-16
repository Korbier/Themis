package org.sc.themis.scene.light.pipeline;

public class PhongShaderSource {

    public static String inject(int set, String source) {
        return source
                .replace("$PHONG_LIGHT_STRUCT$", struct())
                .replace("$PHONG_LIGHT_DESCRIPTORSET$", descriptorset(set))
                .replace("$PHONG_LIGHT_FUNTIONS$", functions());

    }


    public static String struct() {
        return """                   
               struct DirectionalLight {
                   vec4 ambient;
                   vec4 diffuse;
                   vec4 specular;
                   vec4 data;
                   vec4 direction;
               };
               
               struct PointLight {
                   vec4 ambient;
                   vec4 diffuse;
                   vec4 specular;
                   vec4 data;
                   vec4 position;
                   vec4 attenuation;
               };
               
               struct SpotLight {
                   vec4 ambient;
                   vec4 diffuse;
                   vec4 specular;
                   vec4 data;
                   vec4 position;
                   vec4 direction;
                   vec4 attenuation;
                   float innerCutOff; //cos(rad(angle))
                   float outerCutOff; //cos(rad(angle))
               };
               """;
    }

    public static String descriptorset(int set) {
        return """                   
               layout(std140, set = %d, binding = 0) uniform Lights {
                   float directionalLightCount;
                   float pointLightCount;
                   float spotLightCount;
                   float pad;
               } lights;
               
               layout(std430, set = %d, binding = 1) readonly buffer DirectionalLights {
                   DirectionalLight lights[];
               } directionalLights;
               
               layout(std430, set = %d, binding = 2) readonly buffer PointLights {
                   PointLight lights[];
               } pointLights;
               
               layout(std430, set = %d, binding = 3) readonly buffer SpotLights {
                   SpotLight lights[];
               } spotLights;
               """.formatted(set, set, set, set);
    }

    public static String functions() {
        return """     
               float attenuationType1( vec3 fragPosition, vec3 lightPosition, float radius, float falloff ) {
                   float distance = length( lightPosition - fragPosition );
                   float s = distance / radius;
                   if (s >= 1.0) return 0.0;
                   return (1 - s * s) * (1 - s * s) / (1 + falloff * s);
               }
               
               float attenuationType2( vec3 fragPosition, vec3 lightPosition, float radius, float falloff ) {
                   float distance = length( lightPosition - fragPosition );
                   float s = distance / radius;
                   if (s >= 1.0) return 0.0;
                   return (1 - s * s) + (1 - s * s) / (1 + falloff * s * s);
               }
               
               float attenuation( vec3 fragPosition, vec3 normal, vec3 lightPosition, vec4 attenuation ) {
               
                   if ( attenuation.x == 1.0f ) {
                       return attenuationType1( fragPosition, lightPosition, attenuation.y, attenuation.z );
                   }
               
                   if ( attenuation.x == 2.0f ) {
                       return attenuationType2( fragPosition, lightPosition, attenuation.y, attenuation.z );
                   }
               
                   return 1.0f;
               
               }
               
               vec3 ambient(vec3 lightAmbientColor, vec3 materialColor) {
                   return lightAmbientColor * materialColor;
               }
               
               vec3 diffuseDirectional( vec3 nlNormal, vec3 materialColor, vec3 lightDiffuseColor, vec3 lightDirection ) {
                   vec3 oppLightDirection  = normalize( -lightDirection );
                   float diff = max( dot( nlNormal, oppLightDirection), 0.0 );
                   return lightDiffuseColor * materialColor * diff;
               }
               
               vec3 diffuse( vec3 fragPosition, vec3 nlNormal, vec3 materialColor, vec3 lightDiffuseColor, vec3 lightPosition ) {
                   vec3 lightDirection  = normalize( lightPosition - fragPosition );
                   float diff = max( dot( nlNormal, lightDirection), 0.0 );
                   return lightDiffuseColor * materialColor * diff;
               }
               
               vec3 specularDirectional( vec3 fragPosition, vec3 normal, vec3 materialSpecular, float materialShininess, vec3 lightSpecularColor, vec3 lightDirection ) {
               
                   vec3 oppLightDirection  = normalize( -lightDirection );
                   vec3 viewDirection = normalize( global.camera.xyz - fragPosition );
                   vec3 reflectDirection = reflect( -oppLightDirection, normal );
               
                   float specularFactor = max(dot(viewDirection, reflectDirection), 0.0);
               
                   //https://stackoverflow.com/questions/37051358/opengl-es-2-0-specular-light-generates-black-border
                   if ( specularFactor > 0.0 ) {
                       float spec = pow(specularFactor, materialShininess);
                       return lightSpecularColor * spec * materialSpecular;
                   } else {
                       return vec3(0.0f);
                   }
               
               }
               
               vec3 specular( vec3 fragPosition, vec3 normal, vec3 materialSpecular, float materialShininess, vec3 lightSpecularColor, vec3 lightPosition ) {
               
                   vec3 lightDirection  = normalize( lightPosition - fragPosition );
                   vec3 viewDirection = normalize( global.camera.xyz - fragPosition );
                   vec3 reflectDirection = reflect( -lightDirection, normal );
               
                   float specularFactor = max(dot(viewDirection, reflectDirection), 0.0);
               
                   //https://stackoverflow.com/questions/37051358/opengl-es-2-0-specular-light-generates-black-border
                   if ( specularFactor > 0.0 ) {
                       float spec = pow(specularFactor, materialShininess);
                       return lightSpecularColor * spec * materialSpecular;
                   } else {
                       return vec3(0.0f);
                   }
               
               }
               
               vec3 directional( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess, DirectionalLight light ) {
                   vec3 ambientColor = ambient( light.ambient.rgb, materialAmbient );
                   vec3 diffuseColor = diffuseDirectional( nlNormal, materialDiffuse, light.diffuse.rgb, light.direction.xyz );
                   vec3 specularColor = specularDirectional( position, nlNormal, materialSpecular, materialShininess, light.specular.rgb, light.direction.xyz );
                   return ambientColor + diffuseColor + specularColor;
               }
               
               vec3 point( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess, PointLight light ) {
                   vec3 ambientColor = ambient( light.ambient.rgb, materialAmbient );
                   vec3 diffuseColor = diffuse( position, nlNormal, materialDiffuse, light.diffuse.rgb, light.position.xyz );
                   vec3 specularColor = specular( position, nlNormal, materialSpecular, materialShininess, light.specular.rgb, light.position.xyz );
                   float attenuation = attenuation(position, nlNormal, light.position.xyz, light.attenuation);
                   return attenuation * (ambientColor + diffuseColor + specularColor);
               }
               
               float spotIntensity( vec3 fragPosition, SpotLight light ) {
                   float theta = dot(normalize(light.position.xyz - fragPosition), normalize(-light.direction.xyz));
                   float epsilon = light.innerCutOff - light.outerCutOff;
                   return clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0 );
               }
               
               vec3 spot( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess, SpotLight light ) {
               
                   vec3 ambientColor = ambient( light.ambient.rgb, materialAmbient );
                   vec3 diffuseColor = diffuse( position, nlNormal, materialDiffuse, light.diffuse.rgb, light.position.xyz );
                   vec3 specularColor = specular( position, nlNormal, materialSpecular, materialShininess, light.specular.rgb, light.position.xyz );
                   float intensity = spotIntensity(position, light);
               
                   diffuseColor *= intensity;
                   specularColor *= intensity;
               
                   float attenuation = attenuation(position, nlNormal, light.position.xyz, light.attenuation);
                   return attenuation * (ambientColor + diffuseColor + specularColor);
               }
               
               vec3 phong_directionals( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess ) {
                   vec3 color = vec3(0.0f);
                   for (int i = 0; i<lights.directionalLightCount; i++ ) {
                       if ( directionalLights.lights[i].data.x == 1.0f ) {
                           color += directional(nlNormal, position, materialAmbient, materialDiffuse, materialSpecular, materialShininess, directionalLights.lights[i]);
                       }
                   }
                   return color;
               }
               
               vec3 phong_points( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess ) {
                   vec3 color = vec3(0.0f);
                   for (int i = 0; i<lights.pointLightCount; i++ ) {
                       if ( pointLights.lights[i].data.x == 1.0f ) {
                           color += point(nlNormal, position, materialAmbient, materialDiffuse, materialSpecular, materialShininess, pointLights.lights[i]);
                       }
                   }
                   return color;
               }
               
               vec3 phong_spots( vec3 nlNormal, vec3 position, vec3 materialAmbient, vec3 materialDiffuse, vec3 materialSpecular, float materialShininess ) {
                   vec3 color = vec3(0.0f);
                   for (int i = 0; i<lights.spotLightCount; i++ ) {
                       if ( spotLights.lights[i].data.x == 1.0f ) {
                           color += spot(nlNormal, position, materialAmbient, materialDiffuse, materialSpecular, materialShininess, spotLights.lights[i]);
                       }
                   }
                   return color;
               }
               """;
    }


}
