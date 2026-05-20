import csv

def read_csv(path):
    data = {}
    with open(path) as f:
        reader = csv.DictReader(f, delimiter='\t')
        for row in reader:
            key = (row['Model'], row['Solver'])
            t = row['Time'].strip()
            data[key] = int(t) if t else None
    return data

baseline = read_csv('baseline_netlib.csv')
step1 = read_csv('step1_netlib.csv')
step2 = read_csv('step2_netlib.csv')
step3 = read_csv('step3_netlib.csv')

all_keys = sorted(set(baseline.keys()) | set(step1.keys()) | set(step2.keys()) | set(step3.keys()))
ojalgo_keys = [(m, s) for m, s in all_keys if 'ojAlgo' in s]

def fmt(v):
    if v is None:
        return 'TIMEOUT'
    if v < 1_000_000:
        return f"{v/1000:.1f}us"
    if v < 1_000_000_000:
        return f"{v/1_000_000:.1f}ms"
    return f"{v/1_000_000_000:.2f}s"

def ratio(a, b):
    if a is None or b is None:
        return '   -   '
    return f"{a/b:.3f}"

for solver in ['ojAlgo-LP-dual-D', 'ojAlgo-LP-dual-S']:
    print(f"\n{'='*120}")
    print(f"  {solver}")
    print(f"{'='*120}")
    print(f"{'Model':<14} {'Baseline':>12} {'Step1':>12} {'Step2':>12} {'Step3':>12}  {'s1/base':>8} {'s2/base':>8} {'s3/base':>8} {'s3/s2':>8}")
    print(f"{'-'*14} {'-'*12} {'-'*12} {'-'*12} {'-'*12}  {'-'*8} {'-'*8} {'-'*8} {'-'*8}")

    models = sorted(set(m for m, s in ojalgo_keys if s == solver))

    total_b = 0
    total_1 = 0
    total_2 = 0
    total_3 = 0
    count = 0
    improvements = []
    regressions = []

    for model in models:
        key = (model, solver)
        b = baseline.get(key)
        s1 = step1.get(key)
        s2 = step2.get(key)
        s3 = step3.get(key)

        r_s1 = ratio(s1, b)
        r_s2 = ratio(s2, b)
        r_s3 = ratio(s3, b)
        r_s3s2 = ratio(s3, s2)

        if b and s1 and s2 and s3 and b > 1_000_000:
            total_b += b
            total_1 += s1
            total_2 += s2
            total_3 += s3
            count += 1
            pct = (s3 - b) / b * 100
            if pct < -5:
                improvements.append((model, pct))
            elif pct > 5:
                regressions.append((model, pct))

        print(f"{model:<14} {fmt(b):>12} {fmt(s1):>12} {fmt(s2):>12} {fmt(s3):>12}  {r_s1:>8} {r_s2:>8} {r_s3:>8} {r_s3s2:>8}")

    print(f"\n  Summary ({count} models with complete data > 1ms):")
    if total_b > 0:
        print(f"    Baseline total: {total_b/1e9:.2f}s")
        print(f"    Step1 total:    {total_1/1e9:.2f}s  ({total_1/total_b:.4f}x)")
        print(f"    Step2 total:    {total_2/1e9:.2f}s  ({total_2/total_b:.4f}x)")
        print(f"    Step3 total:    {total_3/1e9:.2f}s  ({total_3/total_b:.4f}x)")

    if improvements:
        print(f"\n  Top improvements (step3 vs baseline, >5% faster):")
        for m, p in sorted(improvements, key=lambda x: x[1])[:10]:
            print(f"    {m:<14} {p:+.1f}%")
    if regressions:
        print(f"\n  Regressions (step3 vs baseline, >5% slower):")
        for m, p in sorted(regressions, key=lambda x: -x[1])[:10]:
            print(f"    {m:<14} {p:+.1f}%")

# Also check stability changes
print(f"\n{'='*120}")
print("  Stability changes (TIMEOUT/solve differences)")
print(f"{'='*120}")
for solver in ['ojAlgo-LP-dual-D', 'ojAlgo-LP-dual-S']:
    models = sorted(set(m for m, s in ojalgo_keys if s == solver))
    for model in models:
        key = (model, solver)
        b = baseline.get(key)
        s3 = step3.get(key)
        if (b is None) != (s3 is None):
            print(f"  {solver} / {model}: baseline={'TIMEOUT' if b is None else fmt(b)}, step3={'TIMEOUT' if s3 is None else fmt(s3)}")
