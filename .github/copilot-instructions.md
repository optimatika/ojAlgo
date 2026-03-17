Copilot instructions for ojAlgo

Purpose
- Deliver the highest performance possible while keeping the code clean, modular, and easy to maintain.
- Prefer clarity over cleverness unless a measured performance gain justifies it.
- Identify and exploit existing ojAlgo features/utilities before adding new code; integrate with established patterns instead of reimplementing functionality.

Work plans
- Maintain roadmap/workplan documents alongside the code when applicable.
- Use the checkbox legend style `Legend: [x] done, [ ] pending, [~] partial/in progress` so status is scannable.
- Keep entries concise: state milestone, status, and key next step; update status whenever code changes affect the plan.
- Always cross-reference `.github/copilot-instructions.md` inside the work plan so future updates stay compliant.

General style
- Use descriptive, meaningful names; avoid cryptic abbreviations.
- Favor self-explanatory code over comments. Add short Javadoc for public APIs; avoid excessive inline comments.
- Keep changes small and focused. Maintain binary compatibility; deprecate before removal.
- Be critical of existing code. If something can be simpler/faster/clearer, propose and implement it with evidence.
- Match the surrounding ojAlgo code style (formatting, ordering, idioms) when writing new code; let existing files serve as the baseline.
- Prefer enhancements that compose with current APIs and data structures instead of creating parallel abstractions.

Javadoc and code comments
- Use HTML for layout but keep it tidy and readable in source.
- Avoid unnecessary end tags like </p> and </li>.
- Avoid unnecessary formatting tags like <b> and <i>.
- Avoid html entities and symbols like &lt; &gt; &nbsp; or &copy;.
- Comment classes, methods, fields, and constants as proper Javadoc; avoid unattached line or block comments.
- Treat inline line/block comments as a last resort—when the code needs explanation, attach concise Javadoc to the relevant declaration instead.
- Prefer concise lists and paragraphs; link APIs with {@link ...} where it adds clarity.
- In particular avoid 1 line block comments, and line comments on the same line as code.

Assistant interaction style
- Keep chat output concise and focused; avoid long, repetitive narratives when a shorter explanation suffices.
- Do not both show full code edits in the chat and apply them to files; choose a single representation per change.
- When editing files via tools, describe the intent and rely on the actual file changes instead of pasting large code blocks back into the chat.
- Prefer small, incremental updates to plans and instructions that track the current state of the codebase.

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
- When relevant, compare design/behavior with high-quality open-source solvers (HiGHS, GLOP, CLP, SCIP, CBC, OSQP, CLARABEL, qpOASES).
- Document notable differences and rationale in code comments (short) or commit/PR descriptions.
- Strive for numerically robust implementations (scaling, presolve, pivot rules, tolerance handling) with measured impact.

Modules and dependencies
- Keep module boundaries tight. Update module-info.java when adding/removing exports.
- Avoid adding new dependencies; prefer internal utilities. If unavoidable, justify and keep scope minimal.

Code hygiene
- No commented-out code. Use TODO with an issue reference when needed.
- Keep formatting consistent with the existing codebase. Preserve imports order.
- Do not remove the last CR or LF at the end of files; ensure exactly one trailing newline.
- When in doubt about formatting or structural choices, copy the conventions already used in the nearest ojAlgo file.

PR checklist
- Evidence of performance impact (numbers or profiling) for performance-motivated changes.
- Adequate tests using TestUtils; updated docs/Javadocs where API changes.
- BasicLogger used for diagnostic output; no System.out/err.
- Module-info updated if necessary; no unintended transitive exposure.

Chat output style

- Do not paste large existing files or full code listings into the chat when you can edit files directly.
- Prefer concise, high-level summaries of changes and rely on workspace edits for full code.
- Only include small, focused snippets in the chat when they materially aid understanding or discussion.