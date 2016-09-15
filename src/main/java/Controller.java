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
    private final String passPhrase;
    private final String fileName;

    Controller(String[] args) throws Exception {
        this.options = getOptions();
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.getArgs().length != 2) {
                throw new ParseException("A file and a passphrase are required.");
            }

            this.commandLine = commandLine;
            fileName = commandLine.getArgs()[0];
            passPhrase = commandLine.getArgs()[1];
        } catch (ParseException e) {
            System.out.println("Incorrect arguments:" + e.getMessage());
            printHelpPage();
            throw new Exception("incorrect input program should close");
        }
    }

    private void printHelpPage() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("[-h] [-d] <file> <passphrase>",
                "En/Decrypts stdin to stdout. using the Feistel encryption method",
                options, "");
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("d", "decrypt", false, "decrypt");
        options.addOption("h", "help", false, "Display this help page");
        return options;
    }

    public void run() throws IOException, ParseException {
        if (passPhrase.equals("help") || commandLine.hasOption("h")) {
            printHelpPage();
            return;
        }

        System.out.println("Running Feistel with options -d: "
                + commandLine.hasOption('d') +
                " and pass phrase: '" + passPhrase +
                "' and input file: " + fileName);

        byte[] fileContent = Files.readAllBytes(Paths.get(fileName));

        String fileAppend;

        byte[] result;
        FeistelCipher feistel = new FeistelCipher(passPhrase);
        if (commandLine.hasOption('d')) {
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
