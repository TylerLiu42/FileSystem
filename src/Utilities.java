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
	
	protected void ls() {
		try {
			Statement stmt = con.createStatement();
			PreparedStatement pstmt = con.prepareStatement("select name from file where parent = (select fileID from file where name = ?)");
			pstmt.setString(1, currentDirectory);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) System.out.println(rs.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
