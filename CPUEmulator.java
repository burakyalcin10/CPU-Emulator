import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CPUEmulator {
    private Memory memory;
    private Cache cache;
    private int pc; // Program Counter (absolute address)
    private int ac; // Accumulator (16-bit)
    private int flag; // Comparison flag (-1, 0, 1)
    private int loadAddress;
    private List<String> instructions;
    private boolean halted;

    public CPUEmulator(String programFile, String configFile) throws IOException {
        this.memory = new Memory(65536); // 64 KB
        this.cache = new Cache();
        this.ac = 0;
        this.flag = 0;
        this.instructions = new ArrayList<>();
        this.halted = false;

        loadConfig(configFile);
        loadProgram(programFile);

        // Set initial PC after loading config
        this.pc = loadAddress; // Default if config doesn't specify differently
        loadConfig(configFile); // Reload to get correct initial PC

    }

    private void loadConfig(String configFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line1 = reader.readLine();
            String line2 = reader.readLine();
            if (line1 != null) {
                this.loadAddress = Integer.parseInt(line1.trim().substring(2), 16);
            }
            if (line2 != null) {
                this.pc = Integer.parseInt(line2.trim().substring(2), 16);
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            System.err.println("Error parsing config file: " + e.getMessage());
            throw new IOException("Invalid config file format.", e);
        }
    }

    private void loadProgram(String programFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(programFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String instructionStr = line.trim();
                if (instructionStr.length() == 16 && instructionStr.matches("[01]+")) {
                    instructions.add(instructionStr);
                } else if (!instructionStr.isEmpty()){
                    System.err.println("Skipping invalid line in program file: " + instructionStr);
                }
            }
        }
    }

    private String fetchInstruction() {
        int instructionIndex = pc - loadAddress;
        if (instructionIndex >= 0 && instructionIndex < instructions.size()) {
            return instructions.get(instructionIndex);
        } else {
            System.err.printf("Error: PC (0x%04X) out of loaded program bounds (0x%04X - 0x%04X). Halting.%n",
                              pc, loadAddress, loadAddress + instructions.size() - 1);
            halted = true;
            return null;
        }
    }

    // Memory address is relative to load address as per PDF examples for LOADM/STORE etc.
    private int getMemoryAddress(int operand) {
        return loadAddress + operand;
    }

    public void run() {
        while (!halted) {
            int currentPC = pc;
            String instructionStr = fetchInstruction();
    
            if (halted || instructionStr == null) {
                break;
            }
    
            // System.out.printf("PC: %04X, Instr: %s", currentPC, instructionStr);
    
            int opcode = Integer.parseInt(instructionStr.substring(0, 4), 2);
            int operand = Integer.parseInt(instructionStr.substring(4), 2); // 12-bit operand
    
            boolean pcIncremented = true; // Yeni kontrol bayrağı
    
            switch (opcode) {
                //For debugging, I printed to the console
                case 0: // START
                    // System.out.println(" START");
                    break;
                case 1: // LOAD 
                    ac = operand;
                    // System.out.printf(" LOAD %d -> AC=%d%n", operand, ac);
                    break;
                case 2: // LOADM 
                    int memAddrLoad = getMemoryAddress(operand);
                    // Read 1 byte, treat as unsigned for AC
                    ac = cache.read(memAddrLoad, memory) & 0xFF;
                    // System.out.printf(" LOADM %d (Addr %04X) -> AC=%d%n", operand, memAddrLoad, ac);
                    break;
                case 3: // STORE 
                    int memAddrStore = getMemoryAddress(operand);
                    // Write lower 8 bits of AC
                    cache.write(memAddrStore, (byte) (ac & 0xFF), memory);
                    // System.out.printf(" STORE %d (Addr %04X) <- AC=%d%n", operand, memAddrStore, ac & 0xFF);
                    break;
                case 4: // CMPM 
                    int memAddrCmp = getMemoryAddress(operand);
                    int memValue = cache.read(memAddrCmp, memory) & 0xFF; // Treat as unsigned byte
                    int acValue = ac & 0xFF; 
                    if (acValue > memValue) {
                        flag = 1;
                    } else if (acValue < memValue) {
                        flag = -1;
                    } else {
                        flag = 0;
                    }
                    // System.out.printf(" CMPM %d (Addr %04X) | AC=%d, Mem=%d -> Flag=%d%n", operand, memAddrCmp, acValue, memValue, flag);
                    break;
                case 5: // CJMP
                    if (flag > 0) {
                        pc = loadAddress + operand;
                        pcIncremented = false;
                        // System.out.printf(" CJMP %d -> PC=%04X (Flag > 0)%n", operand, pc);
                    } else {
                        // System.out.printf(" CJMP %d (No jump, Flag <= 0)%n", operand);
                    }
                    break;
                case 6: // JMP 
                    pc = loadAddress + operand;
                    pcIncremented = false;
                    // System.out.printf(" JMP %d -> PC=%04X%n", operand, pc);
                    break;
                case 7: // ADD
                    ac += operand;
                    ac &= 0xFFFF; 
                    // System.out.printf(" ADD %d -> AC=%d%n", operand, ac);
                    break;
                case 8: // ADDM 
                    int memAddrAdd = getMemoryAddress(operand);
                    int memValueAdd = cache.read(memAddrAdd, memory) & 0xFF; // Treat as unsigned byte
                    ac += memValueAdd;
                    ac &= 0xFFFF; 
                    // System.out.printf(" ADDM %d (Addr %04X) | Mem=%d -> AC=%d%n", operand, memAddrAdd, memValueAdd, ac);
                    break;
                case 9: // SUB
                    ac -= operand;
                    ac &= 0xFFFF; 
                    // System.out.printf(" SUB %d -> AC=%d%n", operand, ac);
                    break;
                case 10: // SUBM
                    int memAddrSub = getMemoryAddress(operand);
                    int memValueSub = cache.read(memAddrSub, memory) & 0xFF; // Treat as unsigned byte
                    ac -= memValueSub;
                    ac &= 0xFFFF; 
                    // System.out.printf(" SUBM %d (Addr %04X) | Mem=%d -> AC=%d%n", operand, memAddrSub, memValueSub, ac);
                    break;
                case 11: // MUL
                    ac *= operand;
                    ac &= 0xFFFF; 
                    // System.out.printf(" MUL %d -> AC=%d%n", operand, ac);
                    break;
                case 12: // MULM
                    int memAddrMul = getMemoryAddress(operand);
                    int memValueMul = cache.read(memAddrMul, memory) & 0xFF; // Treat as unsigned byte
                    ac *= memValueMul;
                    ac &= 0xFFFF; 
                    // System.out.printf(" MULM %d (Addr %04X) | Mem=%d -> AC=%d%n", operand, memAddrMul, memValueMul, ac);
                    break;
                case 13: // DISP
                    //System.out.printf("Value in AC: %d%n", ac);
                    break;
                case 14: // HALT
                    // System.out.println(" HALT");
                    halted = true;
                    break;
                default:
                    System.err.printf("Error: Unknown opcode %d at PC %04X%n", opcode, currentPC);
                    halted = true;
                    break;
            }
    
            if (pcIncremented) {
                pc++; 
            }
        }
    
        System.out.printf("Value in AC: %d%n", ac);
        //System.out.printf("Cache hits: %d%n", cache.getHits());
        //System.out.printf("Cache misses: %d%n", cache.getMisses());
        System.out.printf("Cache hit ratio: %.2f%%%n", cache.getHitRatio());
    }
    
}

