# Task 2: Advanced Linux Commands

## Overview

In this task, you will explore advanced Linux commands that involve creating directories, managing file permissions, creating users, and working with scripts. These operations are essential for system administration and managing a secure and efficient Linux environment.

## Prerequisites

- **Operating System:** Linux or any Unix-based system.
- **Terminal Access:** Ensure you have access to a terminal or command line interface.
- **User Privileges:** You need `sudo` privileges to execute certain commands.

## Instructions

### 1. Create a Directory

Begin by creating a new directory named `task2`. This will be your working directory for this task.

```bash
mkdir task2
```

### 2. List Directory Contents

List the contents of the current directory to confirm that `task2` has been created:

```bash
ll
```

### 3. Change Ownership of the Directory

Change the ownership of the `task2` directory to the `root` user and group:

```bash
sudo chown root:root task2
```

### 4. Navigate into the Directory

Change into the `task2` directory to perform further operations:

```bash
cd task2
```

### 5. Create a Script File

Create and open a file named `script.sh` using the `nano` text editor:

```bash
sudo nano script.sh
```

### 6. Write a Script

Inside the `script.sh` file, write the following Bash script:

```bash
#!/bin/bash
echo "this code is working!!"
```

This script will print the message "this code is working!!" when executed.

### 7. List Directory Contents Again

After creating the script, list the contents of the `task2` directory:

```bash
ll
```

### 8. Change Script Permissions

Grant execute permissions to the user, group, and others for the `script.sh` file:

```bash
sudo chmod u+x,g+x,o+x script.sh
```

Next, set full permissions (read, write, and execute) for the `script.sh` file:

```bash
sudo chmod 777 script.sh
```

### 9. List Directory Contents Again

List the contents of the directory once more to verify the permission changes:

```bash
ll
```

### 10. Create a New User

Create a new user named `radda`:

```bash
sudo adduser radda
```

### 11. Change Ownership of the Directory

Return to the parent directory and change the ownership of the `task2` directory to the user `radda`:

```bash
cd ..
sudo chown radda:radda task2
```

### 12. Zip the Directory

Compress the `task2` directory into a ZIP file named `task2.zip`:

```bash
zip task2.zip task2
```

### 13. List Directory Contents Again

List the contents of the current directory to verify that the ZIP file has been created:

```bash
ll
```

### 14. Navigate Back into the Directory

Change back into the `task2` directory:

```bash
cd task2
```

### 15. Execute the Script with Debugging

Finally, execute the `script.sh` file with debugging enabled to see each command as it runs:

```bash
bash -x script.sh
```

## Summary

In this task, you practiced advanced Linux commands, including directory and file management, permission settings, user creation, and script execution. These skills are crucial for effective system administration and secure Linux environment management. By completing this task, you’ve enhanced your ability to handle more complex tasks in Linux.
