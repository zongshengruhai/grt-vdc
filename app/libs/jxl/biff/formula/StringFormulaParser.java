/*********************************************************************
*
*      Copyright (C) 2002 Andrew Khan
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
***************************************************************************/

package jxl.biff.formula;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.io.StringReader;
import java.io.IOException;

import common.Assert;
import common.Logger;

import jxl.Cell;
import jxl.WorkbookSettings;
import jxl.biff.WorkbookMethods;

/**
 * Parses a string formula into a parse tree
 */
class StringFormulaParser implements Parser
{
  /**
   * The logger
   */
  private static Logger logger = Logger.getLogger(StringFormulaParser.class);

  /**
   * The formula string passed to this object
   */
  private String formula;

  /**
   * The parsed formula string, as retrieved from the parse tree
   */
  private String parsedFormula;

  /**
   * The parse tree
   */
  private ParseItem root;

  /**
   * The stack argument used when parsing a function in order to
   * pass multiple arguments back to the calling method
   */
  private Stack arguments;

  /**
   * The workbook settings
   */
  private WorkbookSettings settings;

  /**
   * A handle to the external sheet
   */
  private ExternalSheet externalSheet;

  /**
   * A handle to the name table
   */
  private WorkbookMethods nameTable;

  /**
   * Constructor
   * @param f
   * @param ws
   */
  public StringFormulaParser(String f, ExternalSheet es, WorkbookMethods nt, 
                             WorkbookSettings ws)
  {
    formula = f;
    settings = ws;
    externalSheet = es;
    nameTable = nt;
  }

  /**
   * Parses the list of tokens
   *
   * @exception FormulaException
   */
  public void parse() throws FormulaException
  {
    ArrayList tokens = getTokens();
    
    Iterator i = tokens.iterator();
    
    root = parseCurrent(i);
  }

  /**
   * Recursively parses the token array.  Recursion is used in order
   * to evaluate parentheses and function arguments
   *
   * @param i an iterator of tokens
   * @return the root node of the current parse stack
   * @exception FormulaException if an error occurs
   */
  private ParseItem parseCurrent(Iterator i) throws FormulaException
  {
    Stack stack = new Stack();
    Stack operators = new Stack();
    Stack args = null; // we usually don't need this

    boolean parenthesesClosed = false;
    ParseItem lastParseItem = null;
    
    while (i.hasNext() && !parenthesesClosed)
    {
      ParseItem pi = (ParseItem) i.next();

      if (pi instanceof Operand)
      {
        stack.push(pi);
      }
      else if (pi instanceof StringFunction)
      {
        handleFunction((StringFunction) pi, i, stack);
      }
      else if (pi instanceof Operator)
      {
        Operator op = (Operator) pi;

        // See if the operator is a binary or unary operator
        // It is a unary operator either if the stack is empty, or if
        // the last thing off the stack was another operator
        if (op instanceof StringOperator)
        {
          StringOperator sop = (StringOperator) op;
          if (stack.isEmpty() || lastParseItem instanceof Operator)
          {
            op = sop.getUnaryOperator();
          }
          else
          {
            op = sop.getBinaryOperator();
          }
        }

        if (operators.empty())
        {
          // nothing much going on, so do nothing for the time being
          operators.push(op);
        }
        else
        {
          Operator operator = (Operator) operators.peek();

          // If the latest operator has a higher precedence then add to the 
          // operator stack and wait
          if (op.getPrecedence() < operator.getPrecedence())
          {
            operators.push(op);
          }
          else
          {
            // The operator is a lower precedence so we can sort out
            // some of the items on the stack
            operators.pop(); // remove the operator from the stack
            operator.getOperands(stack);
            stack.push(operator);
            operators.push(op);
          }
        }
      }
      else if (pi instanceof ArgumentSeparator)
      {
        // Clean up any remaining items on this stack
        while (!operators.isEmpty())
        {
          Operator o = (Operator) operators.pop();
          o.getOperands(stack);
          stack.push(o);
        }
        
        // Add it to the argument stack.  Create the argument stack
        // if necessary.  Items will be stored on the argument stack in
        // reverse order
        if (args == null)
        {
          args = new Stack();
        }

        args.push(stack.pop());
        stack.clear();
      }
      else if (pi instanceof OpenParentheses)
      {
        ParseItem pi2 = parseCurrent(i);
        Parenthesis p = new Parenthesis();
        pi2.setParent(p);
        p.add(pi2);
        stack.push(p);
      }
      else if (pi instanceof CloseParentheses)
      {
        parenthesesClosed = true;
      }
      
      lastParseItem = pi;
    }
    
    while (!operators.isEmpty())
    {
      Operator o = (Operator) operators.pop();
      o.getOperands(stack);
      stack.push(o);
    }

    ParseItem rt = !stack.empty()? (ParseItem) stack.pop():null;

    // if the agument stack is not null, then add it to that stack
    // as well for good measure
    if (args != null && rt != null)
    {
      args.push(rt);
    }

    arguments = args;

    if (!stack.empty() || !operators.empty() )
    {
      logger.warn("Formula " + formula + 
                  " has a non-empty parse stack");
    }

    return rt;
  }

  /**
   * Gets the list of lexical tokens using the generated lexical analyzer
   *
   * @return the list of tokens
   * @exception FormulaException if an error occurs
   */
  private ArrayList getTokens() throws FormulaException
  {
    ArrayList tokens = new ArrayList();

    StringReader sr = new StringReader(formula);
    Yylex lex = new Yylex(sr);
    lex.setExternalSheet(externalSheet);
    lex.setNameTable(nameTable);
    try
    {
      ParseItem pi = lex.yylex();
      while (pi != null)
      {
        tokens.add(pi);
        pi = lex.yylex();
      }
    }
    catch (IOException e)
    {
      logger.warn(e.toString());
    }
    catch (Error e)
    {
      throw new FormulaException(FormulaException.lexicalError,
                                 formula + " at char  " + lex.getPos());
    }
      
    return tokens;
  }

  /**
   * Gets the formula as a string.  Uses the parse tree to do this, and
   * does not simply return whatever string was passed in
   */
  public String getFormula()
  {
    if (parsedFormula == null)
    {
      StringBuffer sb = new StringBuffer();
      root.getString(sb);
      parsedFormula = sb.toString();
    }

    return parsedFormula;
  }

  /**
   * Gets the bytes for the formula
   *
   * @return the bytes in RPN
   */
  public byte[] getBytes()
  {
    byte[] bytes = root.getBytes();
    
    if (root.isVolatile())
    {
      byte[] newBytes = new byte[bytes.length + 4];
      System.arraycopy(bytes, 0, newBytes, 4, bytes.length);
      newBytes[0] = Token.ATTRIBUTE.getCode();
      newBytes[1] = (byte) 0x1;
      bytes = newBytes;
    }

    return bytes;
  }

  /**
   * Handles the case when parsing a string when a token is a function
   *
   * @param sf the string function
   * @param i  the token iterator
   * @param stack the parse tree stack
   * @exception FormulaException if an error occurs
   */
  private void handleFunction(StringFunction sf, Iterator i, 
                              Stack stack)
    throws FormulaException
  {
    ParseItem pi2 = parseCurrent(i); 

    // If the function is unknown, then throw an error
    if (sf.getFunction(settings) == Function.UNKNOWN)
    {
      throw new FormulaException(FormulaException.unrecognizedFunction);
    }

    // First check for possible optimized functions and possible
    // use of the Attribute token
    if (sf.getFunction(settings) == Function.SUM && arguments == null)
    {
      // this is handled by an attribute
      Attribute a = new Attribute(sf, settings);
      a.add(pi2);
      stack.push(a);
      return;
    }

    if (sf.getFunction(settings) == Function.IF)
    {
      // this is handled by an attribute
      Attribute a = new Attribute(sf, settings);
          
      // Add in the if conditions as a var arg function in
      // the correct order
      VariableArgFunction vaf = new VariableArgFunction(settings);
      int numargs = arguments.size();
      for (int j = 0 ; j < numargs; j++)
      {
        ParseItem pi3 = (ParseItem) arguments.get(j);
        vaf.add(pi3);
      }
          
      a.setIfConditions(vaf);
      stack.push(a);
      return;
    }

    // Function cannot be optimized.  See if it is a variable argument 
    // function or not
    if (sf.getFunction(settings).getNumArgs() == 0xff)
    {
      // If the arg stack has not been initialized, it means
      // that there was only one argument, which is the
      // returned parse item
      if (arguments == null)
      {
        VariableArgFunction vaf = new VariableArgFunction
          (sf.getFunction(settings), 1, settings);
        vaf.add(pi2);
        stack.push(vaf);
      }
      else
      {
        // Add the args to the function in the correct order
        int numargs = arguments.size();
        VariableArgFunction vaf = new VariableArgFunction
          (sf.getFunction(settings), numargs, settings);
        
        ParseItem[] args = new ParseItem[numargs];
        for (int j = 0 ; j < numargs; j++)
        {
          ParseItem pi3 = (ParseItem) arguments.pop();
          args[numargs-j-1] = pi3;
        }

        for (int j = 0 ; j < args.length ; j++)
        {
          vaf.add(args[j]);
        }
        stack.push(vaf);
        arguments.clear();
        arguments = null;
      }
      return;
    }

    // Function is a standard built in function
    BuiltInFunction bif = new BuiltInFunction(sf.getFunction(settings), 
                                              settings);
      
    int numargs = sf.getFunction(settings).getNumArgs();
    if (numargs == 1)
    {
      // only one item which is the returned ParseItem
      bif.add(pi2);
    }
    else
    {
      if ((arguments == null && numargs != 0) ||
          (arguments != null && numargs != arguments.size()))
      {
        throw new FormulaException(FormulaException.incorrectArguments);
      }
      // multiple arguments so go to the arguments stack.  
      // Unlike the variable argument function, the args are
      // stored in reverse order
      for (int j = 0; j < numargs ; j++)
      {
        ParseItem pi3 = (ParseItem) arguments.get(j);
        bif.add(pi3);
      }
    }
    stack.push(bif);
  }

  /**
   * Default behaviour is to do nothing
   *
   * @param colAdjust the amount to add on to each relative cell reference
   * @param rowAdjust the amount to add on to each relative row reference
   */
  public void adjustRelativeCellReferences(int colAdjust, int rowAdjust)
  {
    root.adjustRelativeCellReferences(colAdjust, rowAdjust);
  }

  /**
   * Called when a column is inserted on the specified sheet.  Tells
   * the formula  parser to update all of its cell references beyond this
   * column
   *
   * @param sheetIndex the sheet on which the column was inserted
   * @param col the column number which was inserted
   * @param currentSheet TRUE if this formula is on the sheet in which the
   * column was inserted, FALSE otherwise
   */
  public void columnInserted(int sheetIndex, int col, boolean currentSheet)
  {
    root.columnInserted(sheetIndex, col, currentSheet);
  }


  /**
   * Called when a column is inserted on the specified sheet.  Tells
   * the formula  parser to update all of its cell references beyond this
   * column
   *
   * @param sheetIndex the sheet on which the column was removed
   * @param col the column number which was removed
   * @param currentSheet TRUE if this formula is on the sheet in which the
   * column was inserted, FALSE otherwise
   */
  public void columnRemoved(int sheetIndex, int col, boolean currentSheet)
  {
    root.columnRemoved(sheetIndex, col, currentSheet);
  }

  /**
   * Called when a column is inserted on the specified sheet.  Tells
   * the formula  parser to update all of its cell references beyond this
   * column
   *
   * @param sheetIndex the sheet on which the column was inserted
   * @param row the column number which was inserted
   * @param currentSheet TRUE if this formula is on the sheet in which the
   * column was inserted, FALSE otherwise
   */
  public void rowInserted(int sheetIndex, int row, boolean currentSheet)
  {
    root.rowInserted(sheetIndex, row, currentSheet);
  }

  /**
   * Called when a column is inserted on the specified sheet.  Tells
   * the formula  parser to update all of its cell references beyond this
   * column
   *
   * @param sheetIndex the sheet on which the column was removed
   * @param row the column number which was removed
   * @param currentSheet TRUE if this formula is on the sheet in which the
   * column was inserted, FALSE otherwise
   */
  public void rowRemoved(int sheetIndex, int row, boolean currentSheet)
  {
    root.rowRemoved(sheetIndex, row, currentSheet);
  }

}
