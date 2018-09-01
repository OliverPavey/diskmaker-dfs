package diskmaker.image;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import diskmaker.image.DiskImage;

public class DiskImageTest {
	
	private static DiskImage image;
	
	@Before
	public void setUp() throws Exception {
		image = new DiskImage();
	}

	@Test
	public void testTitle() {
		image.setTitle("Fred"); // Shorter than 8
		assertEquals(image.getTitle(), "Fred");
		
		image.setTitle("FredWasHere"); // Longer than 8
		assertEquals(image.getTitle(), "FredWasHere");
		
		image.setTitle("0123456789AB"); // 12 long
		assertEquals(image.getTitle(), "0123456789AB");
		
		image.setTitle("0123456789ABCDEF"); // 16 long
		assertEquals(image.getTitle(), "0123456789AB"); // 12 back
	}

	@Test
	public void testPlingBoot() {
		image.setPlingBoot(3);
		assertEquals(3, image.getPlingBoot());
		
		image.setPlingBoot(2);
		assertEquals(2, image.getPlingBoot());
		
		image.setPlingBoot(1);
		assertEquals(1, image.getPlingBoot());
		
		image.setPlingBoot(0);
		assertEquals(0, image.getPlingBoot());
	}
	
	@Test
	public void testSequenceNumber() {
		assertEquals(0, image.getSequenceNumber());
		
		image.setTitle("NEWTITLE");
		assertEquals(1, image.getSequenceNumber());
	}
	
	@Test
	public void testSectorSize() {
		int size = image.getSectorSize();
		assertFalse(0 == size);
		assertEquals(0, size % 256);
	}
	
	@Test
	public void testDiskSize() {
		int k100 = 1024*100;
		int k200 = 1024*200;
		int diskSize = image.getDiskSize();
		assertTrue( k100 == diskSize || k200 == diskSize );
	}
	
	@Test
	public void testSectorCount() {
		int sectorCount = image.getSectorCount();
		assertTrue( 400 == sectorCount || 800 == sectorCount );
	}
}
