# Custom Dashboard wiki

Documentation for the BashaMobile Settings **Custom Dashboard** system: homepage layout pickers, brand-specific dashboards, and customer white-label strings.

## Contents

| Doc | Description |
|-----|-------------|
| [customizable-strings.md](customizable-strings.md) | **Start here** — every string you can rename for customers |
| [overview.md](overview.md) | Architecture, key classes, how style selection flows |
| [picker-pages.md](picker-pages.md) | Custom / YR / KidsSafe / BMobile dashboard pages |
| [style-map.md](style-map.md) | Style ID → XML layout → picker visibility |
| [customer-branding.md](customer-branding.md) | Customer OS card, logos, per-customer build workflow |
| [adb-and-defaults.md](adb-and-defaults.md) | ADB, defaults, SystemUI restart, framework keys |

## Quick customer personalization

For a white-label customer (e.g. “Acme OS” instead of “Customer OS”):

1. Edit **`res/values/system_basic_strings.xml`** — picker labels and dashboard page titles.
2. Edit **`customeros_header_title`** / **`customeros_header_summary`** — BMobile Cards homepage header.
3. Optionally edit **`res/values/banana_strings.xml`** — user profile card copy.
4. Optionally edit **`res/values/kidssafe_strings.xml`** — KidsSafe profile screen.
5. Rebuild Settings: `m Settings`.

See [customizable-strings.md](customizable-strings.md) for the full list.

## Open all branding files in Cursor / VS Code

From any machine with this repo cloned:

```bash
chmod +x scripts/open-custom-dashboard-branding.sh
./scripts/open-custom-dashboard-branding.sh
```

Optional: `--customer acme --init-overlay` creates `vendor-overlays/acme/.../customer_strings.xml` from template.

Details: [scripts/README.md](../../scripts/README.md)
