#!/usr/bin/env node
import {createDocusaurusConfig, generateDocs, loadConfig, scaffoldSite} from "./index.mjs";

function printUsage() {
  console.log(`xiaoeyu

Usage:
  xiaoeyu generate [--config path]
  xiaoeyu print-docusaurus-config [--config path]
  xiaoeyu scaffold-site [--target dir] [--force]
`);
}

function readFlagValue(args, flagName, defaultValue) {
  const flagIndex = args.indexOf(flagName);
  if (flagIndex === -1) {
    return defaultValue;
  }

  const value = args[flagIndex + 1];
  if (!value || value.startsWith("--")) {
    throw new Error(`Missing value for ${flagName}`);
  }

  return value;
}

function main() {
  const [, , command, ...args] = process.argv;

  if (!command || command === "--help" || command === "-h") {
    printUsage();
    return;
  }

  const configFile = readFlagValue(args, "--config", "xiaoeyu.config.json");

  if (command === "generate") {
    const {config, configDir} = loadConfig(configFile, process.cwd());
    const result = generateDocs(config, configDir);
    console.log(
      `Generated ${result.pageCount} pages into ${result.contentDir} and wrote metadata to ${result.metadataFile}.`
    );
    return;
  }

  if (command === "print-docusaurus-config") {
    const {config} = loadConfig(configFile, process.cwd());
    const docusaurusConfig = createDocusaurusConfig(config);
    console.log(JSON.stringify(docusaurusConfig, null, 2));
    return;
  }

  if (command === "scaffold-site") {
    const targetDir = readFlagValue(args, "--target", "docs");
    const force = args.includes("--force");
    const result = scaffoldSite({targetDir, force});
    console.log(`Scaffolded Docusaurus site into ${result.targetDir}.`);
    return;
  }

  throw new Error(`Unknown command: ${command}`);
}

try {
  main();
} catch (error) {
  console.error(error instanceof Error ? error.message : String(error));
  process.exitCode = 1;
}
