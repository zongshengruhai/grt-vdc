/*********************************************************************
*
*      Copyright (C) 2001 Andrew Khan
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

package jxl.write.biff;

import jxl.CellType;
import jxl.Cell;
import jxl.format.CellFormat;
import jxl.biff.Type;
import jxl.biff.StringHelper;
import jxl.biff.IntegerHelper;
import jxl.biff.FormattingRecords;

/**
 * A blank shortItem, which is used to contain formatting information
 */
public abstract class BlankRecord extends CellValue
{
  /**
   * Consructor used when creating a label from the user API
   * 
   * @param c the column
   * @param cont the contents
   * @param r the row
   */
  protected BlankRecord(int c, int r)
  {
    super(Type.BLANK, c, r);
  }

  /**
   * Constructor used when creating a label from the API.  This is 
   * overloaded to allow formatting information to be passed to the shortItem
   * 
   * @param c the column
   * @param r the row
   * @param st the format applied to the cell
   */
  protected BlankRecord(int c, int r, CellFormat st)
  {
     super(Type.BLANK, c, r, st);
  }

  /**
   * Constructor used when copying a formatted blank cell from a read only
   * spreadsheet
   * 
   * @param c the blank cell to copy
   */
  protected BlankRecord(Cell c)
  {
    super(Type.BLANK, c);
  }

  /**
   * Copy constructor
   * 
   * @param c the column
   * @param r the row
   * @param b the shortItem to  copy
   */
  protected BlankRecord(int c, int r, BlankRecord br)
  {
    super(Type.BLANK, c, r, br);
  }

  /**
   * Returns the content type of this cell
   * 
   * @return the content type for this cell
   */
  public CellType getType()
  {
    return CellType.EMPTY;
  }

  /**
   * Quick and dirty function to return the contents of this cell as a string.
   * 
   * @return the contents of this cell as a string
   */
  public String getContents()
  {
    return "";
  }
}



