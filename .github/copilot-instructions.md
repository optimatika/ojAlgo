Copilot instructions for ojAlgo

Purpose
- Deliver the highest performance possible while keeping the code clean, modular, and easy to maintain.
- Prefer clarity over cleverness unless a measured performance gain justifies it.

General style
- Use descriptive, meaningful names; avoid cryptic abbreviations.
- Favor self-explanatory code over comments. Add short Javadoc for public APIs; avoid excessive inline comments.
- Keep changes small and focused. Maintain binary compatibility; deprecate before removal.
- Be critical of existing code. If something can be simpler/faster/clearer, propose and implement it with evidence.

Javadoc
- Use HTML for layout but keep it tidy and readable in source.
- Avoid unnecessary end tags like </p> and </li>.
- Comment classes, methods, fields, and constants as proper Javadoc; avoid unattached line or block comments.
- Prefer concise lists and paragraphs; link APIs with {@link ...} where it adds clarity.

Performance
- Minimize allocations and object churn. Prefer primitives and ojAlgo’s specialised data structures (array/matrix stores) over boxed types or generic collections in hot paths.
- Avoid streams/Optionals in critical loops; consider loop unrolling and vectorization where it pays off.
- Mind cache locality; reuse buffers; avoid unnecessary copying; precompute sizes and strides.
- Choose algorithms with the right asymptotics; measure before/after using the existing JMH/bench harnesses.
- Be concurrency-aware: leverage org.ojalgo.concurrent utilities; avoid synchronized on hot paths; prefer immutability.

Testing
- Use TestUtils instead of Assertions for assertions in tests.
- Prefer deterministic tests. For numerics, use tolerances via TestUtils and avoid brittle equality checks.
- Keep tests fast and focused; no System.out/System.err in tests.

Logging and diagnostics
- Use BasicLogger for output/logging. Do not use System.out or System.err.
- Keep logs terse; avoid logging in tight loops unless gated.
- Line comments inside methods only, otherwise use proper javadoc.

Linear/Convex/Integer Optimisation specifics
- When relevant, compare design/behavior with high-quality open-source solvers (HiGHS, GLOP, CLP, SCIP, CBC).
- Document notable differences and rationale in code comments (short) or commit/PR descriptions.
- Strive for numerically robust implementations (scaling, presolve, pivot rules, tolerance handling) with measured impact.

Modules and dependencies
- Keep module boundaries tight. Update module-info.java when adding/removing exports.
- Avoid adding new dependencies; prefer internal utilities. If unavoidable, justify and keep scope minimal.

Code hygiene
- No commented-out code. Use TODO with an issue reference when needed.
- Keep formatting consistent with the existing codebase. Preserve imports order.
- Do not remove the last CR or LF at the end of files; ensure exactly one trailing newline.

PR checklist
- Evidence of performance impact (numbers or profiling) for performance-motivated changes.
- Adequate tests using TestUtils; updated docs/Javadocs where API changes.
- BasicLogger used for diagnostic output; no System.out/err.
- Module-info updated if necessary; no unintended transitive exposure.
