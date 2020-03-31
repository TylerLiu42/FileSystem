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
	String currentFileID;
	public Utilities(Connection con, String currentFileID) {
		this.con = con;
		this.currentFileID = currentFileID; 
	}

	protected void ls(boolean longFlag) {
		try {
			Statement stmt = con.createStatement();
			PreparedStatement pstmt = con.prepareStatement("select name,fileType,readPermission,writePermission,execPermission,created"
							+ " from File where parent = (select fileID from File where fileID = ?)");
			pstmt.setString(1, currentFileID);
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

	protected PathInfo cd(String path, PathInfo origPath) {
		try {
            PathInfo newPath = resolvePath(path, origPath);
            if (newPath.getFileType() != PathInfo.FileType.Directory) {
                System.out.println("Not a directory");
                return origPath;
            }
            return newPath;
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return null;
	}

    private PathInfo resolvePath(String fullPath, PathInfo origPath) throws SQLException {
		String[] pathComponents = fullPath.split("/");
        int start = 0;
        PathInfo newPath = new PathInfo(origPath);
        if (pathComponents.length == 0 || pathComponents[0].equals("")) {
            // abs path
            currentFileID = PathInfo.ROOTID;
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
                    pstmt = con.prepareStatement("select fileID, fileType, name from File where fileID = (select parent from File where fileID = ?)");
                    newPath.pop();
                } else {
                    pstmt = con.prepareStatement("select fileID, fileType, name from File where parent = ? and name = ?");
                    pstmt.setString(2, name);
                    newPath.push(name);
                }
                pstmt.setString(1, currentFileID);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    currentFileID = rs.getString(1);
                    newPath.setFileID(currentFileID);
                    newPath.setFileType(PathInfo.fileType(rs.getString(2)));
                } else {
                    System.out.println("Path component " + name + " not found");
                    return origPath;
                }
            }
        }
        return newPath;
    }
}
