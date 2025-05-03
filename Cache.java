import java.util.Arrays;

public class Cache {
    private static final int VALID = 0;
    private static final int TAG = 1;
    private static final int DATA0 = 2; // data starts at index 2

    private int size = 16; // bytes
    private int blockSize = 2; // bytes
    private int numBlocks = size / blockSize; // 8 blocks

    private int[][] cache; // [numBlocks][4]
    private int hits;
    private int misses;

    public Cache() {
        this.cache = new int[numBlocks][4];
        // Initialize cache: valid bit = 0, tag = -1 (or some invalid value)
        for (int i = 0; i < numBlocks; i++) {
            cache[i][VALID] = 0; // valid bit
            cache[i][TAG] = -1;  // tag
            // data bytes initialized to 0 by default
        }
        this.hits = 0;
        this.misses = 0;
    }

    private int[] getCacheIndices(int address) {
        // Address: 16 bits
        // Index bits: log2(numBlocks) = log2(8) = 3 bits
        // Offset bits: log2(blockSize) = log2(2) = 1 bit
        // Tag bits: 16 - 3 - 1 = 12 bits

        int index = (address / blockSize) % numBlocks;
        int tag = address / (numBlocks * blockSize);
        int offset = address % blockSize;

        // System.out.printf("Address: %04X -> Tag: %03X, Index: %X, Offset: %X%n", address, tag, index, offset);
        return new int[]{tag, index, offset};
    }

    public byte read(int address, Memory memory) {
        int[] indices = getCacheIndices(address);
        int tag = indices[0];
        int index = indices[1];
        int offset = indices[2];

        int[] block = cache[index];
        int validBit = block[VALID];
        int storedTag = block[TAG];

        if (validBit == 1 && storedTag == tag) {
            // Cache Hit
            hits++;
            // System.out.printf("Cache Read Hit: Address %04X, Index %X, Tag %03X%n", address, index, tag);
            return (byte) block[DATA0 + offset]; // data starts at index 2
        } else {
            // Cache Miss
            misses++;
            // System.out.printf("Cache Read Miss: Address %04X, Index %X, Tag %03X%n", address, index, tag);

            int blockStartAddress = address - offset;
            byte memData0 = memory.read(blockStartAddress);
            byte memData1 = memory.read(blockStartAddress + 1);

            // Update cache block (replace)
            block[VALID] = 1; 
            block[TAG] = tag; 
            block[DATA0] = memData0 & 0xFF; // Store as unsigned int for cache array
            block[DATA0 + 1] = memData1 & 0xFF;

            return (offset == 0) ? memData0 : memData1;
        }
    }

    public void write(int address, byte value, Memory memory) {
        int[] indices = getCacheIndices(address);
        int tag = indices[0];
        int index = indices[1];
        int offset = indices[2];

        // Write-through: Write to memory immediately
        memory.write(address, value);
        // System.out.printf("Cache invoking Memory Write: Address %04X, Value %02X%n", address, value);

        int[] block = cache[index];
        int validBit = block[VALID];
        int storedTag = block[TAG];

        if (validBit == 1 && storedTag == tag) {
            // Cache Hit 
            hits++;
            // System.out.printf("Cache Write Hit: Address %04X, Index %X, Tag %03X%n", address, index, tag);
            block[DATA0 + offset] = value & 0xFF; // Update the specific byte in cache
        } else {
            // Cache Miss - Write-Allocate 
            // Assuming Write-Allocate as it's common with Write-Through
            misses++;
            // System.out.printf("Cache Write Miss: Address %04X, Index %X, Tag %03X%n", address, index, tag);

            // Fetch the block from memory *after* the write
            int blockStartAddress = address - offset;
            byte memData0 = memory.read(blockStartAddress);
            byte memData1 = memory.read(blockStartAddress + 1);

            // Update cache block
            block[VALID] = 1; // Set valid bit
            block[TAG] = tag; // Set tag
            block[DATA0] = memData0 & 0xFF;
            block[DATA0 + 1] = memData1 & 0xFF;

            block[DATA0 + offset] = value & 0xFF; 
        }
    }

    public double getHitRatio() {
        int totalAccesses = hits + misses;
        if (totalAccesses == 0) {
            return 0.0;
        }
        return (double) hits / totalAccesses * 100.0;
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }
}
