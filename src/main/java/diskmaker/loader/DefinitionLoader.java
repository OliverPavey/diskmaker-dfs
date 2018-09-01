package diskmaker.loader;

import java.io.File;

import diskmaker.image.DiskImage;

public interface DefinitionLoader {
	DiskImage convert(String filename);

	default public String filenameFor(File config, String filespec) {
		return config.toPath().getParent().resolve(filespec).normalize().toString();
	}
}
