import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws IOException {  
		String currentDirectory = "root";
		String currentFileID = "1";
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
			System.out.print(currentDirectory + " > Enter command: ");
			String command = scanner.nextLine().trim();
            if (command.equals("")) {
                continue;
            }
			else if (command.equals("ls")) {
				Utilities util = new Utilities(con, currentDirectory, currentFileID);
				util.ls(false);
			}
			else if (command.equals("ls -l")) {
				Utilities util = new Utilities(con, currentDirectory, currentFileID);
				util.ls(true);
			}
			else if (command.substring(0, 2).equals("sh")) {
				String[] commandStr = command.split(" ");
				String executableName = commandStr[1];
				Utilities util = new Utilities(con, currentDirectory, currentFileID);
				util.sh(executableName);
			}
			else if (command.substring(0, 2).equals("cd")) {
				String[] commandStr = command.split(" ");
				String fullPath = commandStr[1];
				String[] pathComponents = fullPath.split("/");
				boolean isForward = true;
				if (command.substring(0, 5).equals("cd ..")) isForward = false;
				for (String path : pathComponents) {
					Utilities util = new Utilities(con, currentDirectory, currentFileID);
					PathInfo newPathInfo = util.cd(path, currentFileID, isForward);
					currentDirectory = newPathInfo.getCurrentDirectory() == null ? currentDirectory : newPathInfo.getCurrentDirectory();
					currentFileID = newPathInfo.getCurrentFileID();
				}
			}
            else {
                System.out.println("Unrecognized command");
            }
		}
	}
}
