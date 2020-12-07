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
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.algebra.%") and c.getPackage().getName().matches("org.ojalgo.array.%"))
    or
    (i.getImportedType().getPackage().getName().matches("org.ojalgo.optimisation.%") and c.getPackage().getName().matches("org.ojalgo.array.%"))
  )
select i, "Illegal import"

