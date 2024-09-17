# Terraform Deployment for Spring Petclinic Application on Azure

This README provides instructions for deploying a Spring Petclinic application on Azure using Terraform. The deployment includes a MySQL Flexible Server with firewall rules, an App Service for the Spring Petclinic application, and source control integration.

## Directory Structure

The Terraform configuration files are organized as follows:

```
terraform/
├── providers.tf
├── resource_group.tf
├── variables.tf
├── webapp.tf
├── firewall.tf
└── database.tf
```

## Files and Their Purpose

### 1. `providers.tf`

Configures the Azure provider for Terraform.

```hcl
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.0"
    }
  }
}

# Configure the Microsoft Azure Provider
provider "azurerm" {
  subscription_id = "71d131e2-d168-45ca-9afe-06ed2ae2e20f"
  features {}
}
```

### 2. `resource_group.tf`

Defines the Azure Resource Group.

```hcl
resource "azurerm_resource_group" "spring-petclinic" {
  name     = local.resource_group
  location = local.location
}
```

### 3. `variables.tf`

Defines local variables used in the configuration.

```hcl
locals {
  resource_group = "spring-petclinic"
  location       = "UK South"
}
```

### 4. `webapp.tf`

Deploys the Azure App Service Plan and Linux Web App for the Spring Petclinic application, and integrates with GitHub for source control.

```hcl
resource "azurerm_service_plan" "spring-petclinic-app-plan" {
  name                = "spring-petclinic-app"
  location            = azurerm_resource_group.spring-petclinic.location
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  sku_name            = "B1"
  os_type             = "Linux"
  depends_on          = [azurerm_resource_group.spring-petclinic, azurerm_mysql_flexible_database.mysql-database]
}

resource "azurerm_linux_web_app" "spring-petclinic-app" {
  name                = "spring-petclinic-app"
  location            = azurerm_resource_group.spring-petclinic.location
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  service_plan_id     = azurerm_service_plan.spring-petclinic-app-plan.id
  app_settings = {
    "MYSQL_URL"              = "jdbc:mysql://spring-petclinic-mysql-server3535.mysql.database.azure.com:3306/petclinic"
    "SPRING_PROFILES_ACTIVE" = "mysql"
    "MYSQL_USER"             = "petclinic"
    "MYSQL_PASS"             = "H@Sh1CoR3!"
  }
  site_config {
    application_stack {
      java_server         = "JAVA"
      java_server_version = "17"
      java_version        = "17"
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
  depends_on = [azurerm_service_plan.spring-petclinic-app-plan]
}

resource "azurerm_app_service_source_control" "source_control" {
  app_id   = azurerm_linux_web_app.spring-petclinic-app.id
  repo_url = "https://github.com/MostafaAMansour/spring-petclinic-azure.git"
  branch   = "main"
  depends_on = [azurerm_linux_web_app.spring-petclinic-app]
}
```

### 5. `firewall.tf`

Configures firewall rules for the MySQL Flexible Server.

```hcl
variable "firewall_rules" {
  type = list(object({
    name            = string
    start_ip        = string
    end_ip          = string
  }))
  
  default = [
    {
      name     = "app-server-firewall-rule-Allow-Azure-services"
      start_ip = "0.0.0.0"
      end_ip   = "0.0.0.0"
    },
    {
      name     = "app-server-firewall-rule-Allow-Client-IP"
      start_ip = "156.195.172.211"
      end_ip   = "156.195.172.211"
    }
  ]
}

resource "azurerm_mysql_flexible_server_firewall_rule" "app_server_firewall_rules" {
  for_each            = { for rule in var.firewall_rules : rule.name => rule }
  name                = each.value.name
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  server_name         = azurerm_mysql_flexible_server.spring-petclinic-mysql-server.name
  start_ip_address    = each.value.start_ip
  end_ip_address      = each.value.end_ip
  depends_on          = [azurerm_mysql_flexible_server.spring-petclinic-mysql-server]
}
```

### 6. `database.tf`

Deploys the MySQL Flexible Server and database.

```hcl
resource "azurerm_mysql_flexible_server" "spring-petclinic-mysql-server" {
  name                = "spring-petclinic-mysql-server3535"
  location            = local.location
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  administrator_login = "petclinic"
  administrator_password = "H@Sh1CoR3!"
  sku_name             = "B_Standard_B1ms"
  depends_on           = [azurerm_resource_group.spring-petclinic]
}

resource "azurerm_mysql_flexible_database" "mysql-database" {
  name                = "petclinic"
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  server_name         = azurerm_mysql_flexible_server.spring-petclinic-mysql-server.name
  charset             = "utf8"
  collation           = "utf8_unicode_ci"
  depends_on          = [azurerm_mysql_flexible_server.spring-petclinic-mysql-server]
}
```

## Deployment Instructions

1. **Initialize Terraform:**
   Initialize Terraform to download the necessary providers and modules.

   ```bash
   terraform init
   ```

2. **Plan the Deployment:**
   Create an execution plan to preview the changes Terraform will make.

   ```bash
   terraform plan
   ```

3. **Apply the Configuration:**
   Apply the configuration to deploy the resources on Azure.

   ```bash
   terraform apply
   ```

4. **Verify Deployment:**
   Check the Azure portal to ensure that the resources are deployed correctly. You can also check the status of your App Service and MySQL server.

5. **Access the Application:**
   Use the URL of the Azure App Service to access the Spring Petclinic application.

## Conclusion

This setup deploys a Spring Petclinic application on Azure, including a MySQL Flexible Server with firewall rules, an App Service Plan, and source control integration. For more information on managing Azure resources with Terraform, refer to the [Terraform Azure Provider documentation](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs).