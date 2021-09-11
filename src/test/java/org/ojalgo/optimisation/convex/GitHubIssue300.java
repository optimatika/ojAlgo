package org.ojalgo.optimisation.convex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.structure.Access1D;

/**
 * @see https://github.com/optimatika/ojAlgo/issues/300
 */
public class GitHubIssue300 extends OptimisationConvexTests {

    public static final BigDecimal[] SOME_PROFILE = DoubleStream.of(0.09725837861004, 0.37672400255866, 4.1113E-10, 0, 4.87388E-9, 0, 7.8263E-9, 1.294368E-8, 0,
            2.1125E-10, 0, 5.136447E-8, 0, 1.55817013356898, 0, 0, 2.76219E-9, 0, 0, 1.509177E-8, 3.189669E-8, 1.02735E-8, 0, 0, 4.867634E-8, 3.41886143390198,
            1.31198668591809, 0, 0, 1.784353E-8, 1.87058E-8, 3.035083E-8, 0, 0, 7.95869E-9, 2.615025E-8, 1.941942E-8, 2.69228E-9, 7.15326E-9, 0, 0, 0,
            2.62665E-8, 0, 3.836841E-8, 0, 1.272672E-8, 3.208536E-8, 0, 0).mapToObj(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    private static final double[][] C = { { 0.41613760141592926 }, { 0.6481579639888118 }, { 0.5869243794547192 }, { 0.5443135054389044 },
            { 0.5111317369150773 }, { 0.4839473522511266 }, { 0.4609877850781009 }, { 0.4411860087792175 }, { 0.42383945439934256 }, { 0.40845718993228614 },
            { 0.39468120562153974 }, { 0.38224183145483126 }, { 0.3709306809876251 }, { 0.18029167159941925 } };
    private static final double[][] Q = {
            { 0.018587823135988498, 0.007021162397427861, 0.005009681251375591, 0.004102850709458084, 0.0035586068120709215, 0.003185841558031,
                    0.002910047482613826, 0.0026953629642789577, 0.002522111749539341, 0.002378479346155001, 0.0022568865936899805, 0.002152218298037034,
                    0.00206088058919344, 9.90132305861861E-4 },
            { 0.007021162397427861, 0.009965463412884035, 0.008160267941570598, 0.007077397406995217, 0.0063358472887708114, 0.005787254876214683,
                    0.005360241925731419, 0.00501565398475626, 0.004729984189344336, 0.004488153939542586, 0.004279987318758956, 0.004098335210957597,
                    0.003938008142740527, 0.0018975678340035142 },
            { 0.005009681251375591, 0.008160267941570598, 0.00707643310125001, 0.0063346993925830165, 0.005786080882302487, 0.005359087117044942,
                    0.005014532539810937, 0.004728899745502296, 0.004487106206432429, 0.004278974527804781, 0.004097355102935802, 0.003937058382800402,
                    0.0037942140191805597, 0.0018329344059960545 },
            { 0.004102850709458084, 0.007077397406995217, 0.0063346993925830165, 0.005785825629228291, 0.005358738652320368, 0.005014146650408315,
                    0.004728499826981299, 0.0044867032254542, 0.004278573955349289, 0.004096959737667273, 0.0039366696352486925, 0.0037938325451070294,
                    0.003665494844472011, 0.0017746792399386886 },
            { 0.0035586068120709215, 0.0063358472887708114, 0.005786080882302487, 0.005358738652320368, 0.005014042600655157, 0.004728346149035535,
                    0.004486524015913385, 0.004278381305760873, 0.004096760237127243, 0.003936467082919278, 0.0037936292175243702, 0.0036652921396899563,
                    0.0035491572668895407, 0.0017217029126344497 },
            { 0.003185841558031, 0.005787254876214683, 0.005359087117044942, 0.005014146650408315, 0.004728346149035535, 0.004486471520224656,
                    0.004278299672718783, 0.004096661501047504, 0.003936358004657833, 0.0037935138367879624, 0.00366517298218384, 0.0035490359661497005,
                    0.0034432834648981375, 0.0016732273034868992 },
            { 0.002910047482613826, 0.005360241925731419, 0.005014532539810937, 0.004728499826981299, 0.004486524015913385, 0.004278299672718783,
                    0.004096631344893359, 0.003936309357364012, 0.003793453342326958, 0.0036651046648719567, 0.003548962376307584, 0.0034432062751420514,
                    0.0033463749440925195, 0.0016286398104268347 },
            { 0.0026953629642789577, 0.00501565398475626, 0.004728899745502296, 0.0044867032254542, 0.004278381305760873, 0.004096661501047504,
                    0.003936309357364012, 0.003793434399276273, 0.003665073193192518, 0.003548922259027447, 0.003443159956276972, 0.0033463240053011292,
                    0.003257225107573363, 0.0015874423461319574 },
            { 0.002522111749539341, 0.004729984189344336, 0.004487106206432429, 0.004278573955349289, 0.004096760237127243, 0.003936358004657833,
                    0.003793453342326958, 0.003665073193192518, 0.003548909455808275, 0.0034431380142672958, 0.0033462951617485616, 0.0032571907386732004,
                    0.0031748456420604195, 0.0015492229878479853 },
            { 0.002378479346155001, 0.004488153939542586, 0.004278974527804781, 0.004096959737667273, 0.003936467082919278, 0.0037935138367879624,
                    0.0036651046648719567, 0.003548922259027447, 0.0034431380142672958, 0.003346285764856861, 0.003257173902653997, 0.0031748224407399327,
                    0.003098416940492942, 0.0015136370827770692 },
            { 0.0022568865936899805, 0.004279987318758956, 0.004097355102935802, 0.0039366696352486925, 0.0037936292175243702, 0.00366517298218384,
                    0.003548962376307584, 0.003443159956276972, 0.0033462951617485616, 0.003257173902653997, 0.003174814684598982, 0.003098402131555284,
                    0.003027252433262245, 0.0014803936076442535 },
            { 0.002152218298037034, 0.004098335210957597, 0.003937058382800402, 0.0037938325451070294, 0.0036652921396899563, 0.0035490359661497005,
                    0.0034432062751420514, 0.0033463240053011292, 0.0032571907386732004, 0.0031748224407399327, 0.003098402131555284, 0.003027244976497713,
                    0.002960771969944466, 0.001449244884801856 },
            { 0.00206088058919344, 0.003938008142740527, 0.0037942140191805597, 0.003665494844472011, 0.0035491572668895407, 0.0034432834648981375,
                    0.0033463749440925195, 0.003257225107573363, 0.0031748456420604195, 0.003098416940492942, 0.003027252433262245, 0.002960771969944466,
                    0.002898481491864027, 0.0014199786298106452 },
            { 9.90132305861861E-4, 0.0018975678340035142, 0.0018329344059960545, 0.0017746792399386886, 0.0017217029126344497, 0.0016732273034868992,
                    0.0016286398104268347, 0.0015874423461319574, 0.0015492229878479853, 0.0015136370827770692, 0.0014803936076442535, 0.001449244884801856,
                    0.0014199786298106452, 6.962058522799804E-4 } };

    public static Stream<Double> alphaValues() {
        return Stream.of(0.269e-6, 0.271e-6, 0.272e-6, 0.273e-6);
    }

    private MatrixStore<Double> ai;
    private MatrixStore<Double> bi;
    private MatrixStore<Double> c;
    private MatrixStore<Double> lModel;
    private MatrixStore<Double> q;
    private MatrixStore<Double> qModel;
    private MatrixStore<Double> qRegul;
    private Access1D<BigDecimal> someProfile;

    @BeforeEach
    public void prepareModel() throws Exception {
        q = Primitive64Store.FACTORY.rows(Q);
        c = Primitive64Store.FACTORY.rows(C);

        // to ensure the result will have all its elements positive, we set ai
        // to negative identity and bi to zero
        ai = MatrixStore.PRIMITIVE64.makeIdentity(c.countRows()).get().negate();
        bi = MatrixStore.PRIMITIVE64.makeZero(c.countRows(), 1).get();
    }

    @Test
    public void resultShouldHonorInequalities() {

        ConvexSolver solve = ConvexSolver.newBuilder().objective(q, c).inequalities(ai, bi).build();

        // solve.options.debug(ConvexSolver.class);

        Result result = solve.solve();
        TestUtils.assertEquals(Optimisation.State.OPTIMAL, result.getState());

        double[] data = result.toRawCopy1D();
        for (int i = 0; i < data.length; i++) {
            TestUtils.assertTrue("Element at index '" + i + "' should be positive", data[i] >= 0);
        }

    }

    @ParameterizedTest
    @MethodSource("alphaValues")
    public void solutionShouldBeBetterThanSomeProfile(final double alpha) {
        Optimisation.State state;
        double objectiveOfMinimum;
        double objectiveOfSomeProfile;

        {
            long nbVariables = Math.toIntExact(lModel.count());
            ExpressionsBasedModel model = new ExpressionsBasedModel();

            //all variables must be positive
            for (int i = 0; i < nbVariables; i++) {
                model.addVariable().lower(0);
            }

            Expression residual = model.addExpression();
            Expression regularisation = model.addExpression();

            residual.setQuadraticFactors(model.getVariables(), qModel);
            residual.setLinearFactors(model.getVariables(), lModel);
            residual.weight(1);

            regularisation.setQuadraticFactors(model.getVariables(), qRegul);
            regularisation.weight(alpha * alpha);

            // model.options.debug(ConvexSolver.class);

            Optimisation.Result result = model.minimise();
            state = result.getState();
            objectiveOfMinimum = result.getValue();
            objectiveOfSomeProfile = model.objective().evaluate(someProfile).doubleValue();
        }

        double relativeError = Math.abs(1 - objectiveOfMinimum / objectiveOfSomeProfile);

        TestUtils.assertEquals(Optimisation.State.OPTIMAL, state);
        TestUtils.assertTrue("relative error " + relativeError + "<= 1", relativeError <= 1);

    }

    @BeforeEach
    void setUp() throws IOException {
        qModel = MatrixReader.readMatrix("model_quadratic.csv");
        lModel = MatrixReader.readMatrix("model_linear.csv");
        qRegul = MatrixReader.readMatrix("regul_quadratic.csv").get();
        someProfile = Access1D.wrap(SOME_PROFILE);
    }

}
