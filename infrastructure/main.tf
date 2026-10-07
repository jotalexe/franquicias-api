terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# ---------------------------------------------------------------------------
# Red: se reutiliza la VPC por defecto de la cuenta para evitar el costo de
# un NAT Gateway. App Runner llega a RDS a traves de un VPC Connector (trafico
# de entrada a la VPC), sin necesitar salida a internet desde la VPC.
# ---------------------------------------------------------------------------

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

resource "aws_security_group" "apprunner_connector" {
  name        = "${var.project_name}-apprunner-connector-sg"
  description = "Security group del VPC connector de App Runner"
  vpc_id      = data.aws_vpc.default.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "rds" {
  name        = "${var.project_name}-rds-sg"
  description = "Permite conexiones Postgres unicamente desde App Runner"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description     = "Postgres desde App Runner"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.apprunner_connector.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ---------------------------------------------------------------------------
# Base de datos: RDS PostgreSQL de un solo nodo (db.t4g.micro), sin acceso
# publico, pensada para una prueba tecnica (no para produccion).
# ---------------------------------------------------------------------------

resource "aws_db_subnet_group" "this" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = data.aws_subnets.default.ids
}

resource "aws_db_instance" "postgres" {
  identifier     = "${var.project_name}-db"
  engine         = "postgres"
  engine_version = "16"
  instance_class = var.db_instance_class

  allocated_storage = 20
  storage_type       = "gp3"

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  skip_final_snapshot = true
  deletion_protection = false
  multi_az             = false
  backup_retention_period = 0
}

# ---------------------------------------------------------------------------
# Registro de la imagen Docker.
# ---------------------------------------------------------------------------

resource "aws_ecr_repository" "this" {
  name                 = var.project_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

# ---------------------------------------------------------------------------
# App Runner: ejecuta el contenedor y escala a cero actividad entre
# despliegues; es la opcion mas economica para un servicio de bajo trafico.
# ---------------------------------------------------------------------------

resource "aws_apprunner_vpc_connector" "this" {
  vpc_connector_name = "${var.project_name}-connector"
  subnets             = data.aws_subnets.default.ids
  security_groups      = [aws_security_group.apprunner_connector.id]
}

resource "aws_iam_role" "apprunner_ecr_access" {
  name = "${var.project_name}-apprunner-ecr-access"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "build.apprunner.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "apprunner_ecr_access" {
  role       = aws_iam_role.apprunner_ecr_access.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"
}

# Rol de instancia (runtime): el que usa el contenedor en ejecucion para leer
# el password de la base de datos desde Secrets Manager. Es distinto del rol
# anterior, que solo se usa en build-time para hacer pull de la imagen ECR.
resource "aws_iam_role" "apprunner_instance_role" {
  name = "${var.project_name}-apprunner-instance-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "tasks.apprunner.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "apprunner_instance_secrets" {
  name = "${var.project_name}-secrets-access"
  role = aws_iam_role.apprunner_instance_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue"]
      Resource = aws_secretsmanager_secret.db_password.arn
    }]
  })
}

resource "aws_apprunner_service" "this" {
  service_name = var.project_name

  source_configuration {
    authentication_configuration {
      access_role_arn = aws_iam_role.apprunner_ecr_access.arn
    }

    image_repository {
      image_identifier      = "${aws_ecr_repository.this.repository_url}:${var.ecr_image_tag}"
      image_repository_type = "ECR"

      image_configuration {
        port = "8080"

        runtime_environment_variables = {
          SPRING_PROFILES_ACTIVE = "dev"
          DATABASE_URL           = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/${var.db_name}"
          DATABASE_USERNAME      = var.db_username
        }

        runtime_environment_secrets = {
          DATABASE_PASSWORD = aws_secretsmanager_secret.db_password.arn
        }
      }
    }

    auto_deployments_enabled = false
  }

  instance_configuration {
    cpu                = "256"
    memory             = "512"
    instance_role_arn  = aws_iam_role.apprunner_instance_role.arn
  }

  network_configuration {
    egress_configuration {
      egress_type       = "VPC"
      vpc_connector_arn = aws_apprunner_vpc_connector.this.arn
    }
  }
}

resource "aws_secretsmanager_secret" "db_password" {
  name = "${var.project_name}-db-password"
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password
}
