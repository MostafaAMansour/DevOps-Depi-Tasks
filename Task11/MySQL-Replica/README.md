# Docker MySQL Master-Slave Replica Setup

## Overview

This guide explains how to set up a MySQL master-slave replication using Docker. The setup involves creating a Docker network and running two MySQL containers: one as the master and one as the slave. This setup is useful for achieving high availability and load distribution.

## Prerequisites

- Docker installed on your system.
- Basic understanding of MySQL replication concepts.

## Step-by-Step Instructions

### 1. Create Docker Network

Create a Docker network named `mysql-replica`:

```bash
docker network create mysql-replica
```

### 2. Run MySQL Master Container

Run the MySQL master container with the following command:

```bash
docker run --name mysql-source --network mysql-replica -p 3015:3306 --env MYSQL_ROOT_PASSWORD=root --env MYSQL_DATABASE=radda --env MYSQL_USER=radda --env MYSQL_PASSWORD=radda --env MYSQLD_SAFE_ARGS="--plugin-load-add=mysql_native_password.so" -d mysql:8.3
```

### 3. Run MySQL Slave Container

Run the MySQL slave container with the following command:

```bash
docker run --name mysql-replic --network mysql-replica -p 3016:3306 --env MYSQL_ROOT_PASSWORD=root --env MYSQL_DATABASE=radda --env MYSQL_USER=radda --env MYSQL_PASSWORD=radda --env MYSQLD_SAFE_ARGS="--plugin-load-add=mysql_native_password.so" -d mysql:8.3
```

### 4. Configure Master

Access the MySQL master container:

```bash
docker exec -it mysql-source bash
```

Login to MySQL:

```bash
mysql -u root -p
```

Run the following commands to configure the master:

```sql
ALTER USER 'replica_user'@'%' IDENTIFIED WITH mysql_native_password BY 'replica_password';
GRANT REPLICATION SLAVE ON *.* TO 'replica_user'@'%' IDENTIFIED BY 'replica_password';
FLUSH PRIVILEGES;
FLUSH TABLES WITH READ LOCK;
UNLOCK TABLES;
SHOW MASTER STATUS;
```

In the `my.cnf` file, add the following configuration under `[mysqld]`:

```ini
server_id = 1
```

Restart the MySQL master container:

```bash
docker restart mysql-source
```

### 5. Configure Slave

Access the MySQL slave container:

```bash
docker exec -it mysql-replic bash
```

Login to MySQL:

```bash
mysql -u root -p
```

Run the following commands to configure the slave:

```sql
STOP SLAVE;
CHANGE MASTER TO MASTER_HOST='mysql-source', MASTER_USER='replica_user', MASTER_PASSWORD='replica_password', MASTER_LOG_FILE='xxxx.xxxxx', MASTER_LOG_POS=position_number;
START SLAVE;
SHOW SLAVE STATUSG;
```

In the `my.cnf` file, add the following configuration under `[mysqld]`:

```ini
server_id = 2
```

Restart the MySQL slave container:

```bash
docker restart mysql-replic
```

## Notes

- Replace `xxxx.xxxxx` and `position_number` with the values obtained from the `SHOW MASTER STATUS` command on the master.
- Ensure the `server_id` values are unique for the master and slave.

This setup will enable master-slave replication between the MySQL containers, allowing for data replication and failover capabilities.
