# CountLinesInAllFiles
Various approaches to count the number of lines in all text files in a specific directory (recursively) where the filename matches a given regular expression.
It shows the simplicity of the one liner `find <directory> -name <pattern> -exec cat {} \; | wc` versus language-based approaches. Most of the example programs is created
using AI (ChatGPT, version 4) with a lot of fine-tuning and error-correction during the conversation. Nevertheless, it is really impressive, how much easier it is to create
solutions for various programming languages in a dialog with an AI buddy sitting next to me.

Programming languages used:

- Java (Java Streams; sequential and parallel)
- C (reading single chars and reading blocks of chars)
- C++ (standard version C++17)
- Python
- Bash

More languages may follow.

`results.txt` lists source code sizes and execution times on a typical Intel-based MacBook Pro.
