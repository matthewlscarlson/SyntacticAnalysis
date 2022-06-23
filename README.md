# SyntacticAnalysis

Lexical analyzer that tokenizes code to see if it follows the rules of a certain EBNF grammar.

The grammar in question:

```
<program> -> program begin <statement_list> end
<statement_list> -> <statement> {;<statement>}
<statement> -> <assignment_statement> | <if_statement> | <loop_statement>
<assignment_statement> -> <variable> = <expression>
<variable> -> identifier (An identifier is a string that begins with a letter followed by 0 or more letters and/or digits)
<expression> -> <term> {(+|-) <term>}
<term> -> <factor> {(* | /) <factor>}
<factor> -> identifier | int_constant | (<expr>)
<if_statement> -> if (<logic_expression>) then <statement>
<logic_expression> -> <variable> (< | >) <variable> (Assume logic expressions have only less than or greater than operators)
<loop_statement> -> loop (<logic_expression>) <statement>
```
The analyzer will prompt you for the name of the file which contains the code. It will then tell you whether or not there is one or more syntax errors.

`prog.txt` is an example of code that contains no syntax errors.

I wrote this back in 2020 for COSC3127, a course at Algoma University.
