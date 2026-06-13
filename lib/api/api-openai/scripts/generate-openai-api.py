#!/usr/bin/env python3
"""Generate typed Kotlin OpenAI REST contracts from openai/openai-openapi."""

from __future__ import annotations

import argparse
import keyword
import re
import subprocess
import sys
import textwrap
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import yaml


SOURCE_REPOSITORY = "https://github.com/openai/openai-openapi"
SOURCE_SPEC_URL = "https://raw.githubusercontent.com/openai/openai-openapi/master/openapi.yaml"
BASE_URL = "https://api.openai.com/v1/"
PACKAGE_NAME = "site.addzero.kcloud.api.openai"
HEADER = "// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand."

HTTP_METHODS = ("get", "post", "put", "delete", "patch")
KOTLIN_KEYWORDS = {
    "as",
    "break",
    "class",
    "continue",
    "do",
    "else",
    "false",
    "for",
    "fun",
    "if",
    "in",
    "interface",
    "is",
    "null",
    "object",
    "package",
    "return",
    "super",
    "this",
    "throw",
    "true",
    "try",
    "typealias",
    "typeof",
    "val",
    "var",
    "when",
    "while",
}


@dataclass(frozen=True)
class KotlinType:
    code: str
    nullable: bool = False

    def rendered(self, force_nullable: bool = False) -> str:
        nullable = self.nullable or force_nullable
        if nullable and not self.code.rstrip().endswith("?"):
            return f"{self.code}?"
        return self.code


@dataclass
class PropertyDef:
    original_name: str
    name: str
    type_ref: KotlinType
    required: bool
    default_value: str | None
    description: str | None


@dataclass
class ModelDef:
    name: str
    kind: str
    properties: list[PropertyDef]
    alias: KotlinType | None
    description: str | None


@dataclass
class ParameterDef:
    original_name: str
    name: str
    location: str
    type_ref: KotlinType
    required: bool


@dataclass
class OperationDef:
    function_name: str
    http_method: str
    path: str
    path_const: str
    summary: str | None
    parameters: list[ParameterDef]
    request_body: KotlinType | None
    request_body_required: bool
    response_type: KotlinType


@dataclass
class InterfaceDef:
    name: str
    operations: list[OperationDef]


def load_spec(spec_path: str | None) -> dict[str, Any]:
    if spec_path:
        with open(spec_path, "r", encoding="utf-8") as handle:
            return yaml.safe_load(handle)

    with urllib.request.urlopen(SOURCE_SPEC_URL) as response:
        return yaml.safe_load(response.read())


def current_openapi_commit() -> str:
    result = subprocess.run(
        ["git", "ls-remote", f"{SOURCE_REPOSITORY}.git", "refs/heads/master"],
        check=True,
        text=True,
        capture_output=True,
    )
    return result.stdout.split()[0]


def split_words(value: str) -> list[str]:
    value = re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", value)
    value = re.sub(r"[^A-Za-z0-9]+", " ", value)
    return [part for part in value.strip().split() if part]


def pascal_case(value: str) -> str:
    words = split_words(value)
    if not words:
        return "Value"
    return "".join(word[:1].upper() + word[1:] for word in words)


def camel_case(value: str) -> str:
    words = split_words(value)
    if not words:
        return "value"
    first = words[0][:1].lower() + words[0][1:]
    rest = "".join(word[:1].upper() + word[1:] for word in words[1:])
    return sanitize_identifier(first + rest)


def sanitize_identifier(value: str) -> str:
    if not value:
        return "value"
    value = re.sub(r"[^A-Za-z0-9_]", "_", value)
    if re.match(r"^[0-9]", value):
        value = f"value{value}"
    if value in KOTLIN_KEYWORDS or keyword.iskeyword(value):
        if value == "object":
            return "objectType"
        return f"{value}Value"
    return value


def path_const_name(path: str) -> str:
    segments = [segment for segment in path.strip("/").split("/") if segment]
    names = []
    for segment in segments:
        clean = segment.strip("{}")
        words = split_words(clean)
        names.append("_".join(word.upper() for word in words))
    return "_BY_".join(names) if names else "ROOT"


def kotlin_string(value: str) -> str:
    return '"' + value.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n") + '"'


def kdoc_block(text: str, indent: str = "") -> str:
    cleaned = re.sub(r"\s+", " ", text.replace("*/", "* /")).strip()
    if not cleaned:
        return ""
    lines = textwrap.wrap(cleaned, width=100, replace_whitespace=True, drop_whitespace=True)
    if not lines:
        lines = [cleaned]
    result = [f"{indent}/**"]
    for line in lines:
        result.append(f"{indent} * {line.rstrip()}")
    result.append(f"{indent} */")
    return "\n".join(result) + "\n"


class OpenAiGenerator:
    def __init__(self, spec: dict[str, Any], package_name: str, source_commit: str) -> None:
        self.spec = spec
        self.package_name = package_name
        self.source_commit = source_commit
        self.schemas: dict[str, dict[str, Any]] = spec.get("components", {}).get("schemas", {})
        self.models: dict[str, ModelDef] = {}
        self.model_names: set[str] = set()
        self.nullable_aliases: dict[str, bool] = {}
        self.processing: set[str] = set()

    def generate(self, output_dir: Path) -> None:
        package_dir = output_dir.joinpath(*self.package_name.split("."))
        models_dir = package_dir / "models"
        package_dir.mkdir(parents=True, exist_ok=True)
        models_dir.mkdir(parents=True, exist_ok=True)

        for kt_file in package_dir.glob("*.kt"):
            kt_file.unlink()
        for kt_file in models_dir.glob("*.kt"):
            kt_file.unlink()

        for name, schema in self.schemas.items():
            self.ensure_component_model(name, schema)

        interfaces = self.parse_interfaces()

        self.write_bodies(package_dir / "OpenAiApiBodies.kt")
        self.write_paths(package_dir / "OpenAiApiPaths.kt")
        for interface in interfaces:
            self.write_interface(package_dir / f"{interface.name}.kt", interface)
        for model in sorted(self.models.values(), key=lambda item: item.name):
            self.write_model(models_dir / f"{model.name}.kt", model)

    def ensure_component_model(self, name: str, schema: dict[str, Any]) -> KotlinType:
        model_name = pascal_case(name)
        if model_name in self.models:
            return KotlinType(model_name)
        if model_name in self.processing:
            return KotlinType(model_name)

        self.processing.add(model_name)
        self.model_names.add(model_name)
        model = self.build_model(model_name, schema)
        self.models[model_name] = model
        self.processing.remove(model_name)
        return KotlinType(model_name)

    def build_model(self, name: str, schema: dict[str, Any]) -> ModelDef:
        object_schema = self.object_schema(schema)
        if object_schema is not None:
            properties = self.collect_properties(object_schema, name)
            return ModelDef(
                name=name,
                kind="data",
                properties=properties,
                alias=None,
                description=schema.get("description") or schema.get("title"),
            )

        alias = self.map_schema(schema, name)
        self.nullable_aliases[name] = alias.nullable
        return ModelDef(
            name=name,
            kind="alias",
            properties=[],
            alias=KotlinType(alias.code, nullable=False),
            description=schema.get("description") or schema.get("title"),
        )

    def object_schema(self, schema: dict[str, Any]) -> dict[str, Any] | None:
        if schema.get("properties") or schema.get("allOf"):
            return schema
        schema_type = schema.get("type")
        if schema_type == "object" and not schema.get("additionalProperties"):
            return schema
        return None

    def collect_properties(self, schema: dict[str, Any], model_name: str) -> list[PropertyDef]:
        merged: dict[str, PropertyDef] = {}
        for required_props, prop_map in self.iter_object_properties(schema):
            for original_name, prop_schema in prop_map.items():
                if original_name in merged:
                    continue
                property_name = camel_case(original_name)
                property_type = self.map_schema(prop_schema, f"{model_name}{pascal_case(original_name)}")
                merged[original_name] = PropertyDef(
                    original_name=original_name,
                    name=property_name,
                    type_ref=property_type,
                    required=original_name in required_props,
                    default_value=self.kotlin_default(prop_schema, property_type),
                    description=prop_schema.get("description") or prop_schema.get("title"),
                )
        return list(merged.values())

    def iter_object_properties(
        self,
        schema: dict[str, Any],
    ) -> list[tuple[set[str], dict[str, dict[str, Any]]]]:
        if "$ref" in schema:
            ref_schema = self.resolve_ref(schema["$ref"])
            return self.iter_object_properties(ref_schema)

        if schema.get("allOf"):
            result: list[tuple[set[str], dict[str, dict[str, Any]]]] = []
            parent_required = set(schema.get("required") or [])
            for item in schema["allOf"]:
                for required, props in self.iter_object_properties(item):
                    result.append((parent_required | required, props))
            return result

        alternatives = self.non_null_alternatives(schema.get("oneOf") or schema.get("anyOf"))
        if len(alternatives) == 1:
            return self.iter_object_properties(alternatives[0])

        props = schema.get("properties") or {}
        required = set(schema.get("required") or [])
        return [(required, props)]

    def map_schema(self, schema: dict[str, Any] | None, suggested_name: str) -> KotlinType:
        if not schema:
            return KotlinType("JsonElement")

        schema = dict(schema)
        nullable = bool(schema.pop("nullable", False))

        schema_type = schema.get("type")
        if isinstance(schema_type, list):
            nullable = nullable or "null" in schema_type
            non_null_types = [item for item in schema_type if item != "null"]
            schema["type"] = non_null_types[0] if len(non_null_types) == 1 else None

        if "$ref" in schema:
            ref_name = pascal_case(schema["$ref"].split("/")[-1])
            ref_schema = self.schemas.get(schema["$ref"].split("/")[-1])
            if ref_schema is not None:
                self.ensure_component_model(schema["$ref"].split("/")[-1], ref_schema)
            return KotlinType(ref_name, nullable or self.nullable_aliases.get(ref_name, False))

        alternatives = self.non_null_alternatives(schema.get("oneOf") or schema.get("anyOf"))
        if alternatives:
            nullable = nullable or self.has_null_alternative(schema.get("oneOf") or schema.get("anyOf"))
            if len(alternatives) == 1:
                mapped = self.map_schema(alternatives[0], suggested_name)
                return KotlinType(mapped.code, mapped.nullable or nullable)
            primitive_types = [self.map_schema(item, suggested_name).code for item in alternatives]
            if len(set(primitive_types)) == 1 and primitive_types[0] in {
                "String",
                "Int",
                "Long",
                "Double",
                "Float",
                "Boolean",
            }:
                return KotlinType(primitive_types[0], nullable)
            return KotlinType("JsonElement", nullable)

        if schema.get("allOf"):
            inline_name = self.unique_inline_name(suggested_name)
            self.models[inline_name] = self.build_model(inline_name, schema)
            self.model_names.add(inline_name)
            return KotlinType(inline_name, nullable)

        if schema.get("enum") and not schema.get("type"):
            return KotlinType("String", nullable)

        schema_type = schema.get("type")
        if schema_type == "string":
            if schema.get("format") == "binary":
                return KotlinType("ByteArray", nullable)
            return KotlinType("String", nullable)
        if schema_type == "integer":
            if schema.get("format") in {"int64", "unixtime"}:
                return KotlinType("Long", nullable)
            return KotlinType("Int", nullable)
        if schema_type == "number":
            if schema.get("format") == "float":
                return KotlinType("Float", nullable)
            return KotlinType("Double", nullable)
        if schema_type == "boolean":
            return KotlinType("Boolean", nullable)
        if schema_type == "array":
            item_name = self.item_model_name(suggested_name)
            item_type = self.map_schema(schema.get("items"), item_name)
            return KotlinType(f"List<{item_type.code}>", nullable)
        if schema_type == "object" or schema.get("properties"):
            if schema.get("properties"):
                inline_name = self.unique_inline_name(suggested_name)
                self.models[inline_name] = self.build_model(inline_name, schema)
                self.model_names.add(inline_name)
                return KotlinType(inline_name, nullable)

            additional = schema.get("additionalProperties")
            if isinstance(additional, dict):
                value_type = self.map_schema(additional, f"{suggested_name}Value")
                return KotlinType(f"Map<String, {value_type.code}>", nullable)
            return KotlinType("Map<String, JsonElement>", nullable)

        return KotlinType("JsonElement", nullable)

    def non_null_alternatives(self, alternatives: list[dict[str, Any]] | None) -> list[dict[str, Any]]:
        if not alternatives:
            return []
        return [item for item in alternatives if item.get("type") != "null"]

    def has_null_alternative(self, alternatives: list[dict[str, Any]] | None) -> bool:
        return any(item.get("type") == "null" for item in alternatives or [])

    def item_model_name(self, suggested_name: str) -> str:
        if suggested_name.endswith("ies"):
            return suggested_name[:-3] + "y"
        if suggested_name.endswith("s") and len(suggested_name) > 1:
            return suggested_name[:-1]
        return f"{suggested_name}Item"

    def unique_inline_name(self, suggested_name: str) -> str:
        base = pascal_case(suggested_name)
        if base not in self.model_names:
            return base
        index = 2
        while f"{base}{index}" in self.model_names:
            index += 1
        return f"{base}{index}"

    def resolve_ref(self, ref: str) -> dict[str, Any]:
        name = ref.split("/")[-1]
        return self.schemas[name]

    def kotlin_default(self, schema: dict[str, Any], type_ref: KotlinType) -> str | None:
        if "default" not in schema:
            return None
        value = schema["default"]
        if value is None:
            return "null"
        if type_ref.code == "String":
            return kotlin_string(str(value))
        if type_ref.code in {"Boolean", "Int", "Long", "Double", "Float"}:
            if isinstance(value, bool):
                return str(value).lower()
            if type_ref.code == "Long":
                return f"{int(value)}L"
            if type_ref.code == "Float":
                return f"{float(value)}f"
            if type_ref.code == "Double":
                return str(float(value))
            return str(int(value))
        return None

    def parse_interfaces(self) -> list[InterfaceDef]:
        grouped: dict[str, list[OperationDef]] = {}
        for path, path_item in (self.spec.get("paths") or {}).items():
            for method in HTTP_METHODS:
                operation = path_item.get(method)
                if not isinstance(operation, dict):
                    continue
                tag = (operation.get("tags") or [self.infer_tag_from_path(path)])[0]
                interface_name = f"OpenAi{pascal_case(tag)}Api"
                operation_def = self.parse_operation(method, path, operation)
                grouped.setdefault(interface_name, []).append(operation_def)
        return [InterfaceDef(name, ops) for name, ops in sorted(grouped.items())]

    def infer_tag_from_path(self, path: str) -> str:
        clean_path = path.strip("/")
        if clean_path.startswith("organization/admin_api_keys"):
            return "AdminApiKeys"
        if clean_path.startswith("chatkit/"):
            return "Chatkit"
        if clean_path.startswith("containers"):
            return "Containers"
        if clean_path.startswith("responses/"):
            return "Responses"
        return "Default"

    def parse_operation(self, method: str, path: str, operation: dict[str, Any]) -> OperationDef:
        function_name = self.operation_function_name(method, path, operation)
        path_const = path_const_name(path)
        parameters = self.parse_parameters(operation)
        request_body, request_body_required = self.parse_request_body(function_name, operation)
        response_type = self.parse_response_type(function_name, operation)
        return OperationDef(
            function_name=function_name,
            http_method=method.upper(),
            path=path.strip("/"),
            path_const=path_const,
            summary=operation.get("summary") or operation.get("description"),
            parameters=parameters,
            request_body=request_body,
            request_body_required=request_body_required,
            response_type=response_type,
        )

    def operation_function_name(self, method: str, path: str, operation: dict[str, Any]) -> str:
        overrides = {
            ("post", "/responses/input_tokens"): "getInputTokenCounts",
            ("post", "/responses/compact"): "compactConversation",
        }
        override = overrides.get((method, path))
        if override:
            return override
        return camel_case(operation.get("operationId") or f"{method}_{path}")

    def parse_parameters(self, operation: dict[str, Any]) -> list[ParameterDef]:
        result: list[ParameterDef] = []
        used: set[str] = set()
        for param in operation.get("parameters") or []:
            location = param.get("in")
            if location not in {"path", "query", "header"}:
                continue
            original_name = param["name"]
            name = camel_case(original_name)
            while name in used:
                name = f"{name}Value"
            used.add(name)
            schema = param.get("schema") or {}
            type_ref = self.map_schema(schema, f"{pascal_case(name)}Parameter")
            result.append(
                ParameterDef(
                    original_name=original_name,
                    name=name,
                    location=location,
                    type_ref=type_ref,
                    required=bool(param.get("required")) or location == "path",
                )
            )
        return result

    def parse_request_body(self, function_name: str, operation: dict[str, Any]) -> tuple[KotlinType | None, bool]:
        request_body = operation.get("requestBody")
        if not request_body:
            return None, False
        content = request_body.get("content") or {}
        selected = self.select_request_content(content)
        if selected is None:
            return None, False
        content_type, schema = selected
        if content_type == "application/sdp":
            return KotlinType("OpenAiTextBody"), bool(request_body.get("required"))
        body_type = self.map_schema(schema, f"{pascal_case(function_name)}Request")
        return self.promote_operation_type(body_type, f"{pascal_case(function_name)}Request"), bool(request_body.get("required"))

    def parse_response_type(self, function_name: str, operation: dict[str, Any]) -> KotlinType:
        responses = operation.get("responses") or {}
        response = None
        for status in ("200", "201", "202", "204"):
            if status in responses:
                response = responses[status]
                break
        if response is None:
            response = next((value for key, value in responses.items() if str(key).startswith("2")), None)
        if response is None:
            return KotlinType("Unit")

        content = response.get("content") or {}
        selected = self.select_response_content(content)
        if selected is None:
            return KotlinType("Unit")

        content_type, schema = selected
        if content_type in {"application/octet-stream", "application/zip", "video/mp4", "image/webp"}:
            return KotlinType("OpenAiBinaryBody")
        if content_type in {"text/event-stream", "application/sdp"}:
            return KotlinType("OpenAiTextBody")
        response_type = self.map_schema(schema, f"{pascal_case(function_name)}Response")
        return self.promote_operation_type(response_type, f"{pascal_case(function_name)}Response")

    def promote_operation_type(self, type_ref: KotlinType, suggested_name: str) -> KotlinType:
        if type_ref.code != "JsonElement":
            return type_ref
        model_name = self.unique_inline_name(suggested_name)
        self.model_names.add(model_name)
        self.models[model_name] = ModelDef(
            name=model_name,
            kind="alias",
            properties=[],
            alias=type_ref,
            description="Untyped JSON payload retained because the OpenAPI schema does not expose a fixed object shape.",
        )
        return KotlinType(model_name, type_ref.nullable)

    def select_request_content(self, content: dict[str, Any]) -> tuple[str, dict[str, Any]] | None:
        for content_type in ("application/json", "multipart/form-data", "application/x-www-form-urlencoded", "application/sdp"):
            if content_type in content:
                return content_type, content[content_type].get("schema") or {}
        if content:
            content_type, media = next(iter(content.items()))
            return content_type, media.get("schema") or {}
        return None

    def select_response_content(self, content: dict[str, Any]) -> tuple[str, dict[str, Any]] | None:
        for content_type in (
            "application/json",
            "application/octet-stream",
            "application/zip",
            "video/mp4",
            "image/webp",
            "text/event-stream",
            "application/sdp",
        ):
            if content_type in content:
                return content_type, content[content_type].get("schema") or {}
        if content:
            content_type, media = next(iter(content.items()))
            return content_type, media.get("schema") or {}
        return None

    def write_bodies(self, path: Path) -> None:
        content = f"""{HEADER}
package {self.package_name}

import kotlinx.serialization.json.JsonElement

typealias OpenAiJsonValue = JsonElement
typealias OpenAiJsonObject = Map<String, JsonElement>
typealias OpenAiBinaryBody = ByteArray
typealias OpenAiTextBody = String
"""
        path.write_text(content, encoding="utf-8")

    def write_paths(self, path: Path) -> None:
        spec_version = self.spec.get("info", {}).get("version", "")
        constants = []
        for raw_path in sorted((self.spec.get("paths") or {}).keys()):
            constants.append(f"    const val {path_const_name(raw_path)} = {kotlin_string(raw_path.strip('/'))}")
        content = "\n".join(
            [
                HEADER,
                f"package {self.package_name}",
                "",
                "object OpenAiApiSpec {",
                f"    const val BASE_URL = {kotlin_string(BASE_URL)}",
                f"    const val SOURCE_REPOSITORY = {kotlin_string(SOURCE_REPOSITORY)}",
                f"    const val SOURCE_SPEC_URL = {kotlin_string(SOURCE_SPEC_URL)}",
                f"    const val SOURCE_SPEC_VERSION = {kotlin_string(str(spec_version))}",
                f"    const val SOURCE_COMMIT = {kotlin_string(self.source_commit)}",
                "}",
                "",
                "object OpenAiApiPaths {",
                *constants,
                "}",
                "",
            ]
        )
        path.write_text(content, encoding="utf-8")

    def write_interface(self, path: Path, interface: InterfaceDef) -> None:
        imports = {"de.jensklingenberg.ktorfit.http.*"}
        for operation in interface.operations:
            self.collect_model_imports(operation.response_type, imports)
            if operation.request_body is not None:
                self.collect_model_imports(operation.request_body, imports)
            for param in operation.parameters:
                self.collect_model_imports(param.type_ref, imports)

        lines = [HEADER, f"package {self.package_name}", ""]
        for item in sorted(imports):
            lines.append(f"import {item}")
        lines.extend(["", f"interface {interface.name} {{", ""])
        used_names: set[str] = set()
        for operation in interface.operations:
            function_name = operation.function_name
            while function_name in used_names:
                function_name = f"{function_name}Value"
            used_names.add(function_name)
            lines.extend(self.render_operation(operation, function_name))
            lines.append("")
        lines.append("}")
        lines.append("")
        path.write_text("\n".join(lines), encoding="utf-8")

    def render_operation(self, operation: OperationDef, function_name: str) -> list[str]:
        result: list[str] = []
        doc = operation.summary or f"REST: {operation.http_method} /{operation.path}"
        result.append(kdoc_block(f"{doc}\n\nREST: {operation.http_method} /{operation.path}", "    ").rstrip())
        result.append(f"    @{operation.http_method}(OpenAiApiPaths.{operation.path_const})")
        params: list[str] = []
        for param in operation.parameters:
            annotation = {"path": "Path", "query": "Query", "header": "Header"}[param.location]
            force_nullable = not param.required
            default = " = null" if not param.required else ""
            params.append(
                f"        @{annotation}({kotlin_string(param.original_name)}) "
                f"{param.name}: {param.type_ref.rendered(force_nullable)}{default}"
            )
        if operation.request_body is not None:
            force_nullable = not operation.request_body_required
            default = " = null" if force_nullable else ""
            params.append(f"        @Body body: {operation.request_body.rendered(force_nullable)}{default}")

        return_type = operation.response_type.rendered()
        return_suffix = "" if return_type == "Unit" else f": {return_type}"
        if params:
            result.append(f"    suspend fun {function_name}(")
            result.append(",\n".join(params))
            result.append(f"    ){return_suffix}")
        else:
            result.append(f"    suspend fun {function_name}(){return_suffix}")
        return result

    def collect_model_imports(self, type_ref: KotlinType, imports: set[str]) -> None:
        for name in re.findall(r"\b[A-Z][A-Za-z0-9_]*\b", type_ref.code):
            if name in self.model_names:
                imports.add(f"{self.package_name}.models.{name}")

    def write_model(self, path: Path, model: ModelDef) -> None:
        model_package = f"{self.package_name}.models"
        imports = {"kotlinx.serialization.Serializable"}
        if model.kind == "data":
            if any(prop.original_name != prop.name for prop in model.properties):
                imports.add("kotlinx.serialization.SerialName")
            for prop in model.properties:
                if "JsonElement" in prop.type_ref.code:
                    imports.add("kotlinx.serialization.json.JsonElement")
        elif model.alias and "JsonElement" in model.alias.code:
            imports.add("kotlinx.serialization.json.JsonElement")
        if model.kind == "data" and not model.properties:
            imports.add("kotlinx.serialization.json.JsonElement")

        lines = [HEADER, f"package {model_package}", ""]
        for item in sorted(imports):
            lines.append(f"import {item}")
        lines.append("")
        if model.description:
            lines.append(kdoc_block(model.description).rstrip())

        if model.kind == "alias":
            alias = model.alias or KotlinType("JsonElement")
            lines.append(f"typealias {model.name} = {alias.rendered()}")
            lines.append("")
            path.write_text("\n".join(lines), encoding="utf-8")
            return

        if not model.properties:
            lines.append("@Serializable")
            lines.append(f"data class {model.name}(")
            lines.append("    val value: Map<String, JsonElement> = emptyMap()")
            lines.append(")")
            lines.append("")
            path.write_text("\n".join(lines), encoding="utf-8")
            return

        lines.append("@Serializable")
        lines.append(f"data class {model.name}(")
        for index, prop in enumerate(model.properties):
            rendered = self.render_property(prop)
            suffix = "," if index < len(model.properties) - 1 else ""
            lines.append(f"{rendered}{suffix}")
        lines.append(")")
        lines.append("")
        path.write_text("\n".join(lines), encoding="utf-8")

    def render_property(self, prop: PropertyDef) -> str:
        parts: list[str] = []
        if prop.description:
            parts.append(kdoc_block(prop.description, "    ").rstrip())
        if prop.original_name != prop.name:
            parts.append(f"    @SerialName({kotlin_string(prop.original_name)})")
        force_nullable = not prop.required
        default = ""
        if prop.default_value is not None:
            default = f" = {prop.default_value}"
        elif not prop.required:
            default = " = null"
        parts.append(f"    val {prop.name}: {prop.type_ref.rendered(force_nullable)}{default}")
        return "\n".join(parts)


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate typed Kotlin OpenAI REST API contracts.")
    parser.add_argument("--spec", help="Optional local OpenAPI YAML path.")
    parser.add_argument(
        "--output",
        default="lib/api/api-openai/src/commonMain/kotlin",
        help="Kotlin source root to write into.",
    )
    args = parser.parse_args()

    spec = load_spec(args.spec)
    source_commit = current_openapi_commit()
    generator = OpenAiGenerator(spec, PACKAGE_NAME, source_commit)
    generator.generate(Path(args.output))
    return 0


if __name__ == "__main__":
    sys.exit(main())
