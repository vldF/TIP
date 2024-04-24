## Lec #1:
Nothing

## Lec #2:

1. На поделать: Дописать реализацию класса `TypeAnalysis` в TIP
2. На поделать: Подумайте, что происходит в получившейся реализации, если в программе есть рекурсивный тип?
   Если в программе есть рекурсивный тип, то анализатор все равно сможет типизировать программу, так как 
   рекурсивные типы разрешимы при помощи union-find

## Lec #3:
1. Допишите метод transfer в трейте `IntraprocSignAnalysisFunctions` (по факту, в классе `SimpleSignAnalysis`)
2. Реализуйте класс `PowersetLattice` в классе `GenericLattices`

## Lec #4:
1. Допишите реализацию live variables analysis (`LiveVarsAnalysis.scala`)
2. Реализуйте reaching definitions analysis (`src/tip/analysis/ReachingDefinitionsAnalysis.scala`)
