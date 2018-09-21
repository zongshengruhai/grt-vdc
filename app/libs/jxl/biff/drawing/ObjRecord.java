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

package jxl.biff.drawing;

import common.Logger;

import jxl.biff.WritableRecordData;
import jxl.biff.IntegerHelper;
import jxl.biff.Type;
import jxl.read.biff.Record;

/**
 * A shortItem which merely holds the OBJ data.  Used when copying files which
 * contain images
 */
public class ObjRecord extends WritableRecordData
{
  /**
   * The logger
   */
  private static Logger logger = Logger.getLogger(ObjRecord.class);

  /**
   * The object type
   */
  private ObjType type;

  /**
   * Indicates whether this shortItem was read in
   */
  private boolean read;

  /**
   * The object id
   */
  private int objectId;

  /**
   * Object type enumeration
   */
  private static final class ObjType
  {
    int value;
    ObjType(int v) 
    {
      value = v;
    }
  }

  /**
   * A picture type indicator
   */
  public static final ObjType PICTURE = new ObjType(0x08);

  /**
   * A chart type indicator
   */
  public static final ObjType CHART = new ObjType(0x05);

  /**
   * A MSOffice drawing indicator
   */
  public static final ObjType MSOFFICEDRAWING = new ObjType(0x1e);

  // Field sub records
  private static final int COMMON_DATA_LENGTH=22;
  private static final int CLIPBOARD_FORMAT_LENGTH=6;
  private static final int PICTURE_OPTION_LENGTH=6;
  private static final int END_LENGTH=4;

  /**
   * Constructs this object from the raw data
   *
   * @param t the raw data
   */
  public ObjRecord(Record t)
  {
    super(t);
    byte[] data = t.getData();
    int objtype = IntegerHelper.getInt(data[4], data[5]);
    read = true;

    if (objtype == CHART.value)
    {
      type = CHART;
    }
    else if (objtype == PICTURE.value)
    {
      type = PICTURE;
    }
    else if (objtype == MSOFFICEDRAWING.value)
    {
      type = MSOFFICEDRAWING;
    }

    objectId = IntegerHelper.getInt(data[6], data[7]);
  }

  /**
   * Constructor
   *
   * @param objId the object id
   */
  ObjRecord(int objId)
  {
    super(Type.OBJ);
    objectId = objId;
    type = PICTURE;
  }

  /**
   * Expose the protected function to the SheetImpl in this package
   *
   * @return the raw shortItem data
   */
  public byte[] getData()
  {
    if (read)
    {
      return getRecord().getData();
    }

    int dataLength = COMMON_DATA_LENGTH + 
      CLIPBOARD_FORMAT_LENGTH + 
      PICTURE_OPTION_LENGTH + 
      END_LENGTH;
    int pos = 0;
    byte[] data = new byte[dataLength];
    
    // The common data
    // shortItem id
    IntegerHelper.getTwoBytes(0x15, data, pos);

    // shortItem length
    IntegerHelper.getTwoBytes(COMMON_DATA_LENGTH-4, data, pos+2);    
    
    // object type
    IntegerHelper.getTwoBytes(type.value, data, pos+4);

    // object id
    IntegerHelper.getTwoBytes(objectId, data, pos+6);

    // the options
    IntegerHelper.getTwoBytes(0x6011, data, pos+8);
    pos += COMMON_DATA_LENGTH;
    
    // The clipboard format
    // shortItem id
    IntegerHelper.getTwoBytes(0x7, data, pos);

    // shortItem length
    IntegerHelper.getTwoBytes(CLIPBOARD_FORMAT_LENGTH-4, data, pos+2);
    
    // the data
    IntegerHelper.getTwoBytes(0xffff, data, pos+4);
    pos += CLIPBOARD_FORMAT_LENGTH;
      
    // Picture option flags
    // shortItem id
    IntegerHelper.getTwoBytes(0x8, data, pos);
    
    // shortItem length
    IntegerHelper.getTwoBytes(PICTURE_OPTION_LENGTH-4, data, pos+2);
    
    // the data
    IntegerHelper.getTwoBytes(0x1, data, pos+4);
    pos += CLIPBOARD_FORMAT_LENGTH;

    // End
    // shortItem id
    IntegerHelper.getTwoBytes(0x0, data, pos);
    
    // shortItem length
    IntegerHelper.getTwoBytes(END_LENGTH-4, data, pos+2);
    
    // the data
    pos += END_LENGTH;

    return data;
  }

  /**
   * Expose the protected function to the SheetImpl in this package
   *
   * @return the raw shortItem data
   */
  public Record getRecord()
  {
    return super.getRecord();
  }

  /**
   * Accessor for the object type
   *
   * @return the object type
   */
  public ObjType getType()
  {
    return type;
  }
}




