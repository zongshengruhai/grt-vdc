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

import java.util.ArrayList;
import java.util.Iterator;

import common.Assert;
import common.Logger;

import jxl.Cell;
import jxl.DateCell;
import jxl.CellType;
import jxl.Range;
import jxl.SheetSettings;
import jxl.WorkbookSettings;
import jxl.CellReferenceHelper;
import jxl.HeaderFooter;
import jxl.biff.Type;
import jxl.biff.FormattingRecords;
import jxl.biff.WorkspaceInformationRecord;
import jxl.biff.drawing.Chart;
import jxl.format.PaperSize;
import jxl.format.PageOrientation;
import jxl.biff.drawing.ObjRecord;
import jxl.biff.drawing.MsoDrawingRecord;
import jxl.biff.drawing.Drawing;

/**
 * Reads the sheet.  This functionality was originally part of the
 * SheetImpl class, but was separated out in order to simplify the former
 * class
 */
final class SheetReader
{
  /**
   * The logger
   */
  private static Logger logger = Logger.getLogger(SheetReader.class);

  /**
   * The excel file
   */
  private File excelFile;

  /**
   * A handle to the shared string table
   */
  private SSTRecord sharedStrings;

  /**
   * A handle to the sheet BOF shortItem, which indicates the stream type
   */
  private BOFRecord sheetBof;

  /**
   * A handle to the workbook BOF shortItem, which indicates the stream type
   */
  private BOFRecord workbookBof;

  /**
   * A handle to the formatting records
   */
  private FormattingRecords formattingRecords;

  /**
   * The  number of rows
   */
  private int numRows;

  /**
   * The number of columns
   */
  private int numCols;

  /**
   * The cells
   */
  private Cell[][] cells;

  /**
   * The start position in the stream of this sheet
   */
  private int startPosition;

  /**
   * The list of non-default row properties
   */
  private ArrayList rowProperties;

  /**
   * An array of column info records.  They are held this way before
   * they are transferred to the more convenient array
   */
  private ArrayList columnInfosArray;

  /**
   * A list of shared formula groups
   */
  private ArrayList sharedFormulas;

  /**
   * A list of hyperlinks on this page
   */
  private ArrayList hyperlinks;

  /**
   * A list of merged cells on this page
   */
  private Range[] mergedCells;

  /**
   * The list of charts on this page
   */
  private ArrayList charts;

  /**
   * The list of drawings on this page
   */
  private ArrayList drawings;

  /**
   * Indicates whether or not the dates are based around the 1904 date system
   */
  private boolean nineteenFour;

  /**
   * The PLS print shortItem
   */
  private PLSRecord plsRecord;

  /**
   * The workspace options
   */
  private WorkspaceInformationRecord workspaceOptions;

  /**
   * The horizontal page breaks contained on this sheet
   */
  private int[] rowBreaks;

  /**
   * The sheet settings
   */
  private SheetSettings settings;

  /**
   * The workbook settings
   */
  private WorkbookSettings workbookSettings;

  /**
   * A handle to the workbook which contains this sheet.  Some of the records
   * need this in order to reference external sheets
   */
  private WorkbookParser workbook;

  /**
   * A handle to the sheet
   */
  private SheetImpl sheet;

  /**
   * Constructor
   *
   * @param fr the formatting records
   * @param sst the shared string table
   * @param f the excel file
   * @param sb the bof shortItem which indicates the start of the sheet
   * @param wb the bof shortItem which indicates the start of the sheet
   * @param wp the workbook which this sheet belongs to
   * @param sp the start position of the sheet bof in the excel file
   * @param sh the sheet
   * @param nf 1904 date shortItem flag
   * @exception BiffException
   */
  SheetReader(File f,
              SSTRecord sst,
              FormattingRecords fr,
              BOFRecord sb,
              BOFRecord wb,
              boolean nf,
              WorkbookParser wp,
              int sp,
              SheetImpl sh)
  {
    excelFile = f;
    sharedStrings = sst;
    formattingRecords = fr;
    sheetBof = sb;
    workbookBof = wb;
    columnInfosArray = new ArrayList();
    sharedFormulas = new ArrayList();
    hyperlinks = new ArrayList();
    rowProperties = new ArrayList(10);
    charts = new ArrayList();
    drawings = new ArrayList();
    nineteenFour = nf;
    workbook = wp;
    startPosition = sp;
    sheet = sh;
    settings = new SheetSettings();
    workbookSettings = workbook.getSettings();
  }

  /**
   * Adds the cell to the array
   *
   * @param cell the cell to add
   */
  private void addCell(Cell cell)
  {
    // Sometimes multiple cells (eg. MULBLANK) can exceed the
    // column/row boundaries.  Ignore these

    if (cell.getRow() < numRows && cell.getColumn() < numCols)
    {
      if (cells[cell.getRow()][cell.getColumn()] != null)
      {
        StringBuffer sb = new StringBuffer();
        CellReferenceHelper.getCellReference
          (cell.getColumn(), cell.getRow(), sb);
        logger.warn("Cell " + sb.toString() +
                      " already contains data");
      }
      cells[cell.getRow()][cell.getColumn()] = cell;
    }
  }

  /**
   * Reads in the contents of this sheet
   */
  final void read()
  {
    Record r = null;
    BaseSharedFormulaRecord sharedFormula = null;
    boolean sharedFormulaAdded = false;

    boolean cont = true;

    // Set the position within the file
    excelFile.setPos(startPosition);

    // Handles to the last drawing and obj records
    MsoDrawingRecord msoRecord = null;
    ObjRecord objRecord = null;

    // A handle to window2 shortItem
    Window2Record window2Record = null;

    // A handle to printgridlines shortItem
    PrintGridLinesRecord printGridLinesRecord = null;

    // A handle to printheaders shortItem
    PrintHeadersRecord printHeadersRecord = null;

    while (cont)
    {
      r = excelFile.next();

      if (r.getType() == Type.UNKNOWN && r.getCode() == 0)
      {
        System.err.print("Warning:  biff code zero found");

        // Try a dimension shortItem
        if (r.getLength() == 0xa)
        {
          logger.warn("Biff code zero found - trying a dimension shortItem.");
          r.setType(Type.DIMENSION);
        }
        else
        {
          logger.warn("Biff code zero found - Ignoring.");
        }
      }

      if (r.getType() == Type.DIMENSION)
      {
        DimensionRecord dr = null;

        if (workbookBof.isBiff8())
        {
          dr = new DimensionRecord(r);
        }
        else
        {
          dr = new DimensionRecord(r, DimensionRecord.biff7);
        }
        numRows = dr.getNumberOfRows();
        numCols = dr.getNumberOfColumns();
        cells = new Cell[numRows][numCols];
      }
      else if (r.getType() == Type.LABELSST)
      {
        LabelSSTRecord label = new LabelSSTRecord(r,
                                                  sharedStrings,
                                                  formattingRecords,
                                                  sheet);
        addCell(label);
      }
      else if (r.getType() == Type.RK || r.getType() == Type.RK2)
      {
        RKRecord rkr = new RKRecord(r, formattingRecords, sheet);

        if (formattingRecords.isDate(rkr.getXFIndex()))
        {
          DateCell dc = new DateRecord
            (rkr, rkr.getXFIndex(), formattingRecords, nineteenFour, sheet);
          addCell(dc);
        }
        else
        {
          addCell(rkr);
        }
      }
      else if (r.getType() == Type.HLINK)
      {
        HyperlinkRecord hr = new HyperlinkRecord(r, sheet, workbookSettings);
        hyperlinks.add(hr);
      }
      else if (r.getType() == Type.MERGEDCELLS)
      {
        MergedCellsRecord  mc = new MergedCellsRecord(r, sheet);
        if (mergedCells == null)
        {
          mergedCells = mc.getRanges();
        }
        else
        {
          Range[] newMergedCells =
            new Range[mergedCells.length + mc.getRanges().length];
          System.arraycopy(mergedCells, 0, newMergedCells, 0,
                           mergedCells.length);
          System.arraycopy(mc.getRanges(),
                           0,
                           newMergedCells, mergedCells.length,
                           mc.getRanges().length);
          mergedCells = newMergedCells;
        }
      }
      else if (r.getType() == Type.MULRK)
      {
        MulRKRecord mulrk = new MulRKRecord(r);

        // Get the individual cell records from the multiple shortItem
        int num = mulrk.getNumberOfColumns();
        int ixf = 0;
        for (int i = 0; i < num; i++)
        {
          ixf = mulrk.getXFIndex(i);

          NumberValue nv = new NumberValue
            (mulrk.getRow(),
             mulrk.getFirstColumn() + i,
             RKHelper.getDouble(mulrk.getRKNumber(i)),
             ixf,
             formattingRecords,
             sheet);


          if (formattingRecords.isDate(ixf))
          {
            DateCell dc = new DateRecord(nv, ixf, formattingRecords,
                                         nineteenFour, sheet);
            addCell(dc);
          }
          else
          {
            nv.setNumberFormat(formattingRecords.getNumberFormat(ixf));
            addCell(nv);
          }
        }
      }
      else if (r.getType() == Type.NUMBER)
      {
        NumberRecord nr = new NumberRecord(r, formattingRecords, sheet);

        if (formattingRecords.isDate(nr.getXFIndex()))
        {
          DateCell dc = new DateRecord(nr,
                                       nr.getXFIndex(),
                                       formattingRecords,
                                       nineteenFour, sheet);
          addCell(dc);
        }
        else
        {
          addCell(nr);
        }
      }
      else if (r.getType() == Type.BOOLERR)
      {
        BooleanRecord br = new BooleanRecord(r, formattingRecords, sheet);

        if (br.isError())
        {
          ErrorRecord er = new ErrorRecord(br.getRecord(), formattingRecords,
                                           sheet);
          addCell(er);
        }
        else
        {
          addCell(br);
        }
      }
      else if (r.getType() == Type.PRINTGRIDLINES)
      {
        printGridLinesRecord = new PrintGridLinesRecord(r);
        settings.setPrintGridLines(printGridLinesRecord.getPrintGridLines());
      }
      else if (r.getType() == Type.PRINTHEADERS)
      {
        printHeadersRecord = new PrintHeadersRecord(r);
        settings.setPrintHeaders(printHeadersRecord.getPrintHeaders());
      }
      else if (r.getType() == Type.WINDOW2)
      {
        window2Record = new Window2Record(r);

        settings.setShowGridLines(window2Record.getShowGridLines());
        settings.setDisplayZeroValues(window2Record.getDisplayZeroValues());
        settings.setSelected();
      }
      else if (r.getType() == Type.PANE)
      {
        PaneRecord pr = new PaneRecord(r);

        if (window2Record != null &&
            window2Record.getFrozen() &&
            window2Record.getFrozenNotSplit())
        {
          settings.setVerticalFreeze(pr.getRowsVisible());
          settings.setHorizontalFreeze(pr.getColumnsVisible());
        }
      }
      else if (r.getType() == Type.CONTINUE)
      {
        ;
      }
      else if (r.getType() == Type.NOTE)
      {
        ;
      }
      else if (r.getType() == Type.ARRAY)
      {
        ;
      }
      else if (r.getType() == Type.PROTECT)
      {
        ProtectRecord pr = new ProtectRecord(r);
        settings.setProtected(pr.isProtected());
      }
      else if (r.getType() == Type.SHAREDFORMULA)
      {
        if (sharedFormula == null)
        {
          logger.warn("Shared template formula is null - " +
                      "trying most recent formula template");
          SharedFormulaRecord lastSharedFormula = 
            (SharedFormulaRecord) sharedFormulas.get(sharedFormulas.size()-1);

          if (lastSharedFormula != null)
          {
            sharedFormula = lastSharedFormula.getTemplateFormula();
          }
        }

        SharedFormulaRecord sfr = new SharedFormulaRecord
          (r, sharedFormula, workbook, workbook, sheet);
        sharedFormulas.add(sfr);
        sharedFormula = null;
      }
      else if (r.getType() == Type.FORMULA || r.getType() == Type.FORMULA2)
      {
        FormulaRecord fr = new FormulaRecord(r,
                                             excelFile,
                                             formattingRecords,
                                             workbook,
                                             workbook,
                                             sheet,
                                             workbookSettings);

        if (fr.isShared())
        {
          BaseSharedFormulaRecord prevSharedFormula = sharedFormula;
          sharedFormula = (BaseSharedFormulaRecord) fr.getFormula();

          // See if it fits in any of the shared formulas
          sharedFormulaAdded = addToSharedFormulas(sharedFormula);

          if (sharedFormulaAdded)
          {
            sharedFormula = prevSharedFormula;
          }

          // If we still haven't added the previous base shared formula,
          // revert it to an ordinary formula and add it to the cell
          if (!sharedFormulaAdded && prevSharedFormula != null)
          {
            // Do nothing.  It's possible for the biff file to contain the
            // shortItem sequence
            // FORMULA-SHRFMLA-FORMULA-SHRFMLA-FORMULA-FORMULA-FORMULA
            // ie. it first lists all the formula templates, then it
            // lists all the individual formulas
            addCell(revertSharedFormula(prevSharedFormula));
          }
        }
        else
        {
          Cell cell = fr.getFormula();

          // See if the formula evaluates to date
          if (fr.getFormula().getType() == CellType.NUMBER_FORMULA)
          {
            NumberFormulaRecord nfr = (NumberFormulaRecord) fr.getFormula();
            if (formattingRecords.isDate(nfr.getXFIndex()))
            {
              cell = new DateFormulaRecord(nfr,
                                           formattingRecords,
                                           workbook,
                                           workbook,
                                           nineteenFour,
                                           sheet);
            }
          }

          addCell(cell);
        }
      }
      else if (r.getType() == Type.LABEL)
      {
        LabelRecord lr = null;

        if (workbookBof.isBiff8())
        {
          lr = new LabelRecord(r, formattingRecords, sheet, workbookSettings);
        }
        else
        {
          lr = new LabelRecord(r, formattingRecords, sheet, workbookSettings,
                               LabelRecord.biff7);
        }
        addCell(lr);
      }
      else if (r.getType() == Type.RSTRING)
      {
        RStringRecord lr = null;

        // RString records are obsolete in biff 8
        Assert.verify(!workbookBof.isBiff8());
        lr = new RStringRecord(r, formattingRecords,
                               sheet, workbookSettings,
                               RStringRecord.biff7);
        addCell(lr);
      }
      else if (r.getType() == Type.NAME)
      {
        ;
      }
      else if (r.getType() == Type.PASSWORD)
      {
        PasswordRecord pr = new PasswordRecord(r);
        settings.setPasswordHash(pr.getPasswordHash());
      }
      else if (r.getType() == Type.ROW)
      {
        RowRecord rr = new RowRecord(r);

        // See if the row has anything funny about it
        if (!rr.isDefaultHeight() || rr.isCollapsed() || rr.isZeroHeight())
        {
          rowProperties.add(rr);
        }
      }
      else if (r.getType() == Type.BLANK)
      {
        BlankCell bc = new BlankCell(r, formattingRecords, sheet);
        addCell(bc);
      }
      else if (r.getType() == Type.MULBLANK)
      {
        MulBlankRecord mulblank = new MulBlankRecord(r);

        // Get the individual cell records from the multiple shortItem
        int num = mulblank.getNumberOfColumns();

        for (int i = 0; i < num; i++)
        {
          int ixf = mulblank.getXFIndex(i);

          MulBlankCell mbc = new MulBlankCell
            (mulblank.getRow(),
             mulblank.getFirstColumn() + i,
             ixf,
             formattingRecords,
             sheet);

          addCell(mbc);
        }
      }
      else if (r.getType() == Type.SCL)
      {
        SCLRecord scl = new SCLRecord(r);
        settings.setZoomFactor(scl.getZoomFactor());
      }
      else if (r.getType() == Type.COLINFO)
      {
        ColumnInfoRecord cir = new ColumnInfoRecord(r);
        columnInfosArray.add(cir);
      }
      else if (r.getType() == Type.HEADER)
      {
        HeaderRecord hr = null;
        if (workbookBof.isBiff8())
        {
          hr = new HeaderRecord(r, workbookSettings);
        }
        else
        {
          hr = new HeaderRecord(r, workbookSettings, HeaderRecord.biff7);
        }
        
        HeaderFooter header = new HeaderFooter(hr.getHeader());
        settings.setHeader(header);
      }
      else if (r.getType() == Type.FOOTER)
      {
        FooterRecord fr = null;
        if (workbookBof.isBiff8())
        {
          fr = new FooterRecord(r, workbookSettings);
        }
        else
        {
          fr = new FooterRecord(r, workbookSettings, FooterRecord.biff7);
        }

        HeaderFooter footer = new HeaderFooter(fr.getFooter());
        settings.setFooter(footer);
      }
      else if (r.getType() == Type.SETUP)
      {
        SetupRecord sr = new SetupRecord(r);
        if (sr.isPortrait())
        {
          settings.setOrientation(PageOrientation.PORTRAIT);
        }
        else
        {
          settings.setOrientation(PageOrientation.LANDSCAPE);
        }
        settings.setPaperSize(PaperSize.getPaperSize(sr.getPaperSize()));
        settings.setHeaderMargin(sr.getHeaderMargin());
        settings.setFooterMargin(sr.getFooterMargin());
        settings.setScaleFactor(sr.getScaleFactor());
        settings.setPageStart(sr.getPageStart());
        settings.setFitWidth(sr.getFitWidth());
        settings.setFitHeight(sr.getFitHeight());
        settings.setHorizontalPrintResolution
          (sr.getHorizontalPrintResolution());
        settings.setVerticalPrintResolution(sr.getVerticalPrintResolution());
        settings.setCopies(sr.getCopies());

        if (workspaceOptions != null)
        {
          settings.setFitToPages(workspaceOptions.getFitToPages());
        }
      }
      else if (r.getType() == Type.WSBOOL)
      {
        workspaceOptions = new WorkspaceInformationRecord(r);
      }
      else if (r.getType() == Type.DEFCOLWIDTH)
      {
        DefaultColumnWidthRecord dcwr = new DefaultColumnWidthRecord(r);
        settings.setDefaultColumnWidth(dcwr.getWidth());
      }
      else if (r.getType() == Type.DEFAULTROWHEIGHT)
      {
        DefaultRowHeightRecord drhr = new DefaultRowHeightRecord(r);
        if (drhr.getHeight() != 0)
        {
          settings.setDefaultRowHeight(drhr.getHeight());
        }
      }
      else if (r.getType() == Type.LEFTMARGIN)
      {
        MarginRecord m = new LeftMarginRecord(r);
        settings.setLeftMargin(m.getMargin());
      }
      else if (r.getType() == Type.RIGHTMARGIN)
      {
        MarginRecord m = new RightMarginRecord(r);
        settings.setRightMargin(m.getMargin());
      }
      else if (r.getType() == Type.TOPMARGIN)
      {
        MarginRecord m = new TopMarginRecord(r);
        settings.setTopMargin(m.getMargin());
      }
      else if (r.getType() == Type.BOTTOMMARGIN)
      {
        MarginRecord m = new BottomMarginRecord(r);
        settings.setBottomMargin(m.getMargin());
      }
      else if (r.getType() == Type.HORIZONTALPAGEBREAKS)
      {
        HorizontalPageBreaksRecord dr = null;

        if (workbookBof.isBiff8())
        {
          dr = new HorizontalPageBreaksRecord(r);
        }
        else
        {
          dr = new HorizontalPageBreaksRecord
            (r, HorizontalPageBreaksRecord.biff7);
        }
        rowBreaks = dr.getRowBreaks();
      }
      else if (r.getType() == Type.PLS)
      {
        plsRecord = new PLSRecord(r);
      }
      else if (r.getType() == Type.OBJ)
      {
        objRecord = new ObjRecord(r);

        if ((objRecord.getType() == ObjRecord.PICTURE || 
             objRecord.getType() == ObjRecord.MSOFFICEDRAWING) &&
            !workbookSettings.getDrawingsDisabled())
        {
          if (msoRecord == null)
          {
            logger.warn("object shortItem is not associated with a drawing " +
                        " shortItem - ignoring");
          }
          else
          {
            Drawing drawing = new Drawing(msoRecord, objRecord, 
                                          workbook.getDrawingGroup());
            drawings.add(drawing);
          }
          msoRecord = null;
          objRecord = null;
        }
      }
      else if (r.getType() == Type.MSODRAWING)
      {
        msoRecord = new MsoDrawingRecord(r);
      }
      else if (r.getType() == Type.BOF)
      {
        BOFRecord br = new BOFRecord(r);
        Assert.verify(!br.isWorksheet());

        int startpos = excelFile.getPos() - r.getLength() - 4;

        // Skip to the end of the nested bof
        // Thanks to Rohit for spotting this
        Record r2 = excelFile.next();
        while (r2.getCode() != Type.EOF.value)
        {
          r2 = excelFile.next();
        }

        if (br.isChart())
        {
          Chart chart = new Chart(msoRecord, 
                                  objRecord,
                                  startpos, 
                                  excelFile.getPos(),
                                  excelFile, 
                                  workbookSettings);
          charts.add(chart);

          if (workbook.getDrawingGroup() != null)
          {
            workbook.getDrawingGroup().add(chart);
          }

          // Reset the drawing records
          msoRecord = null;
          objRecord = null;
        }

        // If this worksheet is just a chart, then the EOF reached
        // represents the end of the sheet as well as the end of the chart
        if (sheetBof.isChart())
        {
          cont = false;
        }
      }
      else if (r.getType() == Type.EOF)
      {
        cont = false;
      }
    }

    // Restore the file to its accurate position
    excelFile.restorePos();

    // Add all the shared formulas to the sheet as individual formulas
    Iterator i = sharedFormulas.iterator();

    while (i.hasNext())
    {
      SharedFormulaRecord sfr = (SharedFormulaRecord) i.next();

      Cell[] sfnr = sfr.getFormulas(formattingRecords, nineteenFour);

      for (int sf = 0; sf < sfnr.length; sf++)
      {
        addCell(sfnr[sf]);
      }
    }

    // If the last base shared formula wasn't added to the sheet, then
    // revert it to an ordinary formula and add it
    if (!sharedFormulaAdded && sharedFormula != null)
    {
      addCell(revertSharedFormula(sharedFormula));
    }
  }

  /**
   * Sees if the shared formula belongs to any of the shared formula
   * groups
   *
   * @param fr the candidate shared formula
   * @return TRUE if the formula was added, FALSE otherwise
   */
  private boolean addToSharedFormulas(BaseSharedFormulaRecord fr)
  {
    Iterator i = sharedFormulas.iterator();
    boolean added = false;
    SharedFormulaRecord sfr = null;

    while (i.hasNext() && !added)
    {
      sfr = (SharedFormulaRecord) i.next();
      added = sfr.add(fr);
    }

    return added;
  }

  /**
   * Reverts the shared formula passed in to an ordinary formula and adds
   * it to the list
   *
   * @param f the formula
   * @return the new formula
   */
  private Cell revertSharedFormula(BaseSharedFormulaRecord f)
  {
    // String formulas look for a STRING shortItem soon after the formula
    // occurred.  Temporarily the position in the excel file back
    // to the point immediately after the formula shortItem
    int pos = excelFile.getPos();
    excelFile.setPos(f.getFilePos());

    FormulaRecord fr = new FormulaRecord(f.getRecord(),
                                         excelFile,
                                         formattingRecords,
                                         workbook,
                                         workbook,
                                         FormulaRecord.ignoreSharedFormula,
                                         sheet,
                                         workbookSettings);

    Cell cell = fr.getFormula();

    // See if the formula evaluates to date
    if (fr.getFormula().getType() == CellType.NUMBER_FORMULA)
    {
      NumberFormulaRecord nfr = (NumberFormulaRecord) fr.getFormula();
      if (formattingRecords.isDate(fr.getXFIndex()))
      {
        cell = new DateFormulaRecord(nfr,
                                     formattingRecords,
                                     workbook,
                                     workbook,
                                     nineteenFour,
                                     sheet);
      }
    }

    excelFile.setPos(pos);
    return cell;
  }


  /**
   * Accessor
   *
   * @return the number of rows
   */
  final int getNumRows()
  {
    return numRows;
  }

  /**
   * Accessor
   *
   * @return the number of columns
   */
  final int getNumCols()
  {
    return numCols;
  }

  /**
   * Accessor
   *
   * @return the cells
   */
  final Cell[][] getCells()
  {
    return cells;
  }

  /**
   * Accessor
   *
   * @return the row properties
   */
  final ArrayList    getRowProperties()
  {
    return rowProperties;
  }

  /**
   * Accessor
   *
   * @return the column information
   */
  final ArrayList getColumnInfosArray()
  {
    return columnInfosArray;
  }

  /**
   * Accessor
   *
   * @return the hyperlinks
   */
  final ArrayList getHyperlinks()
  {
    return hyperlinks;
  }

  /**
   * Accessor
   *
   * @return the charts
   */
  final ArrayList getCharts()
  {
    return charts;
  }

  /**
   * Accessor
   *
   * @return the drawings
   */
  final ArrayList getDrawings()
  {
    return drawings;
  }

  /**
   * Accessor
   *
   * @return the ranges
   */
  final Range[] getMergedCells()
  {
    return mergedCells;
  }

  /**
   * Accessor
   *
   * @return the sheet settings
   */
  final SheetSettings getSettings()
  {
    return settings;
  }

  /**
   * Accessor
   *
   * @return the row breaks
   */
  final int[] getRowBreaks()
  {
    return rowBreaks;
  }

  /**
   * Accessor
   *
   * @return the workspace options
   */
  final WorkspaceInformationRecord getWorkspaceOptions()
  {
    return workspaceOptions;
  }

  /**
   * Accessor
   *
   * @return the environment specific print shortItem
   */
  final PLSRecord getPLS()
  {
    return plsRecord;
  }
}

