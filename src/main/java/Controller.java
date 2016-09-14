import org.apache.commons.cli.*;

/**
 * Created by Sander on 14-9-2016.
 */
public class Controller {

    private CommandLine commandLine;
    private final Options options;
    private final String passPhrase;

    Controller(String[] args) throws Exception {
        this.options = getOptions();
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.getArgs().length != 1) {
                throw new ParseException("The encryption key and only the encryption key should be passed as a non flag argument.");
            }

            this.commandLine = commandLine;
            passPhrase = commandLine.getArgs()[0];
        } catch (ParseException e) {
            System.out.println("Incorrect arguments:");
            printHelpPage();
            throw new Exception("incorrect input program should close");
        }
    }

    private void printHelpPage() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("[-h] [-o] [-d] <key>",
                "En/Decrypts stdin to stdout. using the Feistel encryption method",
                options, "");
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("d", "decrypt", false, "decrypt");
        options.addOption("h", "help", false, "Display this help page");
        return options;
    }

    public void run() {
        if (passPhrase.equals("help") || commandLine.hasOption("h")) {
            printHelpPage();
            return;
        }

        System.out.println("Running Feistel with options -o: "
                + commandLine.hasOption('d') + " and key: " + passPhrase);

        new FeistelCipher(passPhrase)
                .setDecrypting(commandLine.hasOption('d'))
                .run(System.in);
    }
}
