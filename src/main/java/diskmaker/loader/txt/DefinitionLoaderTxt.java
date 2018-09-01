package diskmaker.loader.txt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.stream.Stream;

import diskmaker.image.DiskImage;
import diskmaker.loader.DefinitionLoader;
import diskmaker.model.DiskDefinition;
import diskmaker.model.FileDefinition;

public class DefinitionLoaderTxt implements DefinitionLoader {

	public DiskImage convert(String filename) {
		File config = new File( filename );
		return convert( config );
	}

	public DiskImage convert(File config) {
		if (! config.exists()) {
			System.err.println("Config file not found");
			System.exit(-1);
		}

		final DiskDefinition definition = new DiskDefinition();
		try (Stream<String> stream = Files.lines(config.toPath())) {
			stream.forEach(line -> {

				String[] parts = line.replace("\t"," ").split("=",2);
				if (2 == parts.length) {
					String key = parts[0];
					String value = parts[1];
					switch(key) {
					case "@image":
						String filename = filenameFor(config, value);
						definition.setOutputFilename(filename);
						break;
					case "@label":
						definition.setLabel(value);
						break;
					case "@!boot":
						definition.setPlingBoot(Integer.parseInt(value,16));
						break;
					default:
						if ( ! key.startsWith(" ") ) {
							String source = filenameFor(config, value);
							FileDefinition diskFile = new FileDefinition(key, source);
							definition.addDiskFile(diskFile);
						} else {
							String attributeKey = key.trim();
							FileDefinition diskFile = definition.getDiskFiles().getLast();
							switch (attributeKey) {
							case "load":
								int loadAddr = Integer.parseInt(value, 16);
								diskFile.setLoadAddress(loadAddr);
								break;
							case "exec":
								int execAddr = Integer.parseInt(value, 16);
								diskFile.setExecAddress(execAddr);
								break;
							default:
								throw new InvalidParameterException( String.format( 
										"Unknown key : %s%n", attributeKey) );
							}
						}
						break;
					}
				}

			});
		} catch (IOException e) {
			System.err.println("A problem occurred reading the config file");
			return null;
		}

		if ( null == definition.getOutputFilename() ) {
			System.err.println("Desitnation filename not specified. Cannot proceed.");
			return null;
		}

		final DiskImage image = new DiskImage(definition);
		image.saveToFile( definition.getOutputFilename() );
		
		return image;
	}
}
