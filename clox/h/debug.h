#ifndef CLOX_DEBUG_H
#define CLOX_DEBUG_H

#include "chunk.h"

void disassembleChunk(Chunk *chunk, const char *name);
static int simpleInstruction(const char *name, int offset);
int disassembleInstruction(Chunk *chunk, int i);

#endif //CLOX_DEBUG_H
