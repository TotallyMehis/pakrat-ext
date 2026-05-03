package pak;

public record Lump(int ofs, int len, int vers, int fourCC) {

   private final static String[] LUMP_NAMES = new String[] {
         "Entities", // 0
         "Planes", // 1
         "TexData", // 2
         "Vertexes", // 3
         "Visibility", // 4
         "Nodes", // 5
         "TexInfo", // 6
         "Faces", // 7
         "Lighting", // 8
         "Occlusion", // 9
         "Leafs", // 10
         "FaceIds", // 11
         "Edges", // 12
         "SurfEdges", // 13
         "Models", // 14
         "Worldlights", // 15
         "LeafFaces", // 16
         "LeafBrushes", // 17
         "Brushes", // 18
         "BrushSides", // 19
         "Areas", // 20
         "AreaPortals", // 21
         "Portals", // 22
         "Clusters", // 23
         "PortalVerts", // 24
         "ClusterPortals", // 25
         "DispInfo", // 26
         "OriginalFaces", // 27
         "PhysDisp", // 28
         "PhysCollide", // 29
         "VertNormals", // 30
         "VertNormalIndicies", // 31
         "DispLightmapAlphas", // 32
         "DispVerts", // 33
         "DispLightmapSamplePositions", // 34
         "GameLump", // 35
         "LeafWaterData", // 36
         "Primatives", // 37
         "PrimVerts", // 38
         "PrimIndicies", // 39
         "PakFile", // 40
         "ClipPortalVerts", // 41
         "Cubemaps", // 42
         "TexDataStringData", // 43
         "TexDataStringTable", // 44
         "Overlays", // 45
         "LeafMinDistToWater", // 46
         "FaceMacroTextureInfo", // 47
         "DispTris", // 48
         "PhysCollideSurface", // 49
         "WaterOverlays", // 50
         "LeafAmbientIndexHDR", // 51
         "LeafAmbientIndex", // 52
         "LightingHDR", // 53
         "WorldlightsHDR", // 54
         "LeaflightLDR", // 55
         "LeaflightHDR", // 56
         "", // 57
         "", // 58
         "", // 59
         "", // 60
         "", // 61
         "", // 62
         "", // 63
         "" // 64
   };

   private final static int[] LUMP_SIZES = new int[] {
         1, // 0
         20, // 1
         32, // 2
         12, // 3
         1, // 4
         32, // 5
         72, // 6
         56, // 7
         1, // 8
         1, // 9
         56, // 10
         0, // 11
         4, // 12
         4, // 13
         48, // 14
         86, // 15
         2, // 16
         2, // 17
         12, // 18
         8, // 19
         8, // 20
         12, // 21
         16, // 22
         8, // 23
         2, // 24
         2, // 25
         176, // 26
         56, // 27
         0, // 28
         1, // 29
         12, // 30
         2, // 31
         1, // 32
         1, // 33
         1, // 34
         1, // 35
         12, // 36
         10, // 37
         12, // 38
         2, // 39
         1, // 40
         12, // 41
         16, // 42
         1, // 43
         4, // 44
         352, // 45
         1, // 46
         1, // 47
         1, // 48
         1, // 49
         0, // 50
         0, // 51
         0, // 52
         1, // 53
         1, // 54
         1, // 55
         1, // 56
         0, // 57
         0, // 58
         0, // 59
         0, // 60
         0, // 61
         0, // 62
         0, // 63
         0 // 64
   };

   public static String name(int lindex) {
      return LUMP_NAMES[lindex];
   }

   public static int size(int lindex) {
      return LUMP_SIZES[lindex] == 0 ? -1 : LUMP_SIZES[lindex];
   }
}
