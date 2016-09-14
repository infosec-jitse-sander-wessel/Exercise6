/**
 * Created by Sander on 14-9-2016.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Controller controller = new Controller(args);
            controller.run();
        } catch (Exception ignored) {
            // we let the program end.
        }
    }
}
