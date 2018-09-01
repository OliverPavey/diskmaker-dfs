package diskmaker.image;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import diskmaker.model.DiskDefinition;
import diskmaker.model.FileDefinition;

public class DiskImage {
	
	final private int SECTOR_SIZE = 256;
	final private int DISK_SIZE = 200 * 1024;
	
	final private Charset utf8 = Charset.forName("UTF-8");
	
	private byte[] disk = new byte[DISK_SIZE];
	
	protected int loc(int sector, int offset) {
		return ( sector * SECTOR_SIZE ) + offset;
	}
	
	public String getTitle() {
		StringBuilder sb = new StringBuilder(12);
		for (int i=0; i<8; i++)
			sb.append((char)disk[loc(0,i)]);
		for (int i=0; i<4; i++)
			sb.append((char)disk[loc(1,i)]);
		return sb.toString().trim();
	}
	
	public void setTitle(String label) {
		byte[] bytes = label.getBytes(utf8);
		for (int i=0; i<14; i++) {
			byte b = i>=bytes.length ? 0 : bytes[i];
			int sec = i / 8;
			int off = i % 8;
			disk[loc(sec,off)] = b;
		}
		incSequenceNumber();
	}
	
	public final static int PLING_BOOT_OFF  = 0;
	public final static int PLING_BOOT_LOAD = 1;
	public final static int PLING_BOOT_RUN  = 2;
	public final static int PLING_BOOT_EXEC = 3;
	
	public int getPlingBoot() {
		return ( disk[loc(1,6)] & 0x30 ) >> 4;
	}
	
	public void setPlingBoot(int value) {
		int bits = ( value & 0x3 ) << 4;
		disk[loc(1,6)] = (byte) ( ( disk[loc(1,6)] & 0xCF | bits ) ); 
		incSequenceNumber();
	}
	
	public int getSequenceNumber() {
		return disk[loc(1,4)];
	}
	
	private void incSequenceNumber() {
		disk[loc(1,4)] = (byte) ( ( disk[loc(1,4)]  & 0xFF ) + 1 );
	}
	
	public int getFileCount() {
		return disk[loc(1,5)]/8;
	}
	
	private void incFileCount() {
		disk[loc(1,5)] = (byte) ( ( disk[loc(1,5)] & 0xFF ) + 8 );
		incSequenceNumber();
	}
	
	public int getSectorSize() {
		return SECTOR_SIZE;
	}
	
	public int getDiskSize() {
		return DISK_SIZE;
	}
	
	public int getSectorCount() {
		int low  = disk[loc(1,7)] & 0xFF;
		int high = disk[loc(1,6)] & 0x3;
		return ( high * 0x100 ) + low;
	}
	
	private void setSectorCount(int sectorCount) {
		int low  = sectorCount & 0xFF;
		int high = ( sectorCount & 0x300 ) >> 8;
		
		disk[loc(1,7)] = (byte) low;
		disk[loc(1,6)] = (byte) ( (disk[loc(1,6)] & 0xFC) | (byte)high );
	}
	
	public int nextFreeSector() {
		int numFiles = getFileCount();
		
		if ( 0 == numFiles )
			return 2;

		// Find last file (bearing in mind that this code may not have
		//   built the disk file, so the last file on the disk may not
		//   be the last file in the catalog).
		int lastFileStartSector = 2;
		int lastFileCatalogPosition = 0; // i.e. Last on disk (not last in catalog)
		for (int candidate=0; candidate<numFiles; candidate++) {
			int candidateStartSector = getStartSector(candidate);
			if (candidateStartSector > lastFileStartSector) {
				lastFileCatalogPosition = candidate;
				lastFileStartSector = candidateStartSector;
			}
		}
		
		int lastFileSize = 0;
		if (lastFileCatalogPosition >= 0) {
			lastFileSize = getFileLength(lastFileCatalogPosition);
			lastFileStartSector = getStartSector(lastFileCatalogPosition);
		}
		
		if (0 == lastFileSize)
			return lastFileStartSector + 1; // Even empty files take one sector
		
		int sectorsFullyUsed = lastFileSize / SECTOR_SIZE;
		int overSpill        = lastFileSize % SECTOR_SIZE;
		int sectorsUsed = (0==overSpill) ? sectorsFullyUsed : sectorsFullyUsed+1;
		
		return lastFileStartSector + sectorsUsed;
	}
	
	public int getRemainingSpace() {
		return getDiskSize() - ( nextFreeSector() * SECTOR_SIZE );
	}
	
	/**
	 * returns offset within sector (not offset from start of disk)
	 */
	protected int catalogOffset(int sector, int catalogPosition) {
		switch (sector) {
		case 0:
			return 8 * ( catalogPosition + 1 );
		case 1:
			return 8 * ( catalogPosition + 1 );
		default:
			throw new DiskImageException("Invalid sector for catalog.", sector);
		}
	}
	
	public String getMainFilename(int catalogPosition) {
		int off = catalogOffset(0,catalogPosition);
		StringBuilder sb = new StringBuilder(7);
		for (int i=0; i<7; i++)
			sb.append((char)(disk[loc(0,off+i)] & 0xFF));
		return sb.toString().trim();
	}
	
	protected void setMainFilename(int catalogPosition, String filename) {
		if (filename.length() > 7)
			throw new DiskImageException("Filename too long.", filename);
		
		int off = catalogOffset(0,catalogPosition);
		int start = loc(0,off);
		
		byte[] bytes = filename.getBytes( utf8 );
		for (int i=0; i<bytes.length; i++)
			disk[start+i] = bytes[i];
		for (int i=bytes.length; i<7; i++)
			disk[start+i] = (byte)' ';
	}
	
	public char getDirectoryName(int catalogPosition) {
		int off = catalogOffset(0,catalogPosition);
		return (char) disk[loc(0,off+7)];
	}
	
	protected void setDirectoryName(int catalogPosition, char directoryName) {
		int off = catalogOffset(0,catalogPosition);
		disk[loc(0,off+7)] = (byte) directoryName;
	}
	
	public String getFullFilename(int catalogPosition) {
		char dir = getDirectoryName(catalogPosition);
		String name = getMainFilename(catalogPosition);
		if ( '$' != dir )
			return String.format("%c.%s", dir, name);
		else
			return name;
	}
	
	public int getLoadAddress(int catalogPosition) {
		int off = catalogOffset(1,catalogPosition);
		
		int low = disk[loc(1,off+0)] & 0xFF;
		int mid = disk[loc(1,off+1)] & 0xFF;
		int hi2 = ( disk[loc(1,off+6)] & 0xC ) >> 2;

		return (hi2 << 16) | (mid << 8) | low;
	}
	
	protected void setLoadAddress(int catalogPosition, int address) {
		int off = catalogOffset(1,catalogPosition);
		
		int low = address & 0xFF;
		int mid = ( address & 0xFF00 ) >> 8;
		int hi2 = ( address & 0x30000 ) >> 16;
		
		disk[loc(1,off+0)] = (byte) low;
		disk[loc(1,off+1)] = (byte) mid;
		disk[loc(1,off+6)] = (byte) ( ( disk[loc(1,off+6)] & 0xF3 ) | ( hi2 << 2 ) );
	}
	
	public int getExecutionAddress(int catalogPosition) {
		int off = catalogOffset(1,catalogPosition);
		
		int low = disk[loc(1,off+2)] & 0xFF;
		int mid = disk[loc(1,off+3)] & 0xFF;
		int hi2 = ( disk[loc(1,off+6)] & 0xC0 ) >> 6;

		return (hi2 << 16) | (mid << 8) | low;
	}
	
	protected void setExecutionAddress(int catalogPosition, int address) {
		int off = catalogOffset(1,catalogPosition);
		
		int low = address & 0xFF;
		int mid = ( address & 0xFF00 ) >> 8;
		int hi2 = ( address & 0x30000 ) >> 16;
		
		disk[loc(1,off+2)] = (byte) low;
		disk[loc(1,off+3)] = (byte) mid;
		disk[loc(1,off+6)] = (byte) ( ( disk[loc(1,off+6)] & 0x3F ) | ( hi2 << 6 ) );
	}
	
	public int getFileLength(int catalogPosition) {
		int off = catalogOffset(1,catalogPosition);
		
		int low = disk[loc(1,off+4)] & 0xFF;
		int mid = disk[loc(1,off+5)] & 0xFF;
		int hi2 = ( disk[loc(1,off+6)] & 0x30 ) >> 4;

		return (hi2 << 16) | (mid << 8) | low;
	}
	
	protected void setFileLength(int catalogPosition, int length) {
		int off = catalogOffset(1,catalogPosition);
		
		int low = length & 0xFF;
		int mid = ( length & 0xFF00 ) >> 8;
		int hi2 = ( length & 0x30000 ) >> 16;
		
		disk[loc(1,off+4)] = (byte) low;
		disk[loc(1,off+5)] = (byte) mid;
		disk[loc(1,off+6)] = (byte) ( ( disk[loc(1,off+6)] & 0xCF ) | ( hi2 << 4 ) );
	}
	
	public int getStartSector(int catalogPosition) {
		int off = catalogOffset(1,catalogPosition);
		
		int low = disk[loc(1,off+7)] & 0xFF;
		int hi2 = disk[loc(1,off+6)] & 0x3;

		return (hi2 << 8) | low;
	}
	
	private void setStartSector(int catalogPosition, int sector) {
		int off = catalogOffset(1,catalogPosition);
		
		int low = sector & 0xFF;
		int hi2 = ( sector & 0x300 ) >> 8;
		
		disk[loc(1,off+7)] = (byte) low;
		disk[loc(1,off+6)] = (byte) ( ( disk[loc(1,off+6)] & 0xFC ) | hi2 );
	}
	
	public void addFile( String filename , File source , int load , int exec ) {
		if ( getFileCount() >= 31 )
			throw new DiskImageException("Too many files", filename, getFileCount());
		if ( ! source.exists() )
			throw new DiskImageException("Source file not found", source.getPath());
		if ( ! source.isFile() )
			throw new DiskImageException("Source file is not a file", source.getPath());
		if ( ! source.canRead() )
			throw new DiskImageException("Source file cannot be read", source.getPath());
		if ( source.length() > getRemainingSpace() )
			throw new DiskImageException("Not enough disk space", filename, getRemainingSpace() );
		if ( ! filename.matches("(.\\.)?.{1,7}" ) ) 
			throw new DiskImageException("Unsupported filename", filename);
			
		char directoryName;
		String mainFilename;
		if ( ( filename.length() >= 3 ) & ( '.' == filename.charAt(1) ) ) {
			directoryName = filename.charAt(0);
			mainFilename = filename.substring(2);
		} else {
			directoryName = '$';
			mainFilename = filename;
		}
		
		int catalogPosition = getFileCount();
		
		int fileSize = (int) source.length();
		
		int startSector = nextFreeSector();
		int startSectorLocation = startSector * SECTOR_SIZE;
		
		setMainFilename(catalogPosition, mainFilename);
		setDirectoryName(catalogPosition, directoryName);
		setLoadAddress(catalogPosition, load);
		setExecutionAddress(catalogPosition, exec);
		setFileLength(catalogPosition, fileSize);
		setStartSector(catalogPosition, startSector);
		
		try {
			byte[] fileContent = Files.readAllBytes(source.toPath());
			for (int i=0; i<fileContent.length; i++)
				disk[startSectorLocation + i] = fileContent[i];
		} catch (IOException e) {
			throw new DiskImageException("Failure reading source file", 
					source.getPath(), e);
		}
		
		incFileCount();
	}
	
	public DiskImage() {
		Arrays.fill(disk, (byte)0);
		setSectorCount( DISK_SIZE / SECTOR_SIZE );
	}
	
	public DiskImage(DiskDefinition definition) {
		this();
		setTitle( definition.getLabel() );
		setPlingBoot( definition.getPlingBoot() );
		for (FileDefinition diskFile : definition.getDiskFiles()) {
			File source = new File( diskFile.getSource() );
			addFile( diskFile.getDest(), source, 
				diskFile.getLoadAddress(), diskFile.getExecAddress() );
		}
	}
	
	public DiskImage(String diskImageFilename) {
		this();
		loadFromFile(diskImageFilename);
	}
	
	protected void loadFromFile(String filename) {
		byte[] fileContent;
		try {
			fileContent = Files.readAllBytes(new File(filename).toPath());
		} catch (IOException e) {
			throw new DiskImageException("Diskfile could not be read.", e); 
		}
		if ( fileContent.length > disk.length ) {
			throw new DiskImageException("Diskfile contains image which is too large.");
		}
		Arrays.fill(disk, (byte)0);
		for (int i=0; i<fileContent.length; i++)
			disk[i] = fileContent[i];
	}
	
	public void saveToFile(String filename) {
		System.out.println(this.toString().replaceAll("DiskImage.File", "\nDiskImage.File"));
		
		byte[] saveBytes = Arrays.copyOf(disk, SECTOR_SIZE*nextFreeSector());
		
		Path destination = Paths.get(filename);
		try {
			Files.write(destination, saveBytes, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DiskImage [");
		sb.append("title='").append(getTitle()).append("', ");
		sb.append("diskSize=").append(getDiskSize()).append(", ");
		sb.append("sectorSize=").append(getSectorSize()).append(", ");
		sb.append("sectorCount=").append(getSectorCount()).append(", ");
		sb.append("sequenceNumber=").append(getSequenceNumber()).append(", ");
		sb.append("!boot=").append(getPlingBoot()).append(", ");
		sb.append("remainingSpace=").append(getRemainingSpace()).append(", ");
		sb.append("fileCount=").append(getFileCount()).append(" ");
		for (int catPos=0; catPos<getFileCount(); catPos++) {
			sb.append("DiskImage.File(").append(catPos).append(") [");
			sb.append("fullFilename='").append(getFullFilename(catPos)).append("', ");
			sb.append("directoryName='").append(getDirectoryName(catPos)).append("', ");
			sb.append("mainFilename='").append(getMainFilename(catPos)).append("', ");
			sb.append("fileLength=").append(getFileLength(catPos)).append(", ");
			sb.append("startSector=").append(getStartSector(catPos)).append(", ");
			sb.append("loadAddress=").append(getLoadAddress(catPos)).append(", ");
			sb.append("executionAddress=").append(getExecutionAddress(catPos)).append("] ");
		}
		sb.append("]");
		return sb.toString();
	}
}
