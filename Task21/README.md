# Terraform Deployment for AKS on Azure

This project provides a Terraform configuration for deploying an Azure Kubernetes Service (AKS) cluster and an application on it. The deployment is split into two steps: creating the AKS cluster and deploying the application.

## Directory Structure

```
terraform-kubernetes/
├── step1-cluster-creation/
│   ├── backend.tf
│   ├── cluster.tf
│   ├── disk.tf
│   ├── outputs.tf
│   ├── providers.tf
│   ├── resource_group.tf
│   └── storage_account.tf
└── step2-deploying-application/
    ├── data_restore.tf
    ├── helm.tf
    ├── namespace.tf
    ├── providers.tf
    ├── variables.tf
    └── volume.tf
```

## Step 1: Cluster Creation

This step sets up the AKS cluster and associated resources.

### 1. `backend.tf`

Configures the remote backend for Terraform state storage.

```hcl
terraform {
  backend "azurerm" {
    resource_group_name  = "sp_aks_group"
    storage_account_name = "springpetclinicstorageac"
    container_name       = "sp-storage-cont"
    key                  = "terraform.sp_storage_cont"
  }
}
```

### 2. `cluster.tf`

Defines the AKS cluster and a node pool.

```hcl
resource "azurerm_kubernetes_cluster" "sp_aks" {
  name                = "sp_aks"
  location            = local.location
  resource_group_name = local.resource_group
  dns_prefix          = "sp-aks"

  default_node_pool {
    name       = "default"
    node_count = 1
    vm_size    = "Standard_B2s"
    upgrade_settings {
      drain_timeout_in_minutes      = 0 
      max_surge                     = "10%" 
      node_soak_duration_in_minutes = 0
    }
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Environment = "Production"
  }
  depends_on = [azurerm_resource_group.spring-petclinic]
}

resource "azurerm_kubernetes_cluster_node_pool" "sp_aks_node" {
  name                  = "spaksnode"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.sp_aks.id
  vm_size               = "Standard_B2s"
  node_count            = 1
  max_pods              = 30
  mode                  = "User"
  auto_scaling_enabled  = false
  depends_on            = [azurerm_kubernetes_cluster.sp_aks]
}
```

### 3. `disk.tf`

Creates a managed disk and assigns a role for AKS to access it.

```hcl
resource "azurerm_managed_disk" "myAKSDisk" {
  name                 = "myAKSDisk"
  location             = local.location
  resource_group_name  = local.resource_group
  storage_account_type = "Standard_LRS"
  create_option        = "Empty"
  disk_size_gb         = 4
  depends_on           = [azurerm_resource_group.spring-petclinic]
}

resource "azurerm_role_assignment" "aks_disk_access" {
  principal_id          = azurerm_kubernetes_cluster.sp_aks.identity[0].principal_id
  role_definition_name  = "Contributor"
  scope                 = azurerm_managed_disk.myAKSDisk.id
  depends_on            = [azurerm_managed_disk.myAKSDisk]
}
```

### 4. `outputs.tf`

Outputs necessary information for the next step.

```hcl
output "kubernetes_host" {
  value     = azurerm_kubernetes_cluster.sp_aks.kube_config[0].host
  sensitive = true
}

output "kubernetes_client_certificate" {
  value     = azurerm_kubernetes_cluster.sp_aks.kube_config[0].client_certificate
  sensitive = true
}

output "kubernetes_client_key" {
  value     = azurerm_kubernetes_cluster.sp_aks.kube_config[0].client_key
  sensitive = true
}

output "kubernetes_cluster_ca_certificate" {
  value     = azurerm_kubernetes_cluster.sp_aks.kube_config[0].cluster_ca_certificate
  sensitive = true
}
```

### 5. `providers.tf`

Configures the Azure provider.

```hcl
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.0"
    }
  }
}

provider "azurerm" {
  subscription_id = "71d131e2-d168-45ca-9afe-06ed2ae2e20f"
  features {}
}
```

### 6. `resource_group.tf`

Defines the resource group.

```hcl
resource "azurerm_resource_group" "spring-petclinic" {
  name     = local.resource_group
  location = local.location
}
```

### 7. `storage_account.tf`

Creates the storage account and container for Terraform state.

```hcl
resource "azurerm_storage_account" "springpetclinicstorageac" {
  name                     = "springpetclinicstorageac"
  resource_group_name      = local.resource_group
  location                 = local.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_storage_container" "sp-storage-cont" {
  name                  = "sp-storage-cont"
  storage_account_name  = azurerm_storage_account.springpetclinicstorageac.name
  container_access_type = "private"
}
```

## Step 2: Deploying the Application

This step deploys the application on the AKS cluster using Helm.

### 1. `data_restore.tf`

Fetches the output values from Step 1.

```hcl
data "terraform_remote_state" "step1" {
  backend = "azurerm"

  config = {
    resource_group_name  = "sp_aks_group"
    storage_account_name = "springpetclinicstorageac"
    container_name       = "sp-storage-cont"
    key                  = "terraform.sp_storage_cont"
  }
}
```

### 2. `helm.tf`

Deploys the application using Helm.

```hcl
resource "helm_release" "spring-petclinic" {
  name       = "spring-petclinic-${var.environment}"
  chart      = "../../helm"
  namespace  = "${var.environment}"
  values     = [file("../../helm/values-${var.environment}.yaml")]
  depends_on = [kubernetes_manifest.my_pv, kubernetes_namespace.namespace]
}
```

### 3. `namespace.tf`

Creates the Kubernetes namespace for the application.

```hcl
resource "kubernetes_namespace" "namespace" {
  metadata {
    name = "${var.environment}"
  }
}
```

### 4. `providers.tf`

Configures the Kubernetes and Helm providers using outputs from Step 1.

```hcl
terraform {
  required_providers {
    helm = {
      source = "hashicorp/helm"
      version = "2.15.0"
    }
    kubernetes = {
      source = "hashicorp/kubernetes"
      version = "2.32.0"
    }
  }
}

provider "kubernetes" {
  host                   = data.terraform_remote_state.step1.outputs.kubernetes_host
  client_certificate     = base64decode(data.terraform_remote_state.step1.outputs.kubernetes_client_certificate)
  client_key             = base64decode(data.terraform_remote_state.step1.outputs.kubernetes_client_key)
  cluster_ca_certificate = base64decode(data.terraform_remote_state.step1.outputs.kubernetes_cluster_ca_certificate)
}

provider "helm" {
  kubernetes {
    host                   = data.terraform_remote_state.step1.outputs.kubernetes_host
    client_certificate     = base64decode(data.terraform_remote_state.step1.outputs.kubernetes_client_certificate)
    client_key             = base64decode(data.terraform_remote_state.step1.outputs.kubernetes_client_key)
    cluster_ca_certificate = base64decode(data.terraform_remote_state.step1.outputs.kubernetes_cluster_ca_certificate)
  }
}
```

### 5. `variables.tf`

Defines variables for the deployment.

```hcl
locals {
  resource_group = "sp_aks_group"
  location       = "UK South"
}

variable "environment" {
  type    = string
  default = "dev"
}
```

### 6. `volume.tf`

Deploys Kubernetes manifests for persistent volumes.

```hcl
resource "kubernetes_manifest" "my_pv" {
  manifest = yamldecode(file("../../pv-azure/pv-${var.environment}.yml"))
}
```

## Deployment Instructions

### Step 1: Create AKS Cluster

1. **Initialize Terraform:**
   ```bash
   terraform init
   ```

2. **Plan and Apply Step 1:**
   ```bash
   cd terraform-kubernetes/step1-cluster-creation
   terraform plan
   terraform apply
   ```

### Step 2: Deploy Application

1. **Initialize Terraform:**
   ```bash
   terraform init
   ```

2. **Plan and Apply Step 2:**
   ```bash
   cd terraform-kubernetes/step2-deploying-application
   terraform plan
   terraform apply
   ```

##

 Notes

- Ensure you have the necessary permissions and configurations to create resources in Azure.
- Modify the variables and configurations as needed based on your environment and requirements.
- Helm charts and Kubernetes manifests should be placed in the specified paths.

This README provides a comprehensive overview of how to set up and deploy your AKS cluster and application using Terraform. Adjust the specifics as needed to fit your project's requirements.