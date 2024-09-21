import os
import re
import sys
import argparse

# Default license text
DEFAULT_LICENSE = """/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/"""

# Function to read the license from a file
def read_license_file(license_file):
    with open(license_file, 'r') as file:
        return file.read()

# Function to check if any ignore string is in the comment
def contains_ignore_directive(comment, ignore_directives):
    for directive in ignore_directives:
        if directive in comment:
            return True
    return False

# Function to process a single Java file
def process_java_file(file_path, license_text, ignore_directives):
    with open(file_path, 'r') as file:
        content = file.read()

    # Split the content into two parts: before and after the "package" declaration
    split_content = re.split(r'(\bpackage\b)', content, maxsplit=1, flags=re.IGNORECASE)
    
    if len(split_content) < 3:
        # If there is no "package" declaration, we assume the file is not a valid Java file
        return

    # Extract the portion before the "package" declaration
    before_package = split_content[0].strip()
    after_package = ''.join(split_content[1:])  # Reconstruct the rest of the content

    # Regex to match any existing comment (either JavaDoc or multi-line) at the start of the file
    existing_license_pattern = r'^\/\*.*?\*\/\s*'

    # Find any existing comment
    match = re.match(existing_license_pattern, before_package, re.DOTALL)
    if match:
        # If the comment contains any of the ignore directives, do not replace it
        if contains_ignore_directive(match.group(0), ignore_directives):
            return

        # Remove the existing license if it doesn't match the new one
        before_package = re.sub(existing_license_pattern, '', before_package, count=1, flags=re.DOTALL)

    # Insert the new license at the beginning, ensuring no extra spaces
    new_before_package = f"{license_text}\n{before_package}"

    # Write the updated content back to the file
    with open(file_path, 'w') as file:
        file.write(f"{new_before_package}{after_package}")

# Function to recursively process all Java files in a directory
def process_directory(directory, license_text, ignore_directives):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                process_java_file(file_path, license_text, ignore_directives)

# Main function to handle arguments and run the script
def main():
    parser = argparse.ArgumentParser(description="Insert or replace license notice in Java files.")
    parser.add_argument("directory", help="Directory to process")
    parser.add_argument("-l", "--license", help="Path to the license file", default=None)
    parser.add_argument("-ignore", action='append', help="String to ignore in existing comments", default=[])

    args = parser.parse_args()

    # Use the provided license file if specified, otherwise use the default license
    if args.license:
        license_text = read_license_file(args.license)
    else:
        license_text = DEFAULT_LICENSE

    # Process all Java files in the specified directory
    process_directory(args.directory, license_text, args.ignore)

if __name__ == '__main__':
    main()
