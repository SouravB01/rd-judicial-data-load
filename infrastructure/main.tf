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
  name = "ACCOUNT-NAME"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "ACCOUNT_KEY" {
  name = "ACCOUNT-KEY"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "CONTAINER_NAME" {
  name = "CONTAINER-NAME"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "BLOB_URL_SUFFIX" {
  name = "BLOB-URL-SUFFIX"
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

data "azurerm_key_vault_secret" "POSTGRES-USER" {
  name      = "judicial-api-POSTGRES-USER"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name      = "judicial-api-POSTGRES-PASS"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "POSTGRES_HOST" {

  name      = "judicial-api-POSTGRES-HOST"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

data "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name      = "judicial-api-POSTGRES-PORT"
  value     = "5432"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
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

    POSTGRES_HOST = "${data.azurerm_key_vault_secret.POSTGRES_HOST}"
    POSTGRES_PORT = "${data.azurerm_key_vault_secret.POSTGRES_PORT}"
    POSTGRES_USERNAME = "${data.azurerm_key_vault_secret.POSTGRES-USER}"
    POSTGRES_PASSWORD = "${data.azurerm_key_vault_secret.POSTGRES-PASS}"
    POSTGRES_CONNECTION_OPTIONS = "?"

    ACCOUNT_NAME = "${data.azurerm_key_vault_secret.ACCOUNT_NAME.value}"
    ACCOUNT_KEY = "${data.azurerm_key_vault_secret.ACCOUNT_KEY.value}"
    CONTAINER_NAME = "${data.azurerm_key_vault_secret.CONTAINER_NAME.value}"
    BLOB_URL_SUFFIX = "${data.azurerm_key_vault_secret.BLOB_URL_SUFFIX.value}"

    ROOT_LOGGING_LEVEL = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_RD = "${var.log_level_rd}"
    EXCEPTION_LENGTH = 100
  }
}