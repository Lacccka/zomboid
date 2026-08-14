// Railroader / rr_truck.vert -- the vertex shader for a truck (bogie) entity.
//
// rr_loco.vert, with ONE addition: a compile-time depth bias. Everything else about it --
// the 4-bone MatrixPalette blend, FinalScale, the targetDepth remap, UVScale -- is vanilla
// animalEffect.vert and must not be trimmed for the reasons written on rr_loco.vert.
//
// WHY A SEPARATE SHADER EXISTS AT ALL: the depth knob below, and nothing else.
//
// HOW PZ SORTS TWO MODELS THAT OVERLAP (read from the decompile before this was written):
//   * Characters and animals render per SQUARE -- IsoGridSquare.renderCharacters sorts only
//     WITHIN a square (Collections.sort(movingObjects, comp)); across squares the order is
//     the cell's square order.
//   * A model keeps its real per-vertex depth (AnimatedModel.highResDepthMultiplier is 0 for
//     characters, so the `o.z += (origin.z - o.z) * HighResDepthMultiplier` line is a no-op),
//     and the whole model is then shifted by ONE constant: targetDepth, which comes from
//     IsoDepthHelper.getSquareDepthData for the entity's OWN square
//     (ModelSlotRenderData.performRenderCharacter -> modelSlot.model.targetDepth).
//   * The step is IsoDepthHelper.SQUARE_DEPTH = 0.0028867 per square, i.e. 0.0014434 per
//     unit of (x + y).
//
// WHAT THAT MEANS FOR US: the carbody is a 17-tile model anchored at ONE square, so its far
// end is drawn with the CENTRE's bias. A truck sits ~5 tiles away with its OWN, more honest
// bias. Where the two overlap on screen the difference is real, and for the truck nearer the
// camera it goes the wrong way: it can win pixels the body's near flank should have kept --
// visible as the truck's FAR sideframe showing through the skirt. The error is largest when
// the loco runs along the depth axis (x + y changing fastest along the body) and exactly
// ZERO when it runs across it, so it is direction-dependent, which is what makes it hard to
// predict from a still.
//
// THE KNOB. DEPTH_PUSH is added to the truck's depth, in the same 0..1 units as targetDepth:
// 0.0028867 = one square further away. A positive value makes the truck LOSE to the body
// wherever they overlap (glDepthFunc is GL_LESS, so larger = behind), which is the correct
// direction: the body should occlude everything of the truck that sits under the frame, and
// the truck is only ever meant to be visible where the body has no pixels at all.
//
// IT SHIPS AT 0.0 ON PURPOSE. At 0 this is byte-for-byte rr_loco.vert's arithmetic, so the
// trucks sort exactly as any other entity would and the in-game pass measures the REAL
// problem rather than a correction for it. If the near truck bleeds through, raise it by one
// square at a time (0.0029, 0.0058); too much and the truck starts losing to scenery a tile
// or two behind it, which is the failure at the other end.
#version 330

layout (location = 0) in vec4 vertex;
layout (location = 1) in vec4 normal;
layout (location = 2) in vec4 boneWeights;
layout (location = 3) in vec4 boneIndices;
layout (location = 4) in vec2 uv;

out vec3 vertNormal;
out vec2 texCoords;

uniform mat4 ModelViewProjection;
uniform mat4 MatrixPalette[60];
uniform float targetDepth = 0.5;
uniform vec2 UVScale = vec2(1, 1);
uniform float HighResDepthMultiplier = 0.0;
uniform float FinalScale = 1.0;

// IsoDepthHelper.SQUARE_DEPTH is 0.0028867 -- one square. See the header.
const float DEPTH_PUSH = 0.0;

void main()
{
	vec4 position = vec4(vertex.xyz, 1.0);
	vec4 nrm = vec4(normal.xyz, 0.0);

	texCoords = uv * UVScale.xy;

	mat4 boneEffect = mat4(0.0);
	if (boneWeights.x > 0.0)
		boneEffect += MatrixPalette[int(boneIndices.x)] * boneWeights.x;
	if (boneWeights.y > 0.0)
		boneEffect += MatrixPalette[int(boneIndices.y)] * boneWeights.y;
	if (boneWeights.z > 0.0)
		boneEffect += MatrixPalette[int(boneIndices.z)] * boneWeights.z;
	if (boneWeights.w > 0.0)
		boneEffect += MatrixPalette[int(boneIndices.w)] * boneWeights.w;

	vertNormal = (boneEffect * nrm).xyz;

	vec4 bonedPos = boneEffect * position;
	bonedPos.xyz = bonedPos.xyz * FinalScale;

	vec4 o = ModelViewProjection * bonedPos;

	vec4 origin = ModelViewProjection * vec4(0, 0, 0, 1);
	o.z += (origin.z - o.z) * HighResDepthMultiplier;

	float clip = ((o.z + 1.0) / 2.0);   // -1,+1 -> 0,1
	clip += targetDepth - 0.5 + DEPTH_PUSH;
	o.z = (clip * 2.0) - 1.0;           // 0,1 -> -1,+1

	gl_Position = o;
}
