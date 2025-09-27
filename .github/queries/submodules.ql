/**
 * @id ojalgo-submodules
 * @name ojAlgo submodules
 * @description Checks import statements to enforce an internal (sub)module structure
 * @kind problem
 * @problem.severity error
 * @precision high 
 */
import java

from TopLevelType c, ImportType i, string importedPkg, string importingPkg, string importedRoot, string policy
where i.getCompilationUnit() = c.getCompilationUnit()
  and importedPkg = i.getImportedType().getPackage().getName()
  and importingPkg = c.getPackage().getName()
  and (
    // Case A: org.ojalgo.data% imported from outside org.ojalgo.data%
    (
      importedPkg.matches("org.ojalgo.data%") and not importingPkg.matches("org.ojalgo.data%") and
      importedRoot = "org.ojalgo.data" and policy = "within org.ojalgo.data"
    )
    or
    // Case B: org.ojalgo.optimisation% imported from outside allowed trees (optimisation or data)
    (
      importedPkg.matches("org.ojalgo.optimisation%") and not (
        importingPkg.matches("org.ojalgo.optimisation%") or importingPkg.matches("org.ojalgo.data%")
      ) and
      importedRoot = "org.ojalgo.optimisation" and policy = "within org.ojalgo.optimisation or org.ojalgo.data"
    )
  )
select i,
  "Illegal import: " + importedPkg + " -> " + importingPkg + " (allowed: " + policy + ")"