export function createDocusaurusConfig(xiaoeyuConfig, options = {}) {
  const site = xiaoeyuConfig.site;
  const customCss = options.customCss ?? "./custom.css";
  const editUrl = site.editUrl || site.repositoryUrl || undefined;
  const repositoryUrl = site.repositoryUrl || site.editUrl || undefined;

  return {
    title: site.title,
    tagline: site.tagline,
    future: {
      v4: true
    },
    url: site.url,
    baseUrl: site.baseUrl,
    organizationName: site.organizationName,
    projectName: site.projectName,
    onBrokenLinks: "warn",
    markdown: {
      format: "md",
      hooks: {
        onBrokenMarkdownLinks: "warn",
        onBrokenMarkdownImages: "warn"
      }
    },
    i18n: {
      defaultLocale: site.defaultLocale,
      locales: site.locales
    },
    presets: [
      [
        "classic",
        {
          docs: {
            path: "content",
            routeBasePath: "/",
            sidebarPath: "./sidebars.js",
            editUrl
          },
          blog: false,
          theme: {
            customCss
          }
        }
      ]
    ],
    themes: [
      [
        "@easyops-cn/docusaurus-search-local",
        {
          hashed: true,
          language: ["en", "zh"],
          indexDocs: true,
          indexBlog: false,
          indexPages: false,
          docsDir: "content",
          docsRouteBasePath: "/",
          explicitSearchResultPath: true,
          highlightSearchTermsOnTargetPage: true
        }
      ]
    ],
    themeConfig: {
      colorMode: {
        respectPrefersColorScheme: true
      },
      navbar: {
        title: site.title,
        items: [
          {
            type: "docSidebar",
            sidebarId: "docsSidebar",
            label: site.navbar.docsLabel,
            position: "left"
          },
          {
            type: "search",
            position: "right"
          },
          ...(repositoryUrl
            ? [
                {
                  href: repositoryUrl,
                  label: site.navbar.repositoryLabel,
                  position: "right"
                }
              ]
            : [])
        ]
      },
      docs: {
        sidebar: {
          hideable: true
        }
      },
      ...(options.themeConfig ?? {})
    }
  };
}
