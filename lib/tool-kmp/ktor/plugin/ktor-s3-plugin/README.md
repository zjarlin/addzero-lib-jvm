# ktor-s3-plugin

`ktor-s3-plugin` provides Koin wiring for `S3Client`, `S3Service`, and an `AppStarter` that enables S3 support
from the consumer application's `s3.*` configuration.

Pass `site.addzero.kcloud.s3.S3_APPLICATION_CONFIG_PROPERTY` into Koin when bootstrapping if your application
does not already expose the `ApplicationConfig` as a property.

Required config:

- `s3.endpoint`
- `s3.bucket`
- `s3.accessKey`
- `s3.secretKey`
