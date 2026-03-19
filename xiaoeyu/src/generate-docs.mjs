import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import {spawnSync} from "node:child_process";
import {matchesAnyGlob, normalizeGlobPath} from "./glob.mjs";

function ensureDirectory(filePath) {
  fs.mkdirSync(filePath, {recursive: true});
}

function walkFiles(rootDir, currentDir = rootDir, files = []) {
  const entries = fs.readdirSync(currentDir, {withFileTypes: true});

  for (const entry of entries) {
    const absolutePath = path.join(currentDir, entry.name);
    if (entry.isDirectory()) {
      walkFiles(rootDir, absolutePath, files);
      continue;
    }

    files.push(absolutePath);
  }

  return files;
}

function findFirstHeading(markdown) {
  for (const line of markdown.split(/\r?\n/)) {
    const match = line.match(/^#\s+(.+?)\s*$/);
    if (match) {
      return match[1].trim();
    }
  }

  return "";
}

function stripFirstH1(markdown) {
  let skipped = false;
  const output = [];

  for (const line of markdown.split(/\r?\n/)) {
    if (!skipped && /^#\s+/.test(line)) {
      skipped = true;
      continue;
    }
    output.push(line);
  }

  return output.join("\n").replace(/^\n+/, "");
}

function escapeFrontmatter(text) {
  return String(text).replace(/'/g, "''");
}

function applyTemplate(template, context) {
  return template.replace(/\{(\w+)\}/g, (_, key) => context[key] ?? "");
}

function loadRuleMatchers(ruleFilePath) {
  if (!ruleFilePath || !fs.existsSync(ruleFilePath)) {
    return [];
  }

  return fs
    .readFileSync(ruleFilePath, "utf8")
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line && !line.startsWith("#"))
    .map((line) => ({
      include: line.startsWith("!"),
      pattern: line.startsWith("!") ? line.slice(1) : line
    }));
}

function passesRules(relativePath, rules) {
  let included = true;

  for (const rule of rules) {
    if (matchesAnyGlob(relativePath, [rule.pattern])) {
      included = rule.include;
    }
  }

  return included;
}

function collectGradleSnippet(config, context) {
  const enrichConfig = {
    title: "Maven / Gradle",
    intro: "Published for dependency consumption.",
    groupId: "com.example",
    versionPlaceholder: "latest-version",
    gradleTemplate: "implementation(\"{groupId}:{artifactId}:{version}\")",
    mavenTemplate:
      "<dependency>\n  <groupId>{groupId}</groupId>\n  <artifactId>{artifactId}</artifactId>\n  <version>{version}</version>\n</dependency>",
    matchBuildFiles: ["build.gradle.kts", "build.gradle"],
    ...config
  };

  const hasBuildFile = enrichConfig.matchBuildFiles.some((fileName) =>
    fs.existsSync(path.join(context.absoluteModuleDir, fileName))
  );

  if (!hasBuildFile) {
    return "";
  }

  const templateContext = {
    groupId: enrichConfig.groupId,
    artifactId: context.moduleName,
    version: enrichConfig.versionPlaceholder
  };

  return [
    "",
    `## ${enrichConfig.title}`,
    "",
    enrichConfig.intro,
    "",
    "```kotlin",
    applyTemplate(enrichConfig.gradleTemplate, templateContext),
    "```",
    "",
    "```xml",
    applyTemplate(enrichConfig.mavenTemplate, templateContext),
    "```",
    ""
  ].join("\n");
}

function renderEnrichers(enrichers, context) {
  return enrichers
    .filter((enricher) => enricher && enricher.enabled !== false)
    .map((enricher) => {
      if (enricher.type === "gradleDependencySnippet") {
        return collectGradleSnippet(enricher, context);
      }

      return "";
    })
    .join("");
}

function resolveSource(configDir, sourceConfig) {
  if (sourceConfig.type === "git") {
    const tempRoot = fs.mkdtempSync(path.join(os.tmpdir(), "xiaoeyu-"));
    const checkoutDir = path.join(tempRoot, "repo");
    const args = ["clone", "--depth", "1"];

    if (sourceConfig.branch) {
      args.push("--branch", sourceConfig.branch);
    }

    args.push(sourceConfig.repoUrl, checkoutDir);
    const result = spawnSync("git", args, {encoding: "utf8"});

    if (result.status !== 0) {
      throw new Error(`Failed to clone repository: ${result.stderr || result.stdout}`);
    }

    return {
      repoDir: checkoutDir,
      cleanup() {
        fs.rmSync(tempRoot, {recursive: true, force: true});
      }
    };
  }

  return {
    repoDir: path.resolve(configDir, sourceConfig.path ?? "."),
    cleanup() {}
  };
}

function createIndexDocument(config, links) {
  const featureLines = (config.render.index.features ?? []).map((feature) => `- ${feature}`);
  const sections = [
    "---",
    `title: '${escapeFrontmatter(config.render.index.title)}'`,
    `slug: ${config.render.indexSlug}`,
    "---",
    "",
    `# ${config.render.index.heading}`,
    "",
    config.render.index.description,
    ""
  ];

  if (featureLines.length > 0) {
    sections.push(`## ${config.render.index.featuresTitle ?? "Highlights"}`);
    sections.push("");
    sections.push(...featureLines);
    sections.push("");
  }

  sections.push(`## ${config.render.index.sectionTitle ?? "Modules"}`);
  sections.push("");

  if (links.length > 0) {
    sections.push(...links);
  } else {
    sections.push(config.render.index.emptyMessage);
  }

  sections.push("");
  return sections.join("\n");
}

export function generateDocs(config, configDir) {
  const contentDir = path.resolve(configDir, config.render.contentDir);
  const metadataFile = path.resolve(configDir, config.render.generatedMetaFile);
  const rulesFile = config.collect.rulesFile
    ? path.resolve(configDir, config.collect.rulesFile)
    : "";
  const rules = loadRuleMatchers(rulesFile);
  const source = resolveSource(configDir, config.source);

  try {
    fs.rmSync(contentDir, {recursive: true, force: true});
    ensureDirectory(contentDir);
    ensureDirectory(path.dirname(metadataFile));

    const allFiles = walkFiles(source.repoDir);
    const readmeFiles = allFiles
      .map((absolutePath) => ({
        absolutePath,
        relativePath: normalizeGlobPath(path.relative(source.repoDir, absolutePath))
      }))
      .filter(({relativePath}) => matchesAnyGlob(relativePath, config.collect.include))
      .filter(({relativePath}) => !matchesAnyGlob(relativePath, config.collect.exclude))
      .filter(({relativePath}) => passesRules(relativePath, rules))
      .sort((left, right) => left.relativePath.localeCompare(right.relativePath));

    const links = [];
    const pages = [];

    for (const file of readmeFiles) {
      const moduleDir = path.posix.dirname(file.relativePath);
      const moduleName = path.posix.basename(moduleDir);
      const absoluteModuleDir = path.join(source.repoDir, moduleDir);
      const originalMarkdown = fs.readFileSync(file.absolutePath, "utf8");
      const title = findFirstHeading(originalMarkdown) || moduleName;
      const transformedMarkdown = config.render.stripFirstH1
        ? stripFirstH1(originalMarkdown)
        : originalMarkdown;
      const sourceNotice = config.render.sourceNotice.replace("{path}", file.relativePath);
      const enricherOutput = renderEnrichers(config.enrichers, {
        moduleName,
        moduleDir,
        absoluteModuleDir
      });
      const targetDir = moduleDir === "." ? contentDir : path.join(contentDir, moduleDir);
      const targetFile = path.join(targetDir, "index.md");

      ensureDirectory(targetDir);

      const sections = [
        "---",
        `title: '${escapeFrontmatter(title)}'`,
        `description: '${escapeFrontmatter(sourceNotice)}'`,
        "---",
        "",
        `> ${sourceNotice}`,
        ""
      ];

      if (transformedMarkdown.trim()) {
        sections.push(transformedMarkdown.trim());
      }

      if (enricherOutput) {
        sections.push(enricherOutput.trimEnd());
      }

      fs.writeFileSync(targetFile, `${sections.join("\n")}\n`, "utf8");

      const routePath = moduleDir === "." ? "/" : `/${moduleDir}/`;
      links.push(`- [${title}](${routePath})`);
      pages.push({
        title,
        sourcePath: file.relativePath,
        routePath,
        outputPath: normalizeGlobPath(path.relative(configDir, targetFile))
      });
    }

    if (config.render.generateIndex) {
      const indexDocument = createIndexDocument(config, links);
      fs.writeFileSync(path.join(contentDir, "index.md"), indexDocument, "utf8");
    }

    const metadata = {
      generatedAt: new Date().toISOString(),
      source: config.source,
      site: config.site,
      pageCount: pages.length,
      pages
    };

    fs.writeFileSync(metadataFile, `${JSON.stringify(metadata, null, 2)}\n`, "utf8");

    return {
      pageCount: pages.length,
      contentDir,
      metadataFile,
      pages
    };
  } finally {
    source.cleanup();
  }
}
