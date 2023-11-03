import os
import re
import argparse

def count_lines(file_path):
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        line_count = sum(1 for line in file)
    return line_count

def count_lines_in_all_files(folder_path, regex_str):
    total_lines = 0
    regex = re.compile(regex_str)
    
    for root, dirs, files in os.walk(folder_path):
        for file_name in files:
            if regex.match(file_name):
                file_path = os.path.join(root, file_name)
                total_lines += count_lines(file_path)
    
    return total_lines

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Count lines in all files matching regex in a directory tree.')
    parser.add_argument('folder_path', help='Path to the folder')
    parser.add_argument('regex_str', help='Regular expression string to match files')
    
    args = parser.parse_args()
    total_lines = count_lines_in_all_files(args.folder_path, args.regex_str)
    print(f'Total number of lines: {total_lines}')

