#include <stdlib.h>

#include "../h/chunk.h"
#include "../h/memory.h"
#include "../h/value.h"

void initChunk(Chunk *chunk) {
    chunk->count = 0;
    chunk->capacity = 0;
    chunk->code = NULL;

    chunk->lineCount = 0;
    chunk->lineCapacity = 0;
    chunk->lines = NULL;

    initValueArray(&chunk->constants);
}

void freeChunk(Chunk *chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    FREE_ARRAY(int, chunk->lines, chunk->lineCapacity);
    freeValueArray(&chunk->constants);
    initChunk(chunk);
}

void writeChunk(Chunk *chunk, uint8_t byte, int line) {
    if (chunk->capacity < chunk->count + 1) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);

        chunk->code = GROW_ARRAY(chunk->code, uint8_t,
                oldCapacity, chunk->capacity);
    }

    chunk->code[chunk->count] = byte;
    ++chunk->count;

    // See if we're still on the same line
    if (chunk->lineCount > 0 &&
        chunk->lines[chunk->lineCount - 1].line == line) {
        // Do not append a new LineStart.
        return;
    }

    // Append a new LineStart.
    if (chunk->lineCapacity < chunk->lineCount + 1) {
        int oldCapacity = chunk->lineCapacity;
        chunk->lineCapacity = GROW_CAPACITY(oldCapacity);
        chunk->lines = GROW_ARRAY(chunk->lines, LineStart,
                oldCapacity, chunk->lineCapacity);
    }

    LineStart *lineStart = &chunk->lines[chunk->lineCount++];
    lineStart->offset = chunk->count - 1;
    lineStart->line = line;


}

int addConstant(Chunk *chunk, Value value) {
    writeValueArray(&chunk->constants, value);
    return chunk->constants.count - 1;
}

void writeConstant(Chunk *chunk, Value value, int line) {
    int index = addConstant(chunk, value);

    if (index < 256) {
        writeChunk(chunk, OP_CONSTANT, line);
        writeChunk(chunk, (uint8_t)index, line);
    } else {
        writeChunk(chunk, OP_CONSTANT_LONG, line);
        writeChunk(chunk, (uint8_t)(index & 0xff), line);
        writeChunk(chunk, (uint8_t)((index >> 8) & 0xff), line);
        writeChunk(chunk, (uint8_t)((index >> 16) & 0xff), line);
    }
}

int getLine(Chunk *chunk, int offset)  {
    int start = 0;
    int end = chunk->lineCount - 1;

    /*while (true) {
        int mid = (start + end) / 2;
        LineStart *line = &chunk->lines[mid];

        if (offset < line->offset) {
            end = mid + 1;
        } else if (mid == chunk->lineCount - 1 ||
            offset < chunk->lines[mid + 1].offset) {
            return line->line;
        } else {
            start = mid + 1;
        }
    }*/

    for (int index = 0; index <= chunk->lineCount; ++index) {
        LineStart *line = &chunk->lines[index];
        if (offset == line->offset) {
            return line->line;
        }
    }
}