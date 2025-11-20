region           = "${region}"
environment_name = "${name}"

vpc_cidr            = "${vpcCidr}"
public_subnet_cidr  = "${publicSubnetCidr}"
private_subnet_cidr = "${privateSubnetCidr}"

<#if rdsEnabled>
rds_username = "${rdsUsername}"
rds_password = "${rdsPassword}"
rds_db_name  = "${rdsDbName}"
</#if>
