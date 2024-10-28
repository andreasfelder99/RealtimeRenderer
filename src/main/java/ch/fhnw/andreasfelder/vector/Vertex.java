package ch.fhnw.andreasfelder.vector;

/*
 * Copyright (c) 2013 - 2024 Simon Felix
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public record Vertex(Vector4 position, Vector3 worldCoordinates, Vector3 color, Vector2 st, Vector3 normal) {
	public Vertex(Vector3 Position, Vector3 Color, Vector2 ST, Vector3 Normal) {
		this(new Vector4(Position, 1), Vector3.ZERO, Color, ST, Normal);
	}

	public static Vertex lerp(Vertex a, Vertex b, float t) {
		return new Vertex(Vector4.lerp(a.position, b.position, t),
				Vector3.lerp(a.worldCoordinates, b.worldCoordinates, t), Vector3.lerp(a.color, b.color, t),
				Vector2.lerp(a.st, b.st, t), Vector3.lerp(a.normal, b.normal, t));
	}

	public static Vertex multiply(Vertex a, float x) {
		return a.multiply(x);
	}

	public Vertex multiply(float x) {
		return new Vertex(position.multiply(x), worldCoordinates.multiply(x), color.multiply(x), st.multiply(x),
				normal.multiply(x));
	}

	public static Vertex add(Vertex a, Vertex b) {
		return a.add(b);
	}

	public Vertex add(Vertex b) {
		return new Vertex(position.add(b.position), worldCoordinates.add(b.worldCoordinates), color.add(b.color),
				st.add(b.st), normal.add(b.normal));
	}

	public static Vertex subtract(Vertex a, Vertex b) {
		return a.subtract(b);
	}

	public Vertex subtract(Vertex b) {
		return new Vertex(position.subtract(b.position), worldCoordinates.subtract(b.worldCoordinates),
				color.subtract(b.color), st.subtract(b.st), normal.subtract(b.normal));
	}
}
