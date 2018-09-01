package diskmaker.loader.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import diskmaker.image.DiskImage;
import diskmaker.loader.DefinitionLoader;
import diskmaker.model.DiskDefinition;
import diskmaker.model.FileDefinition;

public class DefinitionLoaderXml implements DefinitionLoader {
	
	@SuppressWarnings("serial")
	public static class XmlDataException extends Exception {
		public XmlDataException(String message) {
			super.getMessage();
		}
	}
	
	public DiskImage convert(String filename) {
		File config = new File( filename );
		return convert( config );
	}
	
	public DiskImage convert(File config) {
		DiskDefinition def = new DiskDefinition();
		
		try (InputStream is = new FileInputStream(config)) {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(config);
			
			Element root = doc.getDocumentElement();
			if ( root == null )
				throw new XmlDataException("Could not extract XML root.");
			if ( ! root.getNodeName().equals("disk") )
				throw new XmlDataException("Root element must be 'disk'.");

			NodeList rootChildren = root.getChildNodes();
			for (int i=0; i<rootChildren.getLength(); i++) {
				Node child = rootChildren.item(i);
				
				switch (child.getNodeName()) {
				case "image":
					def.setOutputFilename( filenameFor(config, child.getTextContent()) );
					break;
				case "label":
					def.setLabel(child.getTextContent());
					break;
				case "boot-opt":
					def.setPlingBoot(Integer.parseInt(child.getTextContent()));
					break;
				case "files":
					NodeList files = child.getChildNodes();
					for (int j=0; j<files.getLength(); j++) {
						Node fileChild = files.item(j);
						convertFile(config, def, fileChild);
					}
					break;
				default:
					break;
				}
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("The configuration file could not be found.");
			return null;
		} catch (IOException e) {
			System.err.println("A problem occurred reading the config file.");
			return null;
		} catch (ParserConfigurationException e) {
			System.err.println("The parser could not process the configuration file.");
			return null;
		} catch (SAXException e) {
			System.err.println("The configuration file could not be parsed.");
			return null;
		} catch (XmlDataException e) {
			System.err.println(e.getMessage());
			return null;
		}
		
		final DiskImage image = new DiskImage(def);
		image.saveToFile( def.getOutputFilename() );
		
		return image;
	}

	private void convertFile(File config, DiskDefinition def, Node fileNode) throws XmlDataException {
		String name = "";
		String source = "";
		int loadAddress = 0;
		int execAddress = 0;
		
		if ("file".equals(fileNode.getNodeName())) {
			
			NodeList childNodes = fileNode.getChildNodes();
			for (int i=0; i<childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);

				switch (childNode.getNodeName()) {
				case "name":
					name = childNode.getTextContent();
					break;
				case "source":
					source = filenameFor(config, childNode.getTextContent());
					break;
				case "loadAddress":
					loadAddress = Integer.parseInt( childNode.getTextContent() , 16 );
					break;
				case "execAddress":
					execAddress = Integer.parseInt( childNode.getTextContent() , 16 );
					break;
				default:
					break;
				}
			}
			
			FileDefinition fileDefinition = new FileDefinition(name, source);
			fileDefinition.setLoadAddress(loadAddress);
			fileDefinition.setExecAddress(execAddress);
			def.addDiskFile(fileDefinition);
		}
	}
}
