# compiler-for-easy-pascal
Pascal風言語で記述されたプログラムを，アセンブラ言語CASLIIで記述されたプログラムに変換する  
(Translates a program written in Pascal-like language into a program written in the assembler language CASLII.)

## Flow 
1. Lexer  
Pascal風言語で記述されたプログラムをトークン列に分割  
(Convert a program written in a Pascal-like language into a sequence of tokens)
1. Parser  
ASTを作成しながら構文的に正しいかを判定  
(Creating the AST and checking for syntactical correctness)
1. Checker  
ASTを根から順にたどり意味的に正しいかを判定  
(Trace the AST from root to determine if it is semantically correct)  
再度ASTをたどりCASLIIプログラムを生成  
(Generate CASLII program by tracing the AST again)
![compiler-for-easy-pascal](https://user-images.githubusercontent.com/88955673/166624022-492eb2cb-69b0-4994-a573-1d8d20038340.png)


## Usage
- Lexer  
$ java Main lexer [Pacal風ファイル(.pas)] [出力先] 
- Compiler  
$ java Main compiler [トークンファイル(.ts)] [出力先] 
- Lexer & Compiler  
$ java Main all [Pacal風ファイル(.pas)] [出力先] 

実行例
- $ java Main lexer data/pas/in.pas tmp/out.ts 
- $ java Main compiler data/ts/in.ts tmp/out.cas 
- $ java Main all data/pas/in.pas tmp/out.ans 
