package pak;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

final class VtfTest {
    @Test
    void readSimpleVtf() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(MdlTest.readFile("flat_normal.vtf"));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        Vtf vtf = Vtf.read(buffer);

        assertEquals(true, vtf.isValid());
        assertEquals(7, vtf.getVersion()[0]);
        assertEquals(1, vtf.getVersion()[1]);
        assertEquals(16, vtf.getWidth());
        assertEquals(16, vtf.getHeight());
        assertEquals(VtfImageFormat.BGR888, vtf.getImageFormat());
        assertEquals(16, vtf.getLowResWidth());
        assertEquals(16, vtf.getLowResHeight());
        assertEquals(VtfImageFormat.DXT1, vtf.getLowResImageFormat());
        assertEquals(128, vtf.getFlags());
        assertEquals(5, vtf.getNumberOfMipMaps());
        assertEquals(1, vtf.getNumberOfFrames());

        byte[] vtfBuffer = vtf.getBuffer();

        assertEquals(0, vtfBuffer.length % 3);

        int expectedBufferSize = 0;
        for (int i = vtf.getNumberOfMipMaps(), mipwidth = 16,
                mipheight = 16; i > 0; i--, mipwidth /= 2, mipheight /= 2) {
            expectedBufferSize += mipwidth * mipheight * 3;
        }

        assertEquals(expectedBufferSize, vtfBuffer.length);

        for (int i = 0; i < vtfBuffer.length - 3; i += 3) {
            assertEquals(-2, vtfBuffer[i]);
            assertEquals(-128, vtfBuffer[i + 1]);
            assertEquals(-128, vtfBuffer[i + 2]);
        }
    }

    @Test
    void readSimpleVtf2() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(MdlTest.readFile("sky_day01_01bk.vtf"));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        Vtf vtf = Vtf.read(buffer);

        assertEquals(true, vtf.isValid());
        assertEquals(7, vtf.getVersion()[0]);
        assertEquals(3, vtf.getVersion()[1]);
        assertEquals(512, vtf.getWidth());
        assertEquals(256, vtf.getHeight());
        assertEquals(VtfImageFormat.BGR888, vtf.getImageFormat());
        assertEquals(16, vtf.getLowResWidth());
        assertEquals(8, vtf.getLowResHeight());
        assertEquals(VtfImageFormat.DXT1, vtf.getLowResImageFormat());
        assertEquals(844, vtf.getFlags());
        assertEquals(10, vtf.getNumberOfMipMaps());
        assertEquals(1, vtf.getNumberOfFrames());
    }

    @Test
    void readSimpleVtf3() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(MdlTest.readFile("sky_day01_01_hdrbk.vtf"));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        Vtf vtf = Vtf.read(buffer);

        assertEquals(true, vtf.isValid());
        assertEquals(7, vtf.getVersion()[0]);
        assertEquals(3, vtf.getVersion()[1]);
        assertEquals(512, vtf.getWidth());
        assertEquals(256, vtf.getHeight());
        assertEquals(VtfImageFormat.BGRA8888, vtf.getImageFormat());
        assertEquals(16, vtf.getLowResWidth());
        assertEquals(8, vtf.getLowResHeight());
        assertEquals(VtfImageFormat.DXT1, vtf.getLowResImageFormat());
        assertEquals(8973, vtf.getFlags());
        assertEquals(10, vtf.getNumberOfMipMaps());
        assertEquals(1, vtf.getNumberOfFrames());
    }
}
