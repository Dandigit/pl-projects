#include "../h/common.h"
#include "../h/chunk.h"
#include "../h/debug.h"

int main(int argc, char *argv[]) {
    Chunk chunk;
    initChunk(&chunk);
    writeChunk(&chunk, OP_RETURN);

    disassembleChunk(&chunk, "test chunk");
    freeChunk(&chunk);



    return 0;
}