const SPECIAL_REGEX_CHARS = /[|\\{}()[\]^$+?.]/g;

function escapeRegex(text) {
  return text.replace(SPECIAL_REGEX_CHARS, "\\$&");
}

export function normalizeGlobPath(filePath) {
  return filePath.replace(/\\/g, "/").replace(/^\.\/+/, "").replace(/\/+/g, "/");
}

export function globToRegExp(pattern) {
  const normalized = normalizeGlobPath(pattern);
  let regex = "^";

  for (let index = 0; index < normalized.length; index += 1) {
    const char = normalized[index];
    const nextChar = normalized[index + 1];

    if (char === "*" && nextChar === "*") {
      const nextNextChar = normalized[index + 2];
      const isDirectoryWildcard = nextNextChar === "/";
      regex += isDirectoryWildcard ? "(?:.*/)?" : ".*";
      index += isDirectoryWildcard ? 2 : 1;
      continue;
    }

    if (char === "*") {
      regex += "[^/]*";
      continue;
    }

    regex += escapeRegex(char);
  }

  regex += "$";
  return new RegExp(regex);
}

export function matchesAnyGlob(filePath, patterns = []) {
  const normalized = normalizeGlobPath(filePath);
  return patterns.some((pattern) => globToRegExp(pattern).test(normalized));
}
