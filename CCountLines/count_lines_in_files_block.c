#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <regex.h>

#define BUFFER_SIZE 4096  // Define the size of the buffer

long countLines(const char *fileName) {
    FILE *file = fopen(fileName, "r");
    if (!file) {
        perror("Failed to open file");
        return -1;
    }

    char buffer[BUFFER_SIZE];
    long lineCount = 0;
    size_t bytesRead;

    while ((bytesRead = fread(buffer, 1, BUFFER_SIZE, file)) > 0) {
        for (size_t i = 0; i < bytesRead; ++i) {
            if (buffer[i] == '\n') {
                lineCount++;
            }
        }
    }

    fclose(file);
    return lineCount;
}

long countLinesInAllFiles(const char *folderPath, const char *regexStr) {
    DIR *dir = opendir(folderPath);
    if (!dir) {
        perror("Failed to open directory");
        return -1;
    }

    regex_t regex;
    if (regcomp(&regex, regexStr, REG_EXTENDED)) {
        fprintf(stderr, "Failed to compile regex\n");
        closedir(dir);
        return -1;
    }

    long totalLines = 0;
    struct dirent *entry;
    while ((entry = readdir(dir)) != NULL) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
            continue;  // Skip the current directory and parent directory
        }

        char filePath[1024];
        snprintf(filePath, sizeof(filePath), "%s/%s", folderPath, entry->d_name);
        struct stat st;
        if (stat(filePath, &st) == 0) {
            if (S_ISREG(st.st_mode) && regexec(&regex, entry->d_name, 0, NULL, 0) == 0) {
                long lines = countLines(filePath);
                if (lines != -1) {
                    totalLines += lines;
                }
            } else if (S_ISDIR(st.st_mode)) {
                long lines = countLinesInAllFiles(filePath, regexStr);  // Recursive call
                if (lines != -1) {
                    totalLines += lines;
                }
            }
        }
    }

    regfree(&regex);
    closedir(dir);
    return totalLines;
}

int main(int ac, char**av) {
    long totalLines = countLinesInAllFiles(av[1], av[2]);
    if (totalLines != -1) {
        printf("Total number of lines: %ld\n", totalLines);
    }
    return 0;
}

