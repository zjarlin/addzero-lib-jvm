# Xiaoeyu

Xiaoeyu turns README files in a repository into a navigable Docusaurus documentation site.

## Capabilities

- Collect README files from a local repository or a remote Git repository
- Apply include, exclude, and ordered rule-based filters
- Generate `content/` pages and site metadata
- Scaffold a ready-to-run Docusaurus site
- Expose a reusable GitHub Action for content generation

## Quick start

1. Add a `xiaoeyu.config.json` file to your repository root.
2. Run `xiaoeyu scaffold-site --target docs`.
3. Run `npm install --prefix docs`.
4. Run `npm run build --prefix docs`.

## Minimal config

```json
{
  "source": {
    "type": "git",
    "repoUrl": "https://github.com/your-org/your-repo",
    "branch": "main"
  },
  "site": {
    "title": "Your Docs",
    "tagline": "Generated from README files.",
    "url": "https://your-org.github.io",
    "baseUrl": "/your-docs/",
    "organizationName": "your-org",
    "projectName": "your-docs",
    "repositoryUrl": "https://github.com/your-org/your-repo",
    "editUrl": "https://github.com/your-org/your-repo/tree/main",
    "defaultLocale": "en",
    "locales": ["en"],
    "navbar": {
      "docsLabel": "Docs",
      "repositoryLabel": "GitHub"
    }
  }
}
```

## Commands

```bash
xiaoeyu generate --config ./xiaoeyu.config.json
xiaoeyu print-docusaurus-config --config ./xiaoeyu.config.json
xiaoeyu scaffold-site --target ./docs
```

## GitHub Action

Use the packaged action from the `xiaoeyu` directory:

```yaml
- uses: your-org/your-xiaoeyu-repo/xiaoeyu@v1
  with:
    config: xiaoeyu.config.json
```
