resource "azurerm_mysql_flexible_server" "spring-petclinic-mysql-server" {
  name                = "spring-petclinic-mysql-server3535"
  location            = local.location
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  administrator_login          = "petclinic"
  administrator_password       = "H@Sh1CoR3!"
  sku_name                     = "B_Standard_B1ms"
  depends_on=[azurerm_resource_group.spring-petclinic]
}

resource "azurerm_mysql_flexible_database" "mysql-database" {
  name                = "petclinic"
  resource_group_name = azurerm_resource_group.spring-petclinic.name
  server_name         = azurerm_mysql_flexible_server.spring-petclinic-mysql-server.name
  charset             = "utf8"
  collation           = "utf8_unicode_ci"
  depends_on=[azurerm_mysql_flexible_server.spring-petclinic-mysql-server]
  
}
