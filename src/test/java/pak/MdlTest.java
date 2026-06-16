package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

final class MdlTest {
    @Test
    void readMdl() throws Exception {
        byte[] content = readFile("roller.mdl");

        ByteBuffer byteBuffer = ByteBuffer.wrap(content);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        Mdl mdl = Mdl.read(byteBuffer);

        assertEquals(true, mdl.isValid());
        assertEquals(132200910, mdl.getChecksum());
        assertEquals(Mdl.MDL_ID, mdl.getId());
        assertEquals(Mdl.MDL_VERSION, mdl.getVersion());
        assertEquals("Roller.mdl", mdl.getName());
        assertEquals(1480, mdl.getLength());
        assertEquals(1, mdl.getTexturePaths().length);
        assertEquals("models\\roller\\\\", mdl.getTexturePaths()[0]);
        assertEquals(1, mdl.getTextures().length);
        assertEquals("rollermine_sheet", mdl.getTextures()[0]);
        assertEquals(0, mdl.getIncmodels().length);
        List<String> textureList = mdl.getTextureList();
        assertEquals(1, textureList.size());
        assertEquals("models\\roller\\\\rollermine_sheet", textureList.get(0));
    }

    @Test
    void readMdl2() throws Exception {
        byte[] content = readFile("v_crowbar.mdl");

        ByteBuffer byteBuffer = ByteBuffer.wrap(content);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        Mdl mdl = Mdl.read(byteBuffer);

        assertEquals(true, mdl.isValid());
        assertEquals(1318145628, mdl.getChecksum());
        assertEquals(Mdl.MDL_ID, mdl.getId());
        assertEquals(Mdl.MDL_VERSION, mdl.getVersion());
        assertEquals("Weapons/v_crowbar.mdl", mdl.getName());
        assertEquals(15368, mdl.getLength());
        assertEquals(2, mdl.getTexturePaths().length);
        assertEquals("models\\Weapons\\V_hand\\", mdl.getTexturePaths()[0]);
        assertEquals("models\\Weapons\\V_crowbar\\", mdl.getTexturePaths()[1]);
        assertEquals(3, mdl.getTextures().length);
        assertEquals("crowbar_cyl", mdl.getTextures()[0]);
        assertEquals("v_hand_sheet", mdl.getTextures()[1]);
        assertEquals("head_uvw", mdl.getTextures()[2]);
        assertEquals(0, mdl.getIncmodels().length);
        List<String> textureList = mdl.getTextureList();
        assertEquals(6, textureList.size());
    }

    @Test
    void readInvalid() throws Exception {
        byte[] content = readFile("test.txt");

        ByteBuffer byteBuffer = ByteBuffer.wrap(content);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        Mdl mdl = Mdl.read(byteBuffer);

        assertEquals(false, mdl.isValid());
    }

    private static byte[] readFile(String fileName) throws Exception {
        return Files
                .readAllBytes(new File(MappakTest.class.getClassLoader().getResource(fileName).toURI()).toPath());
    }
}
