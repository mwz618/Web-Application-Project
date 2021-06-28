import java.io.File;
        import java.io.FileNotFoundException;
        import java.util.Scanner;

public class RecaptchaConstants {


    public static String SECRET_KEY = "";

    public RecaptchaConstants() {
        try (Scanner words = new Scanner(new File("secretkey.txt"))) {
            ;
            SECRET_KEY = words.nextLine();
        } catch (FileNotFoundException e) {
            System.out.println("secretkey.txt not found");
        }
    }
}