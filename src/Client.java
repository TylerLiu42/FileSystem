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
        Utilities util = new Utilities(con, currentPath);
		
		while (true) {
			System.out.print(currentPath.pathStr() + " > Enter command: ");
			String[] command = scanner.nextLine().trim().split(" ");
            if (command.length == 0 || command[0].equals("")) {
                continue;
            }
			else if (command[0].equals("ls")) {
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
				util.sh(executableName);
			}
			else if (command[0].equals("cd")) {
                if (command.length < 2) { continue; }

				String fullPath = command[1];
                PathInfo newPath = util.cd(fullPath);
                currentPath = newPath;
			}
            else if (command[0].equals("find")) {
                if (command.length != 3) { 
                    System.out.println("find takes 2 arguments: directory and partial name");
                    continue;
                }

                String dir = command[1];
                String partialName = command[2];
                util.find(dir, partialName);
            }
            else {
                System.out.println("Unrecognized command");
            }
		}
	}
}
