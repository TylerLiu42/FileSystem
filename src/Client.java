import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws IOException {  
		String currentDirectory = "root";
		BufferedReader br = new BufferedReader(new FileReader("pw.txt"));
		String password = br.readLine();
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");  
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/filesystem", "root", password);
			Statement stmt = con.createStatement();  
		} catch (SQLException e) {
			e.printStackTrace();
		}  
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Scanner scanner = new Scanner(System.in);
		
		while (true) {
			System.out.print("Enter command: ");
			String command = scanner.next();
			if (command.equals("ls")) {
				Utilities util = new Utilities(con, currentDirectory);
				util.ls();
			}
		}
	}
}
