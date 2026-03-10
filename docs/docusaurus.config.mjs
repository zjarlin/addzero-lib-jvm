import {themes as prismThemes} from 'prism-react-renderer';

const config = {
  title: '小鳄鱼',
  tagline: 'addzero-lib-jvm 自动化文档站点',
  future: {
    v4: true,
  },
  url: 'https://zjarlin.github.io',
  baseUrl: '/addzero-lib-jvm/',
  organizationName: 'zjarlin',
  projectName: 'addzero-lib-jvm',
  onBrokenLinks: 'warn',
  markdown: {
    format: 'md',
    hooks: {
      onBrokenMarkdownLinks: 'warn',
      onBrokenMarkdownImages: 'warn',
    },
  },
  i18n: {
    defaultLocale: 'zh-Hans',
    locales: ['zh-Hans'],
  },
  presets: [
    [
      'classic',
      {
        docs: {
          path: 'content',
          routeBasePath: '/',
          sidebarPath: './sidebars.js',
          editUrl: 'https://github.com/zjarlin/addzero-lib-jvm/tree/main',
        },
        blog: false,
        theme: {
          customCss: './custom.css',
        },
      },
    ],
  ],
  themes: [
    [
      '@easyops-cn/docusaurus-search-local',
      {
        hashed: true,
        language: ['en', 'zh'],
        indexDocs: true,
        indexBlog: false,
        indexPages: false,
        docsDir: 'content',
        docsRouteBasePath: '/',
        explicitSearchResultPath: true,
        highlightSearchTermsOnTargetPage: true,
      },
    ],
  ],
  themeConfig: {
    colorMode: {
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: '小鳄鱼',
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docsSidebar',
          label: '文档',
          position: 'left',
        },
        {
          type: 'search',
          position: 'right',
        },
        {
          href: 'https://github.com/zjarlin/addzero-lib-jvm',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    docs: {
      sidebar: {
        hideable: true,
      },
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  },
};

export default config;
