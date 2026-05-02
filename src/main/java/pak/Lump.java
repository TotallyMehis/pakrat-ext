package pak;

public class Lump {
   static String[] lumpname = new String[]{"Entities", "Planes", "TexData", "Vertexes", "Visibility", "Nodes", "TexInfo", "Faces", "Lighting", "Occlusion", "Leafs", "", "Edges", "SurfEdges", "Models", "Worldlights", "LeafFaces", "LeafBrushes", "Brushes", "BrushSides", "Areas", "AreaPortals", "Portals", "Clusters", "PortalVerts", "ClusterPortals", "DispInfo", "OriginalFaces", "", "PhysCollide", "VertNormals", "VertNormalIndicies", "DispLightmapAlphas", "DispVerts", "DispLightmapSamplePositions", "GameLump", "LeafWaterData", "Primatives", "PrimVerts", "PrimIndicies", "PakFile", "ClipPortalVerts", "Cubemaps", "TexDataStringData", "TexDataStringTable", "Overlays", "LeafMinDistToWater", "FaceMacroTextureInfo", "DispTris", "PhysCollideSurface", "", "", "", "LightingHDR", "WorldlightsHDR", "LeaflightLDR", "LeaflightHDR", "", "", "", "", "", "", "", ""};
   static int[] lumpsize = new int[]{1, 20, 32, 12, 1, 32, 72, 56, 1, 1, 56, 0, 4, 4, 48, 86, 2, 2, 12, 8, 8, 12, 16, 8, 2, 2, 176, 56, 0, 1, 12, 2, 1, 1, 1, 1, 12, 10, 12, 2, 1, 12, 16, 1, 4, 352, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0};
   int ofs;
   int len;
   int vers;
   int fourCC;

   public Lump() {
   }

   public static String name(int lindex) {
      return lumpname[lindex];
   }

   public static int size(int lindex) {
      return lumpsize[lindex] == 0 ? -1 : lumpsize[lindex];
   }
}
