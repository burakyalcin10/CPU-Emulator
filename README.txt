*****************************************************************
*                  Akdeniz University                           *
*                Department of Computer Engineering             *
*                                                               *
*              CSE206 - Computer Organization                   *
*                                                               *
*                Assignment #1: CPU Emulator                    *
*                                                               *
*                     Prepared by: Burak Yalçın                 *
*                      Date: 01/05/2025                         *
*****************************************************************

Student ID: 20220808069

==============================
PROJECT DESCRIPTION
==============================

This README file documents the CPU Emulator project developed for the CSE206 Computer Organization course.

The emulator simulates a simplified CPU with support for a 15-instruction set, direct-mapped cache, and 64KB byte-addressable main memory. The system includes fetch-decode-execute logic, cache management, and memory handling.

==============================
PROJECT STRUCTURE
==============================

- Memory.java: Simulates the 64 KB main memory. Provides byte-level read/write support with bounds checking. Supports loading a program into memory.
- Cache.java: Implements a direct-mapped cache. Total size is 16 bytes with 8 blocks (2 bytes per block). Uses a write-through and write-allocate policy. Tracks hit/miss statistics. All memory accesses, including instruction fetches, go through this class.
- CPUEmulator.java: Implements the core CPU logic. Manages registers: Program Counter (PC), Accumulator (AC), and Comparison Flag. Fetches instructions from memory (via cache), decodes, and executes them. Supports all 15 defined instructions.
- Main.java: Entry point of the application. Loads `program.txt` and `config.txt` via command-line arguments. Initializes memory, cache, and CPU emulator, loads the program, and starts execution.

==============================
CACHE IMPLEMENTATION DETAILS
==============================

- Cache Size: 16 bytes
- Block Size: 2 bytes
- Number of Blocks: 8
- Mapping: Direct-Mapped
- Policies:
  - Write-Through: Every write is passed to main memory immediately.
  - Write-Allocate: On write miss, the block is fetched into cache before writing.

Address Breakdown:
A 16-bit memory address is divided as follows:

- Offset (1 bit): Determines byte position within the block (2 bytes/block).
- Index (3 bits): Identifies one of 8 cache blocks.
- Tag (12 bits): Used to verify if the block in cache corresponds to the requested address.

On read:
- If tag matches and valid bit is 1 → Cache Hit
- Else → Cache Miss (block is fetched from memory)

On write:
- Always writes to memory (write-through)
- If hit → updates cache
- If miss → fetches block (write-allocate) and then updates cache

==============================
ADDRESSING MODES AND JUMP FIX
==============================

The emulator supports two addressing modes:

- Absolute Addressing: Intended for JMP and CJMP, where the operand is a full memory address (as per the spec).
- Relative Addressing: Used for memory-related instructions. The effective address is computed as `loadAddress + operand`.

Implementation Note:
While testing the program, I noticed that using absolute addressing for JMP (like operand=6) didn’t work correctly — it caused an infinite loop. After asking the instructor, I learned that the instruction or the sample program might have a mistake. So, I used loadAddress + operand for both JMP and CJMP to make the program work as expected.

pc = loadAddress + operand;

This makes the operand effectively act as an instruction index relative to the start of the loaded program — a necessary fix to ensure correct execution of the sample code.


==============================
DEBUGGING AND NOTES
==============================

- Debug print statements (instruction trace, cache access, memory reads) are left in the code as comments to demonstrate testing efforts.
- Instruction trace was used to verify control flow and cache behavior.
- Named constants are used in place of magic numbers in the cache for clarity.
- `pcIncremented` flag ensures jumps and branches are handled cleanly.

==============================
SAMPLE OUTPUT
==============================

Value in AC: 210
Cache hit ratio: 98.47%