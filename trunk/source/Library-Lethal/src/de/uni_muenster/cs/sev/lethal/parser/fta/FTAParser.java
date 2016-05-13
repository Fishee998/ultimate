/* Generated By:JavaCC: Do not edit this line. FTAParser.java */
package de.uni_muenster.cs.sev.lethal.parser.fta;

import java.io.ByteArrayInputStream;
import java.util.*;
import de.uni_muenster.cs.sev.lethal.symbol.common.RankedSymbol;
import de.uni_muenster.cs.sev.lethal.symbol.standard.*;
import de.uni_muenster.cs.sev.lethal.symbol.special.*;
import de.uni_muenster.cs.sev.lethal.states.*;
import de.uni_muenster.cs.sev.lethal.utils.*;
import de.uni_muenster.cs.sev.lethal.tree.standard.*;
import de.uni_muenster.cs.sev.lethal.factories.*;
import de.uni_muenster.cs.sev.lethal.treeautomata.easy.*;

import java.util.List;

/**
 * Parser for finite tree automata
 * @author Philipp
 */
public class FTAParser implements FTAParserConstants {
        private static FTAParser parser = new FTAParser(new ByteArrayInputStream(new byte[]{}));

        public static EasyFTA parseString(String s) throws ParseException{
                parser.ReInit(new ByteArrayInputStream(s.getBytes()));
                return parser.fta();
        }

        public static EasyFTA parseStrings(List<String> ruleStrings) throws ParseException{
                StringBuffer sb = new StringBuffer();
                for (String s : ruleStrings){
                        sb.append(s);
                        sb.append('\u005cn');
                }
                parser.ReInit(new ByteArrayInputStream(sb.toString().getBytes()));
                return parser.fta();
        }

  final public EasyFTA fta() throws ParseException {
        EasyFTARule rule;
        List<EasyFTARule> rules = new ArrayList<EasyFTARule>();
        EasyFTAEpsRule epsRule;
        List<EasyFTAEpsRule> epsRules = new ArrayList<EasyFTAEpsRule>();
        Set<State> finalStates = new HashSet<State>();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
    case 9:
    case 12:
    case 14:
      if (jj_2_1(2)) {
        rule = ftaRule(finalStates);
                                               rules.add(rule);
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NAME:
        case 9:
        case 12:
        case 14:
          epsRule = ftaEpsRule(finalStates);
                                                                                                      epsRules.add(epsRule);
          break;
        default:
          jj_la1[0] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
      break;
    default:
      jj_la1[1] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LINE_END_COMMENT:
      jj_consume_token(LINE_END_COMMENT);
      break;
    default:
      jj_la1[2] = jj_gen;
      ;
    }
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 6:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_1;
      }
      jj_consume_token(6);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NAME:
      case 9:
      case 12:
      case 14:
        if (jj_2_2(2)) {
          rule = ftaRule(finalStates);
                                                           rules.add(rule);
        } else {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case NAME:
          case 9:
          case 12:
          case 14:
            epsRule = ftaEpsRule(finalStates);
                                                                                                                   epsRules.add(epsRule);
            break;
          default:
            jj_la1[4] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
        }
        break;
      default:
        jj_la1[5] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LINE_END_COMMENT:
        jj_consume_token(LINE_END_COMMENT);
        break;
      default:
        jj_la1[6] = jj_gen;
        ;
      }
    }
    jj_consume_token(0);
          {if (true) return new EasyFTA(rules, epsRules, finalStates);}
    throw new Error("Missing return statement in function");
  }

  final public EasyFTAEpsRule ftaEpsRule(Set<State> finalStates) throws ParseException {
        State srcState;
        State dstState;
    srcState = state(finalStates);
    jj_consume_token(7);
    dstState = state(finalStates);
         {if (true) return new EasyFTAEpsRule(srcState, dstState );}
    throw new Error("Missing return statement in function");
  }

  final public EasyFTARule ftaRule(Set<State> finalStates) throws ParseException {
        Pair<RankedSymbol, List<State>> func;
        State dstState;
    func = function(finalStates);
    jj_consume_token(8);
    dstState = state(finalStates);
         {if (true) return new EasyFTARule(func.getFirst(), func.getSecond(), dstState );}
    throw new Error("Missing return statement in function");
  }

  final public Pair<RankedSymbol, List<State>> function(Set<State> finalStates) throws ParseException {
        String symbolName = null;
        RankedSymbol symbol; // = null;
        State state;
        String stateName;
        List<State> ruleStates = new ArrayList<State>();
    //(symbolName=<NAME>.image | <NIL> {symbol = Nil.getNil();} | <CONS> {symbol = Cons.getCons();} )
                    symbolName = jj_consume_token(NAME).image;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 9:
      jj_consume_token(9);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NAME:
      case 9:
      case 12:
      case 14:
        state = state(finalStates);
                                                              ruleStates.add(state);
        label_2:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case 10:
            ;
            break;
          default:
            jj_la1[7] = jj_gen;
            break label_2;
          }
          jj_consume_token(10);
          state = state(finalStates);
                                                                                 ruleStates.add(state);
        }
        break;
      default:
        jj_la1[8] = jj_gen;
        ;
      }
      jj_consume_token(11);
      break;
    default:
      jj_la1[9] = jj_gen;
      ;
    }
                //if (symbol == null)
                symbol = new StdNamedRankedSymbol(symbolName, ruleStates.size());
                {if (true) return new Pair<RankedSymbol, List<State>>(symbol,ruleStates);}
    throw new Error("Missing return statement in function");
  }

  final public State state(Set<State> finalStates) throws ParseException {
        Object name;

        String finMark = null;
    name = subState();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case FIN_MARK:
        ;
        break;
      default:
        jj_la1[10] = jj_gen;
        break label_3;
      }
      finMark = jj_consume_token(FIN_MARK).image;
    }
                State state = StateFactory.getStateFactory().makeState(name);
                if (finMark != null) finalStates.add(state);
                {if (true) return state;}
    throw new Error("Missing return statement in function");
  }

  final public String subState() throws ParseException {
        String s;
        String stateName;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
      s = jj_consume_token(NAME).image;
                                  stateName=s;
      break;
    case 12:
      jj_consume_token(12);
      s = stateSeq();
      jj_consume_token(13);
                                                  stateName = "[" + s + "]";
      break;
    case 9:
      jj_consume_token(9);
      s = stateSeq();
      jj_consume_token(11);
                                                  stateName = "(" + s + ")";
      break;
    case 14:
      jj_consume_token(14);
      s = stateSeq();
      jj_consume_token(15);
                                                  stateName = "{" + s + "}";
      break;
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
    case 9:
    case 12:
    case 14:
      s = subState();
                                   stateName+=s;
      break;
    default:
      jj_la1[12] = jj_gen;
      ;
    }
                {if (true) return stateName;}
    throw new Error("Missing return statement in function");
  }

  final public String stateSeq() throws ParseException {
        String s;
        String seqName;
    s = subState();
                                seqName=s;
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 10:
        ;
        break;
      default:
        jj_la1[13] = jj_gen;
        break label_4;
      }
      jj_consume_token(10);
      s = subState();
                                      seqName+=","+s;
    }
         {if (true) return seqName;}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_3_1() {
    if (jj_3R_5()) return true;
    return false;
  }

  private boolean jj_3R_6() {
    if (jj_scan_token(NAME)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_7()) jj_scanpos = xsp;
    return false;
  }

  private boolean jj_3R_7() {
    if (jj_scan_token(9)) return true;
    return false;
  }

  private boolean jj_3R_5() {
    if (jj_3R_6()) return true;
    if (jj_scan_token(8)) return true;
    return false;
  }

  private boolean jj_3_2() {
    if (jj_3R_5()) return true;
    return false;
  }

  /** Generated Token Manager. */
  public FTAParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[14];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x5208,0x5208,0x20,0x40,0x5208,0x5208,0x20,0x400,0x5208,0x200,0x10,0x5208,0x5208,0x400,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[2];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public FTAParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public FTAParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new FTAParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public FTAParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new FTAParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public FTAParser(FTAParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(FTAParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[16];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 14; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 16; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 2; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}