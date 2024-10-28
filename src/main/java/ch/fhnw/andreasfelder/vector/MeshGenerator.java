package ch.fhnw.andreasfelder.vector;/*
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
import java.util.List;

public class MeshGenerator {
	public static void addSphere(List<Vertex> _vertices, List<Tri> _tris, int _patches, Vector3 _color) {
		for (var x = 0; x < _patches; x++)
			for (var y = 0; y < _patches; y++) {
				var x1 = (x * 2f / _patches) - 1f;
				var x2 = ((x + 1) * 2f / _patches) - 1f;
				var y1 = (y * 2f / _patches) - 1f;
				var y2 = ((y + 1) * 2f / _patches) - 1f;

				addSpherePatch(_vertices, _tris, new Vector3(x1, y1, -1), new Vector3(x2, y1, -1), new Vector3(x2, y2, -1),
						new Vector3(x1, y2, -1), false, _color);
				addSpherePatch(_vertices, _tris, new Vector3(x1, y1, 1), new Vector3(x2, y1, 1), new Vector3(x2, y2, 1), new Vector3(x1, y2, 1),
						true, _color);
				addSpherePatch(_vertices, _tris, new Vector3(x1, -1, y1), new Vector3(x2, -1, y1), new Vector3(x2, -1, y2),
						new Vector3(x1, -1, y2), true, _color);
				addSpherePatch(_vertices, _tris, new Vector3(x1, 1, y1), new Vector3(x2, 1, y1), new Vector3(x2, 1, y2), new Vector3(x1, 1, y2),
						false, _color);
				addSpherePatch(_vertices, _tris, new Vector3(-1, x1, y1), new Vector3(-1, x2, y1), new Vector3(-1, x2, y2),
						new Vector3(-1, x1, y2), false, _color);
				addSpherePatch(_vertices, _tris, new Vector3(1, x1, y1), new Vector3(1, x2, y1), new Vector3(1, x2, y2), new Vector3(1, x1, y2),
						true, _color);
			}
	}

	private static void addSpherePatch(List<Vertex> _vertices, List<Tri> _tris, Vector3 p1, Vector3 p2, Vector3 p3, Vector3 p4, boolean _flip, Vector3 _color) {
		var n1 = Vector3.normalize(p1);
		var n2 = Vector3.normalize(p2);
		var n3 = Vector3.normalize(p3);
		var n4 = Vector3.normalize(p4);
		var t1 = new Vector2((float) (Math.atan2(n1.x(), n1.y()) / Math.PI / 2) + 0.5f,
				(float) (Math.acos(n1.z()) / Math.PI));
		var t2 = new Vector2((float) (Math.atan2(n2.x(), n2.y()) / Math.PI / 2) + 0.5f,
				(float) (Math.acos(n2.z()) / Math.PI));
		var t3 = new Vector2((float) (Math.atan2(n3.x(), n3.y()) / Math.PI / 2) + 0.5f,
				(float) (Math.acos(n3.z()) / Math.PI));
		var t4 = new Vector2((float) (Math.atan2(n4.x(), n4.y()) / Math.PI / 2) + 0.5f,
				(float) (Math.acos(n4.z()) / Math.PI));
		if (t1.x() > 0.75f || t2.x() > 0.75f || t3.x() > 0.75f || t4.x() > 0.75f) {
			if (t1.x() < 0.25f)
				t1 = new Vector2(t1.x() + 1, t1.y());
			if (t2.x() < 0.25f)
				t2 = new Vector2(t2.x() + 1, t2.y());
			if (t3.x() < 0.25f)
				t3 = new Vector2(t3.x() + 1, t3.y());
			if (t4.x() < 0.25f)
				t4 = new Vector2(t4.x() + 1, t4.y());
		}
		_vertices.add(new Vertex(n1, _color, t1, n1));
		_vertices.add(new Vertex(n2, _color, t2, n2));
		_vertices.add(new Vertex(n3, _color, t3, n3));
		_vertices.add(new Vertex(n4, _color, t4, n4));
		if (_flip) {
			_tris.add(new Tri(_vertices.size() - 3, _vertices.size() - 4, _vertices.size() - 2));
			_tris.add(new Tri(_vertices.size() - 2, _vertices.size() - 4, _vertices.size() - 1));
		} else {
			_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 3, _vertices.size() - 2));
			_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 2, _vertices.size() - 1));
		}
	}
	
	public static void addCube(List<Vertex> _vertices, List<Tri> _tris, Vector3 _colorFront, Vector3 _colorBack,
			Vector3 _colorTop, Vector3 _colorBottom, Vector3 _colorRight, Vector3 _colorLeft) {

		// front
		_vertices.add(new Vertex(new Vector3(-1, -1, -1), _colorFront, new Vector2(0, 0), Vector3.UNIT_Z.negate()));
		_vertices.add(new Vertex(new Vector3(1, -1, -1), _colorFront, new Vector2(1, 0), Vector3.UNIT_Z.negate()));
		_vertices.add(new Vertex(new Vector3(1, 1, -1), _colorFront, new Vector2(1, 1), Vector3.UNIT_Z.negate()));
		_vertices.add(new Vertex(new Vector3(-1, 1, -1), _colorFront, new Vector2(0, 1), Vector3.UNIT_Z.negate()));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 3, _vertices.size() - 2));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 2, _vertices.size() - 1));

		// back
		_vertices.add(new Vertex(new Vector3(1, -1, 1), _colorBack, new Vector2(0, 0), Vector3.UNIT_Z));
		_vertices.add(new Vertex(new Vector3(-1, -1, 1), _colorBack, new Vector2(1, 0), Vector3.UNIT_Z));
		_vertices.add(new Vertex(new Vector3(-1, 1, 1), _colorBack, new Vector2(1, 1), Vector3.UNIT_Z));
		_vertices.add(new Vertex(new Vector3(1, 1, 1), _colorBack, new Vector2(0, 1), Vector3.UNIT_Z));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 3, _vertices.size() - 2));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 2, _vertices.size() - 1));

		// top
		_vertices.add(new Vertex(new Vector3(-1, 1, -1), _colorTop, new Vector2(0, 1), Vector3.UNIT_Y));
		_vertices.add(new Vertex(new Vector3(1, 1, -1), _colorTop, new Vector2(1, 1), Vector3.UNIT_Y));
		_vertices.add(new Vertex(new Vector3(1, 1, 1), _colorTop, new Vector2(1, 0), Vector3.UNIT_Y));
		_vertices.add(new Vertex(new Vector3(-1, 1, 1), _colorTop, new Vector2(0, 0), Vector3.UNIT_Y));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 3, _vertices.size() - 2));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 2, _vertices.size() - 1));

		// bottom
		_vertices.add(new Vertex(new Vector3(1, -1, -1), _colorBottom, new Vector2(0, 1), Vector3.UNIT_Y.negate()));
		_vertices.add(new Vertex(new Vector3(-1, -1, -1), _colorBottom, new Vector2(1, 1), Vector3.UNIT_Y.negate()));
		_vertices.add(new Vertex(new Vector3(-1, -1, 1), _colorBottom, new Vector2(1, 0), Vector3.UNIT_Y.negate()));
		_vertices.add(new Vertex(new Vector3(1, -1, 1), _colorBottom, new Vector2(0, 0), Vector3.UNIT_Y.negate()));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 3, _vertices.size() - 2));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 2, _vertices.size() - 1));

		// right
		_vertices.add(new Vertex(new Vector3(1, -1, -1), _colorRight, new Vector2(0, 1), Vector3.UNIT_X));
		_vertices.add(new Vertex(new Vector3(1, -1, 1), _colorRight, new Vector2(1, 1), Vector3.UNIT_X));
		_vertices.add(new Vertex(new Vector3(1, 1, 1), _colorRight, new Vector2(1, 0), Vector3.UNIT_X));
		_vertices.add(new Vertex(new Vector3(1, 1, -1), _colorRight, new Vector2(0, 0), Vector3.UNIT_X));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 3, _vertices.size() - 2));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 2, _vertices.size() - 1));

		// left
		_vertices.add(new Vertex(new Vector3(-1, -1, 1), _colorLeft, new Vector2(0, 1), Vector3.UNIT_X.negate()));
		_vertices.add(new Vertex(new Vector3(-1, -1, -1), _colorLeft, new Vector2(1, 1), Vector3.UNIT_X.negate()));
		_vertices.add(new Vertex(new Vector3(-1, 1, -1), _colorLeft, new Vector2(1, 0), Vector3.UNIT_X.negate()));
		_vertices.add(new Vertex(new Vector3(-1, 1, 1), _colorLeft, new Vector2(0, 0), Vector3.UNIT_X.negate()));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 3, _vertices.size() - 2));
		_tris.add(new Tri(_vertices.size() - 4, _vertices.size() - 2, _vertices.size() - 1));
	}
}
