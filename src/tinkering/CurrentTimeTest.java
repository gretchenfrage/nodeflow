package tinkering;

public class CurrentTimeTest {

	public static void main(String[] args) throws Exception {
		while (true) {
			System.out.println(System.currentTimeMillis());
			Thread.sleep(1000);
		}
	}

}
