#ifndef CLOX_DEBUG_H
#define CLOX_DEBUG_H

#include "chunk.h"

void disassembleChunk(Chunk *chunk, const char *name);
static int constantInstruction(const char *name, Chunk *chunk, int offset);
static int longConstantInstruction(const char *name, Chunk *chunk, int offset);
static int simpleInstruction(const char *name, int offset);
int disassembleInstruction(Chunk *chunk, int i);

#endif //CLOX_DEBUG_H
