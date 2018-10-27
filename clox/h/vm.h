#ifndef CLOX_VM_H
#define CLOX_VM_H

#include "chunk.h"
#include "value.h"

typedef struct {
    Chunk *chunk;
    uint8_t *ip;
    Value *stack;
    int stackCount;
    int stackCapacity;
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR,
} InterpretResult;

void initVM();
void freeVM();

static void resetStack();

static InterpretResult run();
InterpretResult interpret(Chunk *chunk);

void push(Value value);
Value pop();

#endif //CLOX_VM_H
