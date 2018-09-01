package diskmaker.image;

//TODO: Remove (misused) RuntimeException
@SuppressWarnings("serial") 
class DiskImageException extends RuntimeException {
	public DiskImageException(String message, Object... info) {
		super(augmentedMessage(message,info));
	}
	
	private static String augmentedMessage(String message, Object... info) {
		StringBuilder sb = new StringBuilder(message);
		for (Object obj : info) {
			sb.append(" [").append(obj).append("]");
		}
		return sb.toString();
	}
}