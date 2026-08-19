#!/usr/bin/env bash
#
###############################################################################
# Lacccka B42.20 Compatibility Patch - Linux dedicated server launcher
#
# JVM memory options remain in ProjectZomboid64.json.
# JVM diagnostic logs are stored in logs/jvm/.
#
# B42.20 can lowercase mod/AnimSets path components while resolving XML
# x_extends. Windows normally hides that defect because its filesystem is
# case-insensitive; Linux does not. Before Project Zomboid starts, this launcher
# creates safe lowercase symlink aliases for mods where the issue has been
# reproduced in server logs.
###############################################################################

INSTDIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)" || exit 1
cd "${INSTDIR}" || exit 1

JAVA="${INSTDIR}/jre64/bin/java"
SERVER="${INSTDIR}/ProjectZomboid64"
WORKSHOP_ROOT="${INSTDIR}/steamapps/workshop/content/108600"
JVM_LOG_DIR="${INSTDIR}/logs/jvm"

ensure_log_directories() {
    if ! mkdir -p -- "${JVM_LOG_DIR}"; then
        echo "[LCC][Linux][ERROR] Could not create JVM log directory: ${JVM_LOG_DIR}"
        return 1
    fi

    if [[ ! -w "${JVM_LOG_DIR}" ]]; then
        echo "[LCC][Linux][ERROR] JVM log directory is not writable: ${JVM_LOG_DIR}"
        return 1
    fi
}

ensure_case_alias() {
    local alias_path="$1"
    local target_path="$2"
    local relative_target="$3"

    if [[ -L "${alias_path}" ]]; then
        local alias_real target_real
        alias_real="$(readlink -f -- "${alias_path}" 2>/dev/null || true)"
        target_real="$(readlink -f -- "${target_path}" 2>/dev/null || true)"

        if [[ -n "${alias_real}" && -n "${target_real}" && "${alias_real}" == "${target_real}" ]]; then
            return 0
        fi

        echo "[LCC][Linux][ERROR] Conflicting symlink: ${alias_path}"
        return 1
    fi

    if [[ -e "${alias_path}" ]]; then
        if [[ "${alias_path}" -ef "${target_path}" ]]; then
            return 0
        fi

        echo "[LCC][Linux][ERROR] Case alias already exists and points elsewhere: ${alias_path}"
        echo "[LCC][Linux][ERROR] Expected target: ${target_path}"
        return 1
    fi

    if ! ln -s -- "${relative_target}" "${alias_path}"; then
        echo "[LCC][Linux][ERROR] Could not create case alias: ${alias_path} -> ${relative_target}"
        return 1
    fi

    echo "[LCC][Linux][OK] ${alias_path} -> ${relative_target}"
}

find_workshop_mod_dir() {
    local mod_name="$1"
    local matches=()

    shopt -s nullglob
    matches=("${WORKSHOP_ROOT}"/*/mods/"${mod_name}")
    shopt -u nullglob

    if (( ${#matches[@]} == 0 )); then
        return 1
    fi

    if (( ${#matches[@]} > 1 )); then
        echo "[LCC][Linux][ERROR] Multiple Workshop copies found for mod '${mod_name}':" >&2
        printf '  %s\n' "${matches[@]}" >&2
        return 2
    fi

    printf '%s\n' "${matches[0]}"
}

ensure_linux_mod_case_compat() {
    local mod_name="$1"
    local mod_dir
    local find_rc

    mod_dir="$(find_workshop_mod_dir "${mod_name}")"
    find_rc=$?

    if (( find_rc != 0 )); then
        if (( find_rc == 2 )); then
            return 1
        fi
        echo "[LCC][Linux][WARN] Workshop mod not installed yet: ${mod_name}"
        return 0
    fi

    local mods_dir
    local lowercase_name
    mods_dir="$(dirname -- "${mod_dir}")"
    lowercase_name="${mod_name,,}"

    # The B42.20 XML resolver can lowercase the mod directory itself.
    if [[ "${lowercase_name}" != "${mod_name}" ]]; then
        ensure_case_alias \
            "${mods_dir}/${lowercase_name}" \
            "${mod_dir}" \
            "${mod_name}" || return 1
    fi

    # Affected mods may keep AnimSets in common/, 42/, or the root media tree.
    # Create aliases only for paths that actually exist in the installed mod.
    local media_dir
    local found_animsets=false
    local media_candidates=(
        "${mod_dir}/common/media"
        "${mod_dir}/42/media"
        "${mod_dir}/media"
    )

    for media_dir in "${media_candidates[@]}"; do
        if [[ -d "${media_dir}/AnimSets" ]]; then
            found_animsets=true
            ensure_case_alias \
                "${media_dir}/animsets" \
                "${media_dir}/AnimSets" \
                "AnimSets" || return 1
        fi
    done

    if [[ "${found_animsets}" != true ]]; then
        echo "[LCC][Linux][WARN] No AnimSets directory found for ${mod_name}; no AnimSets alias was needed."
    fi
}

ensure_linux_case_compatibility() {
    [[ "$(uname -s 2>/dev/null || true)" == "Linux" ]] || return 0

    # Only mods for which B42.20 lowercase XML inheritance failures have been
    # reproduced in our Linux dedicated-server logs belong in this list.
    local affected_mods=(
        "Bandits"
        "Lifestyle"
        "Escape from Kentucky4215"
        "tsarslib"
    )

    local mod_name
    for mod_name in "${affected_mods[@]}"; do
        ensure_linux_mod_case_compat "${mod_name}" || return 1
    done
}

if ! "${JAVA}" -version >/dev/null 2>&1; then
    echo "[ERROR] Only 64bit is supported"
    echo "[ERROR] Bundled Java runtime could not be started: ${JAVA}"
    exit 1
fi

ensure_log_directories || exit 1
ensure_linux_case_compatibility || exit 1

export PATH="${INSTDIR}/jre64/bin:${PATH}"
export LD_LIBRARY_PATH="${INSTDIR}/linux64:${INSTDIR}:${INSTDIR}/jre64/lib/amd64${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}}"
export LD_PRELOAD="${LD_PRELOAD:+${LD_PRELOAD}:}libjsig.so"

echo "============================================================"
echo " Project Zomboid Dedicated Server"
echo " Directory:   ${INSTDIR}"
echo " JVM logs:    ${JVM_LOG_DIR}"
echo " Started:     $(date '+%Y-%m-%d %H:%M:%S')"
echo " Server args: $*"
echo "============================================================"

exec "${SERVER}" "$@"
