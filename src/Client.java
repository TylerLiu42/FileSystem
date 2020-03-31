import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.ArrayList;

public class Client {

	public static void main(String[] args) throws IOException {  
        PathInfo currentPath = new PathInfo(new ArrayList(), PathInfo.ROOTID, PathInfo.FileType.Directory);
		BufferedReader br = new BufferedReader(new FileReader("pw.txt"));
		String password = br.readLine();
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");  
			con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/filesystem", "root", password);
			Statement stmt = con.createStatement();  
		} catch (SQLException e) {
			e.printStackTrace();
		}  
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Scanner scanner = new Scanner(System.in);
		
		while (true) {
			System.out.print(currentPath.pathStr() + " > Enter command: ");
			String command = scanner.nextLine().trim();
            if (command.equals("")) {
                continue;
            }
			else if (command.equals("ls")) {
				Utilities util = new Utilities(con, currentPath.getFileID());
				util.ls(false);
			}
			else if (command.equals("ls -l")) {
				Utilities util = new Utilities(con, currentPath.getFileID());
				util.ls(true);
			}
			else if (command.substring(0, 2).equals("sh")) {
				String[] commandStr = command.split(" ");
				String executableName = commandStr[1];
				Utilities util = new Utilities(con, currentPath.getFileID());
				util.sh(executableName);
			}
			else if (command.substring(0, 2).equals("cd")) {
				String[] commandStr = command.split(" ");
                if (commandStr.length < 2) { continue; }

				String fullPath = commandStr[1];
				Utilities util = new Utilities(con, currentPath.getFileID());
                PathInfo newPath = util.cd(fullPath, currentPath);
                currentPath = newPath;
			}
            else {
                System.out.println("Unrecognized command");
            }
		}
	}
}
