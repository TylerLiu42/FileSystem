import java.util.ArrayList;

public class PathInfo {
    public enum FileType {
        HardLink,
        SymLink,
        Directory,
    }

    public static String ROOTID = "";

    ArrayList<String> path;
    String tipFileID;
    FileType tipFileType;

	public PathInfo(ArrayList<String> path, String tipFileID, FileType tipFileType) {
        this.path = path;
        this.tipFileID = tipFileID;
        this.tipFileType = tipFileType;
	}

    public PathInfo(PathInfo other) {
        this.tipFileID = other.tipFileID;
        this.tipFileType = other.tipFileType;
        this.path = (ArrayList<String>)other.path.clone();
    }
	
	public String getFileID() {
        return tipFileID;
	}

	public FileType getFileType() {
        return tipFileType;
	}

	public void setFileID(String fileID) {
        tipFileID = fileID;
	}

	public void setFileType(FileType fileType) {
        tipFileType = fileType;
	}

    public String pathStr() {
        return "/" + String.join("/", path);
    }

    public void push(String filename) {
        path.add(filename);
    }

    public void pop() {
        if (path.size() > 0) {
            tipFileID = ROOTID;
            tipFileType = FileType.Directory;
            path.remove(path.size() - 1);
        }
    }

    public void clear() {
        tipFileID = null;
        tipFileType = FileType.Directory;
        path.clear();
    }

    public static FileType fileType(String type) {
        if (type.equals("directory")) {
            return FileType.Directory;
        } else if (type.equals("symLink")) {
            return FileType.SymLink;
        } else if (type.equals("hardLink")) {
            return FileType.HardLink;
        } else {
            throw new IllegalArgumentException("Invalid file type");
        }
    }
}
