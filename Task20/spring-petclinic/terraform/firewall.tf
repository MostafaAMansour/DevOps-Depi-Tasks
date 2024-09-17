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
  depends_on=[azurerm_mysql_flexible_server.spring-petclinic-mysql-server]
}
