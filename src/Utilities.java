import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class Utilities {
    interface FileOperation {
        public void call(PathInfo dir, String name, String fileID, PathInfo.FileType fileType, String readPermission, String writePermission, String execPermission, String created, String symLink, String contentID); 
    }
    
	Connection con;
	PathInfo pathInfo;

	public Utilities(Connection con, PathInfo pathInfo) {
		this.con = con;
		this.pathInfo = pathInfo;
	}

	protected void ls(boolean longFlag, String path) {
		try {
            PathInfo lsPath = resolvePath(path);
            if (lsPath == null) {
                return;
            }

            String dirStmt = "select name,fileType,readPermission,writePermission,execPermission,created"
                                            + " from File where parent = (select fileID from File where fileID = ?)";
            String fileStmt = "select name,fileType,readPermission,writePermission,execPermission,created from File where fileID = ?";
			PreparedStatement pstmt = con.prepareStatement(lsPath.getFileType() == PathInfo.FileType.Directory ? dirStmt : fileStmt);
			pstmt.setString(1, lsPath.getFileID());
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
                int count = longFlag ? rs.getMetaData().getColumnCount() : 1;
				for (int i = 1; i <= count; i++) {
		           System.out.print(rs.getString(i) + " ");
		       }
			   System.out.println();
			} 
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void sh(String path) {
		try {
			boolean isFullPath = true;
			String[] pathComponents = path.split("/");
			if (pathComponents.length == 1) isFullPath = false;
			String executableName = pathComponents[pathComponents.length-1];
			pathComponents[pathComponents.length-1] = "";
			String pathWithoutFile = String.join("/", pathComponents);
			PathInfo resolvedPath = cd(pathWithoutFile);
			PreparedStatement pstmt = isFullPath
					? con.prepareStatement("select name, data from Content join File using (contentID) where name = ? and parent = ?")
					: con.prepareStatement("select name, data from Content join File using (contentID) where name = ?");
			pstmt.setString(1, executableName);
			if (isFullPath) pstmt.setString(2, resolvedPath.getFileID());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Blob blob = rs.getBlob(2);
				String resolvedFileName = rs.getString(1);
				InputStream in = blob.getBinaryStream();
				OutputStream out = new FileOutputStream(System.getProperty("java.io.tmpdir") + '\\' + resolvedFileName);
				byte[] buff = blob.getBytes(1,(int)blob.length());
				out.write(buff);
				out.close();
				in.close();
                Process p = new ProcessBuilder(resolvedFileName).start();
                p.waitFor();
			} else {
			    System.out.println("Not a regular file or hardlink");
			}
		} catch (SQLException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

    protected void find(String path, String partialName) {
        try {
            PathInfo findDir = resolvePath(path);
            if (findDir == null) { return; }
            if (findDir.getFileType() != PathInfo.FileType.Directory) {
                System.out.println("Can only call find on a directory");
                return;
            }

            FileOperation findOp = new FileOperation() {
                String partial = partialName;
                public void call(PathInfo dir, String name, String fileID, PathInfo.FileType fileType, String readPermission, String writePermission, String execPermission, String created, String symLink, String contentID) {
                    if (name.contains(partial)) {
                        String[] args = {fileType.toString(), readPermission, writePermission, execPermission, created};
                        System.out.println(dir.pathStr()+"/"+name+" "+String.join(" ", args));
                    }
                } 
            };
            dirWalk(findDir, findOp);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void grep(String pattern, String searchStr) {
        FileOperation grepOp = new FileOperation() {
            String search = searchStr;
            String partialName = pattern;
            Connection conn = con;

            public void call(PathInfo dir, String name, String fileID, PathInfo.FileType fileType, String readPermission, String writePermission, String execPermission, String created, String symLink, String contentID) {
                if (partialName != null && !name.contains(partialName)) { return; }
                try {
                    String content;
                    if (fileType == PathInfo.FileType.SymLink) {
                        content = symLink;
                    } else {
                        PreparedStatement pstmt = conn.prepareStatement("select data from Content where contentID = ?");
                        pstmt.setString(1, contentID);
                        ResultSet rs = pstmt.executeQuery();
                        rs.next();
                        content = rs.getString(1);
                    }

                    String[] lines = content.split("\n");
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if (line.contains(search)) {
                            System.out.println(dir.pathStr()+"/"+name+":"+i+": "+line);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } 
        };

        try {
            dirWalk(this.pathInfo, grepOp);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Won't walk thru symlinks
    private void dirWalk(PathInfo dir, FileOperation op) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("select fileID, name, fileType, readPermission, writePermission, execPermission, created, symLink, contentID from File where parent = ?");
        pstmt.setString(1, dir.getFileID());
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            String fileID = rs.getString(1);
            String name = rs.getString(2);
            PathInfo.FileType fileType = PathInfo.fileType(rs.getString(3));

            if (fileType == PathInfo.FileType.Directory) {
                if (!fileID.equals(PathInfo.ROOTID)) {
                    PathInfo nextDir = new PathInfo(dir);
                    nextDir.push(name);
                    nextDir.setFileID(fileID);
                    dirWalk(nextDir, op);
                }
            } else {
                String readPermission = rs.getString(4);
                String writePermission = rs.getString(5);
                String execPermission = rs.getString(6);
                String created = rs.getString(7);
                String symLink = rs.getString(8);
                String contentID = rs.getString(9);
                op.call(dir, name, fileID, fileType, readPermission, writePermission, execPermission, created, symLink, contentID);
            }
        }
    }

	protected PathInfo cd(String path) {
		try {
            PathInfo newPath = resolvePath(path);
            if (newPath == null) { return pathInfo; }
            if (newPath.getFileType() != PathInfo.FileType.Directory) {
                System.out.println("Not a directory");
                return pathInfo;
            }
            return newPath;
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return null;
	}

    private PathInfo resolvePath(String fullPath, ArrayList<String> seenSymLinks) throws SQLException {
		String[] pathComponents = fullPath.split("/");
        int start = 0;
        PathInfo newPath = new PathInfo(pathInfo);
        if (pathComponents.length == 0 || pathComponents[0].equals("")) {
            // abs path
            start = 1;
            newPath.clear();
        }

        String[] path = Arrays.copyOfRange(pathComponents, Math.min(start, pathComponents.length), pathComponents.length);
        for (String name: path) {
            if (name.equals(".")) {
                continue;
            } else {
                PreparedStatement pstmt;
                if (name.equals("..")) {
                    pstmt = con.prepareStatement("select fileID, fileType, symLink from File where fileID = (select parent from File where fileID = ?)");
                    newPath.pop();
                } else {
                    pstmt = con.prepareStatement("select fileID, fileType, symLink from File where parent = ? and name = ?");
                    pstmt.setString(2, name);
                    newPath.push(name);
                }
                pstmt.setString(1, newPath.getFileID());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String fileId = rs.getString(1);
                    PathInfo.FileType type = PathInfo.fileType(rs.getString(2));

                    if (type.equals(PathInfo.FileType.SymLink)) {
                        if (seenSymLinks.contains(fileId)) {
                            System.out.println("Circular symlink in path");
                            return null;
                        }
                        seenSymLinks.add(fileId);
                        String symPath = rs.getString(3);
                        newPath.pop(); // Remove symlink name from the path
                        newPath = new Utilities(con, newPath).resolvePath(symPath, seenSymLinks);
                        if (newPath == null) { return null; }
                    } else {
                        newPath.setFileID(fileId);
                        newPath.setFileType(type);
                    }
                } else {
                    System.out.println("Path component " + name + " not found");
                    return null;
                }
            }
        }
        return newPath;
    }

    private PathInfo resolvePath(String fullPath) throws SQLException {
        return resolvePath(fullPath, new ArrayList());
    }
}
