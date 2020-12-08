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
    (c.getPackage().getName().matches("org.ojalgo.array.%") and i.getImportedType().getPackage().getName().matches("org.ojalgo.algebra.%"))
    or
    (c.getPackage().getName().matches("org.ojalgo.array.%") and i.getImportedType().getPackage().getName().matches("org.ojalgo.matrix.%"))
  )
select i, "Illegal import"

