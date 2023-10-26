from typing import Mapping
from numpy.typing import NDArray, ArrayLike

# -*- coding: utf-8 -*-
# Copyright (c) Vispy Development Team. All Rights Reserved.
# Distributed under the (new) BSD License. See LICENSE.txt for more info.

import numpy as np

def _fix_colors(colors): ...

class MeshData(object):
    def __init__(
        self,
        vertices: NDArray | None = None,
        faces: NDArray | None = None,
        edges=None,
        vertex_colors: NDArray | None = None,
        face_colors: NDArray | None = None,
        vertex_values: NDArray | None = None,
    ): ...
    def get_faces(self): ...
    def get_edges(self, indexed: str | None = None) -> NDArray: ...
    def set_faces(self, faces: NDArray): ...
    def get_vertices(self, indexed: str | None = None) -> NDArray: ...
    def get_bounds(self) -> ArrayLike: ...
    def set_vertices(
        self,
        verts: NDArray | None = None,
        indexed: str | None = None,
        reset_normals: bool = True,
    ): ...
    def reset_normals(self): ...
    def has_face_indexed_data(self): ...
    def has_edge_indexed_data(self): ...
    def has_vertex_color(self): ...
    def has_vertex_value(self): ...
    def has_face_color(self): ...
    def get_face_normals(self, indexed: str | None = None) -> NDArray: ...
    def get_vertex_normals(self, indexed: str | None = None) -> NDArray: ...
    def get_vertex_colors(self, indexed: str | None = None) -> NDArray: ...
    def get_vertex_values(self, indexed: str | None = None) -> NDArray: ...
    def set_vertex_colors(self, colors: ArrayLike, indexed: str | None = None): ...
    def set_vertex_values(self, values: ArrayLike, indexed: str | None = None): ...
    def get_face_colors(self, indexed: str | None = None) -> NDArray: ...
    def set_face_colors(self, colors: ArrayLike, indexed: str | None = None): ...
    @property
    def n_faces(self): ...
    @property
    def n_vertices(self): ...
    def get_edge_colors(self): ...
    def _compute_unindexed_vertices(self): ...
    def get_vertex_faces(self): ...
    def _compute_edges(self, indexed=None): ...
    def save(self) -> Mapping: ...
    def restore(self, state: Mapping): ...
    def is_empty(self): ...