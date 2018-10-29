cmake_minimum_required(VERSION 3.12)
project(clox C)

set(CMAKE_C_STANDARD 99)

add_executable(clox src/main.c src/chunk.c src/debug.c src/memory.c src/value.c src/vm.c src/compiler.c src/scanner.c)