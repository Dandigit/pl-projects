#ifndef CLOX_SCANNER_H
#define CLOX_SCANNER_H

typedef struct {
    const char *start;
    const char *current;
    int line;
} Scanner;

void initScanner(const char *source);

#endif //CLOX_SCANNER_H
