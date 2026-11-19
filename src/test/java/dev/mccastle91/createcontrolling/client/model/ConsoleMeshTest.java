package dev.mccastle91.createcontrolling.client.model;

import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Quad corners must run in vanilla's order, or smooth lighting shades each as its neighbour. */
class ConsoleMeshTest {

    /** A quad's corners counter-clockwise from outside, starting anywhere: the start found is always vanilla's. */
    private static void assertFindsVanillaCorner(Direction side, Vec3... counterClockwise) {
        FaceInfo face = FaceInfo.fromFacing(side);
        for (int offset = 0; offset < 4; offset++) {
            Vec3[] pos = new Vec3[4];
            for (int i = 0; i < 4; i++)
                pos[i] = counterClockwise[(i + offset) % 4];
            int start = ConsoleMesh.vanillaStart(pos, side);
            double minX = 1, maxX = 0, minY = 1, maxY = 0, minZ = 1, maxZ = 0;
            for (Vec3 p : pos) {
                minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
                minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
                minZ = Math.min(minZ, p.z); maxZ = Math.max(maxZ, p.z);
            }
            double[] bounds = { minY, maxY, minZ, maxZ, minX, maxX };
            for (int i = 0; i < 4; i++) {
                FaceInfo.VertexInfo corner = face.getVertexInfo(i);
                Vec3 p = pos[(i + start) % 4];
                String where = side + " corner " + i + " from offset " + offset;
                // the two axes across the face must match exactly; the third only tilts with a slope
                if (side.getAxis() != Direction.Axis.X)
                    assertEquals(bounds[corner.xFace], p.x, 1e-9, where);
                if (side.getAxis() != Direction.Axis.Y)
                    assertEquals(bounds[corner.yFace], p.y, 1e-9, where);
                if (side.getAxis() != Direction.Axis.Z)
                    assertEquals(bounds[corner.zFace], p.z, 1e-9, where);
            }
        }
    }

    @Test
    void cornersStartWhereVanillaStartsThem() {
        // the 1 px strip between two joined floor consoles, sloping up to the north
        assertFindsVanillaCorner(Direction.UP,
                new Vec3(0, 0.5, 0), new Vec3(0, 0.2, 1), new Vec3(0.0625, 0.2, 1), new Vec3(0.0625, 0.5, 0));
        // a whole sloped plate
        assertFindsVanillaCorner(Direction.UP,
                new Vec3(0.0625, 0.5, 0), new Vec3(0.0625, 0.2, 1), new Vec3(0.9375, 0.2, 1), new Vec3(0.9375, 0.5, 0));
        // a ceiling console's face, looking down
        assertFindsVanillaCorner(Direction.DOWN,
                new Vec3(0, 0.8, 1), new Vec3(0, 0.8, 0), new Vec3(1, 0.8, 0), new Vec3(1, 0.8, 1));
        // wall consoles' faces
        assertFindsVanillaCorner(Direction.NORTH,
                new Vec3(1, 1, 0.7), new Vec3(1, 0, 0.7), new Vec3(0, 0, 0.7), new Vec3(0, 1, 0.7));
        assertFindsVanillaCorner(Direction.SOUTH,
                new Vec3(0, 1, 0.3), new Vec3(0, 0, 0.3), new Vec3(1, 0, 0.3), new Vec3(1, 1, 0.3));
        assertFindsVanillaCorner(Direction.WEST,
                new Vec3(0.7, 1, 0), new Vec3(0.7, 0, 0), new Vec3(0.7, 0, 1), new Vec3(0.7, 1, 1));
        assertFindsVanillaCorner(Direction.EAST,
                new Vec3(0.3, 1, 1), new Vec3(0.3, 0, 1), new Vec3(0.3, 0, 0), new Vec3(0.3, 1, 0));
    }
}
