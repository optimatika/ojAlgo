This file is 00readme.qp describing an available set of convex quadratic
programming (QP) problems.


Introduction
------------


The QP problems are assumed to be in the following form:

min f(x) = c_0 + c^T x + 1/2 x^T Q x,  Q symmetric and positive
                                       semidefinite
subject to      A x = b,
                l <= x <= u.

For further details see I. Maros, Cs. Meszaros: A Repository of Convex
Quadratic Programming Problems, Optimization Methods and Software, 1999.

The set of convex quadratic problems can be found in the following
three compressed (zipped) files:  QPDATA1, QPDATA2 and QPDATA3.  The
same set of problems is also available and can be downloaded from
http://www.doc.ic.ac.uk/~im/ by visiting the title "A Selection of Data
Files" and clicking on each of the three zip files.  They are organized
in the following way.  QPDATA1 contains 76 problems from the CUTE
library. 46 problems provided by the Brunel optimization group are in
QPDATA2, while the other 16 problems from miscellaneous sources are
located in QPDATA3.  Data in some problem files contain exponents.  The
exponent indicator is always `e'.  All the files have the .qps
extension.

There is another web site holding the same set of problems in a slightly
different structure:  ftp://ftp.sztaki.hu/pub/oplab/QPDATA is an
anonymous ftp site with three subdirectories corresponding to the above
three zip files.  Here, the subdirectories contain individual zip files
of the problems.

The zipped files can be unzipped by using either "pkunzip", or the
freely available "unzip" program.  The latter can be downloaded from
Info-ZIP: http://www.cdrom.com/pub/infozip/UnZp.html.

Important note:  The .qps files are DOS type text files.  Therefore,
under UNIX it is advisable to use "unzip -aa ..." command to get the
correct UNIX text format of end-of-line.

Both web sites of QPDATA problems are maintained and new problem
instances may be added.  We keep the library open to further
contributions.  Anybody with such an intent should contact
one of the following people by Email:

Istvan Maros:  i.maros@ic.ac.uk

Csaba Meszaros: meszaros@sztaki.hu


Set of problems
---------------

Presently, there are 138 problems available.  Both formulations of QP
problems (separable and non-separable) are well represented in the set.
In a separate section we acknowledge the contributors.  Below is a list
of the problems.

NAME  name of the problem
M     number of rows in A
N     number of variables
NZ    number of nonzeros in A
QN    number of quadratic variables
QNZ   number of off-diagonal entries in the lower triangular part of Q
OPT   solution value obtained by the default settings of BPMPD solver

The separable problems are easily recognized by a 0 entry in the QNZ column.

NAME           M       N      NZ      QN      QNZ       OPT

aug2d      10000   20200   40000   19800        0    1.6874118e+06
aug2dc     10000   20200   40000   20200        0    1.8183681e+06
aug2dcqp   10000   20200   40000   20200        0    6.4981348e+06
aug2dqp    10000   20200   40000   19800        0    6.2370121e+06
aug3d       1000    3873    6546    2673        0    5.5406773e+02
aug3dc      1000    3873    6546    3873        0    7.7126244e+02
aug3dcqp    1000    3873    6546    3873        0    9.9336215e+02
aug3dqp     1000    3873    6546    2673        0    6.7523767e+02
boyd1         18   93261  558985   93261        0   -6.1735220e+07
boyd2     186531   93263  423784       2        0    2.1256767e+01
cont-050    2401    2597   12005    2597        0   -4.5638509e+00
cont-100    9801   10197   49005   10197        0   -4.6443979e+00
cont-101   10098   10197   49599    2700        0    1.9552733e-01
cont-200   39601   40397  198005   40397        0   -4.6848759e+00
cont-201   40198   40397  199199   10400        0    1.9248337e-01
cont-300   90298   90597  448799   23100        0    1.9151232e-01
cvxqp1l     5000   10000   14998   10000    29984    1.0870480e+08
cvxqp1m      500    1000    1498    1000     2984    1.0875116e+06
cvxqp1s       50     100     148     100      286    1.1590718e+04
cvxqp2l     2500   10000    7499   10000    29984    8.1842458e+07
cvxqp2m      250    1000     749    1000     2984    8.2015543e+05
cvxqp2s       25     100      74     100      286    8.1209405e+03
cvxqp3l     7500   10000   22497   10000    29984    1.1571110e+08
cvxqp3m      750    1000    2247    1000     2984    1.3628287e+06
cvxqp3s       75     100     222     100      286    1.1943432e+04
dpklo1        77     133    1575      77        0    3.7009622e-01
dtoc3       9998   14999   34993   14997        0    2.3526248e+02
dual1          1      85      85      85     3473    3.5012966e-02
dual2          1      96      96      96     4412    3.3733676e-02
dual3          1     111     111     111     5997    1.3575584e-01
dual4          1      75      75      75     2724    7.4609084e-01
dualc1       215       9    1935       9       36    6.1552508e+03
dualc2       229       7    1603       7       21    3.5513077e+03
dualc5       278       8    2224       8       28    4.2723233e+02
dualc8       503       8    4024       8       28    1.8309359e+04
exdata      3001    3000    7500    1500  1124250   -1.4184343e+02
genhs28        8      10      24      10        9    9.2717369e-01
gouldqp2     349     699    1047     349      348    1.8427534e-04
gouldqp3     349     699    1047     698      697    2.0627840e+00
hs118         17      15      39      15        0    6.6482045e+02
hs21           1       2       2       2        0   -9.9960000e+01
hs268          5       5      25       5       10    5.7310705e-07
hs35           1       3       3       3        2    1.1111111e-01
hs35mod        1       3       3       3        2    2.5000000e-01
hs51           3       5       7       5        2    8.8817842e-16
hs52           3       5       7       5        2    5.3266476e+00
hs53           3       5       7       5        2    4.0930233e+00
hs76           3       4      10       4        2   -4.6818182e+00
hues-mod       2   10000   19899   10000        0    3.4824690e+07
huestis        2   10000   19899   10000        0    3.4824690e+11
ksip        1001      20   18411      20        0    5.7579794e-01
laser       1000    1002    3000    1002     3000    2.4096014e+06
liswet1    10000   10002   30000   10002        0    3.6122402e+01
liswet10   10000   10002   30000   10002        0    4.9485785e+01
liswet11   10000   10002   30000   10002        0    4.9523957e+01
liswet12   10000   10002   30000   10002        0    1.7369274e+03
liswet2    10000   10002   30000   10002        0    2.4998076e+01
liswet3    10000   10002   30000   10002        0    2.5001220e+01
liswet4    10000   10002   30000   10002        0    2.5000112e+01
liswet5    10000   10002   30000   10002        0    2.5034253e+01
liswet6    10000   10002   30000   10002        0    2.4995748e+01
liswet7    10000   10002   30000   10002        0    4.9884089e+02
liswet8    10000   10002   30000   10002        0    7.1447006e+03
liswet9    10000   10002   30000   10002        0    1.9632513e+03
lotschd        7      12      54       6        0    2.3984159e+03
mosarqp1     700    2500    3422    2500       45   -9.5287544e+02
mosarqp2     600     900    2930     900       45   -1.5974821e+03
powell20   10000   10000   20000   10000        0    5.2089583e+10
primal1       85     325    5815     324        0   -3.5012965e-02
primal2       96     649    8042     648        0   -3.3733676e-02
primal3      111     745   21547     744        0   -1.3575584e-01
primal4       75    1489   16031    1488        0   -7.4609083e-01
primalc1       9     230    2070     229        0   -6.1552508e+03
primalc2       7     231    1617     230        0   -3.5513077e+03
primalc5       8     287    2296     286        0   -4.2723233e+02
primalc8       8     520    4160     519        0   -1.8309430e+04
q25fv47      820    1571   10400     446    59053    1.3744448e+07
qadlittl      56      97     383      17       70    4.8031886e+05
qafiro        27      32      83       3        3   -1.5907818e+00
qbandm       305     472    2494      25       16    1.6352342e+04
qbeaconf     173     262    3375      18        9    1.6471206e+05
qbore3d      233     315    1429      28       50    3.1002008e+03
qbrandy      220     249    2148      16       49    2.8375115e+04
qcapri       271     353    1767      56      838    6.6793293e+07
qe226        223     282    2578      67      897    2.1265343e+02
qetamacr     400     688    2409     378     4069    8.6760370e+04
qfffff80     524     854    6227     278     1638    8.7314747e+05
qforplan     161     421    4563      36      546    7.4566315e+09
qgfrdxpn     616    1092    2377      54      108    1.0079059e+11
qgrow15      300     645    5620      38      462   -1.0169364e+08
qgrow22      440     946    8252      65      787   -1.4962895e+08
qgrow7       140     301    2612      30      327   -4.2798714e+07
qisrael      174     142    2269      42      656    2.5347838e+07
qpcblend      74      83     491      83        0   -7.8425409e-03
qpcboei1     351     384    3485     384        0    1.1503914e+07
qpcboei2     166     143    1196     143        0    8.1719623e+06
qpcstair     356     467    3856     467        0    6.2043875e+06
qpilotno     975    2172   13057      94      391    4.7285869e+06
qptest         2       2       4       2        1    4.3718750e+00
qrecipe       91     180     663      20       30   -2.6661600e+02
qsc205       205     203     551      11       10   -5.8139518e-03
qscagr25     471     500    1554      28      100    2.0173794e+08
qscagr7      129     140     420       8       17    2.6865949e+07
qscfxm1      330     457    2589      56      677    1.6882692e+07
qscfxm2      660     914    5183      74     1057    2.7776162e+07
qscfxm3      990    1371    7777      89     1132    3.0816355e+07
qscorpio     388     358    1426      22       18    1.8805096e+03
qscrs8       490    1169    3182      33       88    9.0456001e+02
qscsd1        77     760    2388      54      691    8.6666667e+00
qscsd6       147    1350    4316      96     1308    5.0808214e+01
qscsd8       397    2750    8584     140     2370    9.4076357e+02
qsctap1      300     480    1692      36      117    1.4158611e+03
qsctap2     1090    1880    6714     141      636    1.7350265e+03
qsctap3     1480    2480    8874     186      861    1.4387547e+03
qseba        515    1028    4352      96      550    8.1481801e+07
qshare1b     117     225    1151      18       21    7.2007832e+05
qshare2b      96      79     694      10       45    1.1703692e+04
qshell       536    1775    3556     405    34385    1.5726368e+12
qship04l     402    2118    6332      14       42    2.4200155e+06
qship04s     402    1458    4352      14       42    2.4249937e+06
qship08l     778    4283   12802     940    34025    2.3760406e+06
qship08s     778    2387    7114     538    11139    2.3857289e+06
qship12l    1151    5427   16170    2023    60205    3.0188766e+06
qship12s    1151    2763    8178    1042    16361    3.0569623e+06
qsierra     1227    2036    7302     122       61    2.3750458e+07
qstair       356     467    3856      66      952    7.9854528e+06
qstandat     359    1075    3031     138      666    6.4118384e+03
s268           5       5      25       5       10    5.7310705e-07
stadat1     3999    2001    9997    2000        0   -2.8526864e+07
stadat2     3999    2001    9997    2000        0   -3.2626665e+01
stadat3     7999    4001   19997    4000        0   -3.5779453e+01
stcqp1      2052    4097   13338    4097    22506    1.5514356e+05
stcqp2      2052    4097   13338    4097    22506    2.2327313e+04
tame           1       2       2       2        1    0.0000000e+00
ubh1       12000   18009   48000    6003        0    1.1160008e+00
values         1     202     202     202     3620   -1.3966211e+00
yao         2000    2002    6000    2002        0    1.9770426e+02
zecevic2       2       2       4       1        0   -4.1250000e+00



Acknowledgements
----------------

The problems have been obtained from the following sources:

CUTE library provided by Ingrid Bongartz and Andy Conn (IBM T.J.  Watson
Research Center), Nick Gould (Rutherford Appleton Laboratory, UK), and
Philippe Toint (Facultes Universitaires Notre-Dame de la Paix):

aug2d aug2dc aug2dcqp aug2dqp aug3d aug3dc aug3dcqp aug3dqp cvxqp1 l
cvxqp1 m cvxqp1 s cvxqp2 l cvxqp2 m cvxqp2 s cvxqp3 l cvxqp3 m cvxqp3 s
dtoc3 dual1 dual2 dual3 dual4 dualc1 dualc2 dualc5 dualc8 genhs28
gouldqp2 gouldqp3 hs118 hs21 hs268 hs35 hs35mod hs51 hs52 hs53 hs76
hues-mod huestis ksip liswet1 liswet10 liswet11 liswet12 liswet2 liswet3
liswet4 liswet5 liswet6 liswet7 liswet8 liswet9 lotschd mosarqp1
mosarqp2 powell20 primal1 primal2 primal3 primal4 primalc1 primalc2
primalc5 primalc8 qpcblend qpcboei1 qpcboei2 qpcstair s268 stcqp1 stcqp2
tame ubh1 yao zecevic2


Helen Jones and Gautam Mitra (Mathematical Programming Group, Brunel
University, London):

q25fv47 qadlittl qafiro qbandm qbeaconf qbore3d qbrandy qcapri qe226
qetamacr qfffff80 qforplan qgfrdxpn qgrow15 qgrow22 qgrow7 qisrael
qpilotno qrecipe qsc205 qscagr25 qscagr7 qscfxm1 qscfxm2 qscfxm3
qscorpio qscrs8 qscsd1 qscsd6 qscsd8 qsctap1 qsctap2 qsctap3 qseba
qshare1b qshare2b qshell qship04l qship04s qship08l qship08s qship12l
qship12s qsierra qstair qstandat


Piet Groeneboom (University of Washington, USA):

stadat1 stadat2 stadat3


Hans D. Mittelmann (Arizona State University, USA):

cont-050 cont-100 cont-101 cont-200 cont-201 cont-300


Athanassia Chalimourda (Ruhr University, Bochum, Germany):

exdata values


Don Boyd (Rensselaer Polytechnic Institute):

boyd1 boyd2


James McNames (Stanford University, USA):

laser


Henry Wolkowitz (University of Waterloo, Canada):

dpklo1

We also included the small QP example shown in section 2 in a separate file
qptest


---------------------------------------------------------------------------
October 1998

I.M.
Cs.M.
