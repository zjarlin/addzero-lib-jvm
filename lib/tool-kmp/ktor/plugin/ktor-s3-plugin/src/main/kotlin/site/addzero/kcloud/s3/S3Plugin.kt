package site.addzero.kcloud.s3

import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import org.koin.ktor.ext.getKoin
import site.addzero.starter.AppStarter
import site.addzero.starter.effectiveConfig
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

const val S3_APPLICATION_CONFIG_PROPERTY = "s3.applicationConfig"

@Module
@ComponentScan("site.addzero.kcloud.s3")
class S3KoinModule {
    @Single
    fun provideS3Config(
        @Property(S3_APPLICATION_CONFIG_PROPERTY)
        config: ApplicationConfig,
    ): S3Config {
        val section = runCatching { config.config("s3") }.getOrNull()
        return S3Config(
            endpoint = section?.propertyOrNull("endpoint")?.getString() ?: "",
            region = section?.propertyOrNull("region")?.getString() ?: "us-east-1",
            bucket = section?.propertyOrNull("bucket")?.getString() ?: "",
            accessKey = section?.propertyOrNull("accessKey")?.getString() ?: "",
            secretKey = section?.propertyOrNull("secretKey")?.getString() ?: "",
        )
    }

    @Single
    fun provideS3Client(config: S3Config): S3Client {
        val credentials = AwsBasicCredentials.create(config.accessKey, config.secretKey)
        return S3Client.builder()
            .endpointOverride(URI.create(config.endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(config.region))
            .forcePathStyle(true)
            .build()
    }
}

/**
 * S3 自动引导实现类。符合 AppStarter 接口，通过 Koin 自动发现。
 */
@Named("s3Starter")
@Single
class S3Starter : AppStarter {
    override val order: Int get() = 60

    override fun Application.enable(): Boolean {
        val s3Config = runCatching { effectiveConfig().config("s3") }.getOrNull()
        return s3Config?.propertyOrNull("enabled")?.getString()?.toBoolean() != false
    }

    override fun Application.onInstall() {
        val s3Config = getKoin().get<S3Config>()
        if (
            s3Config.endpoint.isBlank() ||
            s3Config.bucket.isBlank() ||
            s3Config.accessKey.isBlank() ||
            s3Config.secretKey.isBlank()
        ) {
            log.warn("Skipping S3 starter because endpoint/bucket/accessKey/secretKey are incomplete")
            return
        }

        install(createApplicationPlugin(name = "S3AutoConfiguration") {
            val s3Client = application.getKoin().get<S3Client>()

            application.monitor.subscribe(ApplicationStopping) {
                s3Client.close()
            }
        })
    }
}
