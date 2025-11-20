variable "region" {
  description = "AWS region"
  type        = string
  default     = "${region}"
}

variable "environment_name" {
  description = "Environment name"
  type        = string
  default     = "${name}"
}

variable "vpc_cidr" {
  description = "VPC CIDR"
  type        = string
  default     = "${vpcCidr}"
}

variable "public_subnet_cidr" {
  description = "Public subnet CIDR"
  type        = string
  default     = "${publicSubnetCidr}"
}

variable "private_subnet_cidr" {
  description = "Private subnet CIDR"
  type        = string
  default     = "${privateSubnetCidr}"
}

<#if rdsEnabled>
variable "rds_username" {
  type        = string
  default     = "${rdsUsername}"
  sensitive   = true
}

variable "rds_password" {
  type        = string
  default     = "${rdsPassword}"
  sensitive   = true
}

variable "rds_db_name" {
  type        = string
  default     = "${rdsDbName}"
}
</#if>
