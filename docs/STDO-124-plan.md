**RESUME STATE** *(top of the file; rewritten in place, never appended to; keep under 12 lines)*
- **Reconciled at**: this branch's HEAD, the TIFF rewrite commit.
- **Authoritative**: `PLAN.md` in `lucidworks/tbe-pitches`, branch `STDO-124-bet`, for stage
  sequencing, the done-condition, the stage graph and every measured claim. This file exists only
  so `/team-studios:status` and the compaction resume hook have something to find in this repo.
- **Next**: this repo's release cut (an actual version tag) is a human action, not part of this
  stage's done-condition.

**Decisions** *(append-only)*
- **This file is a pointer, not a fork of the plan.** Full decision log: `tbe-pitches`'s
  `decision-log.md` on `STDO-124-bet`.
- **`TIFFUtils` rewritten onto TwelveMonkeys' `imageio-tiff`**, closing the `jai_*`-withdrawn-from-
  Central route this repo's TIFF handling previously depended on. Verified via a clean local build.

**Wiki candidates** *(append during the work, not at the end)*
- (empty)

---

resume-state template r3-tamarind
