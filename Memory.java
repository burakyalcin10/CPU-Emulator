import java.util.Arrays;

public class Memory {
    private byte[] memory;
    private int size;

    public Memory(int size) {
        this.size = size;
        this.memory = new byte[size]; // Initializes with 0s
        // System.out.println("Memory initialized with size: " + size);
    }

    public byte read(int address) {
        if (address >= 0 && address < size) {
            // System.out.printf("Memory Read: Address %04X -> Value %02X%n", address, memory[address]);
            return memory[address];
        } else {
            System.err.printf("Error: Memory read out of bounds: %04X%n", address);
            return 0; // Returning 0 might hide errors
        }
    }

    public void write(int address, byte value) {
        if (address >= 0 && address < size) {
            memory[address] = value;
        } else {
            System.err.printf("Error: Memory write out of bounds: %04X%n", address);
        }
    }

    public void loadData(int startAddress, byte[] data) {
        if (startAddress + data.length <= size) {
            System.arraycopy(data, 0, memory, startAddress, data.length);
        } else {
            System.err.println("Error: Not enough space to load data at address " + startAddress);
        }
    }
}

