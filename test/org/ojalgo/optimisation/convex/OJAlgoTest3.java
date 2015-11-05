package org.ojalgo.optimisation.convex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;

public class OJAlgoTest3 {

    /**
     * @param args the command line arguments Objective: allocate the maximum qty, and try to keep
     *        proportionality between customer.
     */
    public static void main(final String[] args) {

        /*
         * 5 Products with associated Qty.
         */
        final Map<String, Integer> constraintsProduct = new HashMap<>();
        constraintsProduct.put("PRODUCT_1", 296_000);
        constraintsProduct.put("PRODUCT_2", 888_000);
        constraintsProduct.put("PRODUCT_3", 636_000);
        constraintsProduct.put("PRODUCT_4", 220_000);
        constraintsProduct.put("PRODUCT_5", 0);
        final double stockTotal = constraintsProduct.values().stream().mapToDouble(e -> e.doubleValue()).sum();
        BasicLogger.debug("STOCK_TOTAL " + stockTotal);

        /*
         * Demand of each Customers
         */
        final Map<String, Integer> constraintsCustomer = new LinkedHashMap<>();
        constraintsCustomer.put("CUSTOMER_A_1", 72_000);
        constraintsCustomer.put("CUSTOMER_A_2", 44_000);
        constraintsCustomer.put("CUSTOMER_A_3", 12_000);
        constraintsCustomer.put("CUSTOMER_A_4", 36_000);
        constraintsCustomer.put("CUSTOMER_A_5", 0);
        constraintsCustomer.put("CUSTOMER_A_6", 36_000);
        constraintsCustomer.put("CUSTOMER_A_7", 12_000);
        constraintsCustomer.put("CUSTOMER_A_8", 0);
        constraintsCustomer.put("CUSTOMER_A_9", 0);
        constraintsCustomer.put("CUSTOMER_A_10", 12_000);
        constraintsCustomer.put("CUSTOMER_A_11", 12_000);
        constraintsCustomer.put("CUSTOMER_A_12", 0);
        constraintsCustomer.put("CUSTOMER_A_13", 0);
        constraintsCustomer.put("CUSTOMER_A_14", 0);
        constraintsCustomer.put("CUSTOMER_B_1", 24_000);
        constraintsCustomer.put("CUSTOMER_B_2", 12_000);
        constraintsCustomer.put("CUSTOMER_B_3", 300);
        constraintsCustomer.put("CUSTOMER_C_1", 24_000);
        constraintsCustomer.put("CUSTOMER_D_1", 20_000);
        constraintsCustomer.put("CUSTOMER_E_1", 12_000);
        constraintsCustomer.put("CUSTOMER_F_1", 12_000);
        constraintsCustomer.put("CUSTOMER_G_1", 72_000);
        constraintsCustomer.put("CUSTOMER_H_1", 28_000);
        constraintsCustomer.put("CUSTOMER_I_1", 24_000);
        constraintsCustomer.put("CUSTOMER_J_1", 16_000);
        constraintsCustomer.put("CUSTOMER_K_1", 24_000);
        constraintsCustomer.put("CUSTOMER_L_1", 56_000);
        final double demandTotal = constraintsCustomer.values().stream().mapToDouble(e -> e.doubleValue()).sum();
        BasicLogger.debug("DEMAND_TOTAL " + demandTotal);

        /*
         * User constraint. In this case, user want to allocate 236_000 Qty on Customer_A_*
         */
        final double userConstraintQty = 236_000;
        final List<String> userConstraint = new ArrayList<>();
        userConstraint.add("CUSTOMER_A_1|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_2|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_3|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_4|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_5|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_6|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_7|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_8|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_9|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_10|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_11|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_12|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_13|PRODUCT_2");
        userConstraint.add("CUSTOMER_A_14|PRODUCT_2");

        //List of Variable Names
        final List<String> variablesName = new ArrayList<>();
        variablesName.add("CUSTOMER_A_1|PRODUCT_2");
        variablesName.add("CUSTOMER_A_2|PRODUCT_2");
        variablesName.add("CUSTOMER_A_3|PRODUCT_2");
        variablesName.add("CUSTOMER_A_4|PRODUCT_2");
        variablesName.add("CUSTOMER_A_5|PRODUCT_2");
        variablesName.add("CUSTOMER_A_6|PRODUCT_2");
        variablesName.add("CUSTOMER_A_7|PRODUCT_2");
        variablesName.add("CUSTOMER_A_8|PRODUCT_2");
        variablesName.add("CUSTOMER_A_9|PRODUCT_2");
        variablesName.add("CUSTOMER_A_10|PRODUCT_2");
        variablesName.add("CUSTOMER_A_11|PRODUCT_2");
        variablesName.add("CUSTOMER_A_12|PRODUCT_2");
        variablesName.add("CUSTOMER_A_13|PRODUCT_2");
        variablesName.add("CUSTOMER_A_14|PRODUCT_2");
        variablesName.add("CUSTOMER_B_1|PRODUCT_2");
        variablesName.add("CUSTOMER_B_1|PRODUCT_3");
        variablesName.add("CUSTOMER_B_2|PRODUCT_2");
        variablesName.add("CUSTOMER_B_2|PRODUCT_3");
        variablesName.add("CUSTOMER_B_3|PRODUCT_2");
        variablesName.add("CUSTOMER_B_3|PRODUCT_3");
        variablesName.add("CUSTOMER_C_1|PRODUCT_2");
        variablesName.add("CUSTOMER_C_1|PRODUCT_3");
        variablesName.add("CUSTOMER_D_1|PRODUCT_4");
        variablesName.add("CUSTOMER_D_1|PRODUCT_5");
        variablesName.add("CUSTOMER_D_1|PRODUCT_2");
        variablesName.add("CUSTOMER_D_1|PRODUCT_3");
        variablesName.add("CUSTOMER_E_1|PRODUCT_1");
        variablesName.add("CUSTOMER_F_1|PRODUCT_4");
        variablesName.add("CUSTOMER_F_1|PRODUCT_5");
        variablesName.add("CUSTOMER_F_1|PRODUCT_2");
        variablesName.add("CUSTOMER_F_1|PRODUCT_3");
        variablesName.add("CUSTOMER_G_1|PRODUCT_4");
        variablesName.add("CUSTOMER_G_1|PRODUCT_5");
        variablesName.add("CUSTOMER_G_1|PRODUCT_2");
        variablesName.add("CUSTOMER_G_1|PRODUCT_3");
        variablesName.add("CUSTOMER_H_1|PRODUCT_4");
        variablesName.add("CUSTOMER_H_1|PRODUCT_5");
        variablesName.add("CUSTOMER_H_1|PRODUCT_2");
        variablesName.add("CUSTOMER_H_1|PRODUCT_3");
        variablesName.add("CUSTOMER_I_1|PRODUCT_4");
        variablesName.add("CUSTOMER_I_1|PRODUCT_5");
        variablesName.add("CUSTOMER_I_1|PRODUCT_2");
        variablesName.add("CUSTOMER_I_1|PRODUCT_3");
        variablesName.add("CUSTOMER_J_1|PRODUCT_4");
        variablesName.add("CUSTOMER_J_1|PRODUCT_5");
        variablesName.add("CUSTOMER_J_1|PRODUCT_2");
        variablesName.add("CUSTOMER_J_1|PRODUCT_3");
        variablesName.add("CUSTOMER_K_1|PRODUCT_4");
        variablesName.add("CUSTOMER_K_1|PRODUCT_5");
        variablesName.add("CUSTOMER_K_1|PRODUCT_2");
        variablesName.add("CUSTOMER_K_1|PRODUCT_3");
        variablesName.add("CUSTOMER_L_1|PRODUCT_1");

        /*
         *
         */
        final ExpressionsBasedModel model = new ExpressionsBasedModel();

        final List<Variable> variables = new ArrayList<>();

        /*
         * Create All Variables:
         */
        BasicLogger.debug("---- Variable creation ------");
        variablesName.forEach(name -> {
            final Variable var = Variable.make(name).lower(0.0).weight(1.0);
            model.addVariable(var);
            variables.add(var);
            BasicLogger.debug(var);
        });

        BasicLogger.debug("---- Constraints customers ------");
        //Apply Customers constraints.
        constraintsCustomer.entrySet().stream().filter(e -> !e.getKey().startsWith("CUSTOMER_A")) //CUSTOMER_A of Demand constraint because managed by user constraints.
                .forEach(entry -> {
                    final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
                    final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
                    constraint.upper(entry.getValue().doubleValue());
                    constraint.setLinearFactorsSimple(linked);
                    BasicLogger.debug(constraint);
                });

        BasicLogger.debug("---- User Constraints Customers ------");
        //Apply Product Type constraints.
        final List<Variable> userLinked = variables.stream().filter(v -> userConstraint.contains(v.getName())).collect(Collectors.toList());
        final Expression constraintUser = model.addExpression("CONSTRAINTS_USER_CUSTOMER_A");
        constraintUser.level(userConstraintQty);
        constraintUser.setLinearFactorsSimple(userLinked);
        BasicLogger.debug(constraintUser);

        BasicLogger.debug("---- Constraints Product ------");
        //Apply Product Type constraints.
        constraintsProduct.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().endsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            BasicLogger.debug(constraint);
        });

        /*
         * Objective expression. - Maximize the Sum of all variables - Minimize the Sum of square error vs
         * proportionality.
         */
        BasicLogger.debug("---- Objective  ------");
        final Expression objective = model.addExpression("OBJECTIVE").weight(-1.0);

        // - Maximize the Sum of all variables
        //objective.setLinearFactorsSimple(variables);
        BasicLogger.debug("---- Error formula ------");

        /*
         * Example: x1, x2, x3 linked variables for Customer X Error = ( (x1+x2+x3) / Target - 1 )^2 * ratio
         */
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            if (!linked.isEmpty() && (entry.getValue().doubleValue() > 0)) {
                final double demand = entry.getValue();
                final double ratio = demand / demandTotal;
                final double target = stockTotal * ratio;
                linked.forEach(v1 -> {
                    linked.forEach(v2 -> {
                        objective.set(v1, v2, ratio / (target * target));
                    });
                    objective.set(v1, (-2.0 * ratio) / target);
                });
            }
        });

        //- Maximise
        //model.options.validate = true;
        final Result result = model.maximise();
        BasicLogger.debug(result);

        // GOOD  OPTIMAL 560300.4702803734
        // WRONG OPTIMAL 560300.4015031556
        BasicLogger.debug("");
        BasicLogger.debug("");
        variables.forEach(v -> BasicLogger.debug(v.getName() + " = " + v.getValue().doubleValue()));

        /*
         * WRONG ALLOCATION CUSTOMER_A_1|PRODUCT_2 = 236000.0 CUSTOMER_A_2|PRODUCT_2 = 0.0
         * CUSTOMER_A_3|PRODUCT_2 = 0.0 CUSTOMER_A_4|PRODUCT_2 = 0.0 CUSTOMER_A_5|PRODUCT_2 = 0.0
         * CUSTOMER_A_6|PRODUCT_2 = 0.0 CUSTOMER_A_7|PRODUCT_2 = 0.0 CUSTOMER_A_8|PRODUCT_2 = 0.0
         * CUSTOMER_A_9|PRODUCT_2 = 0.0 CUSTOMER_A_10|PRODUCT_2 = 0.0 CUSTOMER_A_11|PRODUCT_2 = 0.0
         * CUSTOMER_A_12|PRODUCT_2 = 0.0 CUSTOMER_A_13|PRODUCT_2 = 0.0 CUSTOMER_A_14|PRODUCT_2 = 0.0
         * CUSTOMER_B_1|PRODUCT_2 = 24000.0 CUSTOMER_B_1|PRODUCT_3 = 0.0 CUSTOMER_B_2|PRODUCT_2 = 12000.0
         * CUSTOMER_B_2|PRODUCT_3 = 0.0 CUSTOMER_B_3|PRODUCT_2 = 0.0 CUSTOMER_B_3|PRODUCT_3 = 300.0
         * CUSTOMER_C_1|PRODUCT_2 = 24000.0 CUSTOMER_C_1|PRODUCT_3 = 0.0 CUSTOMER_D_1|PRODUCT_4 = 20000.0
         * CUSTOMER_D_1|PRODUCT_5 = 0.0 CUSTOMER_D_1|PRODUCT_2 = 0.0 CUSTOMER_D_1|PRODUCT_3 = 0.0
         * CUSTOMER_E_1|PRODUCT_1 = 12000.0 CUSTOMER_F_1|PRODUCT_4 = 12000.0 CUSTOMER_F_1|PRODUCT_5 = 0.0
         * CUSTOMER_F_1|PRODUCT_2 = 0.0 CUSTOMER_F_1|PRODUCT_3 = 0.0 CUSTOMER_G_1|PRODUCT_4 = 72000.0
         * CUSTOMER_G_1|PRODUCT_5 = 0.0 CUSTOMER_G_1|PRODUCT_2 = 0.0 CUSTOMER_G_1|PRODUCT_3 = 0.0
         * CUSTOMER_H_1|PRODUCT_4 = 28000.0 CUSTOMER_H_1|PRODUCT_5 = 0.0 CUSTOMER_H_1|PRODUCT_2 = 0.0
         * CUSTOMER_H_1|PRODUCT_3 = 0.0 CUSTOMER_I_1|PRODUCT_4 = 24000.0 CUSTOMER_I_1|PRODUCT_5 = 0.0
         * CUSTOMER_I_1|PRODUCT_2 = 0.0 CUSTOMER_I_1|PRODUCT_3 = 0.0 CUSTOMER_J_1|PRODUCT_4 = 16000.0
         * CUSTOMER_J_1|PRODUCT_5 = 0.0 CUSTOMER_J_1|PRODUCT_2 = 0.0 CUSTOMER_J_1|PRODUCT_3 = 0.0
         * CUSTOMER_K_1|PRODUCT_4 = 24000.0 CUSTOMER_K_1|PRODUCT_5 = 0.0 CUSTOMER_K_1|PRODUCT_2 = 0.0
         * CUSTOMER_K_1|PRODUCT_3 = 0.0 CUSTOMER_L_1|PRODUCT_1 = 56000.0
         */
        /*
         * GOOD ALLOCATION CUSTOMER_A_1|PRODUCT_2 = 80150.9434796 CUSTOMER_A_2|PRODUCT_2 = 48981.1320493
         * CUSTOMER_A_3|PRODUCT_2 = 13358.4905589 CUSTOMER_A_4|PRODUCT_2 = 40075.4716767
         * CUSTOMER_A_5|PRODUCT_2 = 0.0 CUSTOMER_A_6|PRODUCT_2 = 40075.4716767 CUSTOMER_A_7|PRODUCT_2 =
         * 13358.4905589 CUSTOMER_A_8|PRODUCT_2 = 0.0 CUSTOMER_A_9|PRODUCT_2 = 0.0 CUSTOMER_A_10|PRODUCT_2 =
         * 0.0 CUSTOMER_A_11|PRODUCT_2 = 0.0 CUSTOMER_A_12|PRODUCT_2 = 0.0 CUSTOMER_A_13|PRODUCT_2 = 0.0
         * CUSTOMER_A_14|PRODUCT_2 = 0.0 CUSTOMER_B_1|PRODUCT_2 = 24000.0 CUSTOMER_B_1|PRODUCT_3 = 0.0
         * CUSTOMER_B_2|PRODUCT_2 = 12000.0 CUSTOMER_B_2|PRODUCT_3 = 0.0 CUSTOMER_B_3|PRODUCT_2 = 0.0
         * CUSTOMER_B_3|PRODUCT_3 = 300.0 CUSTOMER_C_1|PRODUCT_2 = 24000.0 CUSTOMER_C_1|PRODUCT_3 = 0.0
         * CUSTOMER_D_1|PRODUCT_4 = 20000.0 CUSTOMER_D_1|PRODUCT_5 = 0.0 CUSTOMER_D_1|PRODUCT_2 = 0.0
         * CUSTOMER_D_1|PRODUCT_3 = 0.0 CUSTOMER_E_1|PRODUCT_1 = 12000.0 CUSTOMER_F_1|PRODUCT_4 = 12000.0
         * CUSTOMER_F_1|PRODUCT_5 = 0.0 CUSTOMER_F_1|PRODUCT_2 = 0.0 CUSTOMER_F_1|PRODUCT_3 = 0.0
         * CUSTOMER_G_1|PRODUCT_4 = 72000.0 CUSTOMER_G_1|PRODUCT_5 = 0.0 CUSTOMER_G_1|PRODUCT_2 = 0.0
         * CUSTOMER_G_1|PRODUCT_3 = 0.0 CUSTOMER_H_1|PRODUCT_4 = 28000.0 CUSTOMER_H_1|PRODUCT_5 = 0.0
         * CUSTOMER_H_1|PRODUCT_2 = 0.0 CUSTOMER_H_1|PRODUCT_3 = 0.0 CUSTOMER_I_1|PRODUCT_4 = 24000.0
         * CUSTOMER_I_1|PRODUCT_5 = 0.0 CUSTOMER_I_1|PRODUCT_2 = 0.0 CUSTOMER_I_1|PRODUCT_3 = 0.0
         * CUSTOMER_J_1|PRODUCT_4 = 16000.0 CUSTOMER_J_1|PRODUCT_5 = 0.0 CUSTOMER_J_1|PRODUCT_2 = 0.0
         * CUSTOMER_J_1|PRODUCT_3 = 0.0 CUSTOMER_K_1|PRODUCT_4 = 24000.0 CUSTOMER_K_1|PRODUCT_5 = 0.0
         * CUSTOMER_K_1|PRODUCT_2 = 0.0 CUSTOMER_K_1|PRODUCT_3 = 0.0 CUSTOMER_L_1|PRODUCT_1 = 56000.0
         */
    }
}
