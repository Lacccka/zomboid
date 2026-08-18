#!/usr/bin/env python3
"""Fail when an upstream file invalidates an LCC compatibility contract."""

from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "tools" / "compat_contracts.json"


def git_blob_sha(data: bytes) -> str:
    header = f"blob {len(data)}\0".encode("ascii")
    return hashlib.sha1(header + data).hexdigest()


def load_text(path: Path) -> tuple[bytes, str]:
    data = path.read_bytes()
    return data, data.decode("utf-8")


def fail(errors: list[str], contract_id: str, message: str) -> None:
    errors.append(f"[{contract_id}] {message}")


def audit_contract(contract: dict[str, object], errors: list[str]) -> None:
    contract_id = str(contract.get("id") or "unnamed")
    kind = str(contract.get("kind") or "unknown")
    upstream_path = ROOT / str(contract.get("upstream_path") or "")
    patch_path = ROOT / str(contract.get("patch_path") or "")

    if not upstream_path.is_file():
        fail(errors, contract_id, f"upstream file is missing: {upstream_path.relative_to(ROOT)}")
        return
    if not patch_path.is_file():
        fail(errors, contract_id, f"patch file is missing: {patch_path.relative_to(ROOT)}")
        return

    upstream_bytes, upstream_text = load_text(upstream_path)
    patch_bytes, patch_text = load_text(patch_path)
    actual_sha = git_blob_sha(upstream_bytes)
    expected_sha = contract.get("expected_upstream_blob_sha")

    if expected_sha and actual_sha != str(expected_sha):
        fail(
            errors,
            contract_id,
            "upstream blob changed: "
            f"expected {expected_sha}, got {actual_sha}. Manual compatibility review required.",
        )

    for fragment in contract.get("required_upstream_fragments", []):
        if str(fragment) not in upstream_text:
            fail(errors, contract_id, f"upstream contract fragment disappeared: {fragment!r}")

    for fragment in contract.get("required_patch_fragments", []):
        if str(fragment) not in patch_text:
            fail(errors, contract_id, f"patch safety fragment disappeared: {fragment!r}")

    if kind == "full-override" and git_blob_sha(patch_bytes) == actual_sha:
        fail(
            errors,
            contract_id,
            "full override is byte-identical to upstream; remove the override or refresh the contract",
        )

    print(f"[compat-contract] OK {contract_id} ({kind}) upstream={actual_sha}")


def main() -> int:
    try:
        manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        print(f"[compat-contract] ERROR cannot read {MANIFEST}: {exc}", file=sys.stderr)
        return 2

    if manifest.get("schema_version") != 1:
        print("[compat-contract] ERROR unsupported schema_version", file=sys.stderr)
        return 2

    contracts = manifest.get("contracts")
    if not isinstance(contracts, list) or not contracts:
        print("[compat-contract] ERROR manifest has no contracts", file=sys.stderr)
        return 2

    errors: list[str] = []
    for contract in contracts:
        if not isinstance(contract, dict):
            errors.append("[manifest] contract entry is not an object")
            continue
        audit_contract(contract, errors)

    if errors:
        print("\n[compat-contract] FAILED", file=sys.stderr)
        for error in errors:
            print(f"  - {error}", file=sys.stderr)
        return 1

    print(f"\n[compat-contract] PASS: {len(contracts)} contract(s) verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
