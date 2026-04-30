provider "aws" {
  access_key = "test"
  secret_key = "test"
  region     = "us-east-1"

  # Point all API calls to LocalStack
  endpoints {
    s3 = var.localstack_endpoint
  }

  # Required for LocalStack path-style S3 URLs
  s3_use_path_style           = true
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true
}

resource "aws_s3_bucket" "app_deployments" {
  bucket = var.bucket_name
}

resource "aws_s3_bucket_versioning" "app_deployments" {
  bucket = aws_s3_bucket.app_deployments.id

  versioning_configuration {
    status = "Enabled"
  }
}

