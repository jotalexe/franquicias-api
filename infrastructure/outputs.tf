output "ecr_repository_url" {
  description = "URL del repositorio ECR donde publicar la imagen Docker (docker push)"
  value       = aws_ecr_repository.this.repository_url
}

output "rds_endpoint" {
  description = "Endpoint de la instancia RDS PostgreSQL (solo accesible dentro de la VPC)"
  value       = aws_db_instance.postgres.endpoint
}

output "apprunner_service_url" {
  description = "URL publica del servicio desplegado en App Runner"
  value       = aws_apprunner_service.this.service_url
}
