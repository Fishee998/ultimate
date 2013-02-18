/* The following code was generated by JFlex 1.4.2 on 21.03.11 10:48 */

/* Boogie 2 lexer */
package local.stalin.boogie.parser;
import java_cup.runtime.Symbol;

/**
 * This is a autogenerated lexer for Boogie 2.
 * It is generated from Boogie.flex by JFlex.
 */

class Lexer implements java_cup.runtime.Scanner {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int STRING = 2;
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1, 1
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\3\1\2\1\0\1\3\1\1\22\0\1\3\1\64\1\102"+
    "\2\6\1\70\1\72\1\6\1\45\1\46\1\5\1\66\1\53\1\67"+
    "\1\12\1\4\1\10\11\11\1\54\1\55\1\60\1\56\1\61\1\6"+
    "\1\0\32\6\1\47\1\103\1\50\2\6\1\0\1\30\1\13\1\21"+
    "\1\34\1\20\1\25\1\42\1\40\1\27\1\6\1\41\1\35\1\32"+
    "\1\23\1\22\1\17\1\36\1\33\1\24\1\15\1\26\1\14\1\37"+
    "\1\31\1\16\1\6\1\51\1\74\1\52\1\6\53\0\1\6\1\0"+
    "\1\71\10\0\1\6\4\0\1\6\5\0\27\6\1\0\37\6\1\0"+
    "\u013f\6\31\0\162\6\4\0\14\6\16\0\5\6\11\0\1\6\213\0"+
    "\1\6\13\0\1\6\1\0\3\6\1\0\1\6\1\0\24\6\1\0"+
    "\54\6\1\0\46\6\1\0\5\6\4\0\202\6\10\0\105\6\1\0"+
    "\46\6\2\0\2\6\6\0\20\6\41\0\46\6\2\0\1\6\7\0"+
    "\47\6\110\0\33\6\5\0\3\6\56\0\32\6\5\0\13\6\25\0"+
    "\12\7\4\0\2\6\1\0\143\6\1\0\1\6\17\0\2\6\7\0"+
    "\2\6\12\7\3\6\2\0\1\6\20\0\1\6\1\0\36\6\35\0"+
    "\3\6\60\0\46\6\13\0\1\6\u0152\0\66\6\3\0\1\6\22\0"+
    "\1\6\7\0\12\6\4\0\12\7\25\0\10\6\2\0\2\6\2\0"+
    "\26\6\1\0\7\6\1\0\1\6\3\0\4\6\3\0\1\6\36\0"+
    "\2\6\1\0\3\6\4\0\12\7\2\6\23\0\6\6\4\0\2\6"+
    "\2\0\26\6\1\0\7\6\1\0\2\6\1\0\2\6\1\0\2\6"+
    "\37\0\4\6\1\0\1\6\7\0\12\7\2\0\3\6\20\0\11\6"+
    "\1\0\3\6\1\0\26\6\1\0\7\6\1\0\2\6\1\0\5\6"+
    "\3\0\1\6\22\0\1\6\17\0\2\6\4\0\12\7\25\0\10\6"+
    "\2\0\2\6\2\0\26\6\1\0\7\6\1\0\2\6\1\0\5\6"+
    "\3\0\1\6\36\0\2\6\1\0\3\6\4\0\12\7\1\0\1\6"+
    "\21\0\1\6\1\0\6\6\3\0\3\6\1\0\4\6\3\0\2\6"+
    "\1\0\1\6\1\0\2\6\3\0\2\6\3\0\3\6\3\0\10\6"+
    "\1\0\3\6\55\0\11\7\25\0\10\6\1\0\3\6\1\0\27\6"+
    "\1\0\12\6\1\0\5\6\46\0\2\6\4\0\12\7\25\0\10\6"+
    "\1\0\3\6\1\0\27\6\1\0\12\6\1\0\5\6\3\0\1\6"+
    "\40\0\1\6\1\0\2\6\4\0\12\7\25\0\10\6\1\0\3\6"+
    "\1\0\27\6\1\0\20\6\46\0\2\6\4\0\12\7\25\0\22\6"+
    "\3\0\30\6\1\0\11\6\1\0\1\6\2\0\7\6\72\0\60\6"+
    "\1\0\2\6\14\0\7\6\11\0\12\7\47\0\2\6\1\0\1\6"+
    "\2\0\2\6\1\0\1\6\2\0\1\6\6\0\4\6\1\0\7\6"+
    "\1\0\3\6\1\0\1\6\1\0\1\6\2\0\2\6\1\0\4\6"+
    "\1\0\2\6\11\0\1\6\2\0\5\6\1\0\1\6\11\0\12\7"+
    "\2\0\2\6\42\0\1\6\37\0\12\7\26\0\10\6\1\0\42\6"+
    "\35\0\4\6\164\0\42\6\1\0\5\6\1\0\2\6\25\0\12\7"+
    "\6\0\6\6\112\0\46\6\12\0\51\6\7\0\132\6\5\0\104\6"+
    "\5\0\122\6\6\0\7\6\1\0\77\6\1\0\1\6\1\0\4\6"+
    "\2\0\7\6\1\0\1\6\1\0\4\6\2\0\47\6\1\0\1\6"+
    "\1\0\4\6\2\0\37\6\1\0\1\6\1\0\4\6\2\0\7\6"+
    "\1\0\1\6\1\0\4\6\2\0\7\6\1\0\7\6\1\0\27\6"+
    "\1\0\37\6\1\0\1\6\1\0\4\6\2\0\7\6\1\0\47\6"+
    "\1\0\23\6\16\0\11\7\56\0\125\6\14\0\u026c\6\2\0\10\6"+
    "\12\0\32\6\5\0\113\6\25\0\15\6\1\0\4\6\16\0\22\6"+
    "\16\0\22\6\16\0\15\6\1\0\3\6\17\0\64\6\43\0\1\6"+
    "\4\0\1\6\3\0\12\7\46\0\12\7\6\0\130\6\10\0\51\6"+
    "\127\0\35\6\51\0\12\7\36\6\2\0\5\6\u038b\0\154\6\224\0"+
    "\234\6\4\0\132\6\6\0\26\6\2\0\6\6\2\0\46\6\2\0"+
    "\6\6\2\0\10\6\1\0\1\6\1\0\1\6\1\0\1\6\1\0"+
    "\37\6\2\0\65\6\1\0\7\6\1\0\1\6\3\0\3\6\1\0"+
    "\7\6\3\0\4\6\2\0\6\6\4\0\15\6\5\0\3\6\1\0"+
    "\7\6\45\0\1\101\116\0\1\6\15\0\1\6\202\0\1\6\4\0"+
    "\1\6\2\0\12\6\1\0\1\6\3\0\5\6\6\0\1\6\1\0"+
    "\1\6\1\0\1\6\1\0\4\6\1\0\3\6\1\0\7\6\3\0"+
    "\3\6\5\0\5\6\206\0\1\77\1\0\1\76\1\0\1\100\53\0"+
    "\1\43\2\0\1\44\43\0\1\73\1\75\53\0\1\57\13\0\1\65"+
    "\3\0\1\62\1\63\u0d9f\0\2\6\52\0\5\6\5\0\2\6\4\0"+
    "\126\6\6\0\3\6\1\0\132\6\1\0\4\6\5\0\50\6\4\0"+
    "\136\6\21\0\30\6\70\0\20\6\u0200\0\u19b6\6\112\0\u51a6\6\132\0"+
    "\u048d\6\u0773\0\u2ba4\6\u215c\0\u012e\6\2\0\73\6\225\0\7\6\14\0"+
    "\5\6\5\0\1\6\1\0\12\6\1\0\15\6\1\0\5\6\1\0"+
    "\1\6\1\0\2\6\1\0\2\6\1\0\154\6\41\0\u016b\6\22\0"+
    "\100\6\2\0\66\6\50\0\14\6\164\0\5\6\1\0\207\6\23\0"+
    "\12\7\7\0\32\6\6\0\32\6\13\0\131\6\3\0\6\6\2\0"+
    "\6\6\2\0\6\6\2\0\3\6\43\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\2\0\1\1\2\2\1\3\1\4\1\5\2\6\20\5"+
    "\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16"+
    "\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26"+
    "\1\27\1\30\1\31\1\32\1\33\1\34\1\30\1\1"+
    "\1\35\1\1\1\36\1\37\1\40\1\41\1\42\1\43"+
    "\1\44\1\45\1\46\1\2\3\0\25\5\1\47\10\5"+
    "\1\50\1\51\1\26\1\52\1\53\1\54\1\55\1\56"+
    "\1\0\1\57\1\0\2\60\2\5\1\61\12\5\1\62"+
    "\7\5\1\63\13\5\1\40\2\64\1\65\1\5\1\66"+
    "\1\67\1\70\3\5\1\71\2\5\1\72\4\5\1\73"+
    "\10\5\1\74\4\5\1\75\1\76\3\5\1\77\4\5"+
    "\1\100\5\5\1\101\3\5\1\102\1\103\1\104\2\5"+
    "\1\10\1\5\1\7\1\5\1\105\1\106\2\5\1\107"+
    "\1\110\1\5\1\111\2\5\1\112\5\5\1\113\2\5"+
    "\1\114\1\115\2\5\1\116\1\117\1\120\1\121\5\5"+
    "\1\122";

  private static int [] zzUnpackAction() {
    int [] result = new int[236];
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
    "\0\0\0\104\0\210\0\314\0\210\0\u0110\0\210\0\u0154"+
    "\0\u0198\0\u01dc\0\u0220\0\u0264\0\u02a8\0\u02ec\0\u0330\0\u0374"+
    "\0\u03b8\0\u03fc\0\u0440\0\u0484\0\u04c8\0\u050c\0\u0550\0\u0594"+
    "\0\u05d8\0\u061c\0\210\0\210\0\210\0\210\0\210\0\210"+
    "\0\210\0\210\0\210\0\u0660\0\210\0\u06a4\0\210\0\u06e8"+
    "\0\u072c\0\210\0\210\0\u0770\0\210\0\u07b4\0\210\0\210"+
    "\0\210\0\u07f8\0\210\0\u083c\0\210\0\210\0\210\0\210"+
    "\0\210\0\210\0\u0880\0\210\0\u08c4\0\u0908\0\u094c\0\u0990"+
    "\0\u09d4\0\u0a18\0\u0a5c\0\u0aa0\0\u0ae4\0\u0b28\0\u0b6c\0\u0bb0"+
    "\0\u0bf4\0\u0c38\0\u0c7c\0\u0cc0\0\u0d04\0\u0d48\0\u0d8c\0\u0dd0"+
    "\0\u0e14\0\u0e58\0\u0e9c\0\u0ee0\0\u0f24\0\u0f68\0\u0154\0\u0fac"+
    "\0\u0ff0\0\u1034\0\u1078\0\u10bc\0\u1100\0\u1144\0\u1188\0\u11cc"+
    "\0\210\0\u1210\0\210\0\210\0\210\0\210\0\210\0\u1254"+
    "\0\u0990\0\u1298\0\u0154\0\u12dc\0\u1320\0\u1364\0\u0154\0\u13a8"+
    "\0\u13ec\0\u1430\0\u1474\0\u14b8\0\u14fc\0\u1540\0\u1584\0\u15c8"+
    "\0\u160c\0\u0154\0\u1650\0\u1694\0\u16d8\0\u171c\0\u1760\0\u17a4"+
    "\0\u17e8\0\u0154\0\u182c\0\u1870\0\u18b4\0\u18f8\0\u193c\0\u1980"+
    "\0\u19c4\0\u1a08\0\u1a4c\0\u1a90\0\u1ad4\0\u1b18\0\210\0\u1b5c"+
    "\0\u0154\0\u1ba0\0\u0154\0\u0154\0\u0154\0\u1be4\0\u1c28\0\u1c6c"+
    "\0\u0154\0\u1cb0\0\u1cf4\0\u0154\0\u1d38\0\u1d7c\0\u1dc0\0\u1e04"+
    "\0\u0154\0\u1e48\0\u1e8c\0\u1ed0\0\u1f14\0\u1f58\0\u1f9c\0\u1fe0"+
    "\0\u2024\0\u0154\0\u2068\0\u20ac\0\u20f0\0\u2134\0\u0154\0\u0154"+
    "\0\u2178\0\u21bc\0\u2200\0\u0154\0\u2244\0\u2288\0\u22cc\0\u2310"+
    "\0\u0154\0\u2354\0\u2398\0\u23dc\0\u2420\0\u2464\0\u0154\0\u24a8"+
    "\0\u24ec\0\u2530\0\u0154\0\u0154\0\u0154\0\u2574\0\u25b8\0\u0154"+
    "\0\u25fc\0\u0154\0\u2640\0\u0154\0\u0154\0\u2684\0\u26c8\0\u0154"+
    "\0\u0154\0\u270c\0\u2750\0\u2794\0\u27d8\0\u0154\0\u281c\0\u2860"+
    "\0\u28a4\0\u28e8\0\u292c\0\u0154\0\u2970\0\u29b4\0\u0154\0\u0154"+
    "\0\u29f8\0\u2a3c\0\u0154\0\u0154\0\u0154\0\u0154\0\u2a80\0\u2ac4"+
    "\0\u2b08\0\u2b4c\0\u2b90\0\u0154";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[236];
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
    "\1\3\1\4\2\5\1\6\1\7\1\10\1\3\1\11"+
    "\1\12\1\10\1\13\1\14\1\15\1\10\1\16\1\17"+
    "\1\20\1\21\2\10\1\22\1\23\1\24\1\25\1\10"+
    "\1\26\1\27\3\10\1\30\1\31\1\10\1\32\1\33"+
    "\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43"+
    "\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53"+
    "\1\54\1\55\1\56\1\57\1\60\1\61\1\62\1\63"+
    "\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1\10"+
    "\1\73\2\3\77\73\1\74\1\75\106\0\1\5\105\0"+
    "\1\76\1\77\104\0\35\10\40\0\1\10\12\0\1\100"+
    "\1\101\100\0\2\12\1\100\1\101\76\0\6\10\1\102"+
    "\5\10\1\103\10\10\1\104\7\10\40\0\1\10\6\0"+
    "\22\10\1\105\12\10\40\0\1\10\6\0\10\10\1\106"+
    "\14\10\1\107\4\10\1\110\2\10\40\0\1\10\6\0"+
    "\25\10\1\111\7\10\40\0\1\10\6\0\15\10\1\112"+
    "\5\10\1\113\3\10\1\114\5\10\40\0\1\10\6\0"+
    "\14\10\1\115\5\10\1\116\12\10\40\0\1\10\6\0"+
    "\27\10\1\117\5\10\40\0\1\10\6\0\14\10\1\120"+
    "\3\10\1\121\1\122\1\123\2\10\1\124\7\10\40\0"+
    "\1\10\6\0\15\10\1\125\17\10\40\0\1\10\6\0"+
    "\15\10\1\126\1\10\1\127\4\10\1\130\10\10\40\0"+
    "\1\10\6\0\16\10\1\131\4\10\1\132\11\10\40\0"+
    "\1\10\6\0\14\10\1\133\20\10\40\0\1\10\6\0"+
    "\12\10\1\134\22\10\40\0\1\10\6\0\32\10\1\135"+
    "\2\10\40\0\1\10\6\0\22\10\1\136\12\10\40\0"+
    "\1\10\6\0\14\10\1\137\20\10\40\0\1\10\54\0"+
    "\1\71\1\0\1\47\103\0\1\140\101\0\1\141\1\0"+
    "\1\142\103\0\1\53\103\0\1\55\113\0\1\143\107\0"+
    "\1\63\105\0\1\65\7\0\1\73\2\0\77\73\17\0"+
    "\1\144\5\0\1\145\7\0\1\146\46\0\1\147\1\0"+
    "\1\76\1\4\1\5\101\76\5\77\1\150\76\77\10\0"+
    "\2\151\106\0\1\152\75\0\2\10\1\153\1\154\31\10"+
    "\40\0\1\10\6\0\14\10\1\155\20\10\40\0\1\10"+
    "\6\0\12\10\1\156\22\10\40\0\1\10\6\0\25\10"+
    "\1\157\7\10\40\0\1\10\6\0\11\10\1\160\23\10"+
    "\40\0\1\10\6\0\20\10\1\161\14\10\40\0\1\10"+
    "\6\0\12\10\1\162\22\10\40\0\1\10\6\0\14\10"+
    "\1\163\20\10\40\0\1\10\6\0\16\10\1\164\16\10"+
    "\40\0\1\10\6\0\21\10\1\165\13\10\40\0\1\10"+
    "\6\0\16\10\1\166\16\10\40\0\1\10\6\0\15\10"+
    "\1\167\6\10\1\170\10\10\40\0\1\10\6\0\27\10"+
    "\1\171\5\10\40\0\1\10\6\0\26\10\1\172\6\10"+
    "\40\0\1\10\6\0\25\10\1\173\7\10\40\0\1\10"+
    "\6\0\15\10\1\174\17\10\40\0\1\10\6\0\15\10"+
    "\1\175\17\10\40\0\1\10\6\0\27\10\1\176\5\10"+
    "\40\0\1\10\6\0\12\10\1\177\22\10\40\0\1\10"+
    "\6\0\21\10\1\200\13\10\40\0\1\10\6\0\6\10"+
    "\1\201\1\202\25\10\40\0\1\10\6\0\11\10\1\203"+
    "\23\10\40\0\1\10\6\0\16\10\1\204\16\10\40\0"+
    "\1\10\6\0\21\10\1\205\13\10\40\0\1\10\6\0"+
    "\26\10\1\206\6\10\40\0\1\10\6\0\7\10\1\207"+
    "\12\10\1\210\5\10\1\211\4\10\40\0\1\10\6\0"+
    "\12\10\1\212\6\10\1\213\13\10\40\0\1\10\6\0"+
    "\6\10\1\214\26\10\40\0\1\10\6\0\7\10\1\215"+
    "\25\10\40\0\1\10\61\0\1\66\100\0\1\216\25\0"+
    "\4\77\1\5\1\150\76\77\10\0\1\217\1\220\100\0"+
    "\2\10\2\154\31\10\40\0\1\10\6\0\27\10\1\221"+
    "\5\10\40\0\1\10\6\0\22\10\1\222\12\10\40\0"+
    "\1\10\6\0\12\10\1\223\22\10\40\0\1\10\6\0"+
    "\12\10\1\224\22\10\40\0\1\10\6\0\15\10\1\225"+
    "\17\10\40\0\1\10\6\0\13\10\1\226\21\10\40\0"+
    "\1\10\6\0\20\10\1\227\14\10\40\0\1\10\6\0"+
    "\16\10\1\230\16\10\40\0\1\10\6\0\12\10\1\231"+
    "\22\10\40\0\1\10\6\0\16\10\1\232\16\10\40\0"+
    "\1\10\6\0\11\10\1\233\23\10\40\0\1\10\6\0"+
    "\27\10\1\234\5\10\40\0\1\10\6\0\22\10\1\235"+
    "\12\10\40\0\1\10\6\0\13\10\1\236\21\10\40\0"+
    "\1\10\6\0\21\10\1\237\13\10\40\0\1\10\6\0"+
    "\16\10\1\240\16\10\40\0\1\10\6\0\12\10\1\241"+
    "\22\10\40\0\1\10\6\0\30\10\1\242\4\10\40\0"+
    "\1\10\6\0\22\10\1\243\12\10\40\0\1\10\6\0"+
    "\27\10\1\244\5\10\40\0\1\10\6\0\12\10\1\245"+
    "\5\10\1\246\14\10\40\0\1\10\6\0\14\10\1\247"+
    "\20\10\40\0\1\10\6\0\21\10\1\250\13\10\40\0"+
    "\1\10\6\0\20\10\1\251\14\10\40\0\1\10\6\0"+
    "\27\10\1\252\5\10\40\0\1\10\6\0\20\10\1\253"+
    "\14\10\40\0\1\10\6\0\25\10\1\254\7\10\40\0"+
    "\1\10\6\0\27\10\1\255\5\10\40\0\1\10\6\0"+
    "\14\10\1\256\20\10\40\0\1\10\6\0\14\10\1\257"+
    "\20\10\40\0\1\10\61\0\1\70\32\0\2\220\100\0"+
    "\33\10\1\260\1\10\40\0\1\10\6\0\12\10\1\261"+
    "\22\10\40\0\1\10\6\0\25\10\1\262\7\10\40\0"+
    "\1\10\6\0\7\10\1\263\25\10\40\0\1\10\6\0"+
    "\7\10\1\264\25\10\40\0\1\10\6\0\27\10\1\265"+
    "\5\10\40\0\1\10\6\0\27\10\1\266\5\10\40\0"+
    "\1\10\6\0\7\10\1\267\25\10\40\0\1\10\6\0"+
    "\7\10\1\270\25\10\40\0\1\10\6\0\12\10\1\271"+
    "\22\10\40\0\1\10\6\0\20\10\1\272\14\10\40\0"+
    "\1\10\6\0\25\10\1\273\7\10\40\0\1\10\6\0"+
    "\12\10\1\274\22\10\40\0\1\10\6\0\25\10\1\275"+
    "\7\10\40\0\1\10\6\0\24\10\1\276\10\10\40\0"+
    "\1\10\6\0\24\10\1\277\10\10\40\0\1\10\6\0"+
    "\17\10\1\300\15\10\40\0\1\10\6\0\25\10\1\301"+
    "\7\10\40\0\1\10\6\0\21\10\1\302\13\10\40\0"+
    "\1\10\6\0\12\10\1\303\22\10\40\0\1\10\6\0"+
    "\12\10\1\304\22\10\40\0\1\10\6\0\13\10\1\305"+
    "\21\10\40\0\1\10\6\0\26\10\1\306\6\10\40\0"+
    "\1\10\6\0\12\10\1\307\22\10\40\0\1\10\6\0"+
    "\16\10\1\310\16\10\40\0\1\10\6\0\12\10\1\311"+
    "\22\10\40\0\1\10\6\0\27\10\1\312\5\10\40\0"+
    "\1\10\6\0\21\10\1\313\13\10\40\0\1\10\6\0"+
    "\12\10\1\314\22\10\40\0\1\10\6\0\12\10\1\315"+
    "\22\10\40\0\1\10\6\0\21\10\1\316\13\10\40\0"+
    "\1\10\6\0\24\10\1\317\10\10\40\0\1\10\6\0"+
    "\7\10\1\320\25\10\40\0\1\10\6\0\12\10\1\321"+
    "\22\10\40\0\1\10\6\0\21\10\1\322\13\10\40\0"+
    "\1\10\6\0\15\10\1\323\17\10\40\0\1\10\6\0"+
    "\25\10\1\324\7\10\40\0\1\10\6\0\20\10\1\325"+
    "\14\10\40\0\1\10\6\0\16\10\1\326\16\10\40\0"+
    "\1\10\6\0\7\10\1\327\25\10\40\0\1\10\6\0"+
    "\14\10\1\330\20\10\40\0\1\10\6\0\22\10\1\331"+
    "\12\10\40\0\1\10\6\0\12\10\1\332\22\10\40\0"+
    "\1\10\6\0\12\10\1\333\22\10\40\0\1\10\6\0"+
    "\16\10\1\334\16\10\40\0\1\10\6\0\12\10\1\335"+
    "\22\10\40\0\1\10\6\0\25\10\1\336\7\10\40\0"+
    "\1\10\6\0\12\10\1\337\22\10\40\0\1\10\6\0"+
    "\15\10\1\340\17\10\40\0\1\10\6\0\15\10\1\341"+
    "\17\10\40\0\1\10\6\0\15\10\1\342\17\10\40\0"+
    "\1\10\6\0\16\10\1\343\16\10\40\0\1\10\6\0"+
    "\16\10\1\344\16\10\40\0\1\10\6\0\12\10\1\345"+
    "\22\10\40\0\1\10\6\0\7\10\1\346\25\10\40\0"+
    "\1\10\6\0\7\10\1\347\25\10\40\0\1\10\6\0"+
    "\22\10\1\350\12\10\40\0\1\10\6\0\7\10\1\351"+
    "\25\10\40\0\1\10\6\0\21\10\1\352\13\10\40\0"+
    "\1\10\6\0\14\10\1\353\20\10\40\0\1\10\6\0"+
    "\15\10\1\354\17\10\40\0\1\10";

  private static int [] zzUnpackTrans() {
    int [] result = new int[11220];
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
    "\2\0\1\11\1\1\1\11\1\1\1\11\23\1\11\11"+
    "\1\1\1\11\1\1\1\11\2\1\2\11\1\1\1\11"+
    "\1\1\3\11\1\1\1\11\1\1\6\11\1\1\1\11"+
    "\2\1\3\0\37\1\1\11\1\1\5\11\1\0\1\1"+
    "\1\0\44\1\1\11\135\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[236];
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


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  Lexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  Lexer(java.io.InputStream in) {
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
    while (i < 1352) {
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
        case 55: 
          { return symbol(LexerSymbols.TRUE);
          }
        case 83: break;
        case 77: 
          { return symbol(LexerSymbols.FUNCTION);
          }
        case 84: break;
        case 51: 
          { return symbol(LexerSymbols.INT);
          }
        case 85: break;
        case 29: 
          { return symbol(LexerSymbols.AND);
          }
        case 86: break;
        case 11: 
          { return symbol(LexerSymbols.LBKT);
          }
        case 87: break;
        case 27: 
          { return symbol(LexerSymbols.MINUS);
          }
        case 88: break;
        case 8: 
          { return symbol(LexerSymbols.EXISTS);
          }
        case 89: break;
        case 26: 
          { return symbol(LexerSymbols.PLUS);
          }
        case 90: break;
        case 39: 
          { return symbol(LexerSymbols.IF);
          }
        case 91: break;
        case 37: 
          { yybegin(YYINITIAL); 
                                   return symbol(LexerSymbols.ATTR_STRING, 
                                   string.toString().intern());
          }
        case 92: break;
        case 58: 
          { return symbol(LexerSymbols.CALL);
          }
        case 93: break;
        case 3: 
          { return symbol(LexerSymbols.DIVIDE);
          }
        case 94: break;
        case 24: 
          { return symbol(LexerSymbols.NOT);
          }
        case 95: break;
        case 10: 
          { return symbol(LexerSymbols.RPAR);
          }
        case 96: break;
        case 70: 
          { return symbol(LexerSymbols.UNIQUE);
          }
        case 97: break;
        case 16: 
          { return symbol(LexerSymbols.COLON);
          }
        case 98: break;
        case 5: 
          { return symbol(LexerSymbols.ID, yytext().intern());
          }
        case 99: break;
        case 71: 
          { return symbol(LexerSymbols.ASSERT);
          }
        case 100: break;
        case 44: 
          { string.append('\n');
          }
        case 101: break;
        case 66: 
          { return symbol(LexerSymbols.WHERE);
          }
        case 102: break;
        case 61: 
          { return symbol(LexerSymbols.GOTO);
          }
        case 103: break;
        case 56: 
          { return symbol(LexerSymbols.THEN);
          }
        case 104: break;
        case 9: 
          { return symbol(LexerSymbols.LPAR);
          }
        case 105: break;
        case 46: 
          { string.append('\"');
          }
        case 106: break;
        case 48: 
          { return symbol(LexerSymbols.BVTYPE, yytext().intern());
          }
        case 107: break;
        case 79: 
          { return symbol(LexerSymbols.REQUIRES);
          }
        case 108: break;
        case 33: 
          { return symbol(LexerSymbols.IFF);
          }
        case 109: break;
        case 69: 
          { return symbol(LexerSymbols.FINITE);
          }
        case 110: break;
        case 67: 
          { return symbol(LexerSymbols.WHILE);
          }
        case 111: break;
        case 18: 
          { return symbol(LexerSymbols.EQUALS);
          }
        case 112: break;
        case 32: 
          { return symbol(LexerSymbols.EXPLIES);
          }
        case 113: break;
        case 45: 
          { string.append('\r');
          }
        case 114: break;
        case 49: 
          { return symbol(LexerSymbols.VAR);
          }
        case 115: break;
        case 35: 
          { string.setLength(0); yybegin(STRING);
          }
        case 116: break;
        case 15: 
          { return symbol(LexerSymbols.COMMA);
          }
        case 117: break;
        case 50: 
          { return symbol(LexerSymbols.OLD);
          }
        case 118: break;
        case 43: 
          { string.append('\t');
          }
        case 119: break;
        case 53: 
          { return symbol(LexerSymbols.BOOL);
          }
        case 120: break;
        case 59: 
          { return symbol(LexerSymbols.FREE);
          }
        case 121: break;
        case 60: 
          { return symbol(LexerSymbols.REAL);
          }
        case 122: break;
        case 82: 
          { return symbol(LexerSymbols.IMPLEMENTATION);
          }
        case 123: break;
        case 72: 
          { return symbol(LexerSymbols.ASSUME);
          }
        case 124: break;
        case 78: 
          { return symbol(LexerSymbols.MODIFIES);
          }
        case 125: break;
        case 1: 
          { return symbol(LexerSymbols.error, yytext());
          }
        case 126: break;
        case 65: 
          { return symbol(LexerSymbols.AXIOM);
          }
        case 127: break;
        case 54: 
          { return symbol(LexerSymbols.TYPE);
          }
        case 128: break;
        case 4: 
          { return symbol(LexerSymbols.TIMES);
          }
        case 129: break;
        case 22: 
          { return symbol(LexerSymbols.LTEQ);
          }
        case 130: break;
        case 34: 
          { return symbol(LexerSymbols.QSEP);
          }
        case 131: break;
        case 21: 
          { return symbol(LexerSymbols.GREATER);
          }
        case 132: break;
        case 40: 
          { return symbol(LexerSymbols.EQ);
          }
        case 133: break;
        case 68: 
          { return symbol(LexerSymbols.HAVOC);
          }
        case 134: break;
        case 6: 
          { return symbol(LexerSymbols.NUMBER, yytext().intern());
          }
        case 135: break;
        case 75: 
          { return symbol(LexerSymbols.RETURNS);
          }
        case 136: break;
        case 63: 
          { return symbol(LexerSymbols.CONST);
          }
        case 137: break;
        case 14: 
          { return symbol(LexerSymbols.RBRC);
          }
        case 138: break;
        case 20: 
          { return symbol(LexerSymbols.LESS);
          }
        case 139: break;
        case 38: 
          { string.append('\\');
          }
        case 140: break;
        case 80: 
          { return symbol(LexerSymbols.PROCEDURE);
          }
        case 141: break;
        case 76: 
          { return symbol(LexerSymbols.COMPLETE);
          }
        case 142: break;
        case 57: 
          { return symbol(LexerSymbols.ELSE);
          }
        case 143: break;
        case 13: 
          { return symbol(LexerSymbols.LBRC);
          }
        case 144: break;
        case 47: 
          { return symbol(LexerSymbols.REALNUMBER, yytext().intern());
          }
        case 145: break;
        case 23: 
          { return symbol(LexerSymbols.GTEQ);
          }
        case 146: break;
        case 28: 
          { return symbol(LexerSymbols.MOD);
          }
        case 147: break;
        case 74: 
          { return symbol(LexerSymbols.ENSURES);
          }
        case 148: break;
        case 64: 
          { return symbol(LexerSymbols.FALSE);
          }
        case 149: break;
        case 52: 
          { return symbol(LexerSymbols.BITVECTOR, yytext().intern());
          }
        case 150: break;
        case 7: 
          { return symbol(LexerSymbols.FORALL);
          }
        case 151: break;
        case 2: 
          { /* ignore */
          }
        case 152: break;
        case 81: 
          { return symbol(LexerSymbols.INVARIANT);
          }
        case 153: break;
        case 30: 
          { return symbol(LexerSymbols.OR);
          }
        case 154: break;
        case 36: 
          { string.append( yytext() );
          }
        case 155: break;
        case 62: 
          { return symbol(LexerSymbols.BREAK);
          }
        case 156: break;
        case 19: 
          { return symbol(LexerSymbols.COLONEQUALS);
          }
        case 157: break;
        case 41: 
          { return symbol(LexerSymbols.PARTORDER);
          }
        case 158: break;
        case 73: 
          { return symbol(LexerSymbols.RETURN);
          }
        case 159: break;
        case 25: 
          { return symbol(LexerSymbols.NEQ);
          }
        case 160: break;
        case 42: 
          { return symbol(LexerSymbols.CONCAT);
          }
        case 161: break;
        case 17: 
          { return symbol(LexerSymbols.SEMI);
          }
        case 162: break;
        case 12: 
          { return symbol(LexerSymbols.RBKT);
          }
        case 163: break;
        case 31: 
          { return symbol(LexerSymbols.IMPLIES);
          }
        case 164: break;
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
