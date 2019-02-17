NAME          Maros
*
*  Istvan Maros,
*  Computational Techniques of the Simplex Method,
*  Kluwer, 2003, page 93.
*
*  Maximize:
*
*          4.5 x1 + 2.5 x2 + 4.0 x3 + 4.0 x4
*
*  Subject to:
*  
*              x1          +     x3 + 1.5 x4 <= 40
*    20 <=          1.5 x2 + 0.5 x3 + 0.5 x4 <= 30
*          2.5 x1 + 2.0 x2 + 3.0 x3 + 2.0 x4  = 95
*
*  With:
*
*      0.0 <= x1
*      0.0 <= x2
*    -10.0 <= x3 <= 20.0
*      0.0 <= x4 <= 25.0
*
*  Tableau:
*
*             VOL1  VOL2  VOL3  VOL4 |  RHS1
*          +--------------------------------
*  OBJ     |   4.5   2.5   4.0   4.0 |   0.0
*  RES1    |   1.0   0.0   1.0   1.5 |  40.0
*  RES2    |   0.0   1.5   0.5   0.5 |  30.0
*  BALANCE |   2.5   2.0   3.0   2.0 |  95.0
*  --------+-------------------------+------
*
ROWS
 N  OBJ
 L  RES1
 L  RES2
 E  BALANCE
COLUMNS
    VOL1      OBJ                4.5
    VOL1      RES1               1.0
    VOL1      BALANCE            2.5
    VOL2      OBJ                2.5
    VOL2      RES2               1.5
    VOL2      BALANCE            2.0
    VOL3      OBJ                4.0
    VOL3      RES1               1.0
    VOL3      RES2               0.5
    VOL3      BALANCE            3.0
    VOL4      OBJ                4.0
    VOL4      RES1               1.5
    VOL4      RES2               0.5
    VOL4      BALANCE            2.0
RHS
    RHS1      RES1              40.0
    RHS1      RES2              30.0
    RHS1      BALANCE           95.0
RANGES
    RANGE1    BALANCE           10.0
BOUNDS
 LO BOUND1    VOL3             -10.0
 UP BOUND1    VOL3              20.0
 UP BOUND1    VOL4              25.0
ENDATA
