// Count the lines of every file below a folder whose name matches a regex.
// usage: cargo run --release -- <folderPath> <regexStr>
use std::fs::File;
use std::io::{self, Read};
use std::process;

use regex::Regex;
use walkdir::WalkDir;

fn count_lines(path: &std::path::Path) -> io::Result<u64> {
    let mut file = File::open(path)?;
    let mut buf = vec![0u8; 64 * 1024];
    let mut lines = 0u64;
    loop {
        let n = file.read(&mut buf)?;
        if n == 0 {
            return Ok(lines);
        }
        lines += buf[..n].iter().filter(|&&b| b == b'\n').count() as u64;
    }
}

fn count_lines_in_all_files(folder: &str, re: &Regex) -> u64 {
    WalkDir::new(folder)
        .into_iter()
        .filter_map(Result::ok)
        .filter(|e| e.file_type().is_file())
        .filter(|e| re.is_match(&e.file_name().to_string_lossy()))
        .map(|e| match count_lines(e.path()) {
            Ok(n) => n,
            Err(err) => {
                eprintln!("{}: {}", e.path().display(), err);
                0
            }
        })
        .sum()
}

fn main() {
    let args: Vec<String> = std::env::args().collect();
    if args.len() != 3 {
        eprintln!("usage: {} <folderPath> <regexStr>", args[0]);
        process::exit(1);
    }
    let re = Regex::new(&args[2]).unwrap_or_else(|e| {
        eprintln!("invalid regex: {e}");
        process::exit(1);
    });
    println!("Total number of lines: {}", count_lines_in_all_files(&args[1], &re));
}
