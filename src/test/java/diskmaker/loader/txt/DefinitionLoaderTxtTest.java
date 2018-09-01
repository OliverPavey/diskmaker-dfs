package diskmaker.loader.txt;

import java.io.File;

import diskmaker.image.DiskImage;
import diskmaker.loader.DefinitionLoader;
import diskmaker.loader.DefinitionLoaderTest;

public class DefinitionLoaderTxtTest extends DefinitionLoaderTest {
	// N.B. Tests all reside in super class
	
	@Override
	protected DiskImage buildImage() throws Exception {
		
		File definition = new File(getTempPath().toFile(), "poems.txt");
		
		saveResourceToFile(definition, "poems.txt");
		removeWhenComplete(definition);
		
		DefinitionLoader loader = new DefinitionLoaderTxt();
		DiskImage image = loader.convert( definition.getAbsolutePath() );
		
		return image;
	}
}
