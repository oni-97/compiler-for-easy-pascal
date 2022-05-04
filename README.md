# compiler-for-easy-pascal
Pascal風言語で記述されたプログラムを，アセンブラ言語CASLIIで記述されたプログラムに変換する

## Flow 
1. Lexer  
Pascal風言語で記述されたプログラムをトークン列に分割
1. Parser  
ASTを作成しながら構文的に正しいかを判定
1. Checker  
ASTを根から順にたどり意味的に正しいかを判定  
再度ASTをたどりCASLIIプログラムを生成
![compiler-for-easy-pascal](https://user-images.githubusercontent.com/88955673/166624022-492eb2cb-69b0-4994-a573-1d8d20038340.png)
