resource "azurerm_service_plan" "spring-petclinic-app-plan" {
  name                = "spring-petclinic-app"
  location            = azurerm_resource_group.spring-petclinic.location
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  sku_name            = "B1"
  os_type = "Linux"
  depends_on=[azurerm_resource_group.spring-petclinic, azurerm_mysql_flexible_database.mysql-database]
  
}

resource "azurerm_linux_web_app" "spring-petclinic-app" {
  name                = "spring-petclinic-app"
  location            = azurerm_resource_group.spring-petclinic.location
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  service_plan_id = azurerm_service_plan.spring-petclinic-app-plan.id
  app_settings = {
    "MYSQL_URL"   = "jdbc:mysql://spring-petclinic-mysql-server3535.mysql.database.azure.com:3306/petclinic"
    "SPRING_PROFILES_ACTIVE"    = "mysql"
    "MYSQL_USER" = "petclinic"
    "MYSQL_PASS" = "H@Sh1CoR3!"
  }
  site_config {
    application_stack {
      java_server = "JAVA"
      java_server_version = "17"
      java_version = "17"
    }
    app_command_line = "java -jar /home/site/wwwroot/target/*.jar"
  }
  logs {
    detailed_error_messages = false
    failed_request_tracing  = false
    http_logs {
        file_system {
            retention_in_days = 0
            retention_in_mb   = 35
          }
      }
  }
  depends_on=[azurerm_service_plan.spring-petclinic-app-plan]
}

resource "azurerm_app_service_source_control" "source_control" {
  app_id   = azurerm_linux_web_app.spring-petclinic-app.id
  repo_url = "https://github.com/MostafaAMansour/spring-petclinic-azure.git"
  branch   = "main"
  depends_on=[azurerm_linux_web_app.spring-petclinic-app]
}