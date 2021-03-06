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

package jxl.biff.drawing;

import java.io.IOException;

import common.Assert;
import common.Logger;

import jxl.WorkbookSettings;
import jxl.biff.ByteData;
import jxl.biff.IntegerHelper;
import jxl.biff.IndexMapping;
import jxl.biff.Type;
import jxl.read.biff.File;

/**
 * Contains the various biff records used to insert a chart into a 
 * worksheet
 */
public class Chart implements ByteData, EscherStream
{
  /**
   * The logger
   */
  public static Logger logger = Logger.getLogger(Chart.class);

  /**
   * The MsoDrawingRecord associated with the chart
   */
  private MsoDrawingRecord msoDrawingRecord;

  /**
   * The ObjRecord associated with the chart
   */
  private ObjRecord objRecord;

  /**
   * The start pos of the chart bof stream in the data file
   */  
  private int startpos;

  /**
   * The start pos of the chart bof stream in the data file
   */
  private int endpos;  

  /**
   * A handle to the Excel file
   */
  private File file;

  /**
   * The chart byte data
   */
  private byte[] data;

  /**
   * Flag which indicates that the byte data has been initialized
   */
  private boolean initialized;

  /**
   * The workbook settings
   */
  private WorkbookSettings workbookSettings;

  /**
   * Constructor
   *
   * @param mso a <code>MsoDrawingRecord</code> value
   * @param obj an <code>ObjRecord</code> value
   * @param sp an <code>int</code> value
   * @param ep an <code>int</code> value
   * @param f a <code>File</code> value
   * @param ws the workbook settings
   */
  public Chart(MsoDrawingRecord mso, ObjRecord obj, 
               int sp, int ep, File f, WorkbookSettings ws)
  {
    msoDrawingRecord = mso;
    objRecord = obj;
    startpos = sp;
    endpos = ep;
    file = f;
    workbookSettings = ws;
    initialized = false;

    // Note:  mso and obj values can be null if we are creating a chart
    // which takes up an entire worksheet.  Check that both are null or both
    // not null though
    Assert.verify((mso != null && obj != null) || 
                  (mso == null && obj == null));
  }

  /**
   * Gets the entire binary shortItem for the chart as a chunk of binary data
   *
   * @return the bytes
   */
  public byte[] getBytes()
  {
    if (!initialized)
    {
      initialize();
    }

    return data;
  }

  /**
   * Implementation of the EscherStream method
   *
   * @return the data
   */
  public byte[] getData()
  {
    return msoDrawingRecord.getRecord().getData();
  }

  /**
   * Initializes the charts byte data
   */
  private void initialize()
  {
    data = file.read(startpos, endpos - startpos);
    initialized = true;
  }

  /**
   * Rationalizes the sheet's xf index mapping
   *
   * @param xfMapping the index mapping for XFRecords
   * @param fontMapping the index mapping for fonts
   * @param formatMapping the index mapping for formats
   */
  public void rationalize(IndexMapping xfMapping, 
                          IndexMapping fontMapping, 
                          IndexMapping formatMapping)
  {
    if (!initialized)
    {
      initialize();
    }

    // Read through the array, looking for the data types
    // This is a total hack bodge for now - it will eventually need to be
    // integrated properly
    int pos = 0;
    int code = 0;
    int length = 0;
    Type type = null;
    while (pos < data.length)
    {
      code = IntegerHelper.getInt(data[pos], data[pos+1]);
      length = IntegerHelper.getInt(data[pos+2], data[pos+3]);

      type = Type.getType(code);

      if (type == Type.FONTX)
      {
        int fontind = IntegerHelper.getInt(data[pos+4], data[pos+5]);
        IntegerHelper.getTwoBytes(fontMapping.getNewIndex(fontind),
                                  data, pos+4);
      }
      else if (type == Type.FBI)
      {
        int fontind = IntegerHelper.getInt(data[pos+12], data[pos+13]);
        IntegerHelper.getTwoBytes(fontMapping.getNewIndex(fontind),
                                  data, pos+12);
      }
      else if (type == Type.IFMT)
      {
        int formind = IntegerHelper.getInt(data[pos+4], data[pos+5]);
        IntegerHelper.getTwoBytes(formatMapping.getNewIndex(formind),
                                  data, pos+4);
      }
      else if (type == Type.ALRUNS)
      {
        int numRuns = IntegerHelper.getInt(data[pos+4], data[pos+5]);
        int fontPos = pos+6;
        for (int i = 0 ; i < numRuns; i++)
        {
          int fontind = IntegerHelper.getInt(data[fontPos+2], data[fontPos+3]);
          IntegerHelper.getTwoBytes(fontMapping.getNewIndex(fontind), 
                                    data, fontPos+2);
          fontPos += 4;
        }
      }

      pos += length+4;
    }
  }

  /**
   * Gets the SpContainer containing the charts drawing information
   *
   * @return the spContainer
   */
  SpContainer getSpContainer()
  {
    EscherRecordData er = new EscherRecordData(this,0);
    Assert.verify(er.isContainer());
    
    EscherContainer escherData = new EscherContainer(er);

    SpContainer spContainer = null;
    if (escherData.getType() ==  EscherRecordType.DG_CONTAINER)
    {
      EscherRecordData erd = new EscherRecordData(this, 80);
      Assert.verify(erd.getType() == EscherRecordType.SP_CONTAINER);
      spContainer = new SpContainer(erd);
    }
    else
    {
      Assert.verify(escherData.getType() == EscherRecordType.SP_CONTAINER);
      spContainer = new SpContainer(er);
    }

    return spContainer;
  }

  /**
   * Accessor for the mso drawing shortItem
   *
   * @return the drawing shortItem
   */
  MsoDrawingRecord  getMsoDrawingRecord()
  {
    return msoDrawingRecord;
  }

  /**
   * Accessor for the obj shortItem
   *
   * @return the obj shortItem
   */
  ObjRecord getObjRecord()
  {
    return objRecord;
  }
}





