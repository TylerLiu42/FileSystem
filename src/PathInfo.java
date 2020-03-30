
public class PathInfo {
	
	private String currentDirectory;
	private String currentFileID;
	PathInfo(String currentDirectory, String currentFileID) {
		this.currentDirectory = currentDirectory;
		this.currentFileID = currentFileID;
	}
	
	String getCurrentDirectory() {
		return this.currentDirectory;
	}
	
	String getCurrentFileID() {
		return this.currentFileID;
	}
}
