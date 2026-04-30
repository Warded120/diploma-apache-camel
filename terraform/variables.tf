variable "bucket_name" {
  description = "Name of the S3 bucket used to store deployment artifacts"
  type        = string
  default     = "diploma-deployments"
}

variable "localstack_endpoint" {
  description = "LocalStack gateway endpoint"
  type        = string
  default     = "http://localhost:4566"
}

