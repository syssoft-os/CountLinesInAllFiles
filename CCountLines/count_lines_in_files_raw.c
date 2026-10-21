// Bare-minimum variant: POSIX open/read instead of stdio, 64 KB buffer,
// a plain byte loop that the compiler vectorises at -O3, readdir's d_type
// instead of stat per entry, and openat/fdopendir so no path strings are
// built while walking. Symbolic links are not followed (like find -type f).
#include <dirent.h>
#include <fcntl.h>
#include <regex.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#define BUFFER_SIZE 65536

static long countLines(int dirfd, const char *name) {
    static char buf[BUFFER_SIZE];
    int fd = openat(dirfd, name, O_RDONLY);
    if (fd < 0) return 0;
    long lines = 0;
    ssize_t n;
    while ((n = read(fd, buf, sizeof buf)) > 0)
        for (ssize_t i = 0; i < n; i++)
            lines += buf[i] == '\n';
    close(fd);
    return lines;
}

static long countLinesInAllFiles(int dirfd, const regex_t *re) {
    DIR *dir = fdopendir(dirfd);
    if (!dir) { close(dirfd); return 0; }
    long total = 0;
    struct dirent *e;
    while ((e = readdir(dir))) {
        if (e->d_type == DT_DIR) {
            if (strcmp(e->d_name, ".") == 0 || strcmp(e->d_name, "..") == 0) continue;
            int sub = openat(dirfd, e->d_name, O_RDONLY | O_DIRECTORY);
            if (sub >= 0) total += countLinesInAllFiles(sub, re);
        } else if (e->d_type == DT_REG && regexec(re, e->d_name, 0, NULL, 0) == 0) {
            total += countLines(dirfd, e->d_name);
        }
    }
    closedir(dir);  // also closes dirfd
    return total;
}

int main(int argc, char **argv) {
    if (argc != 3) { fprintf(stderr, "usage: %s <folderPath> <regexStr>\n", argv[0]); return 1; }
    regex_t re;
    if (regcomp(&re, argv[2], REG_EXTENDED | REG_NOSUB)) { fprintf(stderr, "bad regex\n"); return 1; }
    int fd = open(argv[1], O_RDONLY | O_DIRECTORY);
    if (fd < 0) { perror(argv[1]); return 1; }
    printf("Total number of lines: %ld\n", countLinesInAllFiles(fd, &re));
    regfree(&re);
    return 0;
}
