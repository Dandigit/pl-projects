#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "../h/memory.h"
#include "../h/object.h"
#include "../h/table.h"
#include "../h/value.h"

#define TABLE_MAX_LOAD 0.75

void initTable(Table* table) {
    table->count = 0;
    table->capacity = 0;
    table->entries = NULL;
}

void freeTable(Table* table) {
    FREE_ARRAY(Entry, table->entries, table->capacity);
    initTable(table);
}

static Entry* findEntry(Entry* entries, int capacity, ObjString* key) {
    uint32_t index = key->hash % capacity;
    Entry* tombstone = NULL;

    while (true) {
        Entry* entry = &entries[index];
        if (entry->key == NULL) {
            if (IS_NIL(entry->value)) {
                // Empty entry
                return tombstone != NULL ? tombstone : entry;
            } else {
                // Tombstone
                if (tombstone == NULL) tombstone = entry;
            }
        } else if (entry->key == key) {
            // Found the entry
            return entry;
        }

        index = (index + 1) % capacity;
    }
}

bool tableGet(Table* table, ObjString* key, Value* out) {
    // Check if the table is empty
    if (table->entries == NULL) return false;

    Entry* entry = findEntry(table->entries, table->capacity, key);
    if (entry->key == NULL) return false;

    *out = entry->value;
    return true;
}

static void adjustCapacity(Table* table, int capacity) {
    Entry* entries = ALLOCATE(Entry, capacity);
    for (int i = 0; i < capacity; ++i) {
        entries[i].key = NULL;
        entries[i].value = NIL_VAL;
    }

    // We need to recalculate where the entries are, as the capacity has changed
    table->count = 0;
    for (int i = 0; i < table->capacity; ++i) {
        Entry* entry = &table->entries[i];
        if (entry->key == NULL) continue;

        Entry* dest = findEntry(entries, capacity, entry->key);
        dest->key = entry->key;
        dest->value = entry->value;

        ++table->count;
    }

    // We can free the old array now, as everything has been copied over.
    FREE_ARRAY(Entry, table->entries, table->capacity);

    table->entries = entries;
    table->capacity = capacity;
}

bool tableSet(Table* table, ObjString* key, Value value) {
    if (table->count + 1 > table->capacity * TABLE_MAX_LOAD) {
        int capacity = GROW_CAPACITY(table->capacity);
        adjustCapacity(table, capacity);
    }

    Entry* entry = findEntry(table->entries, table->capacity, key);

    // Is there already a key in this location?
    bool isNewKey = entry->key == NULL;

    // Only increment the count if we are in a proper empty bucket
    if (isNewKey && IS_NIL(entry->value)) ++table->count;

    entry->key = key;
    entry->value = value;

    return isNewKey;
}

bool tableDelete(Table* table, ObjString* key) {
    // Check for an empty table
    if (table->count == 0) return false;

    // Find the entry.
    Entry* entry = findEntry(table->entries, table->capacity, key);
    if (entry->key == NULL) return false;

    // Place a tombstone in the entry.
    entry->key = NULL;
    entry->value = BOOL_VAL(true);

    return true;
}

void tableAddAll(Table* from, Table* to) {
    for (int i = 0; i < from->capacity; ++i) {
        Entry* entry = &from->entries[i];
        if (entry->key != NULL) {
            tableSet(to, entry->key, entry->value);
        }
    }
}

ObjString* tableFindString(Table* table, const char* chars, int length, uint32_t hash) {
    // We won't find it in an empty table
    if (table->entries == NULL) return NULL;

    uint32_t index = hash % table->capacity;

    while (true) {
        Entry* entry = &table->entries[index];

        if (entry->key == NULL) {
            // If the slot is empty, stop.
            if (IS_NIL(entry->value)) return NULL;
        } else if (entry->key->length == length
                && entry->key->hash == hash
                && memcmp(entry->key->chars, chars, length) == 0) {
            // Found the string
            return entry->key;
        }

        index = (index + 1) % table->capacity;
    }
}

void printTable(Table* table) {
    for (int i = 0; i < table->capacity; ++i) {
        Entry* entry = &table->entries[i];

        // We don't print empty entries and tombstones
        if (entry->key == NULL) continue;

        printf("(\"");
        printValue(OBJ_VAL(entry->key));
        printf("\", ");
        printValue(entry->value);
        printf(")\n");
    }
}