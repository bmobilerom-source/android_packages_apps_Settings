#!/usr/bin/env bash
#
# open-custom-dashboard-branding.sh
#
# Portable helper saved in the Settings repo; run from any PC that has this repo
# cloned and Cursor or VS Code CLI on PATH.
#
# Opens every file you typically edit for Custom Dashboard / vendor white-label.
#
# Usage:
#   ./scripts/open-custom-dashboard-branding.sh
#   ./scripts/open-custom-dashboard-branding.sh --list
#   ./scripts/open-custom-dashboard-branding.sh --customer acme
#   ./scripts/open-custom-dashboard-branding.sh --customer acme --overlay /path/to/vendor/acme/overlay/packages/apps/Settings
#   EDITOR_CMD=cursor ./scripts/open-custom-dashboard-branding.sh
#
# Requirements: bash, git (optional), cursor OR code on PATH
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT=""
CUSTOMER=""
OVERLAY_ROOT=""
INIT_OVERLAY=0
LIST_ONLY=0
EDITOR_CMD="${EDITOR_CMD:-}"

usage() {
    cat <<'EOF'
Open Custom Dashboard branding files in Cursor or VS Code.

Options:
  -h, --help              Show this help
  -l, --list              Print paths only (do not open editor)
  -c, --customer NAME     Customer slug (e.g. acme, riverdale)
  -o, --overlay PATH      Vendor overlay Settings root, e.g.:
                            vendor/acme/overlay/packages/apps/Settings
                          Creates res/values/customer_strings.xml when used with --customer
  --init-overlay          With --customer: write template overlay under repo
                          vendor-overlays/<customer>/packages/apps/Settings/...
                          (staging copy — copy to your ROM vendor tree)

Environment:
  EDITOR_CMD=cursor|code   Force editor (auto-detect if unset)

Examples:
  ./scripts/open-custom-dashboard-branding.sh
  ./scripts/open-custom-dashboard-branding.sh --customer riverdale --init-overlay
  ./scripts/open-custom-dashboard-branding.sh -c acme -o ~/rom/vendor/acme/overlay/packages/apps/Settings
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help) usage; exit 0 ;;
        -l|--list) LIST_ONLY=1; shift ;;
        -c|--customer) CUSTOMER="${2:-}"; shift 2 ;;
        -o|--overlay) OVERLAY_ROOT="${2:-}"; shift 2 ;;
        --init-overlay) INIT_OVERLAY=1; shift ;;
        *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    esac
done

find_repo_root() {
    if git -C "$SCRIPT_DIR" rev-parse --show-toplevel >/dev/null 2>&1; then
        REPO_ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel)"
        return 0
    fi
    # Fallback: scripts/ is one level below repo root
    REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
}

find_editor() {
    if [[ -n "$EDITOR_CMD" ]]; then
        if command -v "$EDITOR_CMD" >/dev/null 2>&1; then
            echo "$EDITOR_CMD"
            return 0
        fi
        echo "EDITOR_CMD=$EDITOR_CMD not found on PATH" >&2
        return 1
    fi
    if command -v cursor >/dev/null 2>&1; then
        echo "cursor"
        return 0
    fi
    if command -v code >/dev/null 2>&1; then
        echo "code"
        return 0
    fi
    echo "Neither 'cursor' nor 'code' found on PATH." >&2
    echo "Install Cursor/VS Code shell command, or set EDITOR_CMD=cursor|code" >&2
    return 1
}

substitute_template() {
    local src="$1"
    local dest="$2"
    local name="${CUSTOMER:-customer}"
    local title
    title="$(echo "$name" | sed 's/[_-]/ /g' | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) tolower(substr($i,2)); print}')"
    sed -e "s/CUSTOMER_NAME/${title}/g" \
        -e "s/CUSTOMER_TAGLINE/Built for ${title}/g" \
        "$src" > "$dest"
}

write_overlay_template() {
    local base="$1"
    local values_dir="${base}/res/values"
    local dest="${values_dir}/customer_strings.xml"
    local template="${SCRIPT_DIR}/templates/customer_overlay_strings.xml.template"

    if [[ ! -f "$template" ]]; then
        echo "Template missing: $template" >&2
        return 1
    fi
    mkdir -p "$values_dir"
    if [[ -f "$dest" ]]; then
        echo "Overlay already exists (skipped): $dest"
    else
        substitute_template "$template" "$dest"
        echo "Created: $dest"
    fi
}

find_repo_root

# Files to open in editor (relative to Settings repo root)
REPO_FILES=(
    "docs/custom-dashboard/README.md"
    "docs/custom-dashboard/customizable-strings.md"
    "docs/custom-dashboard/customer-branding.md"
    "res/values/system_basic_strings.xml"
    "res/values/kidssafe_strings.xml"
    "res/values/banana_strings.xml"
    "res/values/strings_homepage_widgets.xml"
    "res/values/arrays.xml"
    "res/values/colors.xml"
    "res/layout/CustomerOS_header.xml"
    "res/xml/top_level_settings_custom_v2.xml"
    "res/xml/custom_dashboard_settings.xml"
    "res/xml/yr_custom_dashboard_settings.xml"
    "res/xml/kidssafe_dashboard_settings.xml"
    "res/xml/bmobile_dashboard_settings.xml"
)

FILES=()

for rel in "${REPO_FILES[@]}"; do
    abs="${REPO_ROOT}/${rel}"
    if [[ -f "$abs" ]]; then
        FILES+=("$abs")
    else
        echo "Warning: missing $rel" >&2
    fi
done

if [[ -n "$CUSTOMER" && "$INIT_OVERLAY" -eq 1 ]]; then
    staging="${REPO_ROOT}/vendor-overlays/${CUSTOMER}/packages/apps/Settings"
    write_overlay_template "$staging"
    overlay_file="${staging}/res/values/customer_strings.xml"
    if [[ -f "$overlay_file" ]]; then
        FILES+=("$overlay_file")
    fi
fi

if [[ -n "$CUSTOMER" && -n "$OVERLAY_ROOT" ]]; then
    OVERLAY_ROOT="$(cd "$OVERLAY_ROOT" 2>/dev/null && pwd || echo "$OVERLAY_ROOT")"
    write_overlay_template "$OVERLAY_ROOT"
    overlay_file="${OVERLAY_ROOT}/res/values/customer_strings.xml"
    if [[ -f "$overlay_file" ]]; then
        FILES+=("$overlay_file")
    fi
elif [[ -n "$OVERLAY_ROOT" ]]; then
    overlay_file="${OVERLAY_ROOT}/res/values/customer_strings.xml"
    if [[ -f "$overlay_file" ]]; then
        FILES+=("$overlay_file")
    else
        echo "Note: no overlay at $overlay_file (use --customer to create)" >&2
    fi
fi

echo "Settings repo: $REPO_ROOT"
echo "Files (${#FILES[@]}):"
for f in "${FILES[@]}"; do
    echo "  $f"
done

if [[ "$LIST_ONLY" -eq 1 ]]; then
    exit 0
fi

EDITOR="$(find_editor)"
echo "Opening in: $EDITOR"
# Single window, all files as tabs
"$EDITOR" "${FILES[@]}"
