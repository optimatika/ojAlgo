NAME          EMPSTEST                                                          
ROWS
 E  VLRES   
 N  OBJEC   
 E  RAI72   
 G  DEP73   
 L  DEP72   
 E  TRS72   
 G  INV72   
COLUMNS
    RVAD72    RAI72               1.   OBJEC               1.   
    RVAD73    RAI72            1.101   OBJEC               1.   
    DEPN72    DEP72           -1.101   DEP73               1.   
    DEPN73    RAI72           -1.101   TRS72               1.   
    INVT72    INV72              -1.   TRS72              -1.   
    WK1T78    INV72              -1.   TRS72              -1.   
    WK2T78    INV72              -2.   TRS72              -1.   
    WK3T78    INV72              -3.   TRS72              -1.   
RHS
    RHS1      RAI72           -1.234   TRS72             5.67   
    RHS1      INV72              -2.   
    RHS2      INV72              -4.   TRS72               5.   
RANGES
    RAN1      VLRES             2.34   RAI72             -34.   
    RAN1      DEP72            -2.34   
    RAN2      VLRES             2.34   RAI72             -34.   
    RAN2      DEP72            -2.34   
BOUNDS
 FR BNDS1     RVAD72  
 PL BNDS1     RVAD73  
 UP BNDS1     WK1T78         8.07907   
 MI BNDS1     WK2T78  
 LO BNDS1     WK3T78         1.57957   
 UP BNDS1     DEPN72         1.51985   
 FX BNDS1     DEPN73         8.07907   
 FX BNDS1     INVT72         8.07907   
ENDATA
