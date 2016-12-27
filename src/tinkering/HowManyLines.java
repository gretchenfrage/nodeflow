package tinkering;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class HowManyLines {

	public static void main(String[] args) {
		System.out.println(lines(new File("C:\\Users\\kahlo\\Desktop\\Java\\pnet\\src\\com\\phoenixkahlo\\nodenet")));
	}
	
	public static int lines(File file) {
		if (file.isDirectory()) {
			return Arrays.stream(file.listFiles()).mapToInt(HowManyLines::lines).sum();
		} else if (isDotJava(file.getName())) {
			try (InputStream in = new FileInputStream(file)) {
				int lines = 0;
				while (in.available() > 0)
					if (in.read() == '\n')
						lines++;
				return lines;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return 0;
		}
	}
	
	public static boolean isDotJava(String name) {
		return name.length() > 5 && name.substring(name.length() - 5, name.length()).equals(".java");
	}
	
}
