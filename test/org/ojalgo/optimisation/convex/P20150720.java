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

class P20150720 {

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
        BasicLogger.debug("---- Variable creation ------");
        variablesName.forEach(name -> {
            final Variable var = Variable.make(name).integer(true).lower(0.0);
            model.addVariable(var);
            variables.add(var);
            BasicLogger.debug(var);
        });

        BasicLogger.debug("---- Constraints customers ------");
        //Apply Customers constraints.
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.lower(0.0).upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            linked.forEach(v -> v.upper(entry.getValue().doubleValue()));
            BasicLogger.debug(constraint);
        });

        BasicLogger.debug("---- Constraints Product ------");
        //Apply Product Type constraints.
        constraintsProduct.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().endsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.lower(0.0).upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            BasicLogger.debug(constraint);
        });

        /*
         * Objective expression. - Maximize the Sum of all variables - Minimize the Sum of square error vs
         * proportionality.
         */
        BasicLogger.debug("---- Objective  ------");
        final Expression objective = model.addExpression("OBJECTIVE").weight(1.0);

        // - Maximize the Sum of all variables
        objective.setLinearFactorsSimple(variables);

        final List<Variable> errors = new ArrayList<>();
        BasicLogger.debug("---- Error formula ------");
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            if (!linked.isEmpty() && (entry.getValue().doubleValue() > 0)) {
                final Variable error = Variable.make("ERROR:" + entry.getKey());
                model.addVariable(error);
                errors.add(error);

                final Expression errorExp = model.addExpression("ERROR_EXP:" + entry.getKey()).level(stockTotal / demandTotal);
                linked.forEach(v -> errorExp.setLinearFactor(v, 1.0 / entry.getValue().doubleValue()));
                errorExp.setLinearFactor(error, -1.0);

                //- Squared of error.
                objective.setQuadraticFactor(error, error, -1.0);
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
        BasicLogger.debug("---- Variable creation ------");
        variablesName.forEach(name -> {
            final Variable var = Variable.make(name).lower(0.0);
            model.addVariable(var);
            variables.add(var);
            BasicLogger.debug(var);
        });

        BasicLogger.debug("---- Constraints customers ------");
        //Apply Customers constraints.
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            final Expression constraint = model.addExpression("CONSTRAINTS_" + entry.getKey());
            constraint.upper(entry.getValue().doubleValue());
            constraint.setLinearFactorsSimple(linked);
            BasicLogger.debug(constraint);
        });

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
        final Expression objective = model.addExpression("OBJECTIVE").weight(1.0);

        // - Maximize the Sum of all variables
        objective.setLinearFactorsSimple(variables);

        BasicLogger.debug("---- Error formula ------");

        /*
         * Example: x1, x2, x3 linked variables for Customer X Error = ( (x1+x2+x3) -
         * (SupplyTotal/DemandTotal) * DemandCustomerX )^2
         */
        constraintsCustomer.entrySet().forEach(entry -> {
            final List<Variable> linked = variables.stream().filter(v -> v.getName().startsWith(entry.getKey())).collect(Collectors.toList());
            if (!linked.isEmpty() && (entry.getValue().doubleValue() > 0)) {
                linked.forEach(v1 -> {
                    linked.forEach(v2 -> {
                        objective.setQuadraticFactor(v1, v2, -1);
                    });
                    objective.setLinearFactor(v1, ((2 * stockTotal) / demandTotal) * entry.getValue().doubleValue());
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

        final ExpressionsBasedModel model = P20150720.buildModel1();

        //- Maximise -(Sum of Square error)
        model.relax(true);
        // model.options.debug(ConvexSolver.class);
        final Result result = model.maximise();

        BasicLogger.debug(result);
        BasicLogger.debug(model);
    }

}
