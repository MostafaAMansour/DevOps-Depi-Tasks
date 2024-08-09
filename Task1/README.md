# Task 1: Linux Basics

## Overview

In this task, you'll learn some fundamental Linux commands by performing basic operations such as creating a directory, navigating to it, creating files, and removing them. You'll also practice listing the contents of a directory after each operation. This is an essential introduction to working efficiently in a Linux environment.

## Prerequisites

- **Operating System:** Linux or any Unix-based system.
- **Terminal Access:** Ensure you have access to a terminal or command line interface.

## Instructions

### 1. Create a Directory

Start by creating a new directory named `test_dir`. This will be the workspace for this task.

```bash
mkdir test_dir
```

### 2. List Directory Contents
After creating the directory, you can list the contents of the current directory to verify that `test_dir` was successfully created:

```bash
ls
```

### 3. Navigate to the Directory
Next, change into the newly created `test_dir` directory to perform further operations:

```bash
cd test_dir
```
### 4. Create a File with Echo and Redirection
Inside `test_dir`, use the `echo` command to write "Hello, World!" into a file named `test1`. The `>` operator redirects the output from `echo` into the file:

```bash
echo "Hello, World!" > test1
```

### 5. Create an Empty File
Now, create an empty file named `test2` using the `touch` command. This command is commonly used to create empty files or update the timestamp of an existing file:

```bash
touch test2
```

### 8. Remove the File
To clean up, remove the test2 file using the rm command:
```bash
rm test2
```

### 9. List Directory Contents
Finally, list the contents of `test_dir` to ensure that `test2` has been removed, leaving only `test1` in the directory:

```bash
ls
```

Summary
In this task, you've practiced basic Linux commands that include creating and navigating directories, creating and removing files, and listing directory contents. These are foundational skills that will help you work more effectively in a Linux environment, whether you're managing files, writing scripts, or deploying applications.
