resource "helm_release" "spring-petclinic" {
  name       = "spring-petclinic-${var.environment}"
  chart      = "../../helm"
  namespace  = "${var.environment}"
  values = [
    file("../../helm/values-${var.environment}.yaml")
  ]
  depends_on = [kubernetes_manifest.my_pv, kubernetes_namespace.namespace]
}
