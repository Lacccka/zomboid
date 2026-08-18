#!/usr/bin/env bash
#
###############################################################################
# Lacccka B42.20 Compatibility Patch - Linux dedicated server launcher
#
# JVM memory options remain in ProjectZomboid64.json.
# This launcher also applies the Bandits Linux case-sensitivity aliases before
# Project Zomboid parses Bandits AnimSets XML inheritance.
###############################################################################

INSTDIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)" || exit 1
cd "${INSTDIR}" || exit 1

JAVA="${INSTDIR}/jre64/bin/java"
SERVER="${INSTDIR}/ProjectZomboid64"

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
        echo "[LCC][Linux][ERROR] Case alias path already exists and is not ${target_path}: ${alias_path}"
        return 1
    fi

    ln -s -- "${relative_target}" "${alias_path}"
    echo "[LCC][Linux][OK] ${alias_path} -> ${relative_target}"
}

ensure_bandits_linux_case_aliases() {
    [[ "$(uname -s 2>/dev/null || true)" == "Linux" ]] || return 0

    local workshop_mods="${INSTDIR}/steamapps/workshop/content/108600/3268487204/mods"
    local bandits="${workshop_mods}/Bandits"
    local media="${bandits}/common/media"

    if [[ ! -d "${bandits}" ]]; then
        echo "[LCC][Linux][WARN] Bandits Workshop files are not installed yet: ${bandits}"
        echo "[LCC][Linux][WARN] After Steam downloads Bandits, stop the server and start it again so the case aliases can be created before AnimSets load."
        return 0
    fi

    if [[ ! -d "${media}/AnimSets" ]]; then
        echo "[LCC][Linux][ERROR] Bandits AnimSets directory is missing: ${media}/AnimSets"
        return 1
    fi

    # B42.20 XML x_extends resolution lowercases these path components. Windows
    # is case-insensitive, while Linux is not. Keep the upstream files untouched
    # and provide lowercase aliases only on Linux.
    ensure_case_alias "${workshop_mods}/bandits" "${bandits}" "Bandits" || return 1
    ensure_case_alias "${media}/animsets" "${media}/AnimSets" "AnimSets" || return 1
}

if ! "${JAVA}" -version >/dev/null 2>&1; then
    echo "[ERROR] Only 64bit is supported"
    echo "[ERROR] Bundled Java runtime could not be started: ${JAVA}"
    exit 1
fi

ensure_bandits_linux_case_aliases || exit 1

export PATH="${INSTDIR}/jre64/bin:${PATH}"
export LD_LIBRARY_PATH="${INSTDIR}/linux64:${INSTDIR}:${INSTDIR}/jre64/lib/amd64${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}}"
export LD_PRELOAD="${LD_PRELOAD:+${LD_PRELOAD}:}libjsig.so"

echo "============================================================"
echo " Project Zomboid Dedicated Server"
echo " Directory: ${INSTDIR}"
echo " Started:   $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================================"

exec "${SERVER}" "$@"
