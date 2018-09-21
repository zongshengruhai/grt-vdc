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

package jxl.read.biff;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import jxl.NumberCell;
import jxl.CellType;
import jxl.NumberFormulaCell;
import jxl.biff.IntegerHelper;
import jxl.biff.FormattingRecords;
import jxl.biff.FormulaData;
import jxl.biff.WorkbookMethods;
import jxl.biff.formula.FormulaParser;
import jxl.biff.formula.ExternalSheet;
import jxl.biff.formula.FormulaException;

/**
 * A formula's last calculated value
 */
class NumberFormulaRecord extends CellValue
  implements NumberCell, FormulaData, NumberFormulaCell
{
  /**
   * The last calculated value of the formula
   */
  private double value;

  /**
   * The number format
   */
  private NumberFormat format;

  /**
   * The string format for the double value
   */
  private static final DecimalFormat defaultFormat =
    new DecimalFormat("#.###");

  /**
   * The formula as an excel string
   */
  private String formulaString;

  /**
   * A handle to the class needed to access external sheets
   */
  private ExternalSheet externalSheet;

  /**
   * A handle to the name table
   */
  private WorkbookMethods nameTable;

  /**
   * The raw data
   */
  private byte[] data;

  /**
   * Constructs this object from the raw data
   *
   * @param t the raw data
   * @param fr the formatting shortItem
   * @param es the external sheet
   * @param nt the name table
   * @param si the sheet
   */
  public NumberFormulaRecord(Record t, FormattingRecords fr,
                             ExternalSheet es, WorkbookMethods nt,
                             SheetImpl si)
  {
    super(t, fr, si);

    externalSheet = es;
    nameTable = nt;
    data = getRecord().getData();

    format = fr.getNumberFormat(getXFIndex());

    if (format == null)
    {
      format = defaultFormat;
    }

    int num1 = IntegerHelper.getInt(data[6], data[7], data[8], data[9]);
    int num2 = IntegerHelper.getInt(data[10], data[11], data[12], data[13]);

    // bitwise ors don't work with longs, so we have to simulate this
    // functionality the long way round by concatenating two binary
    // strings, and then parsing the binary string into a long.
    // This is very clunky and inefficient, and I hope to
    // find a better way
    String s1 = Integer.toBinaryString(num1);
    while (s1.length() < 32)
    {
      s1 = "0" + s1; // fill out with leading zeros as necessary
    }

    // Long.parseLong doesn't like the sign bit, so have to extract this
    // information and put it in at the end.  (thanks
    // to Ruben for pointing this out)
    boolean negative = ((num2 & 0x80000000) != 0);

    String s = Integer.toBinaryString(num2 & 0x7fffffff) + s1;
    long val = Long.parseLong(s, 2);
    value = Double.longBitsToDouble(val);

    if (negative)
    {
      value = -value;
    }
  }

  /**
   * Interface method which returns the value
   *
   * @return the last calculated value of the formula
   */
  public double getValue()
  {
    return value;
  }

  /**
   * Returns the numerical value as a string
   *
   * @return The numerical value of the formula as a string
   */
  public String getContents()
  {
    return format.format(value);
  }

  /**
   * Returns the cell type
   *
   * @return The cell type
   */
  public CellType getType()
  {
    return CellType.NUMBER_FORMULA;
  }

  /**
   * Gets the raw bytes for the formula.  This will include the
   * parsed tokens array.  Used when copying spreadsheets
   *
   * @return the raw shortItem data
   */
  public byte[] getFormulaData()
  {
    // Lop off the standard information
    byte[] d = new byte[data.length - 6];
    System.arraycopy(data, 6, d, 0, data.length - 6);

    return d;
  }

  /**
   * Gets the formula as an excel string
   *
   * @return the formula as an excel string
   * @exception FormulaException
   */
  public String getFormula() throws FormulaException
  {
    if (formulaString == null)
    {
      byte[] tokens = new byte[data.length - 22];
      System.arraycopy(data, 22, tokens, 0, tokens.length);
      FormulaParser fp = new FormulaParser
        (tokens, this, externalSheet, nameTable,
         getSheet().getWorkbook().getSettings());
      fp.parse();
      formulaString = fp.getFormula();
    }

    return formulaString;
  }

  /**
   * Gets the NumberFormat used to format this cell.  This is the java
   * equivalent of the Excel format
   *
   * @return the NumberFormat used to format the cell
   */
  public NumberFormat getNumberFormat()
  {
    return format;
  }
}
