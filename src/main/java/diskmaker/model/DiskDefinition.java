package diskmaker.model;

import java.util.Arrays;
import java.util.LinkedList;

public class DiskDefinition {

	private String outputFilename = "";
	private String label = "";
	private LinkedList<FileDefinition> diskFiles = new LinkedList<>();
	private int plingBoot = 0;
	
	public String getOutputFilename() {
		return outputFilename;
	}
	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public LinkedList<FileDefinition> getDiskFiles() {
		return diskFiles;
	}
	
	public void addDiskFile(FileDefinition diskFile) {
		this.diskFiles.add( diskFile );
	}
	
	public int getPlingBoot() {
		return plingBoot;
	}
	public void setPlingBoot(int plingBoot) {
		this.plingBoot = plingBoot;
	}
	
	@Override
	public String toString() {
		return "DiskDefinition [outputFilename=" + outputFilename + ", "
				+ "label=" + label + ", "
				+ "diskFiles=" + Arrays.toString(diskFiles.toArray()) + "]";
	}
}
