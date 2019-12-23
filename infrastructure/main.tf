# Temporary fix for template API version error on deployment
provider "azurerm" {
  version = "1.22.0"
}

locals {
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  preview_app_service_plan = "${var.product}-${var.component}-${var.env}"
  non_preview_app_service_plan = "${var.product}-${var.env}"
  app_service_plan = "${var.env == "preview" || var.env == "spreview" ? local.preview_app_service_plan : local.non_preview_app_service_plan}"

  preview_vault_name = "${var.raw_product}-aat"
  non_preview_vault_name = "${var.raw_product}-${var.env}"
  key_vault_name = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"
}

data "azurerm_key_vault" "rd_key_vault" {
  name = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

data "azurerm_key_vault_secret" "ACCOUNT_NAME" {
  name = "ACCOUNT_NAME"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "ACCOUNT_KEY" {
  name = "ACCOUNT_KEY"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "CONTAINER_NAME" {
  name = "CONTAINER_NAME"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "BLOB_URL_SUFFIX" {
  name = "BLOB_URL_SUFFIX"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "SFTP_USER_NAME" {
  name = "SFTP_USER_NAME"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "SFTP_USER_PASSWORD" {
  name = "SFTP_USER_PASSWORD"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "SFTP_HOST" {
  name = "SFTP_HOST"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "SFTP_FILE" {
  name = "SFTP_FILE"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "GPG_PASSWORD" {
  name = "GPG_PASSWORD"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "GPG_PUBLIC_KEY" {
  name = "GPG_PUBLIC_KEY"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "GPG_PRIVATE_KEY" {
  name = "GPG_PRIVATE_KEY"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}




resource "azurerm_resource_group" "rg" {
  name = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"
  tags {
    "Deployment Environment" = "${var.env}"
    "Team Name" = "${var.team_name}"
    "lastUpdated" = "${timestamp()}"
  }
}

module "db-judicial-data-load-data" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}-${var.component}-postgres-db"
  location = "${var.location}"
  subscription = "${var.subscription}"
  env = "${var.env}"
  postgresql_user = "dbjuddata"
  database_name = "dbjuddata"
  common_tags = "${var.common_tags}"
}

module "rd_judicial_data_load" {
  source = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product = "${var.product}-${var.component}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  subscription = "${var.subscription}"
  capacity = "${var.capacity}"
  instance_size = "${var.instance_size}"
  common_tags = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  asp_name = "${local.app_service_plan}"
  asp_rg = "${local.app_service_plan}"
  enable_ase = "${var.enable_ase}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE = false

    POSTGRES_HOST = "${module.db-judicial-data-load-data.host_name}"
    POSTGRES_PORT = "${module.db-judicial-data-load-data.postgresql_listen_port}"
    POSTGRES_DATABASE = "${module.db-judicial-data-load-data.postgresql_database}"
    POSTGRES_USER = "${module.db-judicial-data-load-data.user_name}"
    POSTGRES_USERNAME = "${module.db-judicial-data-load-data.user_name}"
    POSTGRES_PASSWORD = "${module.db-judicial-data-load-data.postgresql_password}"
    POSTGRES_CONNECTION_OPTIONS = "?"

    S2S_URL = "${data.azurerm_key_vault_secret.s2s_url.value}"
    S2S_SECRET = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    IDAM_URL = "${data.azurerm_key_vault_secret.idam_url.value}"
    USER_PROFILE_URL = "${data.azurerm_key_vault_secret.USER_PROFILE_URL.value}"
    CCD_URL = "${data.azurerm_key_vault_secret.CCD_URL.value}"

    OAUTH2_REDIRECT_URI = "${data.azurerm_key_vault_secret.oauth2_redirect_uri.value}"
    OAUTH2_CLIENT_ID = "${data.azurerm_key_vault_secret.oauth2_client_id.value}"
    OAUTH2_CLIENT_SECRET = "${data.azurerm_key_vault_secret.oauth2_client_secret.value}"

    ROOT_LOGGING_LEVEL = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_RD = "${var.log_level_rd}"
    EXCEPTION_LENGTH = 100
  }
}
