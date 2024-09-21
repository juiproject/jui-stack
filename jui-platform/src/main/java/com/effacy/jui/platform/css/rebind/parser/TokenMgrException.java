/* Generated By:JavaCC: Do not edit this line. TokenMgrException.java Version 7.0 */
/* JavaCCOptions: */
package com.effacy.jui.platform.css.rebind.parser;

/** Token Manager Error. */
public class TokenMgrException extends RuntimeException
{

  /**
   * The version identifier for this Serializable class.
   * Increment only if the <i>serialized</i> form of the
   * class changes.
   */
  private static final long serialVersionUID = 1L;

  /*
   * Ordinals for various reasons why an Error of this type can be thrown.
   */

  /**
   * Lexical error occurred.
   */
  public static final int LEXICAL_ERROR = 0;

  /**
   * An attempt was made to create a second instance of a static token manager.
   */
  public static final int STATIC_LEXER_ERROR = 1;

  /**
   * Tried to change to an invalid lexical state.
   */
  public static final int INVALID_LEXICAL_STATE = 2;

  /**
   * Detected (and bailed out of) an infinite loop in the token manager.
   */
  public static final int LOOP_DETECTED = 3;

  /**
   * Indicates the reason why the exception is thrown. It will have
   * one of the above 4 values.
   */
  int errorCode;

  /**
   * Replaces unprintable characters by their escaped (or unicode escaped)
   * equivalents in the given string
   */
  protected static final String addEscapes(String str) {
    StringBuilder retval = new StringBuilder();
    char ch;
    for (int i = 0; i < str.length(); i++) {
      switch (str.charAt(i))
      {
        case '\b':
          retval.append("\\b");
          continue;
        case '\t':
          retval.append("\\t");
          continue;
        case '\n':
          retval.append("\\n");
          continue;
        case '\f':
          retval.append("\\f");
          continue;
        case '\r':
          retval.append("\\r");
          continue;
        case '\"':
          retval.append("\\\"");
          continue;
        case '\'':
          retval.append("\\\'");
          continue;
        case '\\':
          retval.append("\\\\");
          continue;
        default:
          if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
            String s = "0000" + Integer.toString(ch, 16);
            retval.append("\\u" + s.substring(s.length() - 4, s.length()));
          } else {
            retval.append(ch);
          }
          continue;
      }
    }
    return retval.toString();
  }

  /**
   * Returns a detailed message for the Error when it is thrown by the
   * token manager to indicate a lexical error.
   * Parameters :
   *    EOFSeen     : indicates if EOF caused the lexical error
   *    lexState    : lexical state in which this error occurred
   *    errorLine   : line number when the error occurred
   *    errorColumn : column number when the error occurred
   *    errorAfter  : prefix that was seen before this error occurred
   *    curchar     : the offending character
   * Note: You can customize the lexical error message by modifying this method.
   */
  protected static String LexicalErr(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, int curChar) {
    return("Lexical error at line " + //
          errorLine + ", column " + //
          errorColumn + ".  Encountered: " + //
          (EOFSeen ? "<EOF>" : ("'" + addEscapes(String.valueOf(curChar)) + "' (" + curChar + "),")) + //
          (errorAfter == null || errorAfter.length() == 0 ? "" : " after prefix \"" + addEscapes(errorAfter) + "\"")) + //
          (lexState == 0 ? "" : " (in lexical state " + lexState + ")");
  }

  /**
   * You can also modify the body of this method to customize your error messages.
   * For example, cases like LOOP_DETECTED and INVALID_LEXICAL_STATE are not
   * of end-users concern, so you can return something like :
   *
   *     "Internal Error : Please file a bug report .... "
   *
   * from this method for such cases in the release version of your parser.
   */
  @Override
  public String getMessage() {
    return super.getMessage();
  }

  /*
   * Constructors of various flavors follow.
   */

  /** No arg constructor. */
  public TokenMgrException() {
  }

  /** Constructor with message and reason. */
  public TokenMgrException(String message, int reason) {
    super(message);
    errorCode = reason;
  }

  /** Full Constructor. */
  public TokenMgrException(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, int curChar, int reason) {
    this(LexicalErr(EOFSeen, lexState, errorLine, errorColumn, errorAfter, curChar), reason);
  }
}
/* JavaCC - OriginalChecksum=92dfb24456dee8fb0336041d02db2848 (do not edit this line) */
