package diskmaker;

import diskmaker.loader.DefinitionLoader;
import diskmaker.loader.txt.DefinitionLoaderTxt;
import diskmaker.loader.xml.DefinitionLoaderXml;

public class DiskMaker {
	
	public static void main(String[] args) {
		(new DiskMaker()).instanceMain(args);
	}

	public void instanceMain(String[] args) {
		
		DefinitionLoader loader = null;
		
		if (1 != args.length)  {
			System.err.println("Syntax: java -jar DiskMaker <config-file>");
		} else {
			if (args[0].toLowerCase().endsWith(".xml")) {
				loader = new DefinitionLoaderXml();
			} else {
				loader = new DefinitionLoaderTxt();
			}
			
			loader.convert( args[0] );
		}
	}
}
