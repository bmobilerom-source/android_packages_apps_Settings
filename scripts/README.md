# Settings helper scripts

## Custom Dashboard branding editor

**`open-custom-dashboard-branding.sh`** — opens all files you edit for customer / vendor white-label in **Cursor** or **VS Code**.

### Requirements (other PC)

1. Clone this Settings repo
2. Install [Cursor](https://cursor.com) or [VS Code](https://code.visualstudio.com)
3. Enable shell command:
   - **Cursor:** Command Palette → “Shell Command: Install 'cursor' command in PATH”
   - **VS Code:** “Shell Command: Install 'code' command in PATH”
4. Run from **Git Bash**, WSL, macOS, or Linux (bash)

### Quick start

```bash
cd /path/to/android_packages_apps_Settings
chmod +x scripts/open-custom-dashboard-branding.sh
./scripts/open-custom-dashboard-branding.sh
```

### Options

```bash
# List paths without opening editor
./scripts/open-custom-dashboard-branding.sh --list

# Stage overlay template inside repo (copy to ROM vendor tree later)
./scripts/open-custom-dashboard-branding.sh --customer riverdale --init-overlay

# Create/open overlay directly in your ROM tree on the build PC
./scripts/open-custom-dashboard-branding.sh \
  --customer acme \
  --overlay ~/android/vendor/acme/overlay/packages/apps/Settings

# Force editor
EDITOR_CMD=code ./scripts/open-custom-dashboard-branding.sh
```

### What it opens

- Wiki: `docs/custom-dashboard/*.md`
- Strings: `system_basic_strings.xml`, `kidssafe_strings.xml`, `banana_strings.xml`, widgets, `arrays.xml`, `colors.xml`
- Layouts/XML: Customer OS header, picker pages, BMobile Cards homepage

Template for vendor overlay: `scripts/templates/customer_overlay_strings.xml.template`

See [docs/custom-dashboard/customer-branding.md](../docs/custom-dashboard/customer-branding.md).
