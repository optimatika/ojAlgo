NAME          Nazareth
*
*  Example 3.3-1
*
*  J L Nazareth,
*  Computer Solution of Linear Programs,
*  Oxford University Press, 1987, pages 49-50.
*
*  Minimize:
*
*      x1     - x2 +     x3
*
*  Subject to:
*
*    2 x1          + 3 * x3 <= 10
*           4 * x2 + 5 * x3 <= 20
*
*  With:
*
*    0 <= x1 <= 100
*    0 <= x2
*         x3 unrestricted.
*
*  Tableau:
*
*         CLNAM1   CLNAM2  CLNAM3 |  RHS1
*       +--------------------------------
*  OBJ  |   1.0    -1.0    1.0    |   0.0
*  ROW1 |   2.0     0.0    3.0    |  10.0
*  ROW2 |   0.0     4.0    5.0    |  20.0
*  -----+-------------------------+------
*  BV1  | 100.0     0.0    0.0    | empty
*
ROWS
 N  OBJ
 L  ROW1
 L  ROW2
COLUMNS
    CLNAM1    OBJ                1.0
    CLNAM1    ROW1               2.0
    CLNAM2    OBJ               -1.0
    CLNAM2    ROW2               4.0
    CLNAM3    OBJ                1.0
    CLNAM3    ROW1               3.0
    CLNAM3    ROW2               5.0
RHS
    RHS1      ROW1              10.0
    RHS1      ROW2              20.0
BOUNDS
 UP BV1       CLNAM1           100.0
 FR BV1       CLNAM3
ENDATA
