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
			String[] command = scanner.nextLine().trim().split(" ");
            if (command.length == 0 || command[0].equals("")) {
                continue;
            }
			else if (command[0].equals("ls")) {
				Utilities util = new Utilities(con, currentPath);

                int argIdx = 1;
                boolean longFlag = false;
                if (command.length > 1) {
                    if (command[1].equals("-l")) {
                        argIdx++;
                        longFlag = true;
                    }
                }

                if (command.length > argIdx) {
                    util.ls(longFlag, command[argIdx]);
                } else {
                    util.ls(longFlag, ".");
                }
			}
			else if (command[0].equals("sh")) {
				String executableName = command[1];
				Utilities util = new Utilities(con, currentPath);
				util.sh(executableName);
			}
			else if (command[0].equals("cd")) {
                if (command.length < 2) { continue; }

				String fullPath = command[1];
				Utilities util = new Utilities(con, currentPath);
                PathInfo newPath = util.cd(fullPath);
                currentPath = newPath;
			}
            else {
                System.out.println("Unrecognized command");
            }
		}
	}
}
