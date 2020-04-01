import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class Utilities {
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

			Statement stmt = con.createStatement();
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

	protected void sh(String executableName) {
		try {
			Statement stmt = con.createStatement();
			PreparedStatement pstmt = con.prepareStatement("select data from Content join File using (contentID) where name = ?");
			pstmt.setString(1, executableName);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Blob blob = rs.getBlob(1);
				InputStream in = blob.getBinaryStream();
				OutputStream out = new FileOutputStream(executableName);
				byte[] buff = blob.getBytes(1,(int)blob.length());
				out.write(buff);
				out.close();
				in.close();
                Process p = new ProcessBuilder(executableName).start();
                p.waitFor();
			} else {
			    System.out.println("Not a regular file or hardlink");
			}
		} catch (SQLException | IOException | InterruptedException e) {
			e.printStackTrace();
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

    private PathInfo resolvePath(String fullPath) throws SQLException {
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
                Statement stmt = con.createStatement();
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
                    PathInfo.FileType type = PathInfo.fileType(rs.getString(2));

                    if (type.equals(PathInfo.FileType.SymLink)) {
                        String symPath = rs.getString(3);
                        newPath.pop(); // Remove symlink name from the path
                        newPath = new Utilities(con, newPath).resolvePath(symPath);
                        if (newPath == null) { return null; }
                    } else {
                        newPath.setFileID(rs.getString(1));
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
}
