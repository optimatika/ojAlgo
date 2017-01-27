package org.ojalgo.optimisation.integer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TreeMap;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * @author fran
 */
class P20130225 {

    public static void main(final String[] args) {
        new P20130225();
    }

    public static TreeMap preCalculateCosts() {
        final TreeMap costs = new TreeMap();
        final double[] pred1 = { 14.8, 13.8, 12.9, 11.9, 10.9, 10.0, 9.2, 8.4, 7.77, 7.4, 7.5 };
        final double[] pred2 = { 24.1, 23.1, 22.1, 21.1, 20.2, 19.2, 18.2, 17.2, 16.4, 15.7, 15.4 };
        final double[] pred3 = { 7.9, 7.2, 6.6, 6.2, 5.9, 5.9, 6.1, 6.5, 7.1, 7.9, 8.8 };
        final double[] pred4 = { 8.2, 7.3, 6.4, 5.6, 5.0, 4.5, 4.1, 4.0, 4.1, 4.5, 5.2 };
        final double[] pred5 = { 26.3, 25.3, 24.3, 23.3, 22.3, 21.3, 20.3, 19.3, 18.4, 17.5, 16.9 };
        final double[] pred6 = { 19.7, 18.7, 17.7, 16.8, 15.8, 14.8, 13.8, 12.8, 11.8, 10.8, 9.8, 8.9, 8.0, 7.1, 6.2, 5.4, 4.7, 3.9, 3.3, 2.9, 2.8 };
        final double[] pred7 = { 32.1, 31.1, 30.1, 29.1, 28.1, 27.1, 26.1, 25.1, 24.1, 23.2, 22.5 };
        final double[] pred8 = { 33.7, 32.7, 31.7, 30.7, 29.7, 28.7, 27.7, 26.7, 25.7, 24.7, 23.7, 22.7, 21.7, 20.8, 20.0, 19.6 };
        final double[] pred9 = { 35.1, 34.1, 33.1, 32.1, 31.1, 30.1, 29.1, 28.1, 27.1, 26.1, 25.1, 24.1, 23.1, 22.1, 21.1, 20.1, 19.1, 18.1, 17.2, 16.4, 15.9 };
        final double[] pred10 = { 15.8, 14.8, 13.8, 12.8, 11.8, 10.8, 9.8, 8.9, 8.0, 7.1, 6.2, 5.3, 4.6, 3.9, 3.2, 2.6, 2.2, 1.8, 1.5, 1.5, 1.8 };
        final double[] pred11 = { 6.6, 5.8, 5.0, 4.2, 3.5, 2.9, 2.3, 1.9, 1.5, 1.2, 0.9, 0.7, 0.6, 0.6, 0.8, 1.0, 1.4, 2.0, 2.8, 3.6, 4.6 };
        final double[] pred12 = { 7.1, 6.1, 5.3, 4.5, 3.8, 3.1, 2.6, 2.1, 1.6, 1.3, 1.0, 0.8, 0.6, 0.5, 0.5, 0.6, 0.8, 1.2, 1.7, 2.4, 3.3 };
        final double[] pred13 = { 1.1, 0.7, 0.4, 0.3, 0.2, 0.2, 0.2, 0.3, 0.4, 0.5, 0.8, 1.1, 1.5, 2.1, 2.8, 3.5, 4.4, 5.3, 6.2, 7.2, 8.2 };
        final double[] pred14 = { 9.5, 8.6, 7.7, 6.9, 6.1, 5.3, 4.6, 4.0, 3.4, 2.8, 2.3, 1.9, 1.5, 1.2, 1.0, 0.8, 0.6, 0.4, 0.3, 0.3, 0.2, 0.2, 0.3, 0.5, 0.7,
                1.0, 1.5, 2.1, 2.9, 3.7, 4.7 };
        final double[] pred15 = { 0.9, 0.5, 0.3, 0.2, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.1, 0.2, 0.4, 0.7, 1.2, 1.8, 2.6, 3.5, 4.4 };
        final double[] pred16 = { 25.2, 24.2, 23.2, 22.2, 21.2, 20.2, 19.2, 18.2, 17.2, 16.2, 15.5 };
        final double[] pred17 = { 4.1, 3.3, 2.6, 2.0, 1.5, 1.1, 0.8, 0.5, 0.4, 0.2, 0.2, 0.1, 0.1, 0.1, 0.2, 0.3, 0.6, 1.0, 1.5, 2.3, 3.2 };
        final double[] pred18 = { 5.0, 4.2, 3.5, 2.9, 2.6, 2.4, 2.4, 2.6, 3.2, 3.9, 4.8 };
        final double[] pred19 = { 0.4, 0.2, 0.2, 0.1, 0.2, 0.3, 0.4, 0.5, 0.7, 1.0, 1.4, 1.9, 2.5, 3.1, 3.9, 4.7, 5.6, 6.6, 7.6, 8.6, 9.6 };
        final double[] pred20 = { 3.2, 2.5, 2.1, 1.7, 1.5, 1.4, 1.3, 1.3, 1.4, 1.6, 1.9, 2.3, 2.8, 3.4, 4.1, 4.9, 5.7, 6.7, 7.6, 8.6, 9.5 };
        final double[] pred21 = { 21.3, 20.3, 19.3, 18.3, 17.3, 16.3, 15.3, 14.4, 13.4, 12.5, 11.6, 10.7, 9.8, 9.0, 8.2, 7.6, 7.1, 6.7, 6.7, 6.9, 7.5 };
        final double[] pred22 = { 37.9, 36.9, 35.9, 34.9, 33.9, 32.9, 31.9, 30.9, 29.9, 28.9, 27.9, 26.9, 25.9, 24.9, 23.9, 22.9, 21.9, 20.9, 20.0, 19.2,
                18.9 };
        final double[] pred23 = { 17.6, 16.6, 15.6, 14.7, 13.7, 12.7, 11.8, 10.9, 10.0, 9.2, 8.4, 7.6, 6.8, 6.1, 5.4, 4.8, 4.2, 3.7, 3.2, 2.8, 2.4, 2.1, 1.9,
                1.7, 1.7, 1.7, 1.9, 2.2, 2.6, 3.2, 4.0 };
        final double[] pred24 = { 16.0, 15.0, 14.0, 13.0, 12.0, 11.1, 10.2, 9.2, 8.4, 7.5, 6.7, 5.9, 5.2, 4.6, 4.0, 3.5, 3.1, 2.9, 2.9, 3.1, 3.6 };
        final double[] pred25 = { 18.1, 17.1, 16.1, 15.1, 14.1, 13.1, 12.1, 11.1, 10.1, 9.1, 8.2, 7.2, 6.3, 5.5, 4.7, 3.9, 3.2, 2.7, 2.2, 1.9, 1.9 };
        final double[] pred26 = { 27.5, 26.5, 25.5, 24.5, 23.5, 22.5, 21.5, 20.5, 19.5, 18.5, 17.5, 16.5, 15.5, 14.5, 13.6, 12.6, 11.6, 10.7, 9.9, 9.4, 9.4 };
        final double[] pred27 = { 32.9, 31.9, 30.9, 29.9, 28.9, 27.9, 26.9, 25.9, 25.0, 24.2, 23.9 };
        final double[] pred28 = { 54.9, 53.9, 52.9, 51.9, 50.9, 49.9, 48.9, 47.9, 46.9, 45.9, 44.9, 43.9, 42.9, 41.9, 40.9, 39.9, 38.9, 37.9, 36.9, 36.0,
                35.3 };
        costs.put(0, pred1);
        costs.put(1, pred2);
        costs.put(2, pred3);
        costs.put(3, pred4);
        costs.put(4, pred5);
        costs.put(5, pred6);
        costs.put(6, pred7);
        costs.put(7, pred8);
        costs.put(8, pred9);
        costs.put(9, pred10);
        costs.put(10, pred11);
        costs.put(11, pred12);
        costs.put(12, pred13);
        costs.put(13, pred14);
        costs.put(14, pred15);
        costs.put(15, pred16);
        costs.put(16, pred17);
        costs.put(17, pred18);
        costs.put(18, pred19);
        costs.put(19, pred20);
        costs.put(20, pred21);
        costs.put(21, pred22);
        costs.put(22, pred23);
        costs.put(23, pred24);
        costs.put(24, pred25);
        costs.put(25, pred26);
        costs.put(26, pred27);
        costs.put(27, pred28);
        return costs;
    }

    static ExpressionsBasedModel makeModel() {

        final double alpha = 0.1;
        final TreeMap preCalculateCosts = P20130225.preCalculateCosts();
        final TreeMap variablesStation = new TreeMap();
        final TreeMap variablesUVStation = new TreeMap();
        final ArrayList allVariables = new ArrayList();

        for (int i = 0; i < preCalculateCosts.size(); i++) {
            final double[] costs = (double[]) preCalculateCosts.get(i);
            // Cost function = Min(sum(C_i_j*X_i_j + alpha*(sum(Ui + Vi))
            final int availableDocks = costs.length;
            final Variable u = new Variable("U_" + i).lower(BigDecimal.valueOf(0)).weight(BigDecimal.valueOf(alpha));
            final Variable v = new Variable("V_" + i).lower(BigDecimal.valueOf(0)).weight(BigDecimal.valueOf(alpha));

            allVariables.add(u);
            allVariables.add(v);
            final ArrayList uvVariables = new ArrayList();
            uvVariables.add(u);
            uvVariables.add(v);
            variablesUVStation.put(i, uvVariables);

            for (int j = 0; j < availableDocks; j++) {

                final double cost = costs[j];

                final Variable variable = new Variable("X_" + i + "_" + j).binary().weight(BigDecimal.valueOf(cost));

                if (variablesStation.containsKey(i)) {
                    final ArrayList vars = (ArrayList) variablesStation.get(i);
                    vars.add(variable);
                } else {
                    final ArrayList vars = new ArrayList();
                    vars.add(variable);
                    variablesStation.put(i, vars);
                }
                allVariables.add(variable);
            }
        }
        final ExpressionsBasedModel tmpIntegerModel = new ExpressionsBasedModel(allVariables);

        // Exp_total_bikes = sum(j*X_i_j) <= 91;
        final Expression expresion1 = tmpIntegerModel.addExpression("Exp_total_bikes");
        for (int i = 0; i < tmpIntegerModel.countVariables(); i++) {
            final Variable v = tmpIntegerModel.getVariable(i);
            final String name = v.getName();
            if (name.startsWith("X_")) {
                final String state = name.substring(name.lastIndexOf("_") + 1, name.length());
                expresion1.set(v, BigDecimal.valueOf((Integer.parseInt(state))));
            }
        }
        expresion1.upper(BigDecimal.valueOf(91));

        for (int i = 0; i < preCalculateCosts.size(); i++) {
            // Exp_i = sum(X_i_j) = 1
            final ArrayList varsStation = (ArrayList) variablesStation.get(i);

            final Expression expresion2 = tmpIntegerModel.addExpression("Exp_" + i);
            expresion2.setLinearFactorsSimple(varsStation);
            expresion2.level(BigDecimal.valueOf(1));
        }

        for (int i = 0; i < preCalculateCosts.size(); i++) {
            // Exp_UV_i = Ui - Vi + sum(j*X_i_j) = 5
            final Expression expresion3 = tmpIntegerModel.addExpression("Exp_UV_" + i);
            final ArrayList varsStation = (ArrayList) variablesStation.get(i);
            for (int j = 0; j < varsStation.size(); j++) {
                final Variable v = (Variable) varsStation.get(j);
                final String name = v.getName();
                final int state = Integer.parseInt(name.substring(name.lastIndexOf("_") + 1, name.length()));
                expresion3.set(v, state);
            }
            final ArrayList uvStation = (ArrayList) variablesUVStation.get(i);
            final Variable u = (Variable) uvStation.get(0);
            final Variable v = (Variable) uvStation.get(1);
            expresion3.set(u, BigDecimal.ONE);
            expresion3.set(v, BigDecimal.valueOf(-1));
            expresion3.level(BigDecimal.valueOf(5));
        }
        return tmpIntegerModel;
    }

    public P20130225() {
        try {

            final double alpha = 0.1;
            final TreeMap preCalculateCosts = P20130225.preCalculateCosts();
            final TreeMap variablesStation = new TreeMap();
            final TreeMap variablesUVStation = new TreeMap();
            final ArrayList allVariables = new ArrayList();

            for (int i = 0; i < preCalculateCosts.size(); i++) {
                final double[] costs = (double[]) preCalculateCosts.get(i);
                // Cost function = Min(sum(C_i_j*X_i_j + alpha*(sum(Ui + Vi))
                final int availableDocks = costs.length;
                final Variable u = new Variable("U_" + i).lower(BigDecimal.valueOf(0)).weight(BigDecimal.valueOf(alpha));
                final Variable v = new Variable("V_" + i).lower(BigDecimal.valueOf(0)).weight(BigDecimal.valueOf(alpha));

                allVariables.add(u);
                allVariables.add(v);
                final ArrayList uvVariables = new ArrayList();
                uvVariables.add(u);
                uvVariables.add(v);
                variablesUVStation.put(i, uvVariables);

                for (int j = 0; j < availableDocks; j++) {

                    final double cost = costs[j];

                    final Variable variable = new Variable("X_" + i + "_" + j).binary().weight(BigDecimal.valueOf(cost));

                    if (variablesStation.containsKey(i)) {
                        final ArrayList vars = (ArrayList) variablesStation.get(i);
                        vars.add(variable);
                    } else {
                        final ArrayList vars = new ArrayList();
                        vars.add(variable);
                        variablesStation.put(i, vars);
                    }
                    allVariables.add(variable);
                }
            }
            final ExpressionsBasedModel model = new ExpressionsBasedModel(allVariables);

            // Exp_total_bikes = sum(j*X_i_j) <= 91;
            final Expression expresion1 = model.addExpression("Exp_total_bikes");
            for (int i = 0; i < model.countVariables(); i++) {
                final Variable v = model.getVariable(i);
                final String name = v.getName();
                if (name.startsWith("X_")) {
                    final String state = name.substring(name.lastIndexOf("_") + 1, name.length());
                    expresion1.set(v, new BigDecimal(state));
                }
            }
            expresion1.upper(BigDecimal.valueOf(91));

            for (int i = 0; i < preCalculateCosts.size(); i++) {
                // Exp_i = sum(X_i_j) = 1
                final ArrayList varsStation = (ArrayList) variablesStation.get(i);

                final Expression expresion2 = model.addExpression("Exp_" + i);
                expresion2.setLinearFactorsSimple(varsStation);
                expresion2.level(BigDecimal.valueOf(1));
            }

            for (int i = 0; i < preCalculateCosts.size(); i++) {
                // Exp_UV_i = Ui - Vi + sum(j*X_i_j) = 5
                final Expression expresion3 = model.addExpression("Exp_UV_" + i);
                final ArrayList varsStation = (ArrayList) variablesStation.get(i);
                for (int j = 0; j < varsStation.size(); j++) {
                    final Variable v = (Variable) varsStation.get(j);
                    final String name = v.getName();
                    final int state = Integer.parseInt(name.substring(name.lastIndexOf("_") + 1, name.length()));
                    expresion3.set(v, state);
                }
                final ArrayList uvStation = (ArrayList) variablesUVStation.get(i);
                final Variable u = (Variable) uvStation.get(0);
                final Variable v = (Variable) uvStation.get(1);
                expresion3.set(u, BigDecimal.ONE);
                expresion3.set(v, BigDecimal.valueOf(-1));
                expresion3.level(BigDecimal.valueOf(5));
            }

            final Optimisation.Result result = model.minimise();

            System.out.println(result);

            for (int j = 0; j < allVariables.size(); j++) {
                final Variable var = (Variable) allVariables.get(j);
                System.out.println(var);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
