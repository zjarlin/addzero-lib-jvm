/**
 * Docker run 命令转 Docker Compose 工具类
 */
object DockerComposeConverter {

    /**
     * 将 docker run 命令转换为 docker-compose 配置
     * @param dockerRunCommand docker run 命令
     * @return docker-compose yml 字符串
     */
    fun convertToDockerCompose(dockerRunCommand: String): String {
        val command = DockerRunCommand.parse(dockerRunCommand)
        return command.toDockerComposeYml()
    }
}

/**
 * Docker run 命令数据类
 */
data class DockerRunCommand(
    val image: String,
    val name: String? = null,
    val ports: List<String> = emptyList(),
    val environment: Map<String, String> = emptyMap(),
    val volumes: List<String> = emptyList(),
    val network: String? = null,
    val restart: String? = null,
    val otherOptions: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * 解析 docker run 命令
         */
        fun parse(command: String): DockerRunCommand {
            val args = parseCommandLine(command)
            val iterator = args.listIterator()

            var image: String? = null
            var name: String? = null
            val ports = mutableListOf<String>()
            val environment = mutableMapOf<String, String>()
            val volumes = mutableListOf<String>()
            var network: String? = null
            var restart: String? = null
            val otherOptions = mutableMapOf<String, String>()

            while (iterator.hasNext()) {
                val arg = iterator.next()
                when {
                    arg == "docker" || arg == "run" -> continue
                    arg == "--name" && iterator.hasNext() -> name = iterator.next()
                    arg.startsWith("--name=") -> name = arg.substring(7)
                    arg == "-p" && iterator.hasNext() -> ports.add(iterator.next())
                    arg.startsWith("-p") || arg.startsWith("--publish") -> {
                        val portArg = if (arg.startsWith("--publish=")) arg.substring(10) else arg.substring(2)
                        if (portArg.isNotEmpty()) {
                            ports.add(portArg)
                        } else if (iterator.hasNext()) {
                            ports.add(iterator.next())
                        }
                    }
                    arg == "-e" && iterator.hasNext() -> {
                        val env = iterator.next()
                        val parts = env.split("=", limit = 2)
                        environment[parts[0]] = if (parts.size > 1) parts[1] else ""
                    }
                    arg.startsWith("-e") || arg.startsWith("--env") -> {
                        val envArg = if (arg.startsWith("--env=")) arg.substring(6) else arg.substring(2)
                        if (envArg.isNotEmpty()) {
                            val parts = envArg.split("=", limit = 2)
                            environment[parts[0]] = if (parts.size > 1) parts[1] else ""
                        } else if (iterator.hasNext()) {
                            val env = iterator.next()
                            val parts = env.split("=", limit = 2)
                            environment[parts[0]] = if (parts.size > 1) parts[1] else ""
                        }
                    }
                    arg == "-v" && iterator.hasNext() -> volumes.add(iterator.next())
                    arg.startsWith("-v") || arg.startsWith("--volume") -> {
                        val volumeArg = if (arg.startsWith("--volume=")) arg.substring(9) else arg.substring(2)
                        if (volumeArg.isNotEmpty()) {
                            volumes.add(volumeArg)
                        } else if (iterator.hasNext()) {
                            volumes.add(iterator.next())
                        }
                    }
                    arg == "--network" && iterator.hasNext() -> network = iterator.next()
                    arg.startsWith("--network=") -> network = arg.substring(10)
                    arg == "--restart" && iterator.hasNext() -> restart = iterator.next()
                    arg.startsWith("--restart=") -> restart = arg.substring(10)
                    !arg.startsWith("-") && image == null -> image = arg
                    else -> {
                        // 处理其他选项
                        if (arg.startsWith("--") && arg.contains("=")) {
                            val parts = arg.substring(2).split("=", limit = 2)
                            otherOptions[parts[0]] = parts[1]
                        } else if (arg.startsWith("--") && iterator.hasNext()) {
                            otherOptions[arg.substring(2)] = iterator.next()
                        }
                    }
                }
            }

            return DockerRunCommand(
                image = image ?: throw IllegalArgumentException("未找到镜像名称"),
                name = name,
                ports = ports,
                environment = environment,
                volumes = volumes,
                network = network,
                restart = restart,
                otherOptions = otherOptions
            )
        }

        /**
         * 解析命令行参数
         */
        private fun parseCommandLine(command: String): List<String> {
            val args = mutableListOf<String>()
            val currentArg = StringBuilder()
            var inQuotes = false

            var i = 0
            while (i < command.length) {
                val char = command[i]
                when {
                    char == '\\' && i + 1 < command.length -> {
                        // 处理转义字符
                        i++
                        currentArg.append(command[i])
                    }
                    char == '"' -> {
                        inQuotes = !inQuotes
                    }
                    char == ' ' && !inQuotes -> {
                        if (currentArg.isNotEmpty()) {
                            args.add(currentArg.toString())
                            currentArg.clear()
                        }
                    }
                    else -> {
                        currentArg.append(char)
                    }
                }
                i++
            }

            if (currentArg.isNotEmpty()) {
                args.add(currentArg.toString())
            }

            return args
        }
    }

    /**
     * 转换为 docker-compose yml 格式
     */
    fun toDockerComposeYml(): String {
        val serviceName = name ?: image.substringAfterLast("/").substringBefore(":").ifEmpty { "app" }

        val yaml = StringBuilder()
        yaml.append("version: '3.8'\n")
        yaml.append("services:\n")
        yaml.append("  $serviceName:\n")
        yaml.append("    image: $image\n")

        if (name != null) {
            yaml.append("    container_name: $name\n")
        }

        if (ports.isNotEmpty()) {
            yaml.append("    ports:\n")
            ports.forEach { port ->
                yaml.append("      - \"$port\"\n")
            }
        }

        if (environment.isNotEmpty()) {
            yaml.append("    environment:\n")
            environment.forEach { (key, value) ->
                // 处理包含特殊字符的值
                val escapedValue = if (value.contains("\"") || value.contains("'")) {
                    value.replace("\"", "\\\"")
                } else {
                    value
                }
                yaml.append("      $key: \"$escapedValue\"\n")
            }
        }

        if (volumes.isNotEmpty()) {
            yaml.append("    volumes:\n")
            volumes.forEach { volume ->
                yaml.append("      - \"$volume\"\n")
            }
        }

        if (network != null) {
            yaml.append("    networks:\n")
            yaml.append("      - $network\n")
        }

        if (restart != null) {
            yaml.append("    restart: $restart\n")
        }

        return yaml.toString()
    }
}
