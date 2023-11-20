# Assignment Report

## Files added
- CreateTextFile.java: Creates text files of specified size and number with random content.
- MemMonitor.java: Captures memory usage of specified java class with a main-method.
- MainLineNumberReader.java: Variation of the originial Main.java file using a LineNumberReader to count lines.
- MainArray.java: Variation of the originial Main.java file using a basic array to count lines without a buffered reader.

## Performance
All performance samples were captured a PC with an AMD Ryzen 7 7800X3D, 32GB of DDR5 memory, running Windows 11.
All files were read from NVMe storage.

### Main.java:
![Control Layout](Graphs/Main_1000x-10MB-File.png)
![Control Layout](Graphs/Main_100x-100MB-File.png)
![Control Layout](Graphs/Main_10x-1GB-File.png)

### MainLineNumberReader.java:
![Control Layout](Graphs/MainLineNumberReader_1000x-10MB-File.png)
![Control Layout](Graphs/MainLineNumberReader_100x-100MB-File.png)
![Control Layout](Graphs/MainLineNumberReader_10x-1GB-File.png)

### MainArray.java:
![Control Layout](Graphs/MainArray_1000x-10MB-File.png)
![Control Layout](Graphs/MainArray_100x-100MB-File.png)
![Control Layout](Graphs/MainArray_10x-1GB-File.png)

### Runtimes:
![Control Layout](Graphs/Runtimes.png)