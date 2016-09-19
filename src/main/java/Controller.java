import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Sander on 14-9-2016.
 */
public class Controller {

    private CommandLine commandLine;
    private final Options options;

    Controller(String[] args) throws Exception {
        this.options = getOptions();
        CommandLineParser parser = new BasicParser();
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Incorrect arguments:" + e.getMessage());
            printHelpPage();
            throw new Exception("Incorrect input, stopping");
        }
    }

    private void printHelpPage() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("[-h] [-d] <file> <passphrase>",
                "En/Decrypts a file, using the Feistel encryption method.\n" +
                        "<file> is the file to en/decrypt\n" +
                        "<passphrase> the passphrase to use while en/decrypting",
                options, "");
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("d", "decrypt", false, "decrypt");
        options.addOption("h", "help", false, "Display this help page");
        return options;
    }

    public void run() throws IOException, ParseException {
        if (commandLine.hasOption("h")) {
            printHelpPage();
            return;
        }

        if (commandLine.getArgs().length != 2) {
            throw new ParseException("A file and a passphrase are required.");
        }

        String fileName = commandLine.getArgs()[0];
        String passPhrase = commandLine.getArgs()[1];

        System.out.println("Running Feistel with options -d: "
                + commandLine.hasOption('d') +
                " and pass phrase: '" + passPhrase +
                "' and input file: " + fileName);

        run(commandLine.hasOption('d'), fileName, passPhrase);
    }

    private void run(boolean decrypt, String fileName, String passPhrase) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(fileName));

        String fileAppend;

        byte[] result;
        FeistelCipher feistel = new FeistelCipher(passPhrase);
        if (decrypt) {
            fileAppend = ".txt";
            result = feistel.decrypt(fileContent);
        } else {
            fileAppend = ".enc.Feistel";
            result = feistel.encrypt(fileContent);
        }

        Files.write(Paths.get(fileName + fileAppend), result);
        System.out.println("Result written to: " + fileName + fileAppend);
    }
}
