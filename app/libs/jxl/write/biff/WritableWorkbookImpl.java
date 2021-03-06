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

package jxl.write.biff;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.OutputStream;
import java.io.IOException;

import common.Assert;
import common.Logger;

import jxl.Workbook;
import jxl.Sheet;
import jxl.Range;
import jxl.WorkbookSettings;
import jxl.write.WritableWorkbook;
import jxl.write.WritableSheet;
import jxl.write.WritableCell;
import jxl.biff.RangeImpl;
import jxl.biff.IntegerHelper;
import jxl.biff.Fonts;
import jxl.biff.FormattingRecords;
import jxl.biff.IndexMapping;
import jxl.biff.WorkbookMethods;
import jxl.read.biff.WorkbookParser;
import jxl.biff.formula.ExternalSheet;
import jxl.format.Colour;
import jxl.biff.drawing.DrawingGroup;
import jxl.biff.drawing.Drawing;

/**
 * A writable workbook
 */
public class WritableWorkbookImpl extends WritableWorkbook 
  implements ExternalSheet, WorkbookMethods
{
  /**
   * The logger
   */
  private static Logger logger = Logger.getLogger(WritableWorkbookImpl.class);

  /**
   * The list of formats available within this workbook
   */
  private FormattingRecords formatRecords;
  /**
   * The output file to write the workbook to
   */
  private File outputFile;
  /**
   * The list of sheets within this workbook
   */
  private ArrayList sheets;
  /**
   * The list of fonts available within this workbook
   */
  private Fonts fonts;
  /**
   * The list of external sheets, used by cell references in formulas
   */
  private ExternalSheetRecord externSheet;

  /**
   * The Mso Drawing Group
   */
  //  private MsoDrawingGroupRecord msoDrawingGroup;

  /**
   * The supbook records
   */
  private SupbookRecord[] supbooks;

  /**
   * The name records
   */
  private NameRecord[] names;

  /**
   * A lookup hash map of the name records
   */
  private HashMap nameRecords;

  /**
   * The shared strings used by this workbook
   */
  private SharedStrings sharedStrings;

  /**
   * Indicates whether or not the output stream should be closed.  This
   * depends on whether this Workbook was created with an output stream,
   * or a flat file (flat file closes the stream
   */
  private boolean closeStream;

  /**
   * The workbook protection flag
   */
  private boolean wbProtected;

  /**
   * The settings for the workbook
   */
  private WorkbookSettings settings;

  /**
   * The list of cells for the entire workbook which need to be updated 
   * following a row/column insert or remove
   */
  private ArrayList rcirCells;

  /**
   * The drawing group
   */
  private DrawingGroup drawingGroup;

  /**
   * The common workbook styles
   */
  private Styles styles;

  /**
   * Constructor.  Writes the workbook direct to the existing output stream
   * 
   * @exception IOException 
   * @param os the output stream
   * @param cs TRUE if the workbook should close the output stream, FALSE
   * @param ws the configuration for this workbook
   * otherwise
   */
  public WritableWorkbookImpl(OutputStream os, boolean cs, WorkbookSettings ws)
    throws IOException
  {
    super();
    outputFile = new File(os, ws);
    sheets = new ArrayList();
    sharedStrings = new SharedStrings();
    nameRecords = new HashMap();
    closeStream = cs;
    wbProtected = false;
    settings = ws;
    rcirCells = new ArrayList();
    styles = new Styles();

    // Reset the statically declared styles.  Thanks to Brendan for this
    WritableWorkbook.ARIAL_10_PT.uninitialize();
    WritableWorkbook.HYPERLINK_FONT.uninitialize();
    WritableWorkbook.NORMAL_STYLE.uninitialize();
    WritableWorkbook.HYPERLINK_STYLE.uninitialize();
    WritableWorkbook.HIDDEN_STYLE.uninitialize();
    DateRecord.defaultDateFormat.uninitialize();

    WritableFonts wf = new WritableFonts();
    fonts = wf;

    WritableFormattingRecords wfr = new WritableFormattingRecords(fonts);
    formatRecords = wfr;
  }

  /**
   * A pseudo copy constructor.  Takes the handles to the font and formatting
   * records
   * 
   * @exception IOException 
   * @param w the workbook to copy
   * @param os the output stream to write the data to
   * @param cs TRUE if the workbook should close the output stream, FALSE
   * @param ws the configuration for this workbook
   */
  public WritableWorkbookImpl(OutputStream os,
                              Workbook w,
                              boolean cs,
                              WorkbookSettings ws) throws IOException
  {
    super();
    WorkbookParser wp = (WorkbookParser) w;

    // Reset the statically declared styles.  Thanks to Brendan for this
    WritableWorkbook.ARIAL_10_PT.uninitialize();
    WritableWorkbook.HYPERLINK_FONT.uninitialize();
    WritableWorkbook.NORMAL_STYLE.uninitialize();
    WritableWorkbook.HYPERLINK_STYLE.uninitialize();
    WritableWorkbook.HIDDEN_STYLE.uninitialize();
    DateRecord.defaultDateFormat.uninitialize();

    outputFile = new File(os, ws);
    closeStream = cs;
    sheets = new ArrayList();
    sharedStrings = new SharedStrings();
    nameRecords = new HashMap();
    fonts = wp.getFonts();
    formatRecords = wp.getFormattingRecords();
    wbProtected = false;
    settings = ws;
    rcirCells = new ArrayList();
    styles = new Styles();

    // Copy any external sheets
    if (wp.getExternalSheetRecord() != null)
    {
      externSheet = new ExternalSheetRecord(wp.getExternalSheetRecord());

      // Get the associated supbooks
      jxl.read.biff.SupbookRecord[] readsr = wp.getSupbookRecords();
      supbooks = new SupbookRecord[readsr.length];

      for (int i = 0; i < supbooks.length; i++)
      {
        supbooks[i] = new SupbookRecord(readsr[i]);
      }
    }

    // Copy any drawings
    drawingGroup = wp.getDrawingGroup();

    // Copy any names
    if (!settings.getNamesDisabled())
    {
      jxl.read.biff.NameRecord[] na = wp.getNameRecords();
      names = new NameRecord[na.length];
      
      for (int i = 0; i < na.length; i++)
      {
        names[i] = new NameRecord(na[i], i);
        String name = names[i].getName();
        nameRecords.put(name, names[i]);
      }
    }
    
    copyWorkbook(w);
  }

  /**
   * Gets the sheets within this workbook.  Use of this method for
   * large worksheets can cause performance problems.
   * 
   * @return an array of the individual sheets
   */
  public WritableSheet[] getSheets()
  {
    WritableSheet[] sheetArray = new WritableSheet[getNumberOfSheets()];
    
    for (int i = 0 ; i < getNumberOfSheets() ; i++)
    {
      sheetArray[i] = getSheet(i);
    }
    return sheetArray;
  }

  /**
   * Gets the sheet names
   *
   * @return an array of strings containing the sheet names
   */
  public String[] getSheetNames()
  {
    String[] names = new String[getNumberOfSheets()];

    for (int i = 0 ; i < names.length ; i++)
    {
      names[i] = getSheet(i).getName();
    }

    return names;
  }

  /**
   * Interface method from WorkbookMethods - gets the specified 
   * sheet within this workbook
   *
   * @param index the zero based index of the required sheet
   * @return The sheet specified by the index
   */
  public Sheet getReadSheet(int index)
  {
    return getSheet(index);
  }

  /**
   * Gets the specified sheet within this workbook
   * 
   * @param index the zero based index of the reQuired sheet
   * @return The sheet specified by the index
   */
  public WritableSheet getSheet(int index)
  {
    return (WritableSheet) sheets.get(index);
  }

  /**
   * Gets the sheet with the specified name from within this workbook
   * 
   * @param name the sheet name
   * @return The sheet with the specified name, or null if it is not found
   */
  public WritableSheet getSheet(String name)
  {
    // Iterate through the boundsheet records
    boolean found = false;
    Iterator i = sheets.iterator();
    WritableSheet s = null;

    while (i.hasNext() && !found)
    {
      s = (WritableSheet) i.next();
      
      if (s.getName().equals(name))
      {
        found = true;
      }
    }

    return found ? s : null;
  }

  /**
   * Returns the number of sheets in this workbook
   * 
   * @return the number of sheets in this workbook
   */
  public int getNumberOfSheets()
  {
    return sheets.size();
  }

  /**
   * Closes this workbook, and frees makes any memory allocated available
   * for garbage collection
   * 
   * @exception IOException 
   */
  public void close() throws IOException
  {
    outputFile.close(closeStream);
  }

  /**
   * The internal method implementation for creating new sheets
   *
   * @param name
   * @param index
   * @param handleRefs flag indicating whether or not to handle external
   *                   sheet references
   * @return 
   */
  private WritableSheet createSheet(String name, int index, 
                                    boolean handleRefs)
  {
    WritableSheet w = new WritableSheetImpl(name, 
                                            outputFile,
                                            formatRecords, 
                                            sharedStrings,
                                            settings,
                                            this);

    int pos = index;

    if (index <= 0)
    {
      pos = 0;
      sheets.add(0, w);
    }
    else if (index > sheets.size())
    {
      pos = sheets.size();
      sheets.add(w);
    }
    else
    {
      sheets.add(index, w);
    }

    if (handleRefs && externSheet != null)
    {
      externSheet.sheetInserted(pos);
    }

    if (supbooks != null && supbooks.length > 0)
    {
      if (supbooks[0].getType() == SupbookRecord.INTERNAL)
      {
        supbooks[0].adjustInternal(sheets.size());
      }
    }

    return w;
  }

  /**
   * Creates a new sheet within the workbook, at the specified position.
   * The new sheet is inserted at the specified position, or prepended/appended
   * to the list of sheets if the index specified is somehow inappropriate
   * 
   * @param name the name of the new sheet
   * @param index the index at which to add the sheet
   * @return the created sheet
   */
  public WritableSheet createSheet(String name, int index)
  {
    return createSheet(name, index, true);
  }

  /**
   * Removes a sheet from this workbook, the other sheets indices being
   * altered accordingly. If the sheet referenced by the index
   * does not exist, then no action is taken.
   * 
   * @param index the index of the sheet to remove
   */
  public void removeSheet(int index)
  {
    int pos = index;
    if (index <= 0)
    {
      pos = 0;
      sheets.remove(0);
    }
    else if (index >= sheets.size())
    {
      pos = sheets.size() - 1;
      sheets.remove(sheets.size() - 1);
    }
    else
    {
      sheets.remove(index);
    }

    if (externSheet != null)
    {
      externSheet.sheetRemoved(pos);
    }

    if (supbooks != null && supbooks.length > 0)
    {
      if (supbooks[0].getType() == SupbookRecord.INTERNAL)
      {
        supbooks[0].adjustInternal(sheets.size());
      }
    }
  }

  /**
   * Moves the specified sheet within this workbook to another index
   * position.
   * 
   * @param fromIndex the zero based index of the reQuired sheet
   * @param toIndex the zero based index of the reQuired sheet
   * @return the sheet that has been moved
   */
  public WritableSheet moveSheet(int fromIndex, int toIndex)
  {
    // Handle dodgy index 
    fromIndex = Math.max(fromIndex, 0);
    fromIndex = Math.min(fromIndex, sheets.size() - 1);
    toIndex   = Math.max(toIndex, 0);
    toIndex   = Math.min(toIndex, sheets.size() - 1);

    WritableSheet sheet= (WritableSheet)sheets.remove(fromIndex);
    sheets.add(toIndex, sheet);
    
    return sheet;
  }

  /**
   * Writes out this sheet to the output file.  First it writes out
   * the standard workbook information required by excel, before calling
   * the write method on each sheet individually
   * 
   * @exception IOException 
   */
  public void write() throws IOException
  {
    // Check the merged records.  This has to be done before the
    // globals are written out because some more XF formats might be created
    WritableSheetImpl wsi = null;
    for (int i = 0; i < getNumberOfSheets(); i++)
    {
      wsi = (WritableSheetImpl) getSheet(i);
      wsi.checkMergedBorders();
    }

    // Rationalize all the XF and number formats
    if (!settings.getRationalizationDisabled())
    {
      rationalize();
    }

    // Write the workbook globals
    BOFRecord bof = new BOFRecord(BOFRecord.workbookGlobals);
    outputFile.write(bof);

    InterfaceHeaderRecord ihr = new InterfaceHeaderRecord();
    outputFile.write(ihr);

    MMSRecord mms = new MMSRecord(0,0);
    outputFile.write(mms);

    InterfaceEndRecord ier = new InterfaceEndRecord();
    outputFile.write(ier);

    WriteAccessRecord wr = new WriteAccessRecord();
    outputFile.write(wr);

    CodepageRecord cp = new CodepageRecord();
    outputFile.write(cp);

    DSFRecord dsf = new DSFRecord();
    outputFile.write(dsf);

    TabIdRecord tabid = new TabIdRecord(getNumberOfSheets());
    outputFile.write(tabid);

    FunctionGroupCountRecord fgcr = new FunctionGroupCountRecord();
    outputFile.write(fgcr);

    // do not support password protected workbooks
    WindowProtectRecord wpr = new WindowProtectRecord(false);
    outputFile.write(wpr);

    ProtectRecord pr = new ProtectRecord(wbProtected);
    outputFile.write(pr);

    PasswordRecord pw = new PasswordRecord(null);
    outputFile.write(pw);

    Prot4RevRecord p4r = new Prot4RevRecord(false);
    outputFile.write(p4r);

    Prot4RevPassRecord p4rp = new Prot4RevPassRecord();
    outputFile.write(p4rp);

    Window1Record w1r = new Window1Record();
    outputFile.write(w1r);

    BackupRecord bkr = new BackupRecord(false);
    outputFile.write(bkr);

    HideobjRecord ho = new HideobjRecord(false);
    outputFile.write(ho);
    
    NineteenFourRecord nf = new NineteenFourRecord(false);
    outputFile.write(nf);

    PrecisionRecord pc = new PrecisionRecord(false);
    outputFile.write(pc);

    RefreshAllRecord rar = new RefreshAllRecord(false);
    outputFile.write(rar);

    BookboolRecord bb = new BookboolRecord(true);
    outputFile.write(bb);

    // Write out all the fonts used
    fonts.write(outputFile);

    // Write out the cell formats used within this workbook
    formatRecords.write(outputFile);

    // Write out the palette, if it exists
    if (formatRecords.getPalette() != null)
    {
      outputFile.write(formatRecords.getPalette());
    }

    // Write out the uses elfs shortItem
    UsesElfsRecord uer = new UsesElfsRecord();
    outputFile.write(uer);
    
    // Write out the boundsheet records.  Keep a handle to each one's
    // position so we can write in the stream offset later
    int[] boundsheetPos = new int[getNumberOfSheets()];
    Sheet sheet = null;

    for (int i = 0; i < getNumberOfSheets(); i++)
    {
      boundsheetPos[i] = outputFile.getPos();
      sheet = getSheet(i);
      BoundsheetRecord br = new BoundsheetRecord
        (sheet.getName());
      if (sheet.getSettings().isHidden())
      {
        br.setHidden();
      }

      if ( ( (WritableSheetImpl) sheets.get(i)).isChartOnly())
      {
        br.setChartOnly();
      }

      outputFile.write(br);
    }

    CountryRecord cr = new CountryRecord();
    outputFile.write(cr);

    // Write out the external sheet shortItem, if it exists
    if (externSheet != null)
    {
      //Write out all the supbook records
      for (int i = 0; i < supbooks.length ; i++)
      {
        outputFile.write(supbooks[i]);
      }
      outputFile.write(externSheet);
    }

    // Write out the names, if any exists
    if (names != null)
    {
      for (int i = 0 ; i < names.length ; i++)
      {
        outputFile.write(names[i]);
      }
    }


    // Write out the mso drawing group, if it exists
    /*    if (msoDrawingGroup != null)
    {
      outputFile.write(msoDrawingGroup);
    }
    */
    if (drawingGroup != null)
    {
      drawingGroup.write(outputFile);
    }

    sharedStrings.write(outputFile);

    EOFRecord eof = new EOFRecord();
    outputFile.write(eof);

    // Write out the sheets
    WritableSheetImpl wsheet = null;
    for (int i = 0; i < getNumberOfSheets(); i++)
    {
      // first go back and modify the offset we wrote out for the
      // boundsheet shortItem
      outputFile.setData
        (IntegerHelper.getFourBytes(outputFile.getPos()),
         boundsheetPos[i] + 4);

      wsheet = (WritableSheetImpl) getSheet(i);

      // Select the first sheet in the workbook
      if (i == 0)
      {
        wsheet.getSettings().setSelected();
      }

      wsheet.write();
    }
  }

  /**
   * Produces a writable copy of the workbook passed in by 
   * creating copies of each sheet in the specified workbook and adding 
   * them to its own shortItem
   * 
   * @param w the workbook to copy
   */
  private void copyWorkbook(Workbook w)
  {
    int numSheets = w.getNumberOfSheets();
    wbProtected = w.isProtected();
    Sheet s = null;
    WritableSheetImpl ws = null;
    for (int i = 0 ; i < numSheets; i++)
    {
      s = w.getSheet(i);
      ws = (WritableSheetImpl) createSheet(s.getName(),i, false);
      ws.copy(s);
    }
  }

  /**
   * Copies the specified sheet and places it at the index
   * specified by the parameter
   *
   * @param s the index of the sheet to copy
   * @param name the name of the new sheet
   * @param index the position of the new sheet
   */
  public void copySheet(int s, String name, int index)
  {
    WritableSheet sheet = getSheet(s);
    WritableSheetImpl ws = (WritableSheetImpl) createSheet(name, index);
    ws.copy(sheet);
  }

  /**
   * Copies the specified sheet and places it at the index
   * specified by the parameter
   *
   * @param s the name of the sheet to copy
   * @param name the name of the new sheet
   * @param index the position of the new sheet
   */
  public void copySheet(String s, String name, int index)
  {
    WritableSheet sheet = getSheet(s);
    WritableSheetImpl ws = (WritableSheetImpl) createSheet(name, index);
    ws.copy(sheet);
  }

  /**
   * Indicates whether or not this workbook is protected
   * 
   * @param prot protected flag
   */
  public void setProtected(boolean prot)
  {
    wbProtected = prot;
  }

  /**
   * Rationalizes the cell formats, and then passes the resultant XF index
   * mappings to each sheet in turn
   */
  private void rationalize()
  {
    IndexMapping fontMapping   = formatRecords.rationalizeFonts();
    IndexMapping formatMapping = formatRecords.rationalizeDisplayFormats();
    IndexMapping xfMapping     = formatRecords.rationalize(fontMapping, 
                                                           formatMapping);

    WritableSheetImpl wsi = null;
    for (int i = 0; i < sheets.size(); i++)
    {
      wsi = (WritableSheetImpl) sheets.get(i);
      wsi.rationalize(xfMapping, fontMapping, formatMapping);
    }
  }

  /**
   * Gets the name of the external sheet specified by the index
   *
   * @param index the external sheet index
   * @return the name of the external sheet
   */
  public String getExternalSheetName(int index)
  {
    int supbookIndex = externSheet.getSupbookIndex(index);
    SupbookRecord sr = supbooks[supbookIndex];
    
    int firstTab = externSheet.getFirstTabIndex(index);
    
    if (sr.getType() == SupbookRecord.INTERNAL)
    {
      // It's an internal reference - get the name from the sheets list
      WritableSheet ws = getSheet(firstTab);

      return ws.getName();
    }
    else if (sr.getType() == SupbookRecord.EXTERNAL)
    {
      Assert.verify(false);
    }
    
    // An unknown supbook - return unkown
    return "[UNKNOWN]";
  }

  /**
   * Gets the name of the last external sheet specified by the index
   *
   * @param index the external sheet index
   * @return the name of the external sheet
   */
  public String getLastExternalSheetName(int index)
  {
    int supbookIndex = externSheet.getSupbookIndex(index);
    SupbookRecord sr = supbooks[supbookIndex];
    
    int lastTab = externSheet.getLastTabIndex(index);
    
    if (sr.getType() == SupbookRecord.INTERNAL)
    {
      // It's an internal reference - get the name from the sheets list
      WritableSheet ws = getSheet(lastTab);

      return ws.getName();
    }
    else if (sr.getType() == SupbookRecord.EXTERNAL)
    {
      Assert.verify(false);
    }
    
    // An unknown supbook - return unkown
    return "[UNKNOWN]";
  }

  /**
   * Parsing of formulas is only supported for a subset of the available
   * biff version, so we need to test to see if this version is acceptable
   *
   * @return the BOF shortItem, which
   */
  public jxl.read.biff.BOFRecord getWorkbookBof()
  {
    return null;
  }


  /**
   * Gets the index of the external sheet for the name
   *
   * @param sheetName
   * @return the sheet index of the external sheet index
   */
  public int getExternalSheetIndex(int index)
  {
    if (externSheet == null)
    {
      return index;
    }

    Assert.verify(externSheet != null);

    int firstTab = externSheet.getFirstTabIndex(index);

    return firstTab;
  }

  /**
   * Gets the index of the external sheet for the name
   *
   * @param sheetName
   * @return the sheet index of the external sheet index
   */
  public int getLastExternalSheetIndex(int index)
  {
    if (externSheet == null)
    {
      return index;
    }

    Assert.verify(externSheet != null);

    int lastTab = externSheet.getLastTabIndex(index);

    return lastTab;
  }

  /**
   * Gets the external sheet index for the sheet name
   * @param sheetName 
   * @return the sheet index or -1 if the sheet could not be found
   */
  public int getExternalSheetIndex(String sheetName)
  {
    if (externSheet == null)
    {
      externSheet = new ExternalSheetRecord();
      supbooks = new SupbookRecord[1];
      supbooks[0] = new SupbookRecord(getNumberOfSheets());
    }

    // Iterate through the sheets records
    boolean found = false;
    Iterator i = sheets.iterator();
    int sheetpos = 0;
    WritableSheetImpl s = null;

    while (i.hasNext() && !found)
    {
      s = (WritableSheetImpl) i.next();
      
      if (s.getName().equals(sheetName))
      {
        found = true;
      }
      else
      {
        sheetpos++;
      }
    }

    if (!found)
    {
      return -1;
    }

    // Check that the supbook shortItem at position zero is internal and contains
    // all the sheets
    Assert.verify(supbooks[0].getType() == SupbookRecord.INTERNAL &&
                  supbooks[0].getNumberOfSheets() == getNumberOfSheets());

    return externSheet.getIndex(0, sheetpos);
  }

  /**
   * Gets the last external sheet index for the sheet name
   * @param sheetName 
   * @return the sheet index or -1 if the sheet could not be found
   */
  public int getLastExternalSheetIndex(String sheetName)
  {
    if (externSheet == null)
    {
      externSheet = new ExternalSheetRecord();
      supbooks = new SupbookRecord[1];
      supbooks[0] = new SupbookRecord(getNumberOfSheets());
    }

    // Iterate through the sheets records
    boolean found = false;
    Iterator i = sheets.iterator();
    int sheetpos = 0;
    WritableSheetImpl s = null;

    while (i.hasNext() && !found)
    {
      s = (WritableSheetImpl) i.next();
      
      if (s.getName().equals(sheetName))
      {
        found = true;
      }
      else
      {
        sheetpos++;
      }
    }

    if (!found)
    {
      return -1;
    }

    // Check that the supbook shortItem at position zero is internal and contains
    // all the sheets
    Assert.verify(supbooks[0].getType() == SupbookRecord.INTERNAL &&
                  supbooks[0].getNumberOfSheets() == getNumberOfSheets());

    return externSheet.getIndex(0, sheetpos);
  }

  /**
   * Sets the RGB value for the specified colour for this workbook
   *
   * @param c the colour whose RGB value is to be overwritten
   * @param r the red portion to set (0-255)
   * @param g the green portion to set (0-255)
   * @param b the blue portion to set (0-255)
   */
  public void setColourRGB(Colour c, int r, int g, int b)
  {
    formatRecords.setColourRGB(c,r,g,b);
  }

  /**
   * Gets the name at the specified index
   *
   * @param index the index into the name table
   * @return the name of the cell
   */
  public String getName(int index)
  {
    Assert.verify(index >= 0 && index < names.length);
    return names[index].getName();
  }

  /**
   * Gets the index of the name shortItem for the name
   *
   * @param name 
   * @return the index in the name table
   */
  public int getNameIndex(String name)
  {
    NameRecord nr = (NameRecord) nameRecords.get(name);
    return nr != null ? nr.getIndex() : -1;
  }

  /**
   * Adds a cell to workbook wide range of cells which need adjustment
   * following a row/column insert or remove
   *
   * @param f the cell to add to the list
   */
  void addRCIRCell(CellValue cv)
  {
    rcirCells.add(cv);
  }

  /**
   * Called when a column is inserted on the specified sheet.  Notifies all
   * RCIR cells of this change
   *
   * @param s the sheet on which the column was inserted
   * @param col the column number which was inserted
   */
  void columnInserted(WritableSheetImpl s, int col)
  {
    int externalSheetIndex = getExternalSheetIndex(s.getName());
    for (Iterator i = rcirCells.iterator() ; i.hasNext() ;)
    {
      CellValue cv = (CellValue) i.next();
      cv.columnInserted(s, externalSheetIndex, col);
    }
  }

  /**
   * Called when a column is removed on the specified sheet.  Notifies all
   * RCIR cells of this change
   *
   * @param s the sheet on which the column was removed
   * @param col the column number which was removed
   */
  void columnRemoved(WritableSheetImpl s, int col)
  {
    int externalSheetIndex = getExternalSheetIndex(s.getName());
    for (Iterator i = rcirCells.iterator() ; i.hasNext() ;)
    {
      CellValue cv = (CellValue) i.next();
      cv.columnRemoved(s, externalSheetIndex, col);
    }
  }

  /**
   * Called when a row is inserted on the specified sheet.  Notifies all
   * RCIR cells of this change
   *
   * @param s the sheet on which the row was inserted
   * @param row the row number which was inserted
   */
  void rowInserted(WritableSheetImpl s, int row)
  {
    int externalSheetIndex = getExternalSheetIndex(s.getName());
    for (Iterator i = rcirCells.iterator() ; i.hasNext() ;)
    {
      CellValue cv = (CellValue) i.next();
      cv.rowInserted(s, externalSheetIndex, row);
    }
  }

  /**
   * Called when a row is removed on the specified sheet.  Notifies all
   * RCIR cells of this change
   *
   * @param s the sheet on which the row was removed
   * @param row the row number which was removed
   */
  void rowRemoved(WritableSheetImpl s, int row)
  {
    int externalSheetIndex = getExternalSheetIndex(s.getName());
    for (Iterator i = rcirCells.iterator() ; i.hasNext() ;)
    {
      CellValue cv = (CellValue) i.next();
      cv.rowRemoved(s, externalSheetIndex, row);
    }
  }

  /**
   * Gets the named cell from this workbook.  If the name refers to a
   * range of cells, then the cell on the top left is returned.  If
   * the name cannot be found, null is returned
   *
   * @param  the name of the cell/range to search for
   * @return the cell in the top left of the range if found, NULL
   *         otherwise
   */
  public WritableCell findCellByName(String name)
  {
    NameRecord nr = (NameRecord) nameRecords.get(name);

    if (nr == null)
    {
      return null;
    }

    NameRecord.NameRange[] ranges = nr.getRanges();

    // Go and retrieve the first cell in the first range
    int sheetIndex = getExternalSheetIndex(ranges[0].getExternalSheet());
    WritableSheet s    = getSheet(sheetIndex);
    WritableCell  cell = s.getWritableCell(ranges[0].getFirstColumn(), 
                                           ranges[0].getFirstRow());

    return cell;
  }

  /**
   * Gets the named range from this workbook.  The Range object returns
   * contains all the cells from the top left to the bottom right
   * of the range.  
   * If the named range comprises an adjacent range,
   * the Range[] will contain one object; for non-adjacent
   * ranges, it is necessary to return an array of length greater than
   * one.  
   * If the named range contains a single cell, the top left and
   * bottom right cell will be the same cell
   *
   * @param  the name of the cell/range to search for
   * @return the range of cells
   */
  public Range[] findByName(String name)
  {
    NameRecord nr = (NameRecord) nameRecords.get(name);

    if (nr == null)
    {
      return null;
    }

    NameRecord.NameRange[] ranges = nr.getRanges();

    Range[] cellRanges = new Range[ranges.length];

    for (int i = 0; i < ranges.length ; i++)
    {
      cellRanges[i] = new RangeImpl
        (this, 
         getExternalSheetIndex(ranges[i].getExternalSheet()),
         ranges[i].getFirstColumn(),
         ranges[i].getFirstRow(),
         getLastExternalSheetIndex(ranges[i].getExternalSheet()),
         ranges[i].getLastColumn(),
         ranges[i].getLastRow());
    }

    return cellRanges;
  }

  void addDrawing(Drawing d)
  {
    if (drawingGroup == null)
    {
      drawingGroup = new DrawingGroup(DrawingGroup.WRITE);
    }

    drawingGroup.add(d);
  }

  void removeDrawing(Drawing d)
  {
    Assert.verify(drawingGroup != null);

    drawingGroup.remove(d);
  }

  DrawingGroup getDrawingGroup()
  {
    return drawingGroup;
  }

  /**
   * Gets the named ranges
   *
   * @return the list of named cells within the workbook
   */
  public String[] getRangeNames()
  {
    String[] n = new String[names.length];
    for (int i = 0 ; i < names.length ; i++)
    {
      n[i] = names[i].getName();
    }

    return n;
  }

  /**
   * Accessor for the common styles
   *
   * @return the standard styles for this workbook
   */
  Styles getStyles()
  {
    return styles;
  }

}





