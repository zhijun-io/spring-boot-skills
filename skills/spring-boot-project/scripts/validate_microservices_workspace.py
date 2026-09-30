#!/usr/bin/env python3
"""Validate the generated shape of a Spring Boot microservices workspace."""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


SERVICE_ID = re.compile(r"^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")


def database_name(service: str) -> str:
    return service.replace("-", "_")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("workspace", type=Path)
    parser.add_argument(
        "--layout", choices=("independent-projects", "maven-reactor"), required=True
    )
    parser.add_argument("--service", action="append", dest="services", required=True)
    parser.add_argument("--compose", action="store_true")
    parser.add_argument("--taskfile", action="store_true")
    parser.add_argument("--persistence-service", action="append", default=[])
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    root = args.workspace
    errors: list[str] = []

    if not root.is_dir():
        errors.append(f"workspace does not exist: {root}")
        return report(errors)

    services = args.services
    if len(services) < 2:
        errors.append("at least two services are required")
    if len(set(services)) != len(services):
        errors.append("service identifiers must be unique")

    for service in services:
        if not SERVICE_ID.fullmatch(service):
            errors.append(f"invalid service identifier: {service}")
        service_root = root / service
        if not service_root.is_dir():
            errors.append(f"missing service directory: {service_root}")
            continue
        required = (
            service_root / "pom.xml",
            service_root / "src/main/resources/application.yaml",
            service_root / "src/test",
        )
        for path in required:
            if not path.exists():
                errors.append(f"missing {path}")
        if not (service_root / "mvnw").exists() and not (service_root / "mvnw.cmd").exists():
            errors.append(f"missing Maven Wrapper in {service_root}")
        if not any(service_root.rglob("*ArchitectureTest.java")):
            errors.append(f"missing ArchitectureTest in {service_root}")

    readme = root / "README.md"
    if not readme.exists():
        errors.append(f"missing workspace README: {readme}")

    root_pom = root / "pom.xml"
    if args.layout == "independent-projects":
        if root_pom.exists():
            errors.append("independent-projects must not have a root pom.xml")
    else:
        if not root_pom.exists():
            errors.append("maven-reactor requires a root pom.xml")
        else:
            pom = root_pom.read_text(encoding="utf-8")
            if not re.search(r"<packaging>\s*pom\s*</packaging>", pom):
                errors.append("root pom.xml must use pom packaging")
            for service in services:
                if not re.search(rf"<module>\s*{re.escape(service)}\s*</module>", pom):
                    errors.append(f"root pom.xml is missing module: {service}")

    if args.compose and not (root / "compose.yaml").exists():
        errors.append("--compose requires root compose.yaml")
    if args.taskfile and not (root / "Taskfile.yml").exists():
        errors.append("--taskfile requires root Taskfile.yml")

    for service in args.persistence_service:
        if service not in services:
            errors.append(f"persistence service is not in --service list: {service}")
        migration_dir = root / service / "src/main/resources/db/migration"
        if not migration_dir.is_dir():
            errors.append(f"missing migration directory: {migration_dir}")

    if args.persistence_service:
        init_script = root / "docker/postgres/init/001-create-databases.sql"
        if not init_script.exists():
            errors.append(f"missing PostgreSQL database initialization script: {init_script}")
        else:
            sql = init_script.read_text(encoding="utf-8")
            for service in args.persistence_service:
                db_name = database_name(service)
                if not re.search(rf"CREATE\s+DATABASE\s+[\"']?{re.escape(db_name)}[\"']?", sql, re.I):
                    errors.append(f"database initialization is missing service database: {db_name}")

    return report(errors)


def report(errors: list[str]) -> int:
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1
    print("microservices workspace structure: valid")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
