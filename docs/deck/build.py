#!/usr/bin/env python3
"""Generate 11-slide bilingual VylexAI investor decks (EN + DE).

Run: python3 docs/deck/build.py
Outputs:
  /tmp/vylexai_deck_en.html
  /tmp/vylexai_deck_de.html

Then PDF via headless Chrome:
  /Applications/Google\\ Chrome.app/Contents/MacOS/Google\\ Chrome \\
    --headless=new --disable-gpu --no-pdf-header-footer \\
    --print-to-pdf=~/Downloads/VylexAI_Investor_Brief_EN.pdf \\
    file:///tmp/vylexai_deck_en.html

Compliance: zero "mining" anywhere. See
docs/deck/../team/_compliance_doc reference (Linear).
"""
import base64
from pathlib import Path

LOGO_PATH = Path("/Users/niveauimmo./Developer/inngestnyth/vylexai/android/brand/vylexai-logo.svg")
LOGO_DATA = f"data:image/svg+xml;base64,{base64.b64encode(LOGO_PATH.read_bytes()).decode()}"

SLOGAN = "Powering AI with the World's Devices"

# ----------------------------- ILLUSTRATIONS -----------------------------

SMARTPHONE_SVG = '''
<svg viewBox="0 0 320 360" xmlns="http://www.w3.org/2000/svg" style="width:100%; height:100%;">
  <defs>
    <linearGradient id="phoneGrad" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#1AC8FF" stop-opacity="0.20"/>
      <stop offset="100%" stop-color="#1E6EE8" stop-opacity="0.05"/>
    </linearGradient>
    <radialGradient id="glowGrad" cx="50%" cy="40%" r="60%">
      <stop offset="0%" stop-color="#1AC8FF" stop-opacity="0.35"/>
      <stop offset="100%" stop-color="#1AC8FF" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <circle cx="160" cy="160" r="150" fill="url(#glowGrad)"/>
  <g transform="translate(248, 60)">
    <rect x="0" y="0" width="58" height="72" rx="6" fill="rgba(255,255,255,0.04)" stroke="#283249" stroke-width="1.5"/>
    <circle cx="20" cy="36" r="3" fill="#7B8AA5"/>
    <circle cx="38" cy="36" r="3" fill="#7B8AA5"/>
  </g>
  <path d="M 248 96 C 220 96, 210 130, 190 160 L 175 175"
        fill="none" stroke="#4FE3FF" stroke-width="2.5" opacity="0.85"/>
  <rect x="166" y="166" width="14" height="20" rx="3" fill="#4FE3FF"/>
  <g transform="translate(60, 48)">
    <rect x="0" y="0" width="120" height="240" rx="18" fill="rgba(17,24,38,0.95)" stroke="#283249" stroke-width="2"/>
    <rect x="6" y="14" width="108" height="200" rx="6" fill="url(#phoneGrad)"/>
    <rect x="20" y="34" width="80" height="6" rx="2" fill="#4FE3FF" opacity="0.7"/>
    <rect x="20" y="48" width="60" height="4" rx="2" fill="#7B8AA5"/>
    <rect x="20" y="68" width="80" height="44" rx="8" fill="#1AC8FF" opacity="0.10"/>
    <text x="60" y="86" font-family="Helvetica" font-size="11" fill="#4FE3FF" text-anchor="middle" font-weight="700">+0.0042 BSAI</text>
    <text x="60" y="102" font-family="Helvetica" font-size="7" fill="#7B8AA5" text-anchor="middle" letter-spacing="1.5">EARNED · LIVE</text>
    <rect x="20" y="128" width="80" height="4" rx="2" fill="#283249"/>
    <rect x="20" y="128" width="56" height="4" rx="2" fill="#FFD166"/>
    <rect x="20" y="142" width="80" height="3" rx="1.5" fill="#283249"/>
    <rect x="20" y="142" width="34" height="3" rx="1.5" fill="#2DE0A3"/>
    <circle cx="60" cy="226" r="3" fill="#283249"/>
  </g>
  <g opacity="0.85">
    <g transform="translate(40, 40)"><circle cx="0" cy="0" r="14" fill="#FFD166"/>
      <text x="0" y="4" font-family="Helvetica" font-size="9" font-weight="700" fill="#0A0F18" text-anchor="middle">B</text></g>
    <g transform="translate(20, 200)"><circle cx="0" cy="0" r="11" fill="#FFD166" opacity="0.7"/>
      <text x="0" y="3" font-family="Helvetica" font-size="7" font-weight="700" fill="#0A0F18" text-anchor="middle">B</text></g>
    <g transform="translate(220, 290)"><circle cx="0" cy="0" r="13" fill="#FFD166"/>
      <text x="0" y="4" font-family="Helvetica" font-size="8" font-weight="700" fill="#0A0F18" text-anchor="middle">B</text></g>
  </g>
</svg>
'''


def market_chart(rows, accent="#4FE3FF", bar_color="#1AC8FF"):
    out = []
    for i, (label, val, pct) in enumerate(rows):
        y = 30 + i * 56
        out.append(f'''
<text x="0" y="{y - 6}" font-family="Helvetica" font-size="11" fill="#F3F6FB" font-weight="600">{label}</text>
<text x="100%" y="{y - 6}" font-family="Helvetica" font-size="11" fill="{accent}" font-weight="700" text-anchor="end">{val}</text>
<rect x="0" y="{y}" width="100%" height="14" rx="3" fill="rgba(40, 50, 73, 0.5)"/>
<rect x="0" y="{y}" width="{pct}%" height="14" rx="3" fill="{bar_color}"/>
''')
    return f'<svg viewBox="0 0 600 270" xmlns="http://www.w3.org/2000/svg" style="width:100%; height:100%;">{"".join(out)}</svg>'


def profit_forecast(years, values, currency="€"):
    max_v = max(values) * 1.15
    n = len(values)
    pts = [(50 + i * (480 / (n - 1)), 200 - (v / max_v) * 160) for i, v in enumerate(values)]
    path = " ".join(f"{'M' if i == 0 else 'L'} {x:.1f} {y:.1f}" for i, (x, y) in enumerate(pts))
    area = f"M {pts[0][0]:.1f} 200 " + " ".join(f"L {x:.1f} {y:.1f}" for x, y in pts) + f" L {pts[-1][0]:.1f} 200 Z"
    year_labels = "".join(
        f'<text x="{50 + i * (480 / (n - 1)):.1f}" y="220" font-family="Helvetica" font-size="10" fill="#7B8AA5" text-anchor="middle">{y}</text>'
        for i, y in enumerate(years))
    value_labels = "".join(
        f'<text x="{x:.1f}" y="{y - 10:.1f}" font-family="Helvetica" font-size="10" fill="#FFD166" text-anchor="middle" font-weight="700">{currency}{v}M</text>'
        for (x, y), v in zip(pts, values))
    dots = "".join(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="4" fill="#FFD166"/>' for x, y in pts)
    grid = "".join(f'<line x1="50" y1="{40 + i * 40}" x2="530" y2="{40 + i * 40}" stroke="rgba(40,50,73,0.45)" stroke-width="0.8" stroke-dasharray="2 4"/>' for i in range(4))
    return f'''<svg viewBox="0 0 580 240" xmlns="http://www.w3.org/2000/svg" style="width:100%; height:100%;">
  <defs><linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0%" stop-color="#FFD166" stop-opacity="0.30"/>
    <stop offset="100%" stop-color="#FFD166" stop-opacity="0"/>
  </linearGradient></defs>
  {grid}<path d="{area}" fill="url(#areaGrad)"/><path d="{path}" fill="none" stroke="#FFD166" stroke-width="2.5"/>
  {dots}{value_labels}{year_labels}
  <line x1="50" y1="200" x2="530" y2="200" stroke="#283249" stroke-width="1"/>
</svg>'''


# ----------------------------- COPY -----------------------------

EN = {
    "lang": "en",
    "title": "VylexAI · Investor Brief · April 2026",
    "label": "Investor Brief · April 2026",
    "footer": {
        "company": "Dudaev Systems UG",
        "city": "Cottbus, Germany",
        "founder": "Adlan Dudaev, Founder &amp; Chief Technology Officer · adlan@vylexai.com",
        "links": "vylexai.com · github.com/vylexAI",
    },
    "s1_h": "Every device, a node of the [grad]global AI network[/].",
    "s1_lede": "VylexAI transforms everyday devices into a decentralized network for AI compute and secure data storage.",
    "s2": dict(eyebrow="The problem", h="AI compute lives inside [sun]five companies[/].",
               lede="Today's intelligence runs on a handful of hyperscaler data centers. Compute is gated, expensive, and concentrated. Scaling means more concrete and more grid — and the grid is already strained.",
               stat=[("~5", "Hyperscalers control most of global AI compute"), ("3B+", "Smartphones idle every night, fully charged"), ("$200B", "2025 spend on AI compute capex — and rising")],
               bullet_label="And the macro is aligned",
               bullets=["EU AI Act: regulatory tailwind for European AI infrastructure.", "Mobile silicon parity: NPUs run 7B-parameter models on-device.", "Federated learning matured: production-grade frameworks ready."]),
    "s3": dict(eyebrow="The solution", h="One layer, [grad]two primitives[/].",
               lede="VylexAI unifies idle phones, tablets, and PCs into a single verifiable compute layer — and uses the same fleet to hold encrypted, distributed storage.",
               cards=[("Resource 1", "AI Compute", "Spare CPU / NPU cycles run inference, federated learning, validation, microtasks, and lightweight training. Each device is paid in BSAI for verified contribution."),
                      ("Resource 2", "Decentralized Storage", "Spare device memory holds encrypted file shards, dataset shards, distributed caches, and backups. Storage providers earn BSAI per byte-day.")]),
    "s4": dict(eyebrow="How it works", h="Three steps. [grad]One network.[/]",
               cards=[("Step 01", "Install", "Download VylexAI on Android. Account or demo mode — one tap to start."),
                      ("Step 02", "Contribute", "While charging on Wi-Fi at safe temperature, the app contributes compute and encrypted storage in the background."),
                      ("Step 03", "Earn BSAI", "Each verified contribution mints BSAI. Clients submit jobs and pay BSAI; nodes earn it. The economy closes.")],
               footnote='<strong style="color:#FFD166;">Your phone earns even while you sleep.</strong> Verifiable by design — every result is cross-checked by N-way redundancy. Disagreement = no payout.'),
    "s5": dict(eyebrow="Market", h="Multi-market [grad]surface area[/].",
               lede="VylexAI sits at the intersection of five expanding markets — and is the rare project that touches all of them with the same network.",
               chart=market_chart([("AI compute · 2030 forecast", "$680B", 100), ("Cloud storage · 2027", "$220B", 32),
                                   ("Creator economy · 2027", "$170B", 25), ("Mobile monetization · 2027", "$80B", 12)]),
               footer='Plus the <strong style="color:#FFD166;">green tech</strong> overlay — every renewable-powered node earns +10–20% bonus, locking in an ESG narrative across the entire fleet.'),
    "s6": dict(eyebrow="Roadmap", h="Three phases. [grad]One network.[/]",
               phases=[("Phase 1 — now", "Live", "Mobile Network Launch", "Smartphones and tablets. AI compute + encrypted storage + BSAI rewards. Android client live, FastAPI coordinator ready, internal testing track queued."),
                       ("Phase 2 — Q3 / Q4 2026", None, "Desktop Expansion", "PCs and laptops as higher-performance nodes. Larger models, heavier workloads, more storage capacity per node."),
                       ("Phase 3 — 2027", None, "Enterprise Layer", "APIs for companies. AI training marketplace. Storage-as-a-service. Enterprise dashboards. Network monetized end-to-end.")]),
    "s7": dict(eyebrow="Tokenomics &amp; revenue trajectory", h="BSAI — fixed supply, [sun]fair launch[/].",
               stat=[("21M", "Hard cap (Bitcoin-style)"), ("8", "Decimals · 1 BSAI = 100M units"),
                     ("1.1M", "Genesis reserve · ecosystem"), ("+10–20%", "Green-energy bonus")],
               chart_caption="Projected Dudaev Systems revenue, illustrative",
               chart=profit_forecast(["Y1 2026", "Y2 2027", "Y3 2028", "Y4 2029", "Y5 2030"], [0.5, 2.5, 9, 28, 72])),
    "s8": dict(eyebrow="Traction", h="Engineering complete. [grad]Production-ready.[/]",
               rows=[("Android client", "Kotlin · Jetpack Compose · Hilt · WorkManager · MobileNet on-device"),
                     ("FastAPI coordinator", "SQLAlchemy 2 · Postgres 16 · JWT · all 10 endpoints live"),
                     ("Real on-device AI", "MobileNet V1 quant · &lt; 30 ms inference on mid-range Android"),
                     ("Play Integrity attestation", "Token attached to every heartbeat · backend verifier queued"),
                     ("vylexai.com", "Next.js 16 · 5 locales (EN / DE / RU / ZH / HI) · live on Vercel · TLS"),
                     ("Privacy / Terms / Impressum", "§ 5 TMG · § 55 RStV · GDPR-compliant"),
                     ("Release pipeline", "Signed AAB · Hetzner deploy · GitHub Actions · monitoring queued"),
                     ("BSAI ledger", "Schema landed · 21M cap, 8 decimals · genesis distribution designed")]),
    "s9": dict(eyebrow="Team", h="Built in [sun]Germany[/].",
               lede="Engineering across Cottbus, Berlin, and Frankfurt. Inside the EU AI Act framework. Supported by Startup Lausitz and Brandenburgische Technische Universität Cottbus-Senftenberg.",
               team=[("Adlan Dudaev", "Founder · Chief Technology Officer · Cottbus"),
                     ("Ananya Sai Tippani", "AI &amp; ML Engineer · Cottbus"),
                     ("Hammad Hassan Bajwa", "Software Engineer (AI / Backend) · Frankfurt"),
                     ("Riyabrata Mondal", "Lead Full-Stack &amp; Platform · Cottbus"),
                     ("Vishva Hirenkumar Jani", "AI Infrastructure &amp; Backend · Cottbus")]),
    "s10": dict(eyebrow="The ask", h="Pre-seed round to [sun]light up the network[/].",
                amount="€500K", amount_label="target",
                amount_sub="12-month runway covering production launch, first 100K nodes, audit, and growth.",
                use_label="Use of capital",
                uses=["Production coordinator infrastructure (Hetzner, monitoring, backups, Play Integrity verifier)",
                      "Security audit + penetration test before public launch",
                      "Marketing &amp; PR — first 100K provider nodes via Android beta",
                      "YouTube investor demo &amp; content production at BTU Cottbus",
                      "UG legal completion (HRB registration, GDPR audit, EU AI Act compliance review)",
                      "Reserve to seed early BSAI rewards for beta participants"]),
    "s11": dict(
        eyebrow="Comparison · AI infrastructure models",
        h="VylexAI vs. OpenAI Data Center vs. Google TPU.",
        lede="Three approaches to scaling AI compute. VylexAI wins on cost, decentralization, and sovereignty.",
        archs=[
            ("VylexAI", "Decentralized global devices", "decentralized"),
            ("OpenAI DC", "Centralized GPU clusters", "centralized"),
            ("Google TPU", "Centralized TPU clusters", "centralized"),
        ],
        headers=["", "VylexAI", "OpenAI DC", "Google TPU"],
        rows=[
            ("Core compute", "Smartphones, PCs, laptops, repurposed GPU farms", "NVIDIA GPUs", "Proprietary TPU ASICs"),
            ("CAPEX", "Low (existing hardware)", "Very High", "Very High"),
            ("Cost per marginal node", "Very Low", "High", "High"),
            ("Supply-chain risk", "Low", "High (GPU shortage)", "High (silicon)"),
            ("Mass inference", "Excellent", "Good", "Good"),
            ("Edge AI", "Excellent", "Weak", "Weak"),
            ("User monetization", "Yes — participants earn rewards", "No", "No"),
            ("Data sovereignty", "High — local storage &amp; compute", "Provider-limited", "Provider-limited"),
        ],
        footnote="Strategic comparison, not a universal claim. Each system is strongest in its own use case.",
    ),
}

DE = dict(EN)  # start as copy
DE["lang"] = "de"
DE["label"] = "Investor Brief · April 2026"
DE["footer"] = {
    "company": "Dudaev Systems UG",
    "city": "Cottbus, Deutschland",
    "founder": "Adlan Dudaev, Gründer &amp; Chief Technology Officer · adlan@vylexai.com",
    "links": "vylexai.com · github.com/vylexAI",
}
DE["s1_h"] = "Jedes Gerät — ein Knoten des [grad]globalen KI-Netzwerks[/]."
DE["s1_lede"] = "VylexAI macht aus alltäglichen Geräten ein dezentrales Netzwerk für KI-Rechenleistung und sichere Datenspeicherung."
DE["s2"] = dict(eyebrow="Das Problem", h="KI-Rechenleistung liegt in den Händen von [sun]fünf Konzernen[/].",
                lede="Heutige Intelligenz läuft in einer Handvoll Hyperscaler-Rechenzentren. Compute ist begrenzt, teuer und konzentriert. Skalierung bedeutet mehr Beton und mehr Stromnetz — und das Netz ist bereits überlastet.",
                stat=[("~5", "Hyperscaler kontrollieren den Großteil der weltweiten KI-Rechenleistung"),
                      ("3 Mrd.+", "Smartphones liegen jede Nacht aufgeladen ungenutzt herum"),
                      ("$200 Mrd.", "Investitionen 2025 in KI-Compute — Tendenz steigend")],
                bullet_label="Und der Markt-Trend stimmt",
                bullets=["EU AI Act: regulatorischer Rückenwind für europäische KI-Infrastruktur.",
                         "Mobile-Silizium hat aufgeholt: NPUs führen 7B-Parameter-Modelle on-device aus.",
                         "Federated Learning ist reif: produktionsreife Frameworks stehen bereit."])
DE["s3"] = dict(eyebrow="Die Lösung", h="Eine Schicht, [grad]zwei Primitive[/].",
                lede="VylexAI bündelt ungenutzte Smartphones, Tablets und PCs zu einer überprüfbaren Rechenschicht — und nutzt dieselbe Flotte für verschlüsselten verteilten Speicher.",
                cards=[("Ressource 1", "KI-Rechenleistung", "Freie CPU- und NPU-Zyklen führen Inferenz, Federated Learning, Validierung, Microtasks und leichtes Training aus. Jedes Gerät erhält BSAI für jeden überprüften Beitrag."),
                       ("Ressource 2", "Dezentraler Speicher", "Freier Gerätespeicher hält verschlüsselte Datei-Shards, Dataset-Shards, verteilte Caches und Backups. Storage-Anbieter verdienen BSAI pro Byte-Tag.")])
DE["s4"] = dict(eyebrow="So funktioniert es", h="Drei Schritte. [grad]Ein Netzwerk.[/]",
                cards=[("Schritt 01", "Installieren", "VylexAI für Android herunterladen. Konto oder Demo-Modus — ein Tippen genügt."),
                       ("Schritt 02", "Beitragen", "Während das Gerät bei sicherer Temperatur per WLAN lädt, leistet die App im Hintergrund Compute und verschlüsselten Speicher."),
                       ("Schritt 03", "BSAI verdienen", "Jeder überprüfte Beitrag mintet BSAI. Kunden geben Jobs auf und zahlen BSAI; Knoten verdienen sie. Der Wirtschaftskreislauf schließt sich.")],
                footnote='<strong style="color:#FFD166;">Dein Smartphone verdient, sogar wenn du schläfst.</strong> Überprüfbar by design — jedes Ergebnis wird durch N-fache Redundanz gegengeprüft. Keine Übereinstimmung = keine Auszahlung.')
DE["s5"] = dict(eyebrow="Markt", h="Mehrere Märkte. [grad]Eine Plattform.[/]",
                lede="VylexAI liegt im Schnittpunkt von fünf expandierenden Märkten — und ist das seltene Projekt, das alle mit demselben Netzwerk berührt.",
                chart=market_chart([("KI-Rechenleistung · Prognose 2030", "$680 Mrd.", 100), ("Cloud-Speicher · 2027", "$220 Mrd.", 32),
                                    ("Creator Economy · 2027", "$170 Mrd.", 25), ("Mobile Monetarisierung · 2027", "$80 Mrd.", 12)]),
                footer='Dazu kommt der <strong style="color:#FFD166;">Green-Tech-Layer</strong> — jeder Knoten mit erneuerbarer Energie verdient +10–20 % Bonus und verankert die ESG-Story auf Flotten-Ebene.')
DE["s6"] = dict(eyebrow="Roadmap", h="Drei Phasen. [grad]Ein Netzwerk.[/]",
                phases=[("Phase 1 — jetzt", "Live", "Mobile Network Launch", "Smartphones und Tablets. KI-Compute + verschlüsselter Speicher + BSAI-Rewards. Android-Client live, FastAPI-Coordinator bereit, Internal-Testing-Track in Vorbereitung."),
                        ("Phase 2 — Q3 / Q4 2026", None, "Desktop Expansion", "PCs und Laptops als leistungsstärkere Knoten. Größere Modelle, anspruchsvollere Workloads, mehr Speicherkapazität pro Knoten."),
                        ("Phase 3 — 2027", None, "Enterprise Layer", "APIs für Unternehmen. KI-Trainings-Marktplatz. Storage-as-a-Service. Enterprise-Dashboards. End-to-End monetarisiert.")])
DE["s7"] = dict(eyebrow="Tokenomics &amp; Umsatzkurve", h="BSAI — feste Supply, [sun]fairer Launch[/].",
                stat=[("21 Mio.", "Hard Cap (Bitcoin-Style)"), ("8", "Dezimalen · 1 BSAI = 100 Mio. Einheiten"),
                      ("1,1 Mio.", "Genesis-Reserve · Ökosystem"), ("+10–20 %", "Bonus für erneuerbare Energie")],
                chart_caption="Prognostizierter Umsatz von Dudaev Systems, illustrativ",
                chart=profit_forecast(["J1 2026", "J2 2027", "J3 2028", "J4 2029", "J5 2030"], [0.5, 2.5, 9, 28, 72]))
DE["s8"] = dict(eyebrow="Traction", h="Engineering fertig. [grad]Production-ready.[/]",
                rows=[("Android-Client", "Kotlin · Jetpack Compose · Hilt · WorkManager · MobileNet on-device"),
                      ("FastAPI-Coordinator", "SQLAlchemy 2 · Postgres 16 · JWT · alle 10 Endpoints live"),
                      ("Echte On-Device-KI", "MobileNet V1 quant · &lt; 30 ms Inferenz auf Mittelklasse-Android"),
                      ("Play Integrity Attestation", "Token bei jedem Heartbeat · Backend-Verifier in Vorbereitung"),
                      ("vylexai.com", "Next.js 16 · 5 Sprachen (EN / DE / RU / ZH / HI) · Vercel · TLS"),
                      ("Privacy / Terms / Impressum", "§ 5 TMG · § 55 RStV · DSGVO-konform"),
                      ("Release-Pipeline", "Signiertes AAB · Hetzner-Deploy · GitHub Actions · Monitoring queued"),
                      ("BSAI-Ledger", "Schema vorhanden · 21M Cap, 8 Dezimalen · Genesis-Distribution geplant")])
DE["s9"] = dict(eyebrow="Team", h="Made in [sun]Germany[/].",
                lede="Engineering an den Standorten Cottbus, Berlin und Frankfurt. Im Rahmen des EU AI Act. Unterstützt von Startup Lausitz und der Brandenburgischen Technischen Universität Cottbus-Senftenberg.",
                team=EN["s9"]["team"])
DE["s10"] = dict(eyebrow="Der Bedarf", h="Pre-Seed-Runde, um [sun]das Netzwerk zu zünden[/].",
                 amount="500 T€", amount_label="Ziel",
                 amount_sub="12 Monate Runway: Production-Launch, erste 100.000 Knoten, Audit und Growth.",
                 use_label="Mittelverwendung",
                 uses=["Production-Coordinator-Infrastruktur (Hetzner, Monitoring, Backups, Play Integrity Verifier)",
                       "Security-Audit &amp; Penetrationstest vor dem öffentlichen Launch",
                       "Marketing &amp; PR — erste 100.000 Provider-Knoten über die Android-Beta",
                       "YouTube-Investor-Demo &amp; Content-Produktion an der BTU Cottbus",
                       "UG-Legal-Abschluss (HRB-Eintrag, DSGVO-Audit, EU-AI-Act-Compliance)",
                       "Rücklage für frühe BSAI-Rewards an Beta-Teilnehmer"])
DE["s11"] = dict(
    eyebrow="Vergleich · KI-Infrastrukturmodelle",
    h="VylexAI vs. OpenAI Data Center vs. Google TPU.",
    lede="Drei Ansätze zur Skalierung von KI-Compute. VylexAI gewinnt bei Kosten, Dezentralisierung und Souveränität.",
    archs=[
        ("VylexAI", "Dezentrale globale Geräte", "decentralized"),
        ("OpenAI DC", "Zentrale GPU-Cluster", "centralized"),
        ("Google TPU", "Zentrale TPU-Cluster", "centralized"),
    ],
    headers=["", "VylexAI", "OpenAI DC", "Google TPU"],
    rows=[
        ("Core-Compute", "Smartphones, PCs, Laptops, umgewidmete GPU-Farmen", "NVIDIA-GPUs", "Proprietäre TPU-ASICs"),
        ("CAPEX", "Niedrig (vorhandene Hardware)", "Sehr hoch", "Sehr hoch"),
        ("Kosten pro Marginal-Knoten", "Sehr niedrig", "Hoch", "Hoch"),
        ("Lieferketten-Risiko", "Niedrig", "Hoch (GPU-Knappheit)", "Hoch (Silizium)"),
        ("Mass Inference", "Exzellent", "Gut", "Gut"),
        ("Edge AI", "Exzellent", "Schwach", "Schwach"),
        ("User-Monetarisierung", "Ja — Teilnehmer verdienen Rewards", "Nein", "Nein"),
        ("Datensouveränität", "Hoch — lokaler Speicher &amp; Compute", "Provider-limitiert", "Provider-limitiert"),
    ],
    footnote="Strategischer Vergleich, kein universeller Anspruch. Jedes System ist in seinem Kernfall am stärksten.",
)


def arch_icon(kind: str) -> str:
    """Tiny SVG showing decentralized vs centralized topology."""
    if kind == "decentralized":
        # mesh: phones + PCs scattered, connected
        return '''<svg viewBox="0 0 220 100" xmlns="http://www.w3.org/2000/svg" style="width:100%;height:90px;">
  <defs>
    <linearGradient id="iconGrad1" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#4FE3FF"/><stop offset="100%" stop-color="#1E6EE8"/>
    </linearGradient>
  </defs>
  <!-- mesh lines -->
  <g stroke="rgba(79,227,255,.35)" stroke-width="1" fill="none">
    <line x1="50" y1="30" x2="110" y2="50"/>
    <line x1="170" y1="30" x2="110" y2="50"/>
    <line x1="30" y1="70" x2="110" y2="50"/>
    <line x1="190" y1="70" x2="110" y2="50"/>
    <line x1="50" y1="30" x2="170" y2="30"/>
    <line x1="30" y1="70" x2="190" y2="70"/>
    <line x1="50" y1="30" x2="30" y2="70"/>
    <line x1="170" y1="30" x2="190" y2="70"/>
  </g>
  <!-- phones (top corners) -->
  <g fill="url(#iconGrad1)">
    <rect x="42" y="18" width="16" height="24" rx="2"/>
    <rect x="162" y="18" width="16" height="24" rx="2"/>
  </g>
  <!-- PCs (bottom corners + center) -->
  <g fill="#FFD166">
    <rect x="22" y="60" width="16" height="14" rx="1"/>
    <rect x="182" y="60" width="16" height="14" rx="1"/>
    <rect x="100" y="44" width="20" height="14" rx="1"/>
  </g>
  <!-- screen accents -->
  <g fill="#0A0F18">
    <rect x="44" y="20" width="12" height="18"/>
    <rect x="164" y="20" width="12" height="18"/>
    <rect x="24" y="62" width="12" height="8"/>
    <rect x="184" y="62" width="12" height="8"/>
    <rect x="103" y="46" width="14" height="8"/>
  </g>
</svg>'''
    # centralized: server cluster
    return '''<svg viewBox="0 0 220 100" xmlns="http://www.w3.org/2000/svg" style="width:100%;height:90px;">
  <defs>
    <linearGradient id="iconGrad2" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#7B8AA5"/><stop offset="100%" stop-color="#283249"/>
    </linearGradient>
  </defs>
  <!-- big central data-center block -->
  <rect x="80" y="20" width="60" height="60" rx="4" fill="rgba(40,50,73,.6)" stroke="#7B8AA5" stroke-width="1"/>
  <!-- server racks inside -->
  <g fill="url(#iconGrad2)">
    <rect x="86" y="26" width="48" height="6" rx="1"/>
    <rect x="86" y="36" width="48" height="6" rx="1"/>
    <rect x="86" y="46" width="48" height="6" rx="1"/>
    <rect x="86" y="56" width="48" height="6" rx="1"/>
    <rect x="86" y="66" width="48" height="6" rx="1"/>
  </g>
  <!-- LED dots -->
  <g fill="#FFD166">
    <circle cx="92" cy="29" r="1.2"/><circle cx="92" cy="39" r="1.2"/>
    <circle cx="92" cy="49" r="1.2"/><circle cx="92" cy="59" r="1.2"/>
    <circle cx="92" cy="69" r="1.2"/>
  </g>
  <!-- single thick connection line out -->
  <line x1="80" y1="50" x2="20" y2="50" stroke="#7B8AA5" stroke-width="2"/>
  <line x1="140" y1="50" x2="200" y2="50" stroke="#7B8AA5" stroke-width="2"/>
</svg>'''


def render(text):
    return text.replace("[grad]", '<span class="grad">').replace("[sun]", '<span class="sun">').replace("[/]", '</span>')


CSS = """
  @page { size: 280mm 157.5mm; margin: 0; }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  html, body { font-family: -apple-system, "Inter", "Helvetica Neue", Arial, sans-serif;
    color: #F3F6FB; background: #05070C; -webkit-print-color-adjust: exact; print-color-adjust: exact;
    font-size: 10.5pt; line-height: 1.55; }
  .slide { width: 280mm; height: 157.5mm; padding: 16mm 22mm 28mm; page-break-after: always;
    background: #05070C; position: relative; overflow: hidden; display: flex; flex-direction: column; }
  .slide:last-child { page-break-after: auto; }
  .slide::before { content: ""; position: absolute; inset: 0; pointer-events: none;
    background: radial-gradient(ellipse 80% 60% at 50% 0%, rgba(26,200,255,.10) 0%, transparent 60%),
                radial-gradient(ellipse 60% 50% at 85% 20%, rgba(30,110,232,.08) 0%, transparent 55%); }
  .slide-no { position: absolute; bottom: 8mm; right: 22mm; font-size: 9pt; color: #7B8AA5; letter-spacing: .15em; }
  .brand-tag { position: absolute; bottom: 7mm; left: 22mm; display: flex; align-items: center; gap: 8px; }
  .brand-logo { width: 16px; height: 16px; }
  .brand-name { font-size: 9pt; color: #F3F6FB; font-weight: 600; letter-spacing: .06em; }
  .brand-slogan { font-size: 8pt; color: #7B8AA5; font-style: italic; }
  .corner-logo { position: absolute; top: 14mm; left: 22mm; width: 28px; height: 28px; }
  .eyebrow { color: #FFD166; font-size: 10pt; letter-spacing: .22em; text-transform: uppercase;
    margin-bottom: 14px; position: relative; }
  h1 { font-size: 44pt; font-weight: 700; letter-spacing: -.02em; line-height: 1.05; max-width: 24ch; position: relative; }
  h2 { font-size: 28pt; font-weight: 600; letter-spacing: -.01em; line-height: 1.1; margin-bottom: 10px; max-width: 26ch; position: relative; }
  h3 { font-size: 14pt; font-weight: 600; color: #F3F6FB; margin-bottom: 4px; position: relative; }
  .lede { font-size: 14pt; color: #B8C3D9; max-width: 60ch; line-height: 1.45; margin-top: 14px; position: relative; }
  .grad { color: #4FE3FF; }
  .sun { color: #FFD166; }
  .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-top: 18px; position: relative; }
  .grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 14px; margin-top: 18px; position: relative; }
  .grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-top: 18px; position: relative; }
  .card { background: rgba(17,24,38,.7); border: 1px solid rgba(40,50,73,.9); border-radius: 12px; padding: 16px 18px; }
  .card .tag { font-size: 8.5pt; letter-spacing: .2em; text-transform: uppercase; color: #7B8AA5; margin-bottom: 6px; }
  .card .title { font-size: 12pt; font-weight: 600; color: #F3F6FB; margin-bottom: 6px; }
  .card .body { font-size: 10pt; color: #B8C3D9; line-height: 1.5; }
  .card.live { border-color: rgba(255,209,102,.55); }
  .live-pill { display: inline-block; background: #FFD166; color: #0A0F18; font-size: 8pt; font-weight: 700;
    padding: 2px 7px; border-radius: 999px; letter-spacing: .18em; }
  .stat { background: rgba(17,24,38,.7); border: 1px solid rgba(40,50,73,.9); border-radius: 12px; padding: 14px 16px; }
  .stat .num { font-size: 26pt; font-weight: 700; line-height: 1; }
  .stat .label { font-size: 9.5pt; color: #7B8AA5; margin-top: 6px; letter-spacing: .06em; }
  ul.bullets { margin-top: 14px; position: relative; }
  ul.bullets li { list-style: none; color: #B8C3D9; padding-left: 22px; position: relative; margin: 8px 0; font-size: 11pt; }
  ul.bullets li::before { content: "—"; position: absolute; left: 0; color: #FFD166; font-weight: 700; }
  .cover { background: radial-gradient(ellipse 90% 70% at 30% 0%, rgba(26,200,255,.18) 0%, transparent 60%),
    radial-gradient(ellipse 70% 60% at 80% 100%, rgba(255,209,102,.12) 0%, transparent 55%), #05070C;
    padding-bottom: 22mm !important; }
  .cover-content { display: flex; flex-direction: column; flex: 1; position: relative; min-height: 0; }
  .cover h1 { font-size: 40pt; line-height: 1.05; max-width: 18ch; }
  .cover .lede { font-size: 13pt; max-width: 48ch; margin-top: 10px; }
  .cover .bottom-meta { margin-top: auto; padding-top: 14px; font-size: 9pt; color: #7B8AA5;
    letter-spacing: .05em; line-height: 1.7; max-width: 60ch; position: relative; }
  .cover-grid { position: relative; display: grid; grid-template-columns: 1fr 220px; gap: 24px; align-items: center; margin-top: 14px; }
  .hero-illustration { position: relative; width: 220px; height: 240px; }
  .market-chart { width: 100%; height: 240px; margin-top: 18px; position: relative; }
  .forecast-chart { width: 100%; height: 200px; margin-top: 12px; position: relative; }
  .chart-caption { font-size: 9pt; color: #7B8AA5; margin-top: 6px; letter-spacing: .05em; text-align: right; position: relative; }
  .ask-num { font-size: 56pt; font-weight: 700; line-height: 1; color: #FFD166; }
  .row-flex { display: flex; align-items: center; gap: 24px; margin-top: 14px; position: relative; }
  .traction-list { margin-top: 14px; position: relative; }
  .traction-list .row { display: flex; align-items: center; padding: 8px 0;
    border-bottom: 1px solid rgba(40,50,73,.7); font-size: 11pt; color: #B8C3D9; }
  .traction-list .row .check { width: 22px; color: #2DE0A3; font-weight: 700; }
  .traction-list .row b { color: #F3F6FB; font-weight: 600; min-width: 220px; display: inline-block; }
  .team-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; margin-top: 18px; position: relative; }
  .team-card { background: rgba(17,24,38,.7); border: 1px solid rgba(40,50,73,.9); border-radius: 12px;
    padding: 14px; text-align: center; }
  .team-card .name { font-size: 10pt; font-weight: 600; color: #F3F6FB; margin-top: 8px; }
  .team-card .role { font-size: 8.5pt; color: #7B8AA5; margin-top: 2px; line-height: 1.4; }
  .arch-row { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px; margin-top: 10px; position: relative; }
  .arch-card { background: rgba(17,24,38,.6); border: 1px solid rgba(40,50,73,.9); border-radius: 10px;
    padding: 6px 10px 8px; text-align: center; }
  .arch-card .arch-icon { width: 100%; height: 46px; display: flex; align-items: center; justify-content: center; }
  .arch-card .arch-icon svg { height: 46px; }
  .arch-card .arch-name { font-size: 10pt; font-weight: 600; color: #F3F6FB; margin-top: 2px; }
  .arch-card .arch-sub { font-size: 8pt; color: #7B8AA5; margin-top: 1px; }
  .compare-table { width: 100%; border-collapse: collapse; margin-top: 8px; position: relative; font-size: 8.5pt; }
  .compare-table th, .compare-table td { border: 1px solid rgba(40,50,73,.9); padding: 4px 7px; text-align: left;
    vertical-align: middle; line-height: 1.3; }
  .compare-table th { background: rgba(17,24,38,.85); color: #F3F6FB; font-weight: 600; font-size: 9.5pt;
    text-transform: uppercase; letter-spacing: .08em; }
  .compare-table th:first-child, .compare-table td:first-child { background: rgba(17,24,38,.6); color: #F3F6FB;
    font-weight: 600; width: 20%; }
  .compare-table .vylex { color: #4FE3FF; font-weight: 600; }
  .compare-table tr:nth-child(even) td { background: rgba(17,24,38,.35); }
  .compare-footnote { font-size: 8.5pt; color: #7B8AA5; margin-top: 10px; font-style: italic; position: relative; }
"""


def slide_no(n, total=11):
    return f'<div class="slide-no">{n:02d} / {total:02d}</div>'


def brand_tag():
    return f'<div class="brand-tag"><img class="brand-logo" src="{LOGO_DATA}" alt=""/><span class="brand-name">VylexAI</span><span class="brand-slogan">— {SLOGAN}</span></div>'


def corner_logo():
    return f'<img class="corner-logo" src="{LOGO_DATA}" alt=""/>'


def build_slides(t):
    bt = brand_tag()
    s = []

    s.append(f'''<section class="slide cover">
  {corner_logo()}
  <div class="cover-content">
    <div style="margin-top: 18mm;"></div>
    <div class="eyebrow">{t["label"]}</div>
    <div class="cover-grid">
      <div><h1>{render(t["s1_h"])}</h1><p class="lede">{t["s1_lede"]}</p></div>
      <div class="hero-illustration">{SMARTPHONE_SVG}</div>
    </div>
    <div class="bottom-meta">
      <strong style="color:#F3F6FB;">{t["footer"]["company"]}</strong> · {t["footer"]["city"]}<br/>
      {t["footer"]["founder"]}<br/>
      {t["footer"]["links"]}
    </div>
  </div>
  {bt}{slide_no(1)}
</section>''')

    p = t["s2"]
    stats = "".join(f'<div class="stat"><div class="num grad">{n}</div><div class="label">{l}</div></div>' for n, l in p["stat"])
    bullets = "".join(f'<li style="font-size:10pt;margin:4px 0;">{b}</li>' for b in p["bullets"])
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2>{render(p["h"])}</h2>
  <p class="lede" style="font-size:12pt;margin-top:10px;">{p["lede"]}</p>
  <div class="grid-3" style="margin-top:14px;">{stats}</div>
  <div class="eyebrow" style="margin-top:16px;font-size:9pt;">{p["bullet_label"]}</div>
  <ul class="bullets" style="margin-top:2px;">{bullets}</ul>
  {bt}{slide_no(2)}
</section>''')

    p = t["s3"]
    cards = "".join(f'<div class="card"><div class="tag">{tag}</div><div class="title sun">{title}</div><div class="body">{body}</div></div>' for tag, title, body in p["cards"])
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2>{render(p["h"])}</h2><p class="lede">{p["lede"]}</p>
  <div class="grid-2">{cards}</div>{bt}{slide_no(3)}
</section>''')

    p = t["s4"]
    cards = "".join(f'<div class="card"><div class="tag">{tag}</div><div class="title">{title}</div><div class="body">{body}</div></div>' for tag, title, body in p["cards"])
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2>{render(p["h"])}</h2>
  <div class="grid-3">{cards}</div>
  <div class="row-flex"><p style="font-size:11pt;color:#B8C3D9;max-width:78ch;">{p["footnote"]}</p></div>
  {bt}{slide_no(4)}
</section>''')

    p = t["s5"]
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2>{render(p["h"])}</h2><p class="lede">{p["lede"]}</p>
  <div class="market-chart">{p["chart"]}</div>
  <p style="margin-top:8px;font-size:11pt;position:relative;">{p["footer"]}</p>
  {bt}{slide_no(5)}
</section>''')

    p = t["s6"]
    cards = []
    for tag, live, title, body in p["phases"]:
        live_html = f' <span class="live-pill">{live}</span>' if live else ""
        cls = ' live' if live else ''
        cards.append(f'<div class="card{cls}"><div class="tag">{tag}{live_html}</div><div class="title">{title}</div><div class="body">{body}</div></div>')
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2>{render(p["h"])}</h2>
  <div class="grid-3">{"".join(cards)}</div>{bt}{slide_no(6)}
</section>''')

    p = t["s7"]
    stats = "".join(f'<div class="stat"><div class="num grad">{n}</div><div class="label">{l}</div></div>' for n, l in p["stat"])
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2>{render(p["h"])}</h2>
  <div class="grid-4">{stats}</div>
  <div class="forecast-chart">{p["chart"]}</div>
  <div class="chart-caption">{p["chart_caption"]}</div>{bt}{slide_no(7)}
</section>''')

    p = t["s8"]
    rows = "".join(f'<div class="row"><span class="check">✓</span><b>{a}</b>{b}</div>' for a, b in p["rows"])
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2>{render(p["h"])}</h2>
  <div class="traction-list">{rows}</div>{bt}{slide_no(8)}
</section>''')

    p = t["s9"]
    team = "".join(f'<div class="team-card"><div class="name">{n}</div><div class="role">{r}</div></div>' for n, r in p["team"])
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2>{render(p["h"])}</h2><p class="lede">{p["lede"]}</p>
  <div class="team-grid">{team}</div>{bt}{slide_no(9)}
</section>''')

    p = t["s10"]
    uses = "".join(f"<li>{u}</li>" for u in p["uses"])
    s.append(f'''<section class="slide cover">
  {corner_logo()}<div class="cover-content">
    <div style="margin-top: 18mm;"></div>
    <div class="eyebrow">{p["eyebrow"]}</div><h1>{render(p["h"])}</h1>
    <div style="margin-top:22px;display:flex;gap:36px;align-items:flex-start;position:relative;">
      <div style="min-width:200px;">
        <div class="ask-num">{p["amount"]}<span style="color:#7B8AA5;font-size:13pt;margin-left:8px;font-weight:400;">{p["amount_label"]}</span></div>
        <div style="color:#7B8AA5;font-size:10.5pt;margin-top:6px;max-width:26ch;">{p["amount_sub"]}</div>
      </div>
      <div style="flex:1;"><h3>{p["use_label"]}</h3><ul class="bullets" style="margin-top:6px;">{uses}</ul></div>
    </div>
  </div>{bt}{slide_no(10)}
</section>''')

    # Slide 11 — Comparison: arch icons + compact table
    p = t["s11"]
    arch_cards = "".join(
        f'<div class="arch-card"><div class="arch-icon">{arch_icon(kind)}</div>'
        f'<div class="arch-name">{name}</div><div class="arch-sub">{sub}</div></div>'
        for name, sub, kind in p["archs"]
    )
    header_row = "".join(f"<th>{h}</th>" for h in p["headers"])
    body_rows = "".join(
        '<tr><td>{}</td><td class="vylex">{}</td><td>{}</td><td>{}</td></tr>'.format(*row)
        for row in p["rows"]
    )
    s.append(f'''<section class="slide">
  <div class="eyebrow">{p["eyebrow"]}</div><h2 style="font-size:22pt;">{render(p["h"])}</h2>
  <p class="lede" style="font-size:11pt;margin-top:6px;">{p["lede"]}</p>
  <div class="arch-row">{arch_cards}</div>
  <table class="compare-table">
    <thead><tr>{header_row}</tr></thead>
    <tbody>{body_rows}</tbody>
  </table>
  <div class="compare-footnote">{p["footnote"]}</div>
  {bt}{slide_no(11)}
</section>''')

    return "\n".join(s)


def build_html(t):
    return f'''<!DOCTYPE html>
<html lang="{t["lang"]}">
<head><meta charset="UTF-8" /><title>{t["title"]}</title><style>{CSS}</style></head>
<body>{build_slides(t)}</body></html>'''


for slug, t in [("en", EN), ("de", DE)]:
    out = Path(f"/tmp/vylexai_deck_{slug}.html")
    out.write_text(build_html(t), encoding="utf-8")
    print(f"wrote {out}")
