/**
 * <p>This package contains adapter classes to <a href="http://math.nist.gov/javanumerics/jama/" target="_blank">JAMA</a>.
 * The entire original JAMA package is here but made package private.
 * Use the adapters.</p>
 * <p>Instructions for the ojAlgo developer if/when JAMA is updated:</p>
 * <ol>
 * <li>Delete all *.java files in this package with names that do not begin with Jama*.</li>
 * <li>Move the new JAMA files do this package and update the files' package declarations.</li>
 * <li>Change all the new classes from public to default (package private).</li>
 * <li>Apply fixes
 * <ul>
 * <li>Towards the end of the LU decomposition constructor the if statement<code>((j < m) &amp; (LU[j][j] != 0.0))</code> needs to be changed to <code>((j < m) &amp;&amp; (LU[j][j] != 0.0))</code>.</li>
 * <li>The getL() and getU() methods of the LU decomposition should be modified. Introduce a new parameter d that is the minimum of m and n. Use that to set the column dimension of L and the row dimension of U.</li>
 * <li>Add a package private method isSymmetric() that exposes the already existing field issymmetric in the EigenvalueDecomposition class.</li>
 * <li>Add another constructor to EigenvalueDecomposition that takes an additional boolean parameter 'symmetric' as input, and does not test for symmetry.</li>
 * <li>SingularValueDecomposition: Make wantu and wantv input parameters to the existing constructor, and add another constructor with the old signature that sets both those parateres to true.</li>
 * </ul></li>
 * </ol>
 *
 * @author apete
 */
package org.ojalgo.matrix.jama;