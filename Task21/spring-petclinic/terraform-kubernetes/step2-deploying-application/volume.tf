resource "kubernetes_manifest" "my_pv" {
  manifest = yamldecode(file("../../pv-azure/pv-${var.environment}.yml"))
}