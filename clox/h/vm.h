#ifndef CLOX_VM_H
#define CLOX_VM_H

#include "chunk.h"
#include "table.h"
#include "value.h"

typedef struct {
    Chunk *chunk;
    uint8_t *ip;
    Value *stack;
    Value *stackTop;

    Table globals;
    Table strings;

    Obj* objects;
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR,
} InterpretResult;

extern VM vm;

void initVM();
void freeVM();

static void resetStack();

static InterpretResult run();
InterpretResult interpret(const char *source);

void push(Value value);
Value pop();

#endif //CLOX_VM_H
