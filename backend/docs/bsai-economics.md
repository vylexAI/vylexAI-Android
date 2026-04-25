# BSAI Tokenomics

Source of truth: Adlan Dudaev, WhatsApp 2026-04-25, item 7 of the SMI-127 technical inputs.

## Hard parameters

| Parameter | Value | Notes |
|---|---|---|
| Total supply (hard cap) | **21,000,000 BSAI** | Bitcoin-style fixed cap. No further mint after launch. |
| Decimal places | **8** | `1 BSAI = 100,000,000` minimal units. Matches Bitcoin satoshi. |
| Genesis reserve | **1,100,000 BSAI** | ~5.24% of total supply. Allocated at network genesis. |
| Genesis wallets | **100** corporate wallets | Owned by Dudaev Systems UG. |

## Why 8 decimals

Required for:
- Microtransactions (sub-cent task rewards)
- User rewards (one task pays fractions of a BSAI)
- Network fees with precision
- Internal ecosystem accounting
- Future price scalability (if BSAI appreciates, fine-grained units stay usable)

## Genesis reserve usage

Distributed only into ecosystem development:
- Developer salaries
- Bug bounties for vulnerability discovery
- Grants to startups building on VylexAI
- Network infrastructure
- Strategic project growth

## Implementation status

- [x] `Numeric(18, 8)` already used on `LedgerEntry.amount_bsai`, `Job.reward_bsai`, `Task.reward_bsai` — matches the 8-decimal spec.
- [x] Constants exposed via `app/core/config.py` (`bsai_decimals`, `bsai_hard_cap`, `bsai_genesis_reserve`, `bsai_genesis_wallet_count`).
- [ ] **Hard-cap enforcement** in the mint path (currently no check; placeholder fine for Technical Preview, must land before any production rollout).
- [ ] **Genesis distribution logic** — needs design ticket. Open questions: how are the 100 wallets generated? Are they HD-derived from a single Dudaev Systems master key? How is the unlock schedule controlled?
- [ ] **BSAI economy module** in the docs / whitepaper — Adlan is writing the new whitepaper around this; align our schema once draft is ready.

## Open questions for the spec

1. **Mint schedule.** Bitcoin halves every 210k blocks. What's our equivalent for compute/storage rewards? A flat per-task rate, halving every N tasks, or a curve?
2. **Genesis wallets.** Hot/cold split? Multisig? Single-key? Need ops procedure before launch.
3. **Storage rewards.** Adlan's redesign plan introduces decentralized storage as Resource #2. The same 21M cap covers both compute + storage rewards, or separate sub-caps?
4. **Green energy bonus.** +10–20% multiplier — does this come out of the same 21M cap (effectively reducing other rewards), or from a separate ESG-grant pool inside the genesis reserve?

## References

- `app/core/config.py` — constants
- `app/models.py:119-134` — `LedgerEntry`
- `app/api/jobs.py:13` — base task cost
- `android/docs/team/VylexAI_redesign_plan.docx` — strategic context
