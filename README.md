# Simple Text Analyzer

Simple Text Analyzer is a Java-based application that allows you to add words to the collection and get closest words by value or lexical order.

## Features

- Adding words through an API.
- Analyzing existing words and providing closest words by value and lexical order to the input.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/Baska5/simple-text-analyzer.git
2. Run the project
   
## Usage

After running the application use command:
```
curl -X POST http://localhost:8080/analyze -H "Content-Type: application/json" -d '{"text":"word"}'
```
It will return null for both fields since there are no words in the file, however after running command multiple times with different values, it will provide 
some meaningful results.
The program will return null if it's:
* the first request and file is empty
* request contains blank or empty value
* request contains non-English characters.

## Acknowledgements
This project was developed as an assignment for Cloudonix.
