#include <iostream>
#include <fstream>
#include <regex>
#include <filesystem>

long countLines(const std::filesystem::path& filePath) {
    std::ifstream file(filePath);
    if (!file.is_open()) {
        std::cerr << "Failed to open file: " << filePath << '\n';
        return -1;
    }

    long lineCount = 0;
    std::string line;
    while (std::getline(file, line)) {
        lineCount++;
    }

    return lineCount;
}

long countLinesInAllFiles(const std::filesystem::path& folderPath, const std::regex& regex) {
    long totalLines = 0;
    for (const auto& entry : std::filesystem::recursive_directory_iterator(folderPath)) {
        if (entry.is_regular_file() && std::regex_match(entry.path().filename().string(), regex)) {
            long lines = countLines(entry.path());
            if (lines != -1) {
                totalLines += lines;
            }
        }
    }

    return totalLines;
}

int main(int argc, char* argv[]) {
    if (argc != 3) {
        std::cerr << "Usage: " << argv[0] << " <folderPath> <regexStr>\n";
        return 1;
    }

    std::filesystem::path folderPath(argv[1]);
    std::regex regexStr(argv[2]);

    long totalLines = countLinesInAllFiles(folderPath, regexStr);
    if (totalLines != -1) {
        std::cout << "Total number of lines: " << totalLines << '\n';
    }

    return 0;
}

