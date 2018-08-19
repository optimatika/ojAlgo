package org.ojalgo.optimisation.convex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;

public abstract class P20150720 {

    public static PrimitiveDenseStore SOL_1_CPLEX = PrimitiveDenseStore.FACTORY
            .columns(new double[] { 399.999999654, 249.999999654, 149.999999654, 0.0, 0.0, 0.0, 0.0, 49.9999996543, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    -0.564712643687, -0.564712643692, -0.564712643701, -0.564712643747, -0.574712643678 });
    public static PrimitiveDenseStore SOL_2_CPLEX = PrimitiveDenseStore.FACTORY
            .columns(new double[] { 22988.5054745, 14367.8158165, 8620.68937468, 0.0, 0.0, 0.0, 0.0, 2873.56291279, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 });
    public static PrimitiveDenseStore SOL_3_CPLEX = PrimitiveDenseStore.FACTORY.columns(new double[] { 74542.7288388, 48961.3064461, 13408.93322, 40061.705994,
            0.0, 40061.705994, 13408.93322, 0.0, 7.04812228046, 2773.81908243, 2773.81908243, 0.0, 0.0, 0.0, 11604.5308488, 12395.4691449, 4339.53410099,
            7660.46589271, 78.8113907425, 221.188602961, 11604.5308488, 12395.4691449, 10813.3798123, 0.0, 9186.62018135, 0.0, 11999.9999937, 0.0, 0.0,
            4339.53410099, 7660.46589271, 0.0, 0.0, 47953.0468934, 24046.9531003, 0.0, 0.0, 14244.4816129, 13755.5183808, 12395.4691449, 0.0, 11604.5308488,
            0.0, 0.0, 0.0, 6679.71873106, 9320.28126264, 12395.4691449, 0.0, 11604.5308488, 0.0, 55999.9999937 });

    public static ExpressionsBasedModel buildModel1() {

        //List of Variable Names
        final List<String> variablesName = new ArrayList<>();
        variablesName.add("CUSTOMER_1|PRODUCT_TYPE_1");
        variablesName.add("CUSTOMER_2|PRODUCT_TYPE_1");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_1");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_2");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_3");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_4");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_5");
        variablesName.add("CUSTOMER_4|PRODUCT_TYPE_1");
        variablesName.add("CUSTOMER_4|PRODUCT_TYPE_2");
        variablesName.add("CUSTOMER_5|PRODUCT_TYPE_2");
        variablesName.add("CUSTOMER_5|PRODUCT_TYPE_3");
        variablesName.add("CUSTOMER_5|PRODUCT_TYPE_4");
        variablesName.add("CUSTOMER_6|PRODUCT_TYPE_2");
        variablesName.add("CUSTOMER_6|PRODUCT_TYPE_3");
        variablesName.add("CUSTOMER_6|PRODUCT_TYPE_4");
        variablesName.add("CUSTOMER_6|PRODUCT_TYPE_5");

        /*
         * Constraints for each Customer = Demand of each Customer Sum of all variable linked to the customer
         * <= Demand.
         */
        final Map<String, Integer> constraintsCustomer = new LinkedHashMap<>();
        constraintsCustomer.put("CUSTOMER_1", 40_000);
        constraintsCustomer.put("CUSTOMER_2", 25_000);
        constraintsCustomer.put("CUSTOMER_3", 15_000);
        constraintsCustomer.put("CUSTOMER_4", 5_000);
        constraintsCustomer.put("CUSTOMER_5", 2_000);

        final double demandTotal = constraintsCustomer.values().stream().mapToDouble(e -> e.doubleValue()).sum();

        /*
         * Constraits for each product type = Stock per product type. Sum of all variable linked to this
         * product type <=
         */
        final Map<String, Integer> constraintsProduct = new HashMap<>();
        constraintsProduct.put("PRODUCT_TYPE_1", 50_000);
        constraintsProduct.put("PRODUCT_TYPE_2", 0);
        constraintsProduct.put("PRODUCT_TYPE_3", 0);
        constraintsProduct.put("PRODUCT_TYPE_4", 0);
        constraintsProduct.put("PRODUCT_TYPE_5", 0);
        final double stockTotal = constraintsProduct.values().stream().mapToDouble(e -> e.doubleValue()).sum();

        /*
         *
         */
        final ExpressionsBasedModel model = new ExpressionsBasedModel();

        final List<Variable> variables = new ArrayList<>();

        //Create All Variables:
        //BasicLogger.debug("---- Variable creation ------");
        variablesName.forEach(name -> {
            final Variable var = Variable.make(name).integer(true).lower(0.0);
            model.addVariable(var);
            variables.add(var);
            //BasicLogger.debug(var);
        });

        //BasicLogger.debug("---- Constraints customers ------");
        //Apply Customers constraints.
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.lower(0.0).upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            linked.forEach(v -> v.upper(entry.getValue().doubleValue()));
            //BasicLogger.debug(constraint);
        });

        //BasicLogger.debug("---- Constraints Product ------");
        //Apply Product Type constraints.
        constraintsProduct.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().endsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.lower(0.0).upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            //BasicLogger.debug(constraint);
        });

        /*
         * Objective expression. - Maximize the Sum of all variables - Minimize the Sum of square error vs
         * proportionality.
         */
        //BasicLogger.debug("---- Objective  ------");
        final Expression objective = model.addExpression("OBJECTIVE").weight(1.0);

        // - Maximize the Sum of all variables
        objective.setLinearFactorsSimple(variables);

        final List<Variable> errors = new ArrayList<>();
        //BasicLogger.debug("---- Error formula ------");
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            if (!linked.isEmpty() && (entry.getValue().doubleValue() > 0)) {
                final Variable error = Variable.make("ERROR:" + entry.getKey());
                model.addVariable(error);
                errors.add(error);

                final Expression errorExp = model.addExpression("ERROR_EXP:" + entry.getKey()).level(stockTotal / demandTotal);
                linked.forEach(v -> errorExp.set(v, 1.0 / entry.getValue().doubleValue()));
                errorExp.set(error, -1.0);

                //- Squared of error.
                objective.set(error, error, -1.0);
            }
        });
        return model;
    }

    public static ExpressionsBasedModel buildModel2() {

        //List of Variable Names
        final List<String> variablesName = new ArrayList<>();
        variablesName.add("CUSTOMER_1|PRODUCT_TYPE_1");
        variablesName.add("CUSTOMER_2|PRODUCT_TYPE_1");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_1");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_2");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_3");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_4");
        variablesName.add("CUSTOMER_3|PRODUCT_TYPE_5");
        variablesName.add("CUSTOMER_4|PRODUCT_TYPE_1");
        variablesName.add("CUSTOMER_4|PRODUCT_TYPE_2");
        variablesName.add("CUSTOMER_5|PRODUCT_TYPE_2");
        variablesName.add("CUSTOMER_5|PRODUCT_TYPE_3");
        variablesName.add("CUSTOMER_5|PRODUCT_TYPE_4");
        variablesName.add("CUSTOMER_6|PRODUCT_TYPE_2");
        variablesName.add("CUSTOMER_6|PRODUCT_TYPE_3");
        variablesName.add("CUSTOMER_6|PRODUCT_TYPE_4");
        variablesName.add("CUSTOMER_6|PRODUCT_TYPE_5");

        /*
         * Constraints for each Customer = Demand of each Customer Sum of all variable linked to the customer
         * <= Demand.
         */
        final Map<String, Integer> constraintsCustomer = new LinkedHashMap<>();
        constraintsCustomer.put("CUSTOMER_1", 40_000);
        constraintsCustomer.put("CUSTOMER_2", 25_000);
        constraintsCustomer.put("CUSTOMER_3", 15_000);
        constraintsCustomer.put("CUSTOMER_4", 5_000);
        constraintsCustomer.put("CUSTOMER_5", 2_000);

        final double demandTotal = constraintsCustomer.values().stream().mapToDouble(e -> e.doubleValue()).sum();

        /*
         * Constraits for each product type = Stock per product type. Sum of all variable linked to this
         * product type <=
         */
        final Map<String, Integer> constraintsProduct = new HashMap<>();
        constraintsProduct.put("PRODUCT_TYPE_1", 50_000);
        constraintsProduct.put("PRODUCT_TYPE_2", 0);
        constraintsProduct.put("PRODUCT_TYPE_3", 0);
        constraintsProduct.put("PRODUCT_TYPE_4", 0);
        constraintsProduct.put("PRODUCT_TYPE_5", 0);
        final double stockTotal = constraintsProduct.values().stream().mapToDouble(e -> e.doubleValue()).sum();

        /*
         *
         */
        final ExpressionsBasedModel model = new ExpressionsBasedModel();

        final List<Variable> variables = new ArrayList<>();

        //Create All Variables:
        //BasicLogger.debug("---- Variable creation ------");
        variablesName.forEach(name -> {
            final Variable var = Variable.make(name).lower(0.0);
            model.addVariable(var);
            variables.add(var);
            //BasicLogger.debug(var);
        });

        //BasicLogger.debug("---- Constraints customers ------");
        //Apply Customers constraints.
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            //BasicLogger.debug(constraint);
        });

        //BasicLogger.debug("---- Constraints Product ------");
        //Apply Product Type constraints.
        constraintsProduct.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().endsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            //BasicLogger.debug(constraint);
        });

        /*
         * Objective expression. - Maximize the Sum of all variables - Minimize the Sum of square error vs
         * proportionality.
         */
        //BasicLogger.debug("---- Objective  ------");
        final Expression objective = model.addExpression("OBJECTIVE").weight(1.0);

        // - Maximize the Sum of all variables
        objective.setLinearFactorsSimple(variables);

        //BasicLogger.debug("---- Error formula ------");

        /*
         * Example: x1, x2, x3 linked variables for Customer X Error = ( (x1+x2+x3) -
         * (SupplyTotal/DemandTotal) * DemandCustomerX )^2
         */
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            if (!linked.isEmpty() && (entry.getValue().doubleValue() > 0)) {
                linked.forEach(v1 -> {
                    linked.forEach(v2 -> {
                        objective.set(v1, v2, -1);
                    });
                    objective.set(v1, ((2 * stockTotal) / demandTotal) * entry.getValue().doubleValue());
                });
            }
        });
        return model;
    }

    /**
     * Sent in 2015-09-03
     */
    public static ExpressionsBasedModel buildModel3() {

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
        // BasicLogger.debug("STOCK_TOTAL " + stockTotal);

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
        // BasicLogger.debug("DEMAND_TOTAL " + demandTotal);

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
        // BasicLogger.debug("---- Variable creation ------");
        variablesName.forEach(name -> {
            final Variable var = Variable.make(name).lower(0.0).weight(1.0);
            model.addVariable(var);
            variables.add(var);
            //BasicLogger.debug(var);
        });

        // BasicLogger.debug("---- Constraints customers ------");
        //Apply Customers constraints.
        constraintsCustomer.entrySet().stream().filter(e -> !e.getKey().startsWith("CUSTOMER_A")) //CUSTOMER_A of Demand constraint because managed by user constraints.
                .forEach(entry -> {
                    final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
                    final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
                    constraint.upper(entry.getValue().doubleValue());
                    constraint.setLinearFactorsSimple(linked);
                    // BasicLogger.debug(constraint);
                });

        // BasicLogger.debug("---- User Constraints Customers ------");
        //Apply Product Type constraints.
        final List<Variable> userLinked = variables.stream().filter(v -> userConstraint.contains(v.getName())).collect(Collectors.toList());
        final Expression constraintUser = model.addExpression("CONSTRAINTS_USER_CUSTOMER_A");
        constraintUser.level(userConstraintQty);
        constraintUser.setLinearFactorsSimple(userLinked);
        // BasicLogger.debug(constraintUser);

        // BasicLogger.debug("---- Constraints Product ------");
        //Apply Product Type constraints.
        constraintsProduct.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().endsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            // BasicLogger.debug(constraint);
        });

        /*
         * Objective expression. - Maximize the Sum of all variables - Minimize the Sum of square error vs
         * proportionality.
         */
        // BasicLogger.debug("---- Objective  ------");
        final Expression objective = model.addExpression("OBJECTIVE").weight(-1.0);

        // - Maximize the Sum of all variables
        //objective.setLinearFactorsSimple(variables);
        // BasicLogger.debug("---- Error formula ------");

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
        return model;
    }

    /**
     * @param args the command line arguments Objective: allocate the maximum qty, and try to keep
     *        proportionality between customer.
     */
    public static void main(final String[] args) {

        final ExpressionsBasedModel model = P20150720.buildModel3();

        //- Maximise -(Sum of Square error)
        model.relax(true);
        // model.options.debug(ConvexSolver.class);
        final Result result = model.maximise();

        BasicLogger.debug(result);
        //BasicLogger.debug(model);
    }

}
