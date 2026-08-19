#!/usr/bin/env bash
#
###############################################################################
# Lacccka B42.20 Compatibility Patch - Linux dedicated server launcher
#
# JVM memory options remain in ProjectZomboid64.json.
# JVM diagnostic logs are stored in logs/jvm/.
#
# B42.20 can lowercase mod/AnimSets paths (including XML filenames) while
# resolving x_extends. Windows normally hides this because its filesystem is
# case-insensitive; Linux does not. This launcher creates safe lowercase symlink
# aliases for confirmed affected Workshop mods and keeps them alive while PZ
# performs its Workshop startup pass.
###############################################################################

INSTDIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)" || exit 1
cd "${INSTDIR}" || exit 1

JAVA="${INSTDIR}/jre64/bin/java"
SERVER="${INSTDIR}/ProjectZomboid64"
WORKSHOP_ROOT="${INSTDIR}/steamapps/workshop/content/108600"
JVM_LOG_DIR="${INSTDIR}/logs/jvm"
LAUNCHER_LOG="${JVM_LOG_DIR}/launcher.log"
CASE_ALIAS_KEEPER_SECONDS=180
CASE_ALIAS_KEEPER_INTERVAL=1

lcc_log() {
    local level="$1"
    shift
    local line
    line="$(date '+%Y-%m-%d %H:%M:%S') [LCC][Linux][${level}] $*"
    printf '%s\n' "${line}"
    if [[ -d "${JVM_LOG_DIR}" ]]; then
        printf '%s\n' "${line}" >> "${LAUNCHER_LOG}" 2>/dev/null || true
    fi
}

ensure_runtime_directories() {
    if ! mkdir -p -- "${JVM_LOG_DIR}"; then
        echo "[LCC][Linux][ERROR] Could not create JVM log directory: ${JVM_LOG_DIR}"
        return 1
    fi

    if [[ ! -w "${JVM_LOG_DIR}" ]]; then
        echo "[LCC][Linux][ERROR] JVM log directory is not writable: ${JVM_LOG_DIR}"
        return 1
    fi

    # PZ registers a watcher for this path on dedicated servers. An empty
    # directory is enough to avoid a noisy NoSuchFileException at startup.
    if [[ -n "${HOME:-}" ]]; then
        mkdir -p -- "${HOME}/Zomboid/mods" || {
            lcc_log WARN "Could not create optional local-mod watcher directory: ${HOME}/Zomboid/mods"
        }
    fi
}

CASE_ALIAS_CREATED=0

ensure_case_alias() {
    local alias_path="$1"
    local target_path="$2"
    local relative_target="$3"
    local quiet_create="${4:-false}"

    CASE_ALIAS_CREATED=0

    if [[ -L "${alias_path}" ]]; then
        local alias_real target_real
        alias_real="$(readlink -f -- "${alias_path}" 2>/dev/null || true)"
        target_real="$(readlink -f -- "${target_path}" 2>/dev/null || true)"

        if [[ -n "${alias_real}" && -n "${target_real}" && "${alias_real}" == "${target_real}" ]]; then
            return 0
        fi

        lcc_log ERROR "Conflicting symlink: ${alias_path}"
        return 1
    fi

    if [[ -e "${alias_path}" ]]; then
        if [[ "${alias_path}" -ef "${target_path}" ]]; then
            return 0
        fi

        lcc_log ERROR "Case alias already exists and points elsewhere: ${alias_path}"
        lcc_log ERROR "Expected target: ${target_path}"
        return 1
    fi

    if ! ln -s -- "${relative_target}" "${alias_path}"; then
        lcc_log ERROR "Could not create case alias: ${alias_path} -> ${relative_target}"
        return 1
    fi

    CASE_ALIAS_CREATED=1
    if [[ "${quiet_create}" != true ]]; then
        lcc_log OK "${alias_path} -> ${relative_target}"
    fi
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
        lcc_log ERROR "Multiple Workshop copies found for mod '${mod_name}'" >&2
        printf '  %s\n' "${matches[@]}" >&2
        return 2
    fi

    printf '%s\n' "${matches[0]}"
}

ensure_casefold_tree_aliases() {
    local tree_root="$1"
    local created=0
    local path base lower alias_path

    while IFS= read -r -d '' path; do
        [[ "${path}" == "${tree_root}" ]] && continue

        base="$(basename -- "${path}")"
        lower="${base,,}"

        [[ "${base}" == "${lower}" ]] && continue

        alias_path="$(dirname -- "${path}")/${lower}"
        ensure_case_alias "${alias_path}" "${path}" "${base}" true || return 1

        if (( CASE_ALIAS_CREATED == 1 )); then
            ((created += 1))
        fi
    done < <(find "${tree_root}" -depth \( -type d -o -type f \) -print0)

    if (( created > 0 )); then
        lcc_log OK "Created ${created} lowercase entry aliases under ${tree_root}"
    fi
}

ensure_linux_mod_case_compat() {
    local mod_name="$1"
    local phase="${2:-preflight}"
    local quiet_missing="${3:-false}"
    local mod_dir find_rc

    mod_dir="$(find_workshop_mod_dir "${mod_name}")"
    find_rc=$?

    if (( find_rc != 0 )); then
        if (( find_rc == 2 )); then
            return 1
        fi
        if [[ "${quiet_missing}" != true ]]; then
            lcc_log WARN "Workshop mod not installed yet: ${mod_name}"
        fi
        return 0
    fi

    local mods_dir lowercase_name
    mods_dir="$(dirname -- "${mod_dir}")"
    lowercase_name="${mod_name,,}"

    # The B42.20 XML resolver can lowercase the mod directory itself.
    if [[ "${lowercase_name}" != "${mod_name}" ]]; then
        ensure_case_alias \
            "${mods_dir}/${lowercase_name}" \
            "${mod_dir}" \
            "${mod_name}" || return 1
    fi

    local media_dir
    local found_animsets=false
    local root_alias_created=0
    local media_candidates=(
        "${mod_dir}/common/media"
        "${mod_dir}/42/media"
        "${mod_dir}/42.20/media"
        "${mod_dir}/media"
    )

    for media_dir in "${media_candidates[@]}"; do
        if [[ ! -d "${media_dir}/AnimSets" ]]; then
            continue
        fi

        found_animsets=true

        ensure_case_alias \
            "${media_dir}/animsets" \
            "${media_dir}/AnimSets" \
            "AnimSets" || return 1

        root_alias_created="${CASE_ALIAS_CREATED}"

        # Lifestyle and some B42 content also have mixed-case XML filenames.
        # B42.20 may lowercase the filename in x_extends, not just directories.
        # Populate lowercase aliases for every mixed-case entry. On the initial
        # preflight we always verify the full tree; during the keeper loop this
        # is repeated only when Steam removed/recreated the root alias.
        if [[ "${phase}" == "preflight" || "${root_alias_created}" == "1" ]]; then
            ensure_casefold_tree_aliases "${media_dir}/AnimSets" || return 1
        fi
    done

    if [[ "${found_animsets}" != true && "${quiet_missing}" != true ]]; then
        lcc_log WARN "No AnimSets directory found for ${mod_name}; no AnimSets aliases were needed."
    fi
}

ensure_linux_case_compatibility() {
    local phase="${1:-preflight}"
    local quiet_missing="${2:-false}"

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
        ensure_linux_mod_case_compat "${mod_name}" "${phase}" "${quiet_missing}" || return 1
    done
}

start_case_alias_keeper() {
    [[ "$(uname -s 2>/dev/null || true)" == "Linux" ]] || return 0

    (
        local elapsed=0

        # Project Zomboid performs its own Workshop pass after the launcher has
        # started. If Steam refreshes a mod directory, symlinks inside it can be
        # removed. Recreate only missing aliases during the startup window.
        while (( elapsed < CASE_ALIAS_KEEPER_SECONDS )); do
            sleep "${CASE_ALIAS_KEEPER_INTERVAL}"
            elapsed=$((elapsed + CASE_ALIAS_KEEPER_INTERVAL))
            ensure_linux_case_compatibility "keeper" true || {
                lcc_log WARN "Case-alias keeper encountered a conflict; will retry."
            }
        done

        lcc_log INFO "Case-alias keeper finished after ${CASE_ALIAS_KEEPER_SECONDS}s."
    ) &

    lcc_log INFO "Case-alias keeper started for ${CASE_ALIAS_KEEPER_SECONDS}s."
}

if ! "${JAVA}" -version >/dev/null 2>&1; then
    echo "[ERROR] Only 64bit is supported"
    echo "[ERROR] Bundled Java runtime could not be started: ${JAVA}"
    exit 1
fi

ensure_runtime_directories || exit 1

: > "${LAUNCHER_LOG}" || {
    echo "[LCC][Linux][ERROR] Could not initialize launcher log: ${LAUNCHER_LOG}"
    exit 1
}

lcc_log INFO "Starting Linux compatibility preflight."
ensure_linux_case_compatibility "preflight" false || exit 1
lcc_log OK "Linux compatibility preflight completed."

start_case_alias_keeper

export PATH="${INSTDIR}/jre64/bin:${PATH}"
export LD_LIBRARY_PATH="${INSTDIR}/linux64:${INSTDIR}:${INSTDIR}/jre64/lib/amd64${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}}"
export LD_PRELOAD="${LD_PRELOAD:+${LD_PRELOAD}:}libjsig.so"

echo "============================================================"
echo " Project Zomboid Dedicated Server"
echo " Directory:    ${INSTDIR}"
echo " JVM logs:     ${JVM_LOG_DIR}"
echo " Launcher log: ${LAUNCHER_LOG}"
echo " Started:      $(date '+%Y-%m-%d %H:%M:%S')"
echo " Server args:  $*"
echo "============================================================"

exec "${SERVER}" "$@"
