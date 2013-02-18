/* The following code was generated by JFlex 1.4.2 on 21.03.11 10:47 */

/* SMT-Lib lexer */
package local.stalin.smt.smtlib;
import java_cup.runtime.Symbol;

/**
 * This is a autogenerated lexer for Boogie 2.
 * It is generated from Boogie.flex by JFlex.
 */

public class Lexer implements java_cup.runtime.Scanner {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int PATTERN = 4;
  public static final int STRING = 2;
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2, 2
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\3\1\2\1\0\1\3\1\1\22\0\1\3\1\0\1\56"+
    "\1\53\1\52\2\53\1\7\1\43\1\44\1\5\1\53\1\0\1\53"+
    "\1\54\1\4\1\10\11\11\1\42\1\0\1\53\1\12\1\53\1\51"+
    "\1\53\32\6\1\47\1\55\1\50\1\0\1\34\1\0\1\13\1\16"+
    "\1\20\1\15\1\17\1\31\1\36\1\21\1\25\1\6\1\24\1\32"+
    "\1\22\1\14\1\33\1\35\1\6\1\23\1\26\1\27\1\40\1\6"+
    "\1\41\1\30\1\37\1\6\1\45\1\53\1\46\1\53\53\0\1\6"+
    "\12\0\1\6\4\0\1\6\5\0\27\6\1\0\37\6\1\0\u013f\6"+
    "\31\0\162\6\4\0\14\6\16\0\5\6\11\0\1\6\213\0\1\6"+
    "\13\0\1\6\1\0\3\6\1\0\1\6\1\0\24\6\1\0\54\6"+
    "\1\0\46\6\1\0\5\6\4\0\202\6\10\0\105\6\1\0\46\6"+
    "\2\0\2\6\6\0\20\6\41\0\46\6\2\0\1\6\7\0\47\6"+
    "\110\0\33\6\5\0\3\6\56\0\32\6\5\0\13\6\25\0\12\7"+
    "\4\0\2\6\1\0\143\6\1\0\1\6\17\0\2\6\7\0\2\6"+
    "\12\7\3\6\2\0\1\6\20\0\1\6\1\0\36\6\35\0\3\6"+
    "\60\0\46\6\13\0\1\6\u0152\0\66\6\3\0\1\6\22\0\1\6"+
    "\7\0\12\6\4\0\12\7\25\0\10\6\2\0\2\6\2\0\26\6"+
    "\1\0\7\6\1\0\1\6\3\0\4\6\3\0\1\6\36\0\2\6"+
    "\1\0\3\6\4\0\12\7\2\6\23\0\6\6\4\0\2\6\2\0"+
    "\26\6\1\0\7\6\1\0\2\6\1\0\2\6\1\0\2\6\37\0"+
    "\4\6\1\0\1\6\7\0\12\7\2\0\3\6\20\0\11\6\1\0"+
    "\3\6\1\0\26\6\1\0\7\6\1\0\2\6\1\0\5\6\3\0"+
    "\1\6\22\0\1\6\17\0\2\6\4\0\12\7\25\0\10\6\2\0"+
    "\2\6\2\0\26\6\1\0\7\6\1\0\2\6\1\0\5\6\3\0"+
    "\1\6\36\0\2\6\1\0\3\6\4\0\12\7\1\0\1\6\21\0"+
    "\1\6\1\0\6\6\3\0\3\6\1\0\4\6\3\0\2\6\1\0"+
    "\1\6\1\0\2\6\3\0\2\6\3\0\3\6\3\0\10\6\1\0"+
    "\3\6\55\0\11\7\25\0\10\6\1\0\3\6\1\0\27\6\1\0"+
    "\12\6\1\0\5\6\46\0\2\6\4\0\12\7\25\0\10\6\1\0"+
    "\3\6\1\0\27\6\1\0\12\6\1\0\5\6\3\0\1\6\40\0"+
    "\1\6\1\0\2\6\4\0\12\7\25\0\10\6\1\0\3\6\1\0"+
    "\27\6\1\0\20\6\46\0\2\6\4\0\12\7\25\0\22\6\3\0"+
    "\30\6\1\0\11\6\1\0\1\6\2\0\7\6\72\0\60\6\1\0"+
    "\2\6\14\0\7\6\11\0\12\7\47\0\2\6\1\0\1\6\2\0"+
    "\2\6\1\0\1\6\2\0\1\6\6\0\4\6\1\0\7\6\1\0"+
    "\3\6\1\0\1\6\1\0\1\6\2\0\2\6\1\0\4\6\1\0"+
    "\2\6\11\0\1\6\2\0\5\6\1\0\1\6\11\0\12\7\2\0"+
    "\2\6\42\0\1\6\37\0\12\7\26\0\10\6\1\0\42\6\35\0"+
    "\4\6\164\0\42\6\1\0\5\6\1\0\2\6\25\0\12\7\6\0"+
    "\6\6\112\0\46\6\12\0\51\6\7\0\132\6\5\0\104\6\5\0"+
    "\122\6\6\0\7\6\1\0\77\6\1\0\1\6\1\0\4\6\2\0"+
    "\7\6\1\0\1\6\1\0\4\6\2\0\47\6\1\0\1\6\1\0"+
    "\4\6\2\0\37\6\1\0\1\6\1\0\4\6\2\0\7\6\1\0"+
    "\1\6\1\0\4\6\2\0\7\6\1\0\7\6\1\0\27\6\1\0"+
    "\37\6\1\0\1\6\1\0\4\6\2\0\7\6\1\0\47\6\1\0"+
    "\23\6\16\0\11\7\56\0\125\6\14\0\u026c\6\2\0\10\6\12\0"+
    "\32\6\5\0\113\6\25\0\15\6\1\0\4\6\16\0\22\6\16\0"+
    "\22\6\16\0\15\6\1\0\3\6\17\0\64\6\43\0\1\6\4\0"+
    "\1\6\3\0\12\7\46\0\12\7\6\0\130\6\10\0\51\6\127\0"+
    "\35\6\51\0\12\7\36\6\2\0\5\6\u038b\0\154\6\224\0\234\6"+
    "\4\0\132\6\6\0\26\6\2\0\6\6\2\0\46\6\2\0\6\6"+
    "\2\0\10\6\1\0\1\6\1\0\1\6\1\0\1\6\1\0\37\6"+
    "\2\0\65\6\1\0\7\6\1\0\1\6\3\0\3\6\1\0\7\6"+
    "\3\0\4\6\2\0\6\6\4\0\15\6\5\0\3\6\1\0\7\6"+
    "\164\0\1\6\15\0\1\6\202\0\1\6\4\0\1\6\2\0\12\6"+
    "\1\0\1\6\3\0\5\6\6\0\1\6\1\0\1\6\1\0\1\6"+
    "\1\0\4\6\1\0\3\6\1\0\7\6\3\0\3\6\5\0\5\6"+
    "\u0ebb\0\2\6\52\0\5\6\5\0\2\6\4\0\126\6\6\0\3\6"+
    "\1\0\132\6\1\0\4\6\5\0\50\6\4\0\136\6\21\0\30\6"+
    "\70\0\20\6\u0200\0\u19b6\6\112\0\u51a6\6\132\0\u048d\6\u0773\0\u2ba4\6"+
    "\u215c\0\u012e\6\2\0\73\6\225\0\7\6\14\0\5\6\5\0\1\6"+
    "\1\0\12\6\1\0\15\6\1\0\5\6\1\0\1\6\1\0\2\6"+
    "\1\0\2\6\1\0\154\6\41\0\u016b\6\22\0\100\6\2\0\66\6"+
    "\50\0\14\6\164\0\5\6\1\0\207\6\23\0\12\7\7\0\32\6"+
    "\6\0\32\6\13\0\131\6\3\0\6\6\2\0\6\6\2\0\6\6"+
    "\2\0\3\6\43\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\3\0\1\1\2\2\2\3\1\4\2\5\1\6\15\4"+
    "\1\1\1\7\1\10\1\11\1\12\2\1\1\13\1\14"+
    "\1\15\1\16\3\4\1\11\1\17\2\3\1\0\1\20"+
    "\21\4\1\21\1\4\10\22\1\0\1\23\1\0\1\24"+
    "\1\25\1\26\1\27\1\30\1\31\2\4\1\2\1\0"+
    "\1\3\2\0\1\32\1\33\4\4\1\34\1\35\1\4"+
    "\1\36\2\4\1\37\3\4\1\40\3\4\7\22\2\4"+
    "\1\0\1\41\6\4\1\42\1\4\1\43\4\4\6\22"+
    "\1\44\1\42\7\4\1\45\1\4\1\46\1\4\1\47"+
    "\6\22\1\45\2\4\1\50\2\4\1\51\1\52\1\4"+
    "\1\22\1\53\3\22\1\54\2\4\1\55\1\4\1\56"+
    "\4\22\1\57\1\22\1\60\2\4\4\22\1\61\1\62"+
    "\1\4\4\22\1\4\2\22\1\63\1\22\1\4\1\64"+
    "\1\65\1\66\1\67";

  private static int [] zzUnpackAction() {
    int [] result = new int[208];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\57\0\136\0\215\0\274\0\215\0\353\0\u011a"+
    "\0\u0149\0\u0178\0\u01a7\0\u011a\0\u01d6\0\u0205\0\u0234\0\u0263"+
    "\0\u0292\0\u02c1\0\u02f0\0\u031f\0\u034e\0\u037d\0\u03ac\0\u03db"+
    "\0\u040a\0\u0439\0\215\0\215\0\u0468\0\215\0\u0497\0\u04c6"+
    "\0\215\0\u04f5\0\u0524\0\215\0\u0553\0\u0582\0\u05b1\0\215"+
    "\0\215\0\u05e0\0\u060f\0\u063e\0\u066d\0\u069c\0\u06cb\0\u06fa"+
    "\0\u0729\0\u0758\0\u0787\0\u07b6\0\u07e5\0\u0814\0\u0843\0\u0872"+
    "\0\u08a1\0\u08d0\0\u08ff\0\u092e\0\u095d\0\u098c\0\u0149\0\u09bb"+
    "\0\u09ea\0\u0a19\0\u0a48\0\u0a77\0\u0aa6\0\u0ad5\0\u0b04\0\u0b33"+
    "\0\u0468\0\215\0\u0b62\0\u0b91\0\u0bc0\0\215\0\215\0\215"+
    "\0\215\0\u0bef\0\u0c1e\0\u0c4d\0\u0c7c\0\u0cab\0\u0cda\0\u0d09"+
    "\0\u0149\0\u0149\0\u0d38\0\u0d67\0\u0d96\0\u0dc5\0\u0149\0\u0149"+
    "\0\u0df4\0\u0149\0\u0e23\0\u0e52\0\u0149\0\u0e81\0\u0eb0\0\u0edf"+
    "\0\u0149\0\u0f0e\0\u0f3d\0\u0f6c\0\u0f9b\0\u0fca\0\u0ff9\0\u1028"+
    "\0\u1057\0\u1086\0\u10b5\0\u10e4\0\u1113\0\u1142\0\215\0\u1171"+
    "\0\u11a0\0\u11cf\0\u11fe\0\u122d\0\u125c\0\u0149\0\u128b\0\u0149"+
    "\0\u12ba\0\u12e9\0\u1318\0\u1347\0\u1376\0\u13a5\0\u13d4\0\u1403"+
    "\0\u1432\0\u1461\0\u09ea\0\u0553\0\u1490\0\u14bf\0\u14ee\0\u151d"+
    "\0\u154c\0\u157b\0\u15aa\0\u0149\0\u15d9\0\u0149\0\u1608\0\u0149"+
    "\0\u1637\0\u1666\0\u1695\0\u16c4\0\u16f3\0\u1722\0\u0553\0\u1751"+
    "\0\u1780\0\u0149\0\u17af\0\u17de\0\u0149\0\u0149\0\u180d\0\u183c"+
    "\0\u09ea\0\u186b\0\u189a\0\u18c9\0\u09ea\0\u18f8\0\u1927\0\u0149"+
    "\0\u1956\0\u0149\0\u1985\0\u19b4\0\u19e3\0\u1a12\0\u09ea\0\u1a41"+
    "\0\u0149\0\u1a70\0\u1a9f\0\u1ace\0\u1afd\0\u1b2c\0\u1b5b\0\u09ea"+
    "\0\u0149\0\u1b8a\0\u1bb9\0\u1be8\0\u1c17\0\u1c46\0\u1c75\0\u1ca4"+
    "\0\u1cd3\0\u09ea\0\u1d02\0\u1d31\0\u09ea\0\u09ea\0\u09ea\0\u0149";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[208];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\4\1\5\2\6\1\7\1\10\1\11\1\4\1\12"+
    "\1\13\1\14\1\15\1\16\1\17\1\20\1\21\5\11"+
    "\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\4"+
    "\3\11\1\31\1\11\1\32\1\33\1\34\1\35\1\36"+
    "\2\4\1\37\1\40\1\10\2\4\1\41\1\42\2\4"+
    "\52\42\1\43\1\44\1\4\1\5\2\6\2\10\1\45"+
    "\1\4\1\12\1\13\1\14\14\45\1\46\1\45\1\47"+
    "\2\45\1\4\5\45\1\4\1\33\1\34\1\50\1\51"+
    "\2\4\1\37\1\4\1\10\3\4\61\0\1\6\60\0"+
    "\1\52\1\53\4\0\1\10\40\0\1\10\7\0\2\10"+
    "\4\0\1\10\40\0\1\10\11\0\4\11\1\0\27\11"+
    "\5\0\1\54\4\0\1\11\56\0\1\55\12\0\2\13"+
    "\42\0\1\55\10\0\4\11\1\0\1\11\1\56\25\11"+
    "\5\0\1\54\4\0\1\11\10\0\4\11\1\0\20\11"+
    "\1\57\6\11\5\0\1\54\4\0\1\11\10\0\4\11"+
    "\1\0\12\11\1\60\14\11\5\0\1\54\4\0\1\11"+
    "\10\0\4\11\1\0\4\11\1\61\22\11\5\0\1\54"+
    "\4\0\1\11\10\0\4\11\1\0\15\11\1\62\11\11"+
    "\5\0\1\54\4\0\1\11\10\0\4\11\1\0\7\11"+
    "\1\63\4\11\1\64\1\11\1\65\10\11\5\0\1\54"+
    "\4\0\1\11\10\0\4\11\1\0\1\66\26\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\6\11\1\67"+
    "\1\11\1\70\16\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\20\11\1\71\6\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\1\72\16\11\1\73\1\74"+
    "\6\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\4\11\1\75\13\11\1\76\6\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\10\11\1\77\16\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\1\11\1\100"+
    "\25\11\5\0\1\54\4\0\1\11\10\0\1\101\4\0"+
    "\1\102\1\103\2\101\1\104\6\101\1\105\2\101\1\106"+
    "\1\107\1\101\1\0\1\110\4\101\15\0\45\111\1\0"+
    "\1\112\6\111\1\113\1\111\6\0\1\114\4\0\21\114"+
    "\1\0\5\114\23\0\1\115\4\0\21\115\1\0\5\115"+
    "\15\0\1\42\2\0\52\42\16\0\1\116\6\0\1\117"+
    "\3\0\1\120\26\0\1\121\6\0\4\45\1\0\27\45"+
    "\12\0\1\45\10\0\4\45\1\0\10\45\1\122\16\45"+
    "\12\0\1\45\10\0\4\45\1\0\1\123\26\45\12\0"+
    "\1\45\2\0\1\124\1\5\1\6\1\124\2\52\4\124"+
    "\1\52\40\124\1\52\3\124\4\125\1\53\1\126\4\125"+
    "\1\53\40\125\1\53\3\125\10\0\1\127\1\130\55\0"+
    "\2\55\53\0\4\11\1\0\2\11\1\131\24\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\14\11\1\132"+
    "\12\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\13\11\1\133\13\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\1\11\1\134\25\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\12\11\1\135\14\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\22\11\1\136"+
    "\4\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\4\11\1\137\22\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\16\11\1\140\2\11\1\141\5\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\14\11\1\142"+
    "\12\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\4\11\1\143\22\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\25\11\1\144\1\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\10\11\1\145\16\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\17\11\1\146"+
    "\7\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\4\11\1\147\22\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\10\11\1\150\16\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\14\11\1\151\12\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\23\11\1\152"+
    "\3\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\11\11\1\153\1\11\1\154\13\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\101\1\0\27\101\12\0\1\101\10\0"+
    "\4\101\1\0\13\101\1\155\13\101\12\0\1\101\10\0"+
    "\4\101\1\0\20\101\1\156\6\101\12\0\1\101\10\0"+
    "\4\101\1\0\15\101\1\157\11\101\12\0\1\101\10\0"+
    "\4\101\1\0\14\101\1\160\12\101\12\0\1\101\10\0"+
    "\4\101\1\0\20\101\1\161\6\101\12\0\1\101\10\0"+
    "\4\101\1\0\20\101\1\162\6\101\12\0\1\101\10\0"+
    "\4\101\1\0\1\163\26\101\12\0\1\101\47\0\2\111"+
    "\6\0\1\111\7\0\4\114\1\0\27\114\12\0\1\114"+
    "\10\0\4\115\1\0\27\115\12\0\1\115\10\0\4\45"+
    "\1\0\25\45\1\164\1\45\12\0\1\45\10\0\4\45"+
    "\1\0\17\45\1\165\7\45\12\0\1\45\2\0\1\124"+
    "\1\5\1\6\54\124\5\125\1\166\55\125\1\10\1\126"+
    "\4\125\1\53\40\125\1\53\3\125\42\0\1\54\5\0"+
    "\1\167\16\0\2\130\30\0\1\54\5\0\1\167\14\0"+
    "\4\11\1\0\14\11\1\170\12\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\5\11\1\171\21\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\13\11\1\172"+
    "\13\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\17\11\1\173\7\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\14\11\1\174\12\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\20\11\1\175\6\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\4\11\1\176"+
    "\22\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\13\11\1\177\13\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\14\11\1\200\12\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\1\201\26\11\5\0\1\54"+
    "\4\0\1\11\10\0\4\11\1\0\12\11\1\202\14\11"+
    "\5\0\1\54\4\0\1\11\10\0\4\11\1\0\1\11"+
    "\1\203\25\11\5\0\1\54\4\0\1\11\10\0\4\11"+
    "\1\0\1\204\26\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\101\1\0\13\101\1\205\13\101\12\0\1\101\10\0"+
    "\4\101\1\0\14\101\1\206\12\101\12\0\1\101\10\0"+
    "\4\101\1\0\14\101\1\207\12\101\12\0\1\101\10\0"+
    "\4\101\1\0\1\210\26\101\12\0\1\101\10\0\4\101"+
    "\1\0\10\101\1\211\16\101\12\0\1\101\10\0\4\101"+
    "\1\0\23\101\1\212\3\101\12\0\1\101\10\0\4\101"+
    "\1\0\14\101\1\213\12\101\12\0\1\101\10\0\4\45"+
    "\1\0\4\45\1\214\22\45\12\0\1\45\10\0\4\45"+
    "\1\0\13\45\1\215\13\45\12\0\1\45\2\0\4\125"+
    "\1\6\1\166\51\125\6\0\4\11\1\0\12\11\1\216"+
    "\14\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\6\11\1\217\20\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\14\11\1\220\12\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\12\11\1\221\14\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\6\11\1\222"+
    "\20\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\10\11\1\223\16\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\4\11\1\224\22\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\17\11\1\225\7\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\5\11\1\226"+
    "\21\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\20\11\1\227\6\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\14\11\1\230\12\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\101\1\0\25\101\1\231\1\101\12\0"+
    "\1\101\10\0\4\101\1\0\4\101\1\232\22\101\12\0"+
    "\1\101\10\0\4\101\1\0\10\101\1\233\16\101\12\0"+
    "\1\101\10\0\4\101\1\0\14\101\1\234\12\101\12\0"+
    "\1\101\10\0\4\101\1\0\7\101\1\235\17\101\12\0"+
    "\1\101\10\0\4\101\1\0\12\101\1\236\14\101\12\0"+
    "\1\101\10\0\4\45\1\0\4\45\1\237\22\45\12\0"+
    "\1\45\10\0\4\11\1\0\1\11\1\240\25\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\7\11\1\241"+
    "\17\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\13\11\1\242\13\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\4\11\1\243\22\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\4\11\1\244\22\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\24\11\1\245"+
    "\2\11\5\0\1\54\4\0\1\11\10\0\4\11\1\0"+
    "\17\11\1\246\7\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\11\1\0\26\11\1\247\5\0\1\54\4\0\1\11"+
    "\10\0\4\101\1\0\7\101\1\250\17\101\12\0\1\101"+
    "\10\0\4\101\1\0\13\101\1\251\13\101\12\0\1\101"+
    "\10\0\4\101\1\0\1\252\26\101\12\0\1\101\10\0"+
    "\4\101\1\0\25\101\1\253\1\101\12\0\1\101\10\0"+
    "\4\101\1\0\25\101\1\254\1\101\12\0\1\101\10\0"+
    "\4\101\1\0\5\101\1\255\21\101\12\0\1\101\10\0"+
    "\4\11\1\0\5\11\1\256\21\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\11\1\0\1\257\26\11\5\0\1\54"+
    "\4\0\1\11\10\0\4\11\1\0\13\11\1\260\13\11"+
    "\5\0\1\54\4\0\1\11\10\0\4\11\1\0\1\11"+
    "\1\261\25\11\5\0\1\54\4\0\1\11\10\0\4\11"+
    "\1\0\1\11\1\262\25\11\5\0\1\54\4\0\1\11"+
    "\10\0\4\101\1\0\22\101\1\263\4\101\12\0\1\101"+
    "\10\0\4\101\1\0\13\101\1\264\2\101\1\265\3\101"+
    "\1\266\4\101\12\0\1\101\10\0\4\101\1\0\13\101"+
    "\1\267\13\101\12\0\1\101\10\0\4\101\1\0\17\101"+
    "\1\270\7\101\12\0\1\101\10\0\4\11\1\0\14\11"+
    "\1\271\12\11\5\0\1\54\4\0\1\11\10\0\4\11"+
    "\1\0\10\11\1\272\16\11\5\0\1\54\4\0\1\11"+
    "\10\0\4\11\1\0\21\11\1\273\5\11\5\0\1\54"+
    "\4\0\1\11\10\0\4\101\1\0\14\101\1\274\12\101"+
    "\12\0\1\101\10\0\4\101\1\0\20\101\1\275\6\101"+
    "\12\0\1\101\10\0\4\101\1\0\25\101\1\276\1\101"+
    "\12\0\1\101\10\0\4\101\1\0\10\101\1\277\16\101"+
    "\12\0\1\101\10\0\4\101\1\0\1\300\26\101\12\0"+
    "\1\101\10\0\4\11\1\0\11\11\1\301\15\11\5\0"+
    "\1\54\4\0\1\11\10\0\4\11\1\0\4\11\1\302"+
    "\22\11\5\0\1\54\4\0\1\11\10\0\4\101\1\0"+
    "\12\101\1\303\14\101\12\0\1\101\10\0\4\101\1\0"+
    "\10\101\1\304\16\101\12\0\1\101\10\0\4\101\1\0"+
    "\1\101\1\305\25\101\12\0\1\101\10\0\4\101\1\0"+
    "\4\101\1\306\22\101\12\0\1\101\10\0\4\11\1\0"+
    "\17\11\1\307\7\11\5\0\1\54\4\0\1\11\10\0"+
    "\4\101\1\0\20\101\1\310\6\101\12\0\1\101\10\0"+
    "\4\101\1\0\14\101\1\311\12\101\12\0\1\101\10\0"+
    "\4\101\1\0\13\101\1\312\13\101\12\0\1\101\10\0"+
    "\4\101\1\0\2\101\1\313\24\101\12\0\1\101\10\0"+
    "\4\11\1\0\13\11\1\314\13\11\5\0\1\54\4\0"+
    "\1\11\10\0\4\101\1\0\1\101\1\315\25\101\12\0"+
    "\1\101\10\0\4\101\1\0\13\101\1\316\13\101\12\0"+
    "\1\101\10\0\4\101\1\0\13\101\1\317\13\101\12\0"+
    "\1\101\10\0\4\11\1\0\4\11\1\320\22\11\5\0"+
    "\1\54\4\0\1\11\2\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[7520];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\3\0\1\11\1\1\1\11\24\1\2\11\1\1\1\11"+
    "\2\1\1\11\2\1\1\11\3\1\2\11\2\1\1\0"+
    "\34\1\1\0\1\11\1\0\2\1\4\11\3\1\1\0"+
    "\1\1\2\0\35\1\1\0\1\11\131\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[208];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
  private StringBuffer string = new StringBuffer();
  private MySymbolFactory symFactory;
  
  public void setSymbolFactory(MySymbolFactory factory) {
    symFactory = factory;
  }

  private Symbol symbol(int type) {
    return symFactory.newSymbol(yytext(), type, yyline+1, yycolumn, yyline+1, yycolumn+yylength());
  }
  private Symbol symbol(int type, String value) {
    return symFactory.newSymbol(value, type, yyline+1, yycolumn, yyline+1, yycolumn+yylength(), value);
  }


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public Lexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public Lexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 1306) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead > 0) {
      zzEndRead+= numRead;
      return false;
    }
    // unlikely but not impossible: read 0 characters, but not at end of stream    
    if (numRead == 0) {
      int c = zzReader.read();
      if (c == -1) {
        return true;
      } else {
        zzBuffer[zzEndRead++] = (char) c;
        return false;
      }     
    }

	// numRead < 0
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() throws java.io.IOException {
    if (!zzEOFDone) {
      zzEOFDone = true;
      yyclose();
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public java_cup.runtime.Symbol next_token() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      for (zzCurrentPosL = zzStartRead; zzCurrentPosL < zzMarkedPosL;
                                                             zzCurrentPosL++) {
        switch (zzBufferL[zzCurrentPosL]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn++;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 2: 
          { /* ignore */
          }
        case 56: break;
        case 32: 
          { return symbol(LexerSymbols.LET);
          }
        case 57: break;
        case 42: 
          { return symbol(LexerSymbols.FORALL);
          }
        case 58: break;
        case 1: 
          { return symbol(LexerSymbols.error, yytext());
          }
        case 59: break;
        case 15: 
          { yybegin(YYINITIAL); return symbol(LexerSymbols.RCPAR);
          }
        case 60: break;
        case 31: 
          { return symbol(LexerSymbols.XOR);
          }
        case 61: break;
        case 16: 
          { return symbol(LexerSymbols.RATIONAL, yytext());
          }
        case 62: break;
        case 34: 
          { return symbol(LexerSymbols.TRUE);
          }
        case 63: break;
        case 46: 
          { return symbol(LexerSymbols.UNKNOWN);
          }
        case 64: break;
        case 33: 
          { return symbol(LexerSymbols.INDEXED_ID, yytext());
          }
        case 65: break;
        case 47: 
          { return symbol(LexerSymbols.ATTR_STATUS);
          }
        case 66: break;
        case 24: 
          { string.append('\t');
          }
        case 67: break;
        case 11: 
          { string.setLength(0); yybegin(STRING);
          }
        case 68: break;
        case 5: 
          { return symbol(LexerSymbols.NUMERAL, yytext());
          }
        case 69: break;
        case 55: 
          { return symbol(LexerSymbols.IF_THEN_ELSE);
          }
        case 70: break;
        case 50: 
          { return symbol(LexerSymbols.BENCHMARK);
          }
        case 71: break;
        case 52: 
          { return symbol(LexerSymbols.ATTR_ASSUMPTION);
          }
        case 72: break;
        case 14: 
          { yybegin(YYINITIAL); 
                                   return symbol(LexerSymbols.ATTR_STRING, 
                                   string.toString());
          }
        case 73: break;
        case 35: 
          { return symbol(LexerSymbols.FLET);
          }
        case 74: break;
        case 43: 
          { return symbol(LexerSymbols.ATTR_NOTES);
          }
        case 75: break;
        case 37: 
          { return symbol(LexerSymbols.FALSE);
          }
        case 76: break;
        case 13: 
          { string.append('\\');
          }
        case 77: break;
        case 44: 
          { return symbol(LexerSymbols.ATTR_LOGIC);
          }
        case 78: break;
        case 9: 
          { return symbol(LexerSymbols.LCPAR);
          }
        case 79: break;
        case 23: 
          { string.append('\r');
          }
        case 80: break;
        case 10: 
          { return symbol(LexerSymbols.RCPAR);
          }
        case 81: break;
        case 20: 
          { return symbol(LexerSymbols.VAR, yytext().substring(1));
          }
        case 82: break;
        case 51: 
          { return symbol(LexerSymbols.ATTR_EXTRAFUNS);
          }
        case 83: break;
        case 25: 
          { string.append('\"');
          }
        case 84: break;
        case 54: 
          { return symbol(LexerSymbols.ATTR_EXTRAPREDS);
          }
        case 85: break;
        case 30: 
          { return symbol(LexerSymbols.SAT);
          }
        case 86: break;
        case 41: 
          { return symbol(LexerSymbols.THEORY);
          }
        case 87: break;
        case 18: 
          { return symbol(LexerSymbols.ATTRIBUTE, yytext().substring(1));
          }
        case 88: break;
        case 45: 
          { return symbol(LexerSymbols.IMPLIES);
          }
        case 89: break;
        case 27: 
          { return symbol(LexerSymbols.NOT);
          }
        case 90: break;
        case 26: 
          { return symbol(LexerSymbols.AND);
          }
        case 91: break;
        case 48: 
          { return symbol(LexerSymbols.DISTINCT);
          }
        case 92: break;
        case 3: 
          { return symbol(LexerSymbols.ARITH_SYMB, yytext());
          }
        case 93: break;
        case 22: 
          { string.append('\n');
          }
        case 94: break;
        case 6: 
          { return symbol(LexerSymbols.EQUALS);
          }
        case 95: break;
        case 21: 
          { return symbol(LexerSymbols.FVAR, yytext().substring(1));
          }
        case 96: break;
        case 36: 
          { yybegin(PATTERN); return symbol(LexerSymbols.ATTR_PATTERN);
          }
        case 97: break;
        case 28: 
          { return symbol(LexerSymbols.ITE);
          }
        case 98: break;
        case 7: 
          { return symbol(LexerSymbols.LPAR);
          }
        case 99: break;
        case 40: 
          { return symbol(LexerSymbols.EXISTS);
          }
        case 100: break;
        case 49: 
          { return symbol(LexerSymbols.ATTR_FORMULA);
          }
        case 101: break;
        case 29: 
          { return symbol(LexerSymbols.IFF);
          }
        case 102: break;
        case 8: 
          { return symbol(LexerSymbols.RPAR);
          }
        case 103: break;
        case 4: 
          { return symbol(LexerSymbols.ID, yytext());
          }
        case 104: break;
        case 12: 
          { string.append( yytext() );
          }
        case 105: break;
        case 17: 
          { return symbol(LexerSymbols.OR);
          }
        case 106: break;
        case 39: 
          { return symbol(LexerSymbols.UNSAT);
          }
        case 107: break;
        case 53: 
          { return symbol(LexerSymbols.ATTR_EXTRASORTS);
          }
        case 108: break;
        case 38: 
          { return symbol(LexerSymbols.LOGIC);
          }
        case 109: break;
        case 19: 
          { return symbol(LexerSymbols.USERVAL, yytext());
          }
        case 110: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            zzDoEOF();
              { return new java_cup.runtime.Symbol(LexerSymbols.EOF); }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
