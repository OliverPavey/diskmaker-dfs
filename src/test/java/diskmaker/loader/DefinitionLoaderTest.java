package diskmaker.loader;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import diskmaker.image.DiskImage;

public class DefinitionLoaderTest {

	private DiskImage image, expected;
	private Path tempPath;
	private File f1, f2, f3, expectedImage;
	private ArrayList<File> removables = new ArrayList<>();
	
	protected Path getTempPath() {
		return tempPath;
	}

	@Before
	public void setUp() throws Exception {
		tempPath = Files.createTempDirectory("unitTest");
		f1 = new File(tempPath.toFile(), "Poem1");
		f2 = new File(tempPath.toFile(), "Poem2");
		f3 = new File(tempPath.toFile(), "Poem3");
		expectedImage = new File(tempPath.toFile(), "poems.ssd");
		
		saveResourceToFile(f1, "$.Poem1");
		saveResourceToFile(f2, "$.Poem2");
		saveResourceToFile(f3, "$.Poem3");
		saveResourceToFile(expectedImage, "poems.ssd");
		removeWhenComplete(f1, f2, f3, expectedImage);

		expected = new DiskImage(expectedImage.getAbsolutePath());

		image = buildImage();
	}
	
	// Will be overridden in subclasses
	protected DiskImage buildImage() throws Exception {
		DiskImage di = new DiskImage();
		di.setTitle("MyPoemDisk");
		// Write in this order so the catalog positions on the two disks match.
		di.addFile("Poem3", f3, 0, 0);
		di.addFile("Poem2", f2, 0, 0);
		di.addFile("Poem1", f1, 0, 0);
		return di;
	}
	
	protected void saveResourceToFile(File output, String resourceName) throws IOException {
		ClassLoader resourceAccess = this.getClass().getClassLoader();
		try (InputStream inputStream = resourceAccess.getResourceAsStream(resourceName)) {
			if (null == inputStream)
				throw new RuntimeException(String.format(
						"Resource not found: '%s'",resourceName));
			java.nio.file.Files.copy(inputStream, output.toPath(), 
					StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	protected void removeWhenComplete(File... files) {
		for (File file : files) {
			removables.add(file);
		}
	}

	@After
	public void tearDown() throws Exception {
		for (File removable : removables) {
			if ( removable != null) {
				if ( removable.exists() ) {
					removable.delete();
				}
			}
		}
		tempPath.toFile().delete(); // remove last.
	}

	@Test
	public void testExpectedLoadedOk() {
		// Check expected values have loaded correctly, hence "expected" on right.
		assertEquals("MyPoemDisk", expected.getTitle());
		assertEquals(3, expected.getFileCount());
	}
	
	@Test
	public void testTitle() {
		assertEquals(expected.getTitle() , image.getTitle() );
	}
	
	@Test
	public void testPlingBoot() {
		assertEquals(expected.getPlingBoot() , image.getPlingBoot() );
	}
	
	@Test
	public void testFileCount() {
		assertEquals(expected.getFileCount() , image.getFileCount() );
	}
	
	@Test
	public void testSectorSize() {
		assertEquals(expected.getSectorSize() , image.getSectorSize() );
	}
	
	@Test
	public void testDiskSize() {
		assertEquals(expected.getDiskSize() , image.getDiskSize() );
	}
	
	@Test
	public void testSectorCount() {
		assertEquals(expected.getSectorCount() , image.getSectorCount() );
	}
	
	@Test
	public void testNextFreeSector() {
		assertEquals(expected.nextFreeSector() , image.nextFreeSector() );
	}
	
	@Test
	public void testRemainingSpace() {
		assertEquals(expected.getRemainingSpace() , image.getRemainingSpace() );
	}
	
	final static String LOOP_MSG = "Loop at index %d";
	final static int NUM_FILES = 3;
	
	@Test
	public void testMainFilename() {
		for (int i=0; i<NUM_FILES; i++) { String msg=String.format(LOOP_MSG, i);
			assertEquals(msg, expected.getMainFilename(i), image.getMainFilename(i));
		}
	}
	
	@Test
	public void testDirectoryName() {
		for (int i=0; i<NUM_FILES; i++) { String msg=String.format(LOOP_MSG, i);
			assertEquals(msg, expected.getDirectoryName(i) , image.getDirectoryName(i) );
		}
	}
	
	@Test
	public void testFullFilename() {
		for (int i=0; i<NUM_FILES; i++) { String msg=String.format(LOOP_MSG, i);
			assertEquals(msg, expected.getFullFilename(i) , image.getFullFilename(i) );
		}
	}
	
	@Test
	public void testLoadAddress() {
		for (int i=0; i<NUM_FILES; i++) { String msg=String.format(LOOP_MSG, i);
			assertEquals(msg, expected.getLoadAddress(i) , image.getLoadAddress(i) );
		}
	}
	
	@Test
	public void testExecutionAddress() {
		for (int i=0; i<NUM_FILES; i++) { String msg=String.format(LOOP_MSG, i);
			assertEquals(msg, expected.getExecutionAddress(i), image.getExecutionAddress(i));
		}
	}
	
	@Test
	public void testFileLength() {
		for (int i=0; i<NUM_FILES; i++) { String msg=String.format(LOOP_MSG, i);
			assertEquals(msg, expected.getFileLength(i), image.getFileLength(i));
		}
	}
	
	@Test
	public void testStartSector() {
		// Note reverse order (as files appear in opposite order on the two disks).
		assertEquals("Expected item 0", expected.getStartSector(0), image.getStartSector(2));
		assertEquals("Expected item 1", expected.getStartSector(1), image.getStartSector(1));
		assertEquals("Expected item 2", expected.getStartSector(2), image.getStartSector(0));
	}
}
