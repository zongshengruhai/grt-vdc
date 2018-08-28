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

package jxl.read.biff;

import jxl.biff.IntegerHelper;
import jxl.biff.RecordData;

/**
 * A row  record
 */
public class RowRecord extends RecordData
{
  /**
   * The number of this row
   */
  private int rowNumber;
  /**
   * The height of this row
   */
  private int rowHeight;
  /**
   * Flag to indicate whether this row is collapsed or not
   */
  private boolean collapsed;
  /**
   * Indicates whether this row has zero height (ie. whether it is hidden)
   */
  private boolean zeroHeight;

  /**
   * Indicates that the row is default height
   */
  private static final int defaultHeightIndicator = 0xff;

  /**
   * Constructs this object from the raw data
   *
   * @param t the raw data
   */
  RowRecord(Record t)
  {
    super(t);

    byte[] data = getRecord().getData();
    rowNumber = IntegerHelper.getInt(data[0], data[1]);
    rowHeight = IntegerHelper.getInt(data[6], data[7]);

    byte opts = data[12];

    collapsed = (opts & 0x20) != 0;
  }

  /**
   * Interrogates whether this row is of default height
   *
   * @return TRUE if this is set to the default height, FALSE otherwise
   */
  boolean isDefaultHeight()
  {
    return rowHeight == defaultHeightIndicator;
  }

  /**
   * Gets the row number
   *
   * @return the number of this row
   */
  public int getRowNumber()
  {
    return rowNumber;
  }

  /**
   * Gets the height of the row
   *
   * @return the row height
   */
  public int getRowHeight()
  {
    return rowHeight;
  }

  /**
   * Queries whether the row is collapsed
   *
   * @return the collapsed indicator
   */
  public boolean isCollapsed()
  {
    return collapsed;
  }
  /**
   * Queries whether the row has been set to zero height
   *
   * @return the zero height indicator
   */
  public boolean isZeroHeight()
  {
    return zeroHeight;
  }
}


