# Reducing the crane working time for the Block Relocation Problem

Instances, solutions and code for the 'Reducing the crane working time for the Block
Relocation Problem' paper can be found here.

## Online materials
The [online materials](online-materials.pdf) document contains the instances that were unsolvable for the da Silva Firmino et al. sets and also contains the complete results for the Caserta et al. instances.

## Source code
The [source code](src/code) for the proposed reactive GRASP as well as the re-implementation of the initial reactive GRASP by da Silva Firmino et al. is given here. The parameters for the algorithm should be set in the Main class. The GRASP class contains the code that is shared among the two GRASP algorithms and will execute the reactive GRASP by da Silva Firmino et al. when the survivors parameter is set to zero and the proposed reactive GRASP if it is set higher.

## Solutions
All [solutions](solutions) used for the data in the paper are available.

When the solution for a certain instance is extracted, four files can be found:
* Generated: the total amount of generated solutions in the 21 executions for the instance.
* Moves: all the moves for the 21 solutions.
* Relocations: the total amount of relocation for the 21 solutions.
* Solutions: the crane working time for the 21 solutions.

An example of the moves within a solution:
```
0,2,3,5,1;1,2,2,3,5;1,1,2,4,6;1,0,3,5,2;0,1,1,0,7;0,0,3,5,3;2,4,1,1,6;2,3,1,2,5;2,2,3,5,4;1,2,3,5,5;1,1,3,5,6;1,0,3,5,7;2,1,1,0,9;2,0,3,5,8;1,0,3,5,9;
```
The moves within the solution are seperated by '**;**'. The five integers within a move are respectively:<br />
originating stack, originating tier, target stack, target tier, container priority


## License

This project is licensed under the MIT License - see the [License](License) file for details.
