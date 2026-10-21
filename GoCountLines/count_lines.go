// Count the lines of every file below a folder whose name matches a regex.
// usage: go run count_lines.go <folderPath> <regexStr>
package main

import (
	"bytes"
	"fmt"
	"io"
	"io/fs"
	"os"
	"path/filepath"
	"regexp"
)

func countLines(path string) (int64, error) {
	f, err := os.Open(path)
	if err != nil {
		return 0, err
	}
	defer f.Close()

	buf := make([]byte, 64*1024)
	var lines int64
	for {
		n, err := f.Read(buf)
		lines += int64(bytes.Count(buf[:n], []byte{'\n'}))
		if err == io.EOF {
			return lines, nil
		}
		if err != nil {
			return lines, err
		}
	}
}

func countLinesInAllFiles(folder string, re *regexp.Regexp) (int64, error) {
	var total int64
	err := filepath.WalkDir(folder, func(path string, d fs.DirEntry, err error) error {
		if err != nil {
			return err
		}
		if d.Type().IsRegular() && re.MatchString(d.Name()) {
			n, err := countLines(path)
			if err != nil {
				fmt.Fprintln(os.Stderr, err)
				return nil // keep walking, like the other variants
			}
			total += n
		}
		return nil
	})
	return total, err
}

func main() {
	if len(os.Args) != 3 {
		fmt.Fprintf(os.Stderr, "usage: %s <folderPath> <regexStr>\n", os.Args[0])
		os.Exit(1)
	}
	re := regexp.MustCompile(os.Args[2])
	total, err := countLinesInAllFiles(os.Args[1], re)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
	fmt.Println("Total number of lines:", total)
}
