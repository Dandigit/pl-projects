#include <stdio.h>

#include "../h/common.h"
#include "../h/debug.h"
#include "../h/memory.h"
#include "../h/vm.h"

VM vm;

void initVM() {
    vm.stack = NULL;
    vm.stackCapacity = 0;
    resetStack();
}

void freeVM() {

}

static void resetStack() {
    vm.stackCount = 0;
}

static InterpretResult run() {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define READ_CONSTANT_LONG() (vm.chunk->constants.values[(READ_BYTE() | (READ_BYTE() << 8) | (READ_BYTE() << 16))])

#define BINARY_OP(op) \
    do { \
        double b = pop(); \
        double a = pop(); \
        push(a op b); \
    } while (false)

    while (true) {
#ifdef DEBUG_TRACE_EXECUTION
        // Show the contents of the stack
        printf("          ");
        for (int index = 0; index < vm.stackCount; ++index) {
            printf("[ ");
            printValue(vm.stack[index]);
            printf(" ]");
        }
        printf("\n");

        disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
#endif
        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                break;
            }
            case OP_CONSTANT_LONG: {
                Value constant = READ_CONSTANT_LONG();
                push(constant);
                break;
            }
            case OP_ADD: BINARY_OP(+); break;
            case OP_SUBTRACT: BINARY_OP(-); break;
            case OP_MULTIPLY: BINARY_OP(*); break;
            case OP_DIVIDE: BINARY_OP(/); break;
            case OP_NEGATE: push(-pop()); break;
            case OP_RETURN: {
                printValue(pop());
                printf("\n");
                return INTERPRET_OK;
            }
        }
    }

#undef READ_BYTE
#undef READ_CONSTANT
#undef READ_CONSTANT_LONG
#undef BINARY_OP
}

InterpretResult interpret(Chunk *chunk) {
    vm.chunk = chunk;
    vm.ip = vm.chunk->code;
    return run();
}

void push(Value value) {
    if (vm.stackCapacity < vm.stackCount + 1) {
        int oldCapacity = vm.stackCapacity;
        vm.stackCapacity = GROW_STACK_CAPACITY(oldCapacity);
        vm.stack = GROW_ARRAY(vm.stack, Value,
                oldCapacity, vm.stackCapacity);
    }

    vm.stack[vm.stackCount] = value;
    ++vm.stackCount;
}

Value pop() {
    --vm.stackCount;
    return vm.stack[vm.stackCount];
}