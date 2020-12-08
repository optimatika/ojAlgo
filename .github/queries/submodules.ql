/**
 * @id ojAlgo-submodules
 * @name ojAlgo submodules
 * @description Checks import statements to enforce an internal (sub)module structure
 * @kind problem
 * @problem.severity error
 * @precision high 
 */
import java

from  TopLevelType c, ImportType i
where i.getCompilationUnit() = c.getCompilationUnit()
  and (
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.optimisation.%") and c.getPackage().getName().matches("org.ojalgo.core.%"))
    or
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.optimisation.%") and c.getPackage().getName().matches("org.ojalgo.matrix.%"))
    or
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.optimisation.%") and c.getPackage().getName().matches("org.ojalgo.data.%"))
    or
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.data.%") and c.getPackage().getName().matches("org.ojalgo.core.%"))
    or
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.data.%") and c.getPackage().getName().matches("org.ojalgo.matrix.%"))
    or
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.data.%") and c.getPackage().getName().matches("org.ojalgo.optimisation.%"))
    or
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.matrix.%") and c.getPackage().getName().matches("org.ojalgo.core.%"))
  )
select i, "Illegal import"

