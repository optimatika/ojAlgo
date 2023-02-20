package org.ojalgo.optimisation.convex;

import static org.ojalgo.matrix.store.Primitive64Store.FACTORY;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.type.context.NumberContext;



public class IterativelyRefinedQuadruplePrecisionQP extends OptimisationConvexTests{

   @Test
   void testQP1(){
      Primitive64Store AE = FACTORY.rows(new double[][]{{1.0, 1.0, 1.0, 1.0, 1.0}});
      Primitive64Store BE = FACTORY.rows(new double[][]{{8.5E-18}});
      Primitive64Store AI = FACTORY.rows(new double[][] {
               { -1.0,	0.0,	0.0,	0.0,	0.0 },
               { 0.0,	-1.0,	0.0,	0.0,	0.0 },
               { 0.0,	0.0,	-1.0,	0.0,	0.0 },
               { 0.0,	0.0,	0.0,	-1.0,	0.0 },
               { 0.0,	0.0,	0.0,	0.0,	-1.0 }
      });
      Primitive64Store BI = FACTORY.rows(new double[][]{
               { 0.7907669085467611 },
               { 0.0019999999999900154 },
               { 0.20323309144826854 },
               { 0.0019999999999900154 },
               { 0.0019999999999903207 }
      });
      Primitive64Store C = FACTORY.rows(new double[][]{
               { -550875.2032141489 },
               { -562399.9568628508 },
               { -564250.6957511578 },
               { -691369.2681449897 },
               { -1400521.5234620553 }
      });
      Primitive64Store Q = FACTORY.rows(new double[][]{
               { 10514.489048700269,	0.0,	0.0,	0.0,	0.0 },
               { 0.0,	4157254.9979421264,	0.0,	0.0,	0.0 },
               { 0.0,	0.0,	40911.20171773545,	0.0,	0.0 },
               { 0.0,	0.0,	0.0,	4157254.9979421264,	0.0 },
               { 0.0,	0.0,	0.0,	0.0,	4157254.9979414917 }
      });
      Optimisation.Result result = getConvexSolverSolutionResultQuadruplePrecision(Q, C, AE, BE, AI, BI);
      MatrixStore<Double> x = Primitive64Store.FACTORY.columns(result);
      MatrixStore<Double> y = Primitive64Store.FACTORY.columns(result.getMultipliers().get());
      final double precision = getResidualQuadruplePrecision(x, y, Q, C, AE, BE, AI, BI);
      TestUtils.assertLessThan(1e-15,precision);
   }

   @Test
   void testQP2(){
      Primitive64Store AE = FACTORY.rows(new double[][]{{1.0, 1.0, 1.0, 1.0, 1.0}});
      Primitive64Store BE = FACTORY.rows(new double[][]{{0}});
      Primitive64Store AI = FACTORY.rows(new double[][] {
               { -1.0,	0.0,	0.0,	0.0,	0.0 },
               { 0.0,	-1.0,	0.0,	0.0,	0.0 },
               { 0.0,	0.0,	-1.0,	0.0,	0.0 },
               { 0.0,	0.0,	0.0,	-1.0,	0.0 },
               { 0.0,	0.0,	0.0,	0.0,	-1.0 }
      });
      Primitive64Store BI = FACTORY.rows(new double[][]{
               { 0.199999999999 },
               { 0.199999999999 },
               { 0.199999999999 },
               { 0.199999999999 },
               { 0.199999999999 }
      });
      Primitive64Store C = FACTORY.rows(new double[][]{
               { -539445.3637318831 },
               { -600689.6904218349 },
               { -564117.362709679 },
               { -729659.0017039739 },
               { -1438811.2570210383 }
      });
      Primitive64Store Q = FACTORY.rows(new double[][]{
               { 41572.55,	0.0,	0.0,	0.0,	0.0 },
               { 0.0,	41572.55,	0.0,	0.0,	0.0 },
               { 0.0,	0.0,	41572.55,	0.0,	0.0 },
               { 0.0,	0.0,	0.0,	41572.55,	0.0 },
               { 0.0,	0.0,	0.0,	0.0,	41572.55 }
      });
      Optimisation.Result result = getConvexSolverSolutionResultQuadruplePrecision(Q, C, AE, BE, AI, BI);
      MatrixStore<Double> x = Primitive64Store.FACTORY.columns(result);
      MatrixStore<Double> y = Primitive64Store.FACTORY.columns(result.getMultipliers().get());
      final double precision = getResidualQuadruplePrecision(x, y, Q, C, AE, BE, AI, BI);
      TestUtils.assertLessThan(1e-15,precision);
   }

   static Optimisation.Result getConvexSolverSolutionResultQuadruplePrecision(MatrixStore<Double> Q_in, MatrixStore<Double> C_in,
                                                                              MatrixStore<Double> ae_in, MatrixStore<Double> be_in,
                                                                              MatrixStore<Double> ai_in, MatrixStore<Double> bi_in) {
      // Algorithm from:
      // Solving quadratic programs to high precision using scaled iterative refinement
      // Mathematical Programming Computation (2019) 11:421–455
      // https://doi.org/10.1007/s12532-019-00154-6

      // Required threshold for final residuals
      final double epsPrimal = 1E-15;
      final double epsDual = 1E-15;
      final double epsSlack = 1E-15;

      //  Constants to modify
      final double maxZoomFactor = 1.0E12;
      final int maxRefinementIterations = 5;
      final int maxTriesOnFailure = 3;
      final double smallestNoneZeroHessian = 1e-10;

      // order of size of parameters in model
      double be_Size = be_in.aggregateAll(Aggregator.LARGEST).doubleValue();
      be_Size = be_Size > 1E-15 ? be_Size : 1;
      final double C_Size = C_in.aggregateAll(Aggregator.LARGEST).doubleValue();
      final double Q_Size = Q_in.aggregateAll(Aggregator.LARGEST).doubleValue();

      MatrixStore<Quadruple> Q0 = GenericStore.R128.makeWrapper(Q_in);
      MatrixStore<Quadruple> C0 = GenericStore.R128.makeWrapper(C_in);
      MatrixStore<Quadruple> ae0 = GenericStore.R128.makeWrapper(ae_in);
      MatrixStore<Quadruple> be0 = GenericStore.R128.makeWrapper(be_in);
      MatrixStore<Quadruple> ai0 = GenericStore.R128.makeWrapper(ai_in);
      MatrixStore<Quadruple> bi0 = GenericStore.R128.makeWrapper(bi_in);

      Optimisation.Result x_y_double = getConvexSolverSolutionResult(Q_in, C_in, ae_in, be_in, ai_in, bi_in);
      if (x_y_double.getState() == Optimisation.State.INFEASIBLE) {
//         trust solver and abort if infeasible.
         return x_y_double;
      }
      if (!x_y_double.getState().isOptimal()) {
//         sometimes it works second time...?!
         x_y_double = getConvexSolverSolutionResult(Q_in, C_in, ae_in, be_in, ai_in, bi_in);
      }
      MatrixStore<Quadruple> x0 = GenericStore.R128.columns(x_y_double);
      MatrixStore<Quadruple> y0 = GenericStore.R128.columns(x_y_double.getMultipliers().get());
      double initialSolutionValue = x_y_double.getValue();

//  Set initial values
      double scaleP0 = 1;
      double scaleD0 = 1;
      int iteration = 0;

      while (x_y_double.getState().isFeasible()) {
/*  If set of active inequalities do not change between iterations.
    Then one can try to solve the system of linear equations (KKT) using high precision.
    and return this answer if it is a success, after checking inequality multipliers.
*/
         iteration++;
// Compute residuals in Quadruple precision
         MatrixStore<Quadruple> be1 = be0.subtract(ae0.multiply(x0));
         double maxEqualityResidual = be1.aggregateAll(Aggregator.LARGEST).doubleValue();
         MatrixStore<Quadruple> bi1 = bi0.subtract(ai0.multiply(x0));
         double maxInequalityResidual = bi1.negate().aggregateAll(Aggregator.MAXIMUM).doubleValue();
         double maxPrimalResidual = Math.max(maxEqualityResidual, maxInequalityResidual);
         double scaleP1 = Math.min(1/maxPrimalResidual, maxZoomFactor*scaleP0);
         scaleP1 = Math.max(1, scaleP1);
         MatrixStore<Quadruple> C1 = C0.subtract(Q0.multiply(x0)).subtract(ae0.below(ai0).transpose().multiply(y0));
         double maxGradientResidual = C1.negate().aggregateAll(Aggregator.LARGEST).norm();
         double maxComplementarySlackness = C1.transpose().multiply(bi1).get(0).norm();
         double scaleD1 = Math.min(1/maxGradientResidual, maxZoomFactor*scaleD0);
         scaleD1 = Math.max(1, scaleD1);
         double relativeGradientResidual = maxGradientResidual/C_Size;
         final double relativeComplementarySlackness = maxComplementarySlackness/C_Size;
         if (maxPrimalResidual < epsPrimal && relativeGradientResidual < epsDual && relativeComplementarySlackness < epsSlack) {
//  Passed threshold for final residuals
            break;
         }
         final double scaledHessianNorm = Q_Size*scaleD1/scaleP1;
         if (scaledHessianNorm < smallestNoneZeroHessian) {
//  avoid that ojAlgo classifies hessian as being ZERO. Should different solver be used for this case instead?
            scaleP1 = Q_Size*scaleD1/scaledHessianNorm;
         }
         if (iteration > maxRefinementIterations) {
            if (maxPrimalResidual < Math.sqrt(epsPrimal) && relativeGradientResidual < Math.sqrt(epsDual) && relativeComplementarySlackness < Math.sqrt(epsSlack)) {
//  solution not that bad. I use it in SQP where we will probably do another iteration anyway.
               break;
            }
            Quadruple objectiveValue = Q0.multiplyBoth(x0).divide(2).subtract(x0.transpose().multiply(C0).get(0));
            final Optimisation.State state = x_y_double.getState() == Optimisation.State.OPTIMAL ? Optimisation.State.APPROXIMATE :
                     Optimisation.State.FAILED;
            Optimisation.Result result = Optimisation.Result.of(objectiveValue.doubleValue(), state, x0.toRawCopy1D());
            result.multipliers(y0);
            final double v = (initialSolutionValue - objectiveValue.doubleValue())/initialSolutionValue;
            return result;
         }
         // Prepare approximate model
         int noTries = 0;
         do {
            noTries++;
            MatrixStore<Double> Q1_ = Primitive64Store.FACTORY.makeWrapper(Q0.multiply(scaleD1/scaleP1));
            MatrixStore<Double> C1_ = Primitive64Store.FACTORY.makeWrapper(C1.multiply(scaleD1));
            MatrixStore<Double> ae1_ = Primitive64Store.FACTORY.makeWrapper(ae0);
            MatrixStore<Double> be1_ = Primitive64Store.FACTORY.makeWrapper(be1.multiply(scaleP1));
            MatrixStore<Double> ai1_ = Primitive64Store.FACTORY.makeWrapper(ai0);
            MatrixStore<Double> bi1_ = Primitive64Store.FACTORY.makeWrapper(bi1.multiply(scaleP1));
            //solve updated QP
            x_y_double = getConvexSolverSolutionResult(Q1_, C1_, ae1_, be1_, ai1_, bi1_);
            if (x_y_double.getState().isFailure()) {
// on failure try a smaller zoom factor
               double increaseP = scaleP1/scaleP0;
               double newIncreaseP = Math.sqrt(increaseP);
               scaleP1 = scaleP0*newIncreaseP;
               double increaseD = scaleD1/scaleD0;
               double newIncreaseD = Math.sqrt(increaseD);
               scaleD1 = scaleD0*newIncreaseD;
            }
         } while (x_y_double.getState().isFailure() && noTries < maxTriesOnFailure);

         MatrixStore<Quadruple> x1 = GenericStore.R128.columns(x_y_double);
         MatrixStore<Quadruple> y1 = GenericStore.R128.columns(x_y_double.getMultipliers().get());
 //  update tentative solution in high precision
         x0 = x0.add(x1.divide(scaleP1));
         y0 = y0.add(y1.divide(scaleD1));
         scaleP0 = scaleP1;
         scaleD0 = scaleD1;
      }
      Quadruple objectiveValue = Q0.multiplyBoth(x0).divide(2).subtract(x0.transpose().multiply(C0).get(0));
      Optimisation.Result result = Optimisation.Result.of(objectiveValue.doubleValue(), x_y_double.getState(), x0.toRawCopy1D());
      result.multipliers(y0);
      final double improvement = (initialSolutionValue - objectiveValue.doubleValue())/initialSolutionValue;
      return result;
   }

   static double getResidualQuadruplePrecision(MatrixStore<Double> x_in, MatrixStore<Double> y_in, MatrixStore<Double> Q_in,
                                               MatrixStore<Double> C_in, MatrixStore<Double> Ae_in, MatrixStore<Double> be_in,
                                               MatrixStore<Double> Ai_in, MatrixStore<Double> bi_in) {

      final double epsPrimal = 1E-15;
      final double epsDual = Math.sqrt(1E-15);
      final double epsSlack = Math.sqrt(1E-15);

      double be_Size = be_in.aggregateAll(Aggregator.LARGEST).doubleValue();
      be_Size = be_Size > 1E-15 ? be_Size : 1;
      final double C_Size = C_in.aggregateAll(Aggregator.LARGEST).doubleValue();
      final double Q_Size = Q_in.aggregateAll(Aggregator.LARGEST).doubleValue();

      MatrixStore<Quadruple> x = GenericStore.R128.columns(x_in);
      MatrixStore<Quadruple> y = GenericStore.R128.columns(y_in);

      MatrixStore<Quadruple> Q = GenericStore.R128.makeWrapper(Q_in);
      MatrixStore<Quadruple> C = GenericStore.R128.makeWrapper(C_in);
      MatrixStore<Quadruple> Ae = GenericStore.R128.makeWrapper(Ae_in);
      MatrixStore<Quadruple> Be = GenericStore.R128.makeWrapper(be_in);
      MatrixStore<Quadruple> Ai = GenericStore.R128.makeWrapper(Ai_in);
      MatrixStore<Quadruple> Bi = GenericStore.R128.makeWrapper(bi_in);

      // Compute residuals in Quadruple precission
      MatrixStore<Quadruple> be1 = Be.subtract(Ae.multiply(x));
      double maxEqualityResidual = be1.aggregateAll(Aggregator.LARGEST).doubleValue();
      MatrixStore<Quadruple> bi1 = Bi.subtract(Ai.multiply(x));
      double maxInequalityResidual = bi1.negate().aggregateAll(Aggregator.MAXIMUM).doubleValue();
      double maxPrimalResidual = Math.max(maxEqualityResidual, maxInequalityResidual);
      MatrixStore<Quadruple> C1 = C.subtract(Q.multiply(x)).subtract(Ae.below(Ai).transpose().multiply(y));
      double maxGradientResidual = C1.negate().aggregateAll(Aggregator.LARGEST).norm();
      double maxComplementarySlackness = C1.transpose().multiply(bi1).get(0).norm();
      double relativeGradientResidual = maxGradientResidual/C_Size;
      final double relativeComplementarySlackness = maxComplementarySlackness/C_Size;
      Quadruple objectiveValue = Q.multiplyBoth(x).divide(2).subtract(x.transpose().multiply(C).get(0));
      if (maxPrimalResidual < epsPrimal && relativeGradientResidual < epsDual && relativeComplementarySlackness < epsSlack) {
         return Math.max(relativeComplementarySlackness, Math.max(maxPrimalResidual, relativeGradientResidual));
      } else {
         return Math.max(relativeComplementarySlackness, Math.max(maxPrimalResidual, relativeGradientResidual));
      }
   }

   static Optimisation.Result getConvexSolverSolutionResult(MatrixStore<Double> H, MatrixStore<Double> g, MatrixStore<Double> AE,
                                                            MatrixStore<Double> BE, MatrixStore<Double> AI, MatrixStore<Double> BI) {
      ConvexSolver.Builder builder = ConvexSolver.newBuilder();
      builder.objective(H, g);
      if (AE != null && BE != null) {
         builder.equalities(AE, BE);
      }
      if (AI != null && BI != null) {
         builder.inequalities(AI, BI);
      }
      Optimisation.Options options = new Optimisation.Options();
      options.sparse = false;
      //      options.validate=true;
      options.convex().solverSPD(Cholesky.R064::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(16));
      //      options.convex().solverSPD( LDL.R064.modified(1.0)::make).solverGeneral(LU.R064::make).iterative(NumberContext.of(16));
      final ConvexSolver convexModel = builder.build(options);
      final Optimisation.Result startValue = Optimisation.Result.of(Optimisation.State.APPROXIMATE, new double[H.getColDim()]);
      final Optimisation.Result result = convexModel.solve(startValue);
      if (result.getState().isFailure()) {
         int i = 0;
      }
      return result;
   }
}
