#!/bin/bash
# Experiment: 4 ftranU/btranU combos × ETA_FILL_RATIO values
# Modifies source via python3 regex, compiles, runs CuteNetlibCase, parses XML results
set -euo pipefail
cd "$(dirname "$0")"

SPARSE_LU="src/main/java/org/ojalgo/matrix/decomposition/SparseLU.java"
SPARSE_DEC="src/main/java/org/ojalgo/optimisation/linear/SparseDecomposition.java"
RESULTS_DIR="target/experiment_results"
rm -rf "$RESULTS_DIR"
mkdir -p "$RESULTS_DIR"

apply_combo() {
    local ftran_style="$1"  # "U" or "UR"
    local btran_style="$2"  # "U" or "UR"

    python3 - "$ftran_style" "$btran_style" "$SPARSE_LU" << 'PYEOF'
import re, sys

ftran_style, btran_style, path = sys.argv[1], sys.argv[2], sys.argv[3]

with open(path) as f:
    content = f.read()

if ftran_style == "U":
    ftran = '''    private void ftranU(final double[] arg) {
        int totalCols = myUPivotRow.length;
        for (int iLogic = totalCols - 1; iLogic >= 0; iLogic--) {
            int pivotRow = myUPivotRow[iLogic];
            if (pivotRow < 0) {
                continue;
            }
            double pivotMultiplier = arg[pivotRow] / myDiagU[iLogic];
            arg[pivotRow] = pivotMultiplier;
            if (pivotMultiplier != 0.0) {
                myU.getColumn(iLogic).axpy(-pivotMultiplier, arg);
            }
        }
    }'''
else:
    ftran = '''    private void ftranU(final double[] arg) {
        int totalCols = myUPivotRow.length;
        for (int iLogic = totalCols - 1; iLogic >= 0; iLogic--) {
            int pivotRow = myUPivotRow[iLogic];
            if (pivotRow < 0) {
                continue;
            }
            arg[pivotRow] = (arg[pivotRow] - myUR.getRow(pivotRow).dotViaLookup(arg, myUPivotRow)) / myDiagU[iLogic];
        }
    }'''

if btran_style == "U":
    btran = '''    private void btranU(final double[] arg) {
        int totalCols = myUPivotRow.length;
        for (int iLogic = 0; iLogic < totalCols; iLogic++) {
            int pivotRow = myUPivotRow[iLogic];
            if (pivotRow < 0) {
                continue;
            }
            arg[pivotRow] = (arg[pivotRow] - myU.getColumn(iLogic).dot(arg)) / myDiagU[iLogic];
        }
    }'''
else:
    btran = '''    private void btranU(final double[] arg) {
        int totalCols = myUPivotRow.length;
        for (int iLogic = 0; iLogic < totalCols; iLogic++) {
            int pivotRow = myUPivotRow[iLogic];
            if (pivotRow < 0) {
                continue;
            }
            arg[pivotRow] /= myDiagU[iLogic];
            if (arg[pivotRow] != 0.0) {
                myUR.getRow(pivotRow).axpyViaLookup(-arg[pivotRow], arg, myUPivotRow);
            }
        }
    }'''

content = re.sub(r'    private void ftranU\(final double\[\] arg\) \{.*?\n    \}', ftran, content, flags=re.DOTALL)
content = re.sub(r'    private void btranU\(final double\[\] arg\) \{.*?\n    \}', btran, content, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(content)
PYEOF
}

set_eta_ratio() {
    sed -i '' "s/private static final double ETA_FILL_RATIO = [0-9.]*;/private static final double ETA_FILL_RATIO = $1;/" "$SPARSE_DEC"
}

parse_results() {
    python3 - << 'PYEOF'
import xml.etree.ElementTree as ET, sys
try:
    tree = ET.parse('target/surefire-reports/TEST-org.ojalgo.optimisation.linear.CuteNetlibCase.xml')
    root = tree.getroot()
    tests = root.get('tests', '0')
    failures = root.get('failures', '0')
    errors = root.get('errors', '0')
    skipped = root.get('skipped', '0')
    fails = []
    for tc in tree.findall('.//testcase'):
        f = tc.find('failure')
        e = tc.find('error')
        if f is not None: fails.append(tc.get('name'))
        if e is not None: fails.append(tc.get('name'))
    print(f"{tests}\t{failures}\t{errors}\t{skipped}\t{','.join(fails)}")
except Exception as ex:
    print(f"0\t0\t0\t0\tPARSE_ERROR:{ex}")
PYEOF
}

COMBOS=("U:U" "U:UR" "UR:U" "UR:UR")
RATIOS="0.6 0.8 1.05 1.5 3.0 5.0"

echo -e "combo\teta\ttests\tfail\terr\tskip\tfailed_tests" > "$RESULTS_DIR/summary.tsv"

for combo in "${COMBOS[@]}"; do
    IFS=: read ftran btran <<< "$combo"
    label="ftran${ftran}_btran${btran}"
    echo ""
    echo "========================================"
    echo "$label"
    echo "========================================"
    
    # Restore from git first, then apply combo
    git checkout -- "$SPARSE_LU" "$SPARSE_DEC"
    apply_combo "$ftran" "$btran"
    
    for ratio in $RATIOS; do
        set_eta_ratio "$ratio"
        echo -n "  ratio=$ratio ... "
        
        if ! ./mvnw -q -pl . test-compile 2>/dev/null; then
            echo "COMPILE FAILED"
            echo -e "${label}\t${ratio}\t0\t0\t0\t0\tCOMPILE_FAILED" >> "$RESULTS_DIR/summary.tsv"
            continue
        fi
        
        ./mvnw -q -pl . test \
            -Dtest="org.ojalgo.optimisation.linear.CuteNetlibCase" \
            -DexcludedGroups=slow \
            -Dsurefire.failIfNoSpecifiedTests=false 2>/dev/null || true
        
        result=$(parse_results)
        echo "$result" | awk -F'\t' '{printf "tests=%s fail=%s err=%s skip=%s\n",$1,$2,$3,$4}'
        echo -e "${label}\t${ratio}\t${result}" >> "$RESULTS_DIR/summary.tsv"
    done
done

# Restore originals
git checkout -- "$SPARSE_LU" "$SPARSE_DEC"

echo ""
echo "========================================"
echo "SUMMARY"
echo "========================================"
column -t -s $'\t' "$RESULTS_DIR/summary.tsv"
