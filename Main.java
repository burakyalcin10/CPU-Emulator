import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Main <program_file> <config_file>");
            System.exit(1);
        }

        String programFile = args[0];
        String configFile = args[1];

        try {
            
            CPUEmulator emulator = new CPUEmulator(programFile, configFile);
            emulator.run();
        } catch (IOException e) {
            System.err.println("Error initializing or running emulator: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

