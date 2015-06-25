/* Boogie 2 lexer */
package de.uni_freiburg.informatik.ultimate.boogie.parser;
import java_cup.runtime.Symbol;

/**
 * This is a autogenerated lexer for Boogie 2.
 * It is generated from Boogie.flex by JFlex.
 */
%%

%class Lexer
%unicode
%cupsym LexerSymbols
%cup
%line
%column
%public

%{
  private StringBuffer string = new StringBuffer();
  private BoogieSymbolFactory symFactory;
  
  public void setSymbolFactory(BoogieSymbolFactory factory) {
    symFactory = factory;
  }

  private Symbol symbol(int type) {
    return symFactory.newSymbol(yytext(), type, yyline+1, yycolumn, yyline+1, yycolumn+yylength());
  }
  private Symbol symbol(int type, String value) {
    return symFactory.newSymbol(value, type, yyline+1, yycolumn, yyline+1, yycolumn+yylength(), value);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment   = "/*" ~"*/" 
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
BoogieLetter = [:letter:] | ['~#$\^_.?\\]
BoogieLetterDigit = {BoogieLetter} | [:digit:]
Identifier = {BoogieLetter} {BoogieLetterDigit}*

DecIntegerLiteral = 0 | [1-9][0-9]*
RealIntegerLiteral = {DecIntegerLiteral} "." [0-9]+
BvIntegerLiteral = {DecIntegerLiteral} "bv" {DecIntegerLiteral}
BvType = "bv" {DecIntegerLiteral}

%state STRING

%%

<YYINITIAL>  {
  "type"          { return symbol(LexerSymbols.TYPE); }
  "const"         { return symbol(LexerSymbols.CONST); }
  "function"      { return symbol(LexerSymbols.FUNCTION); }
  "axiom"         { return symbol(LexerSymbols.AXIOM); }
  "var"           { return symbol(LexerSymbols.VAR); }
  "procedure"     { return symbol(LexerSymbols.PROCEDURE); }
  "implementation" { return symbol(LexerSymbols.IMPLEMENTATION); }

  "finite"        { return symbol(LexerSymbols.FINITE); }
  "unique"        { return symbol(LexerSymbols.UNIQUE); }
  "complete"      { return symbol(LexerSymbols.COMPLETE); }
  "returns"       { return symbol(LexerSymbols.RETURNS); }
  "where"         { return symbol(LexerSymbols.WHERE); }
  "free"          { return symbol(LexerSymbols.FREE); }
  "ensures"       { return symbol(LexerSymbols.ENSURES); }
  "requires"      { return symbol(LexerSymbols.REQUIRES); }
  "modifies"      { return symbol(LexerSymbols.MODIFIES); }
  "invariant"     { return symbol(LexerSymbols.INVARIANT); }

  "assume"        { return symbol(LexerSymbols.ASSUME); }
  "assert"        { return symbol(LexerSymbols.ASSERT); }
  "havoc"         { return symbol(LexerSymbols.HAVOC); }
  "async"         { return symbol(LexerSymbols.ASYNC); }
  "call"          { return symbol(LexerSymbols.CALL); }
  "if"            { return symbol(LexerSymbols.IF); }
  "then"          { return symbol(LexerSymbols.THEN); }
  "else"          { return symbol(LexerSymbols.ELSE); }
  "while"         { return symbol(LexerSymbols.WHILE); }
  "break"         { return symbol(LexerSymbols.BREAK); }
  "return"        { return symbol(LexerSymbols.RETURN); }
  "goto"          { return symbol(LexerSymbols.GOTO); }

  "old"           { return symbol(LexerSymbols.OLD); }

  "forall"        { return symbol(LexerSymbols.FORALL); }
  "\u2200"        { return symbol(LexerSymbols.FORALL); }
  "exists"        { return symbol(LexerSymbols.EXISTS); }
  "\u2203"        { return symbol(LexerSymbols.EXISTS); }
  "bool"          { return symbol(LexerSymbols.BOOL); }
  "int"           { return symbol(LexerSymbols.INT); }
  "real"          { return symbol(LexerSymbols.REAL); }
  "false"         { return symbol(LexerSymbols.FALSE); }
  "true"          { return symbol(LexerSymbols.TRUE); }

  /* Other Symbols */
  "("             { return symbol(LexerSymbols.LPAR); }
  ")"             { return symbol(LexerSymbols.RPAR); }
  "["             { return symbol(LexerSymbols.LBKT); }
  "]"             { return symbol(LexerSymbols.RBKT); }
  "{"             { return symbol(LexerSymbols.LBRC); }
  "}"             { return symbol(LexerSymbols.RBRC); }
  ","             { return symbol(LexerSymbols.COMMA); }
  ":"             { return symbol(LexerSymbols.COLON); }
  ";"             { return symbol(LexerSymbols.SEMI); }
  ":="            { return symbol(LexerSymbols.COLONEQUALS); }
  "\u2254"        { return symbol(LexerSymbols.COLONEQUALS); }
  "="             { return symbol(LexerSymbols.EQUALS); }
  "<"             { return symbol(LexerSymbols.LESS); }
  ">"             { return symbol(LexerSymbols.GREATER); }
  "<="            { return symbol(LexerSymbols.LTEQ); }
  "\u2264"        { return symbol(LexerSymbols.LTEQ); }
  ">="            { return symbol(LexerSymbols.GTEQ); }
  "\u2265"        { return symbol(LexerSymbols.GTEQ); }
  "!="            { return symbol(LexerSymbols.NEQ); }
  "\u2260"        { return symbol(LexerSymbols.NEQ); }
  "=="            { return symbol(LexerSymbols.EQ); }
  "<:"            { return symbol(LexerSymbols.PARTORDER); }
  "+"             { return symbol(LexerSymbols.PLUS); }
  "-"             { return symbol(LexerSymbols.MINUS); }
  "*"             { return symbol(LexerSymbols.TIMES); }
  "/"             { return symbol(LexerSymbols.DIVIDE); }
  "%"             { return symbol(LexerSymbols.MOD); }
  "!"             { return symbol(LexerSymbols.NOT); }
  "\u00ac"        { return symbol(LexerSymbols.NOT); }
  "&&"            { return symbol(LexerSymbols.AND); }
  "\u2227"        { return symbol(LexerSymbols.AND); }
  
  "||"            { return symbol(LexerSymbols.OR); }
  "\u2228"        { return symbol(LexerSymbols.OR); }
  "==>"           { return symbol(LexerSymbols.IMPLIES); }
  "\u21d2"        { return symbol(LexerSymbols.IMPLIES); }
  "<=="           { return symbol(LexerSymbols.EXPLIES); }
  "\u21d0"        { return symbol(LexerSymbols.EXPLIES); }
  "<==>"          { return symbol(LexerSymbols.IFF); }
  "\u21d4"        { return symbol(LexerSymbols.IFF); }
  "::"            { return symbol(LexerSymbols.QSEP); }
  "\u2022"        { return symbol(LexerSymbols.QSEP); }
  "++"            { return symbol(LexerSymbols.CONCAT); }

  /* Numbers, Ids and Strings */

  {BvType}                       { return symbol(LexerSymbols.BVTYPE, yytext().intern()); }

  /* identifiers */ 
  {Identifier}                   { return symbol(LexerSymbols.ID, yytext().intern()); }
 
  /* literals */
  {BvIntegerLiteral}             { return symbol(LexerSymbols.BITVECTOR, yytext().intern()); }
  {DecIntegerLiteral}            { return symbol(LexerSymbols.NUMBER, yytext().intern()); }
  {RealIntegerLiteral}           { return symbol(LexerSymbols.REALNUMBER, yytext().intern()); }
  \"                             { string.setLength(0); yybegin(STRING); }

 
  /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<STRING> {
  \"                             { yybegin(YYINITIAL); 
                                   return symbol(LexerSymbols.ATTR_STRING, 
                                   string.toString().intern()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}

/* error fallback */
.|\n                             { return symbol(LexerSymbols.error, yytext()); }

