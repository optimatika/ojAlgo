Dear Visitor,

In this directory you can find some problems which are difficult
to solve, at least with interior point methods. Basically, modelling
mistakes made these problems "crazy". But, these problems are excelent
examples to test numerical robustnes of a solver and observe numerically
bad behavior.

Here are short stories about these problems:

de063155
de063157
           These problems comes from an early version of the water
           management system Aquarius, developed at TU Delft. By building
           the optimization problem, almost every modelling mistakes were
           happen, for example wrong measurements which resulted in extreme
           large and small coefficients. No tolerances were used by the 
           computation of the matrix values. Actually, I found that these 
           problems are unsolvable by most of the solvers. 
           A note: the large values in the RHS are not to be ignored, those 
           constaints are binding at the optimum !

de080285   Similar to the previous two problems. Very small values are
           presented in the matrix, which "blow-up" the model if scaling
           is applied. Additionally (which, I believe more strange for IPMs), 
           for some of the free variables a lower bound -10000.0 was introduced.

gen
gen1
gen2
gen4
           These problems are approximations in L1 norm and come from
           image reconstruction problems. Some of them were created by
           using fixed point format, which resulted in different relative
           accuracy in the coefficients (I believe, this is the source
           of the extreme numerical instability). The problems are
           additionally degenerate and it is very difficult to solve them
           to 8-digit accuracy.

l30        Originally a convex optimization problem, but formulated as
           LP. Many free variables, heavy fill-in.


iprob      Created artifically. All variables are free. Very badly
           conditioned. Optimal value (by exact aritmetic) has to be
           2990.00 
            



ADDITIONS ARE WELCOME !


Csaba Meszaros,
meszaros@sztaki.hu



