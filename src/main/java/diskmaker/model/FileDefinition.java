package diskmaker.model;

public class FileDefinition {

	private String dest;
	private String source;
	private int loadAddress;
	private int execAddress;
	
	public FileDefinition(String dest, String source) {
		super();
		this.dest = dest;
		this.source = source;
	}
	
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public int getLoadAddress() {
		return loadAddress;
	}
	public void setLoadAddress(int loadAddress) {
		this.loadAddress = loadAddress;
	}
	public int getExecAddress() {
		return execAddress;
	}
	public void setExecAddress(int execAddress) {
		this.execAddress = execAddress;
	}

	@Override
	public String toString() {
		return "FileDefinition [dest=" + dest + ", source=" + source + "]";
	}
}
