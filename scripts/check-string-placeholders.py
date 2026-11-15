#!/usr/bin/env python3
"""Fails if a shared-ui string resource declares a non-positional placeholder.

Compose Resources does not format strings with java.lang.String.format: it applies a
single regex, `%(\\d)\\$[ds]`, so only indexed placeholders (%1$s, %2$d) are substituted.
A bare %s or %d never matches and is rendered literally on screen.

This only covers shared-ui/composeResources. Resources under app/src/main/res go through
Android's getString(), which does use String.format, so a bare %s is valid there.
"""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

RESOURCE_DIRS = [
    "shared-ui/src/commonMain/composeResources/values",
    "shared-ui/src/commonMain/composeResources/values-es",
]

# What the Compose Resources runtime actually substitutes.
SUPPORTED_CONVERSIONS = ("s", "d")
PLACEHOLDER = re.compile(r"%(?:(\d+)\$)?([a-zA-Z])")


def iter_strings(path):
    for element in ET.parse(path).getroot():
        name = element.get("name")
        if not name:
            continue
        if element.tag == "string":
            yield name, "".join(element.itertext())
        elif element.tag == "plurals":
            for item in element:
                yield f"{name}[{item.get('quantity')}]", "".join(item.itertext())


def check(repo_root):
    failures = []
    for resource_dir in RESOURCE_DIRS:
        path = repo_root / resource_dir / "strings.xml"
        if not path.exists():
            failures.append((resource_dir, "-", f"missing file: {path}"))
            continue
        for name, text in iter_strings(path):
            # %% is a literal percent sign, not a placeholder.
            for index, conversion in PLACEHOLDER.findall(text.replace("%%", "")):
                if not index:
                    failures.append(
                        (resource_dir, name, f"non-positional %{conversion} -- use %1${conversion}")
                    )
                elif conversion not in SUPPORTED_CONVERSIONS:
                    failures.append(
                        (resource_dir, name, f"unsupported conversion %{index}${conversion}")
                    )
    return failures


def main():
    repo_root = Path(__file__).resolve().parent.parent
    failures = check(repo_root)
    if not failures:
        print("check-string-placeholders: OK")
        return 0
    print("check-string-placeholders: FAIL\n")
    for resource_dir, name, problem in failures:
        print(f"  {resource_dir}/strings.xml :: {name}\n      {problem}")
    print(
        "\nCompose Resources only substitutes indexed placeholders (regex: %(\\d)\\$[ds]).\n"
        "A bare %s or %d is rendered literally on screen."
    )
    return 1


if __name__ == "__main__":
    sys.exit(main())
