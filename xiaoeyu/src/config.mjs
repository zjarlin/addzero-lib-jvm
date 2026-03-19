import fs from "node:fs";
import path from "node:path";

function deepMerge(baseValue, overrideValue) {
  if (Array.isArray(baseValue) || Array.isArray(overrideValue)) {
    return overrideValue ?? baseValue;
  }

  if (
    baseValue &&
    overrideValue &&
    typeof baseValue === "object" &&
    typeof overrideValue === "object"
  ) {
    const result = {...baseValue};
    for (const [key, value] of Object.entries(overrideValue)) {
      result[key] = deepMerge(baseValue[key], value);
    }
    return result;
  }

  return overrideValue ?? baseValue;
}

function createDefaultConfig() {
  return {
    source: {
      type: "local",
      path: "."
    },
    site: {
      title: "Xiaoeyu",
      tagline: "Repository documentation site generated from README files.",
      url: "https://example.com",
      baseUrl: "/",
      organizationName: "",
      projectName: "docs",
      repositoryUrl: "",
      editUrl: "",
      defaultLocale: "en",
      locales: ["en"],
      navbar: {
        docsLabel: "Docs",
        repositoryLabel: "Repository"
      }
    },
    collect: {
      include: ["**/README.md"],
      exclude: [
        "README.md",
        "docs/**",
        "**/.git/**",
        "**/node_modules/**",
        "**/build/**",
        "**/target/**"
      ],
      rulesFile: ""
    },
    render: {
      contentDir: "docs/content",
      generatedMetaFile: "docs/.xiaoeyu/site-metadata.json",
      stripFirstH1: true,
      generateIndex: true,
      indexSlug: "/",
      sourceNotice: "Automatically collected from `{path}`.",
      index: {
        title: "Repository Docs",
        heading: "Repository Docs",
        description: "Documentation site generated from README files.",
        featuresTitle: "Highlights",
        features: [
          "Automatically collects module README files.",
          "Supports include and exclude rules.",
          "Can be published through GitHub Pages or any static hosting."
        ],
        emptyMessage: "No README files matched the collection rules yet.",
        sectionTitle: "Modules"
      }
    },
    enrichers: []
  };
}

export function resolveConfigPath(configFile = "xiaoeyu.config.json", cwd = process.cwd()) {
  return path.resolve(cwd, configFile);
}

export function loadConfig(configFile = "xiaoeyu.config.json", cwd = process.cwd()) {
  const resolvedConfigFile = resolveConfigPath(configFile, cwd);
  const rawText = fs.readFileSync(resolvedConfigFile, "utf8");
  const parsedConfig = JSON.parse(rawText);
  const config = deepMerge(createDefaultConfig(), parsedConfig);
  const configDir = path.dirname(resolvedConfigFile);

  return {
    config,
    configDir,
    configFile: resolvedConfigFile
  };
}
