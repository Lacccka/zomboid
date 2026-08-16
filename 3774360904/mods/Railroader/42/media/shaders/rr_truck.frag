// Railroader / rr_truck.frag -- the fragment shader for a truck (bogie) entity.
//
// rr_loco.frag WITHOUT the headlight block, i.e. plain vanilla animalEffect lighting.
//
// The lamps live on the carbody and only on the carbody, so a truck has no lens to light --
// and keeping the magenta key out of this file is the point rather than an omission. The
// truck's islands are grimy near-black metal; if a future re-bake ever drifted one of those
// texels toward purple, the key would light it up in the dark for no reason anybody could
// explain. RETOPO_BRIEF's "nowhere except the lenses may min(R,B)-G exceed 15" rule protects
// the carbody's shader; this file protects the truck by not having the rule at all.
//
// A model script names ONE shader for both stages, so a `.frag` has to exist next to
// rr_truck.vert. This is it.
#version 110

varying vec3 vertNormal;
varying vec2 texCoords;

uniform sampler2D Texture;
uniform float Alpha;

uniform vec3 TintColour;
uniform vec3 AmbientColour;
uniform vec3 Light0Direction;
uniform vec3 Light0Colour;
uniform vec3 Light1Direction;
uniform vec3 Light1Colour;
uniform vec3 Light2Direction;
uniform vec3 Light2Colour;
uniform vec3 Light3Direction;
uniform vec3 Light3Colour;
uniform vec3 Light4Direction;
uniform vec3 Light4Colour;

void main()
{
	vec3 normal = normalize(vertNormal);
	vec4 texSample = texture2D(Texture, texCoords);

	if (texSample.w < 0.01)
	{
		discard;
	}

	vec3 col = texSample.xyz;

	vec3 lighting = vec3(0.0);
	lighting += Light0Colour * max(dot(normal, normalize(Light0Direction)), 0.0);
	lighting += Light1Colour * max(dot(normal, normalize(Light1Direction)), 0.0);
	lighting += Light2Colour * max(dot(normal, normalize(Light2Direction)), 0.0);
	lighting += Light3Colour * max(dot(normal, normalize(Light3Direction)), 0.0);
	lighting += Light4Colour * max(dot(normal, normalize(Light4Direction)), 0.0);
	lighting += AmbientColour;
	lighting = min(lighting, vec3(1.0));

	col = col * TintColour * lighting;

	gl_FragColor = vec4(Alpha * col, Alpha * texSample.w);
}
