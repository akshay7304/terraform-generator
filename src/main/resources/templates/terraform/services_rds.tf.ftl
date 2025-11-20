<#if rdsEnabled>
resource "aws_db_subnet_group" "main" {
  name       = "${r"${var.environment_name}"}-db-subnet-group"

  # FIX: RDS must use only private subnet
  subnet_ids = [aws_subnet.private.id]

  tags = {
    Name = "${r"${var.environment_name}"}-db-subnet-group"
  }
}

resource "aws_security_group" "rds" {
  name        = "${r"${var.environment_name}"}-rds-sg"
  description = "Security group for RDS instance"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = <#if rdsEngine == "postgres">5432<#else>3306</#if>
    to_port     = <#if rdsEngine == "postgres">5432<#else>3306</#if>
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
    description = "Allow database access from VPC"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "${r"${var.environment_name}"}-rds-sg"
  }
}

resource "aws_db_instance" "main" {
  identifier             = "${r"${var.environment_name}"}-db"
  engine                 = "${rdsEngine}"
  engine_version         = <#if rdsEngine == "postgres">"15.4"<#else>"8.0.35"</#if>
  instance_class         = "${rdsInstanceClass}"

  allocated_storage      = 20
  storage_type           = "gp3"
  storage_encrypted      = true
  
  db_name  = var.rds_db_name
  username = var.rds_username
  password = var.rds_password
  
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "mon:04:00-mon:05:00"
  
  skip_final_snapshot       = true
  final_snapshot_identifier = "${r"${var.environment_name}"}-db-final-snapshot"

  enabled_cloudwatch_logs_exports = <#if rdsEngine == "postgres">["postgresql"]<#else>["error","general","slowquery"]</#if>

  tags = {
    Name = "${r"${var.environment_name}"}-rds"
  }
}
</#if>
