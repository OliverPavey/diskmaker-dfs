package diskmaker.image;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import diskmaker.image.DiskImage;

public class DiskImageThreeFileTest {

	private static DiskImage image;
	private static Path tempPath;
	private static File f1, f2, f3;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tempPath = Files.createTempDirectory("unitTest");
		f1 = createTempFile("f1.txt",   95, (byte)'a'); // Sector  2,  1 sector
		f2 = createTempFile("f2.txt", 3359, (byte)'b'); // Sector  3, 14 sectors
		f3 = createTempFile("f3.txt",   26, (byte)'c'); // Sector 17,  1 sector

		image = new DiskImage();
		image.addFile("One"  , f1,      0,      0);
		image.addFile("x.Two", f2, 0x2000, 0x3000);
		image.addFile("Three", f3,      0,      0); 
	}

	private static File createTempFile(String name, int size, byte fill) throws Exception {
		File f = new File(tempPath.toFile(), name);
		try (FileOutputStream fos = new FileOutputStream(f)) {
			byte[] bytes = new byte[size];
			Arrays.fill(bytes, fill);
			fos.write(bytes);
			fos.close();
		}
		return f;
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for (File removable : new File[]{f1,f2,f3,tempPath.toFile()})
			removable.delete();
	}

	@Test
	public void testNextFreeSector() {
		assertEquals(18, image.nextFreeSector());
	}

	@Test
	public void testRemainingSpace() {
		assertEquals((800-18)*256, image.getRemainingSpace());
	}

	@Test
	public void testFileCount() {
		assertEquals(3, image.getFileCount());
	}

	@Test
	public void testMainFilename() {
		assertEquals("One", image.getMainFilename(0));
		assertEquals("Two", image.getMainFilename(1));
		assertEquals("Three", image.getMainFilename(2));
	}

	@Test
	public void testDirectoryName() {
		assertEquals('$', image.getDirectoryName(0));
		assertEquals('x', image.getDirectoryName(1));
		assertEquals('$', image.getDirectoryName(2));
	}

	@Test
	public void testFullFilename() {
		assertEquals("One",   image.getFullFilename(0));
		assertEquals("x.Two", image.getFullFilename(1));
		assertEquals("Three", image.getFullFilename(2));
	}

	@Test
	public void testLoadAddress() {
		assertEquals(     0, image.getLoadAddress(0));
		assertEquals(0x2000, image.getLoadAddress(1));
		assertEquals(     0, image.getLoadAddress(2));
	}

	@Test
	public void testExecutionAddress() {
		assertEquals(     0, image.getExecutionAddress(0));
		assertEquals(0x3000, image.getExecutionAddress(1));
		assertEquals(     0, image.getExecutionAddress(2));
	}

	@Test
	public void testFileLength() {
		assertEquals(  95, image.getFileLength(0));
		assertEquals(3359, image.getFileLength(1));
		assertEquals(  26, image.getFileLength(2));
	}

	@Test
	public void testStartSector() {
		assertEquals( 2, image.getStartSector(0));
		assertEquals( 3, image.getStartSector(1));
		assertEquals(17, image.getStartSector(2));
	}
}
