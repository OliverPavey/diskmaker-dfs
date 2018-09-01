package diskmaker.loader.xml;

import java.io.File;

import diskmaker.image.DiskImage;
import diskmaker.loader.DefinitionLoader;
import diskmaker.loader.DefinitionLoaderTest;

public class DefinitionLoaderXmlTest extends DefinitionLoaderTest {

	@Override
	protected DiskImage buildImage() throws Exception {
		
		File definition = new File(getTempPath().toFile(), "poems.xml");
		
		saveResourceToFile(definition, "poems.xml");
		removeWhenComplete(definition);
		
		DefinitionLoader loader = new DefinitionLoaderXml();
		DiskImage image = loader.convert( definition.getAbsolutePath() );
		
		return image;
	}
}
