variable "aws_region" {
  description = "Region de AWS donde se despliega la infraestructura"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Prefijo usado para nombrar los recursos"
  type        = string
  default     = "franchise-api"
}

variable "db_name" {
  description = "Nombre de la base de datos"
  type        = string
  default     = "franchise_db"
}

variable "db_username" {
  description = "Usuario administrador de la base de datos"
  type        = string
  default     = "franchise_user"
}

variable "db_password" {
  description = "Password de la base de datos. No tiene default: se debe pasar via TF_VAR_db_password o un terraform.tfvars no versionado."
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "Clase de instancia RDS (db.t4g.micro = capa gratuita/economica)"
  type        = string
  default     = "db.t4g.micro"
}

variable "ecr_image_tag" {
  description = "Tag de la imagen Docker a desplegar en App Runner"
  type        = string
  default     = "latest"
}
