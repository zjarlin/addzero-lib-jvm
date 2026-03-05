# Tool RustFS

Kotlin helpers for interacting with a RustFS cluster through the AWS S3 SDK. RustFS is an open-source, S3-compatible
object storage engine written in Rust (see the [installation docs](https://docs.rustfs.com/installation/)).

## Quick Start

1. Follow the RustFS quick start and spin up a local node (for example via Docker Compose):
   ```bash
   docker run -p 9000:9000 -p 9001:9001 \
     -e RUSTFS_ACCESS_KEY=rustfsadmin \
     -e RUSTFS_SECRET_KEY=rustfsadmin \
     rustfs/rustfs:latest
   ```
2. Use the defaults provided by `RustfsConfig.default()` to connect to that instance.
3. Use `RustfsUtil` APIs (`ensureBucket`, `putObject`, `listObjects`, `getPresignedObjectUrl`, etc.) to drive objects.

## Configuration

`RustfsConfig` stores endpoint + credentials. By default it matches the Docker quick-start values:

| Field      | Default            | Notes                                |
|------------|--------------------|--------------------------------------|
| endpoint   | `http://127.0.0.1:9000` | RustFS S3 endpoint (HTTP).            |
| accessKey  | `rustfsadmin`      | Default admin key from the docs.     |
| secretKey  | `rustfsadmin`      | Default admin secret from the docs.  |
| region     | `us-east-1`        | Required by AWS SDK even for single region RustFS deployments. |

Override them if your cluster runs elsewhere.

## Testing

The module includes light unit tests covering configuration defaults and presigned URL generation. Integration tests
can be added later by pointing the client to a real RustFS node (credentials/bucket via env vars or system properties).
