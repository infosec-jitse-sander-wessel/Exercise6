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
                throw new ParseException("The encryption key and only the encryption key should be passed as a non flag argument.");
            }

            this.commandLine = commandLine;
            passPhrase = commandLine.getArgs()[0];
            fileName = commandLine.getArgs()[1];
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

    public void run() throws IOException, ParseException {
        if (passPhrase.equals("help") || commandLine.hasOption("h")) {
            printHelpPage();
            return;
        }

        System.out.println("Running Feistel with options -d: "
                + commandLine.hasOption('d') +
                " and key: " + passPhrase +
                " and input file: " + fileName);

        byte[] fileContent;

        if (commandLine.hasOption('d')) {
            fileContent = Files.readAllBytes(Paths.get(fileName));
        } else {
            fileContent = Files.lines(Paths.get(fileName))
                    .reduce((firstLine, secondLine) -> firstLine + "\n" + secondLine)
                    .orElseThrow(() -> new ParseException("Failed to read the encrypted file"))
                    .getBytes();
        }

        System.out.println(new String(fileContent));

        byte[] result = new FeistelCipher(passPhrase)
                .setDecrypting(commandLine.hasOption('d'))
                .run(fileContent);

        System.out.println(new String(result));

        if (commandLine.hasOption('d')) {
            String decryptedFile = "decrypted_" + fileName + ".txt";
            Files.write(Paths.get(decryptedFile), result);
        } else {
            String decryptedFile = "encrypted_" + fileName + ".enc.Feistel";
            Files.write(Paths.get(decryptedFile), result);
        }
    }
}
