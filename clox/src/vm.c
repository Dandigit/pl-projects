#include <stdio.h>

#include "../h/common.h"
#include "../h/compiler.h"
#include "../h/debug.h"
#include "../h/memory.h"
#include "../h/vm.h"

// The maximum amount of values a single instruction
// can push to the stack.
#define MAX_STACK_PER_INSTRUCTION 1

VM vm;

void initVM() {
    vm.stack = NULL;
    resetStack();
}

void freeVM() {
    FREE_ARRAY(Value, vm.stack, vm.chunk->count);
}

static void resetStack() {
    vm.stackTop = vm.stack;
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
        for (Value *slot = vm.stack; slot < vm.stackTop; ++slot) {
            printf("[ ");
            printValue(*slot);
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
            case OP_NEGATE:
                if (!IS_NUMBER(peek(0))) {
                    runtimeError("Operand must be a number.");
                    return INTERPRET_RUNTIME_ERROR;
                }

                push(NUMBER_VAL(-AS_NUMBER(pop()))); break;
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

/*InterpretResult interpret(Chunk *chunk) {
    vm.chunk = chunk;
    vm.ip = vm.chunk->code;
    vm.stack = GROW_ARRAY(vm.stack, Value, 0,
            vm.chunk->count * MAX_STACK_PER_INSTRUCTION);
    resetStack();
    return run();
}*/

InterpretResult interpret(const char *source) {
    Chunk chunk;
    initChunk(&chunk);

    if (!compile(source, &chunk)) {
        freeChunk(&chunk);
        return INTERPRET_COMPILE_ERROR;
    }

    vm.chunk = &chunk;
    vm.ip = vm.chunk->code;
    vm.stack = GROW_ARRAY(vm.stack, Value, 0,
            vm.chunk->count * MAX_STACK_PER_INSTRUCTION);
    resetStack();

    InterpretResult result = run();

    freeChunk(&chunk);
    return result;
}

void push(Value value) {
    *vm.stackTop = value;
    ++vm.stackTop;
}

Value pop() {
    --vm.stackTop;
    return *vm.stackTop;
}

static Value peek(int distance) {
    return vm.stackTop[-1 - distance];
}