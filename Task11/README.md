**Docker mysql master slave replica
**

docker create network mysql-replica

docker run --name mysql-replic --network mysql-replica -p 3016:3306 --env MYSQL_ROOT_PASSWORD=root --env MYSQL_DATABASE=radda --env MYSQ_USER=radda --env MYSQL_PASSWORD=radda --env MYSQLD_SAFE_ARGS="--plugin-load-add=mysql_native_password.so" -d mysql:8.3

docker run --name mysql-source --network mysql-replica -p 3015:3306 --env MYSQL_ROOT_PASSWORD=root --env MYSQL_DATABASE=radda --env MYSQ_USER=radda --env MYSQL_PASSWORD=radda --env MYSQLD_SAFE_ARGS="--plugin-load-add=mysql_native_password.so" -d mysql:8.3

Source commands using mysql_native_password as a plugin

docker exec -it mysql-source bash
mysql -u root -p

ALTER USER 'replica_user'@'%' IDENTIFIED WITH mysql_native_password BY 'replica_password';
GRANT REPLICATION SLAVE ON *.* TO 'replica_user'@'%' IDENTIFIED BY 'replica_password';
FLUSH PRIVILEGES;
FLUSH TABLES WITH READ LOCK;
UNLOCK TABLES;
SHOW MASTER STATUS;
in my.cnf add in [mysqld]
server_id = 1


docker restart mysql-source

Replic commands using mysql_native_password as a plugin

docker exec -it mysql-replic bash
mysql -u root -p
STOP SLAVE;
CHANGE MASTER TO MASTER_HOST='mysql-master', MASTER_USER='replica_user', MASTER_PASSWORD='replica_password', MASTER_LOG_FILE='xxxx.xxxxx', MASTER_LOG_POS=position_number; //found in MASTER STATUS
in my.cnf add in [mysqld]
server_id = 2

docker restart mysql-replic

START SLAVE;
SHOW SLAVE STATUS\G;

