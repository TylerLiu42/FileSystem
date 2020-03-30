import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Utilities {
	Connection con;
	String currentDirectory;
	public Utilities(Connection con, String currentDirectory) {
		this.con = con;
		this.currentDirectory = currentDirectory;
	}
	
	protected void ls(boolean longFlag) {
		try {
			Statement stmt = con.createStatement();
			PreparedStatement pstmt = longFlag ? 
					con.prepareStatement("select name,fileType,readPermission,writePermission,execPermission,created"
							+ " from file where parent = (select fileID from file where name = ?)")
					: con.prepareStatement("select name from file where parent = (select fileID from file where name = ?)");
			pstmt.setString(1, currentDirectory);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
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
			PreparedStatement pstmt = con.prepareStatement("select data from content join file using (contentID) where name = ?");
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
			}
			Process p = new ProcessBuilder(executableName).start();
			p.waitFor();
		} catch (SQLException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
