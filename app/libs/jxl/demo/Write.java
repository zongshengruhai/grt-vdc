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

package jxl.demo;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.net.MalformedURLException;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.Range;
import jxl.CellView;
import jxl.CellReferenceHelper;
import jxl.HeaderFooter;
import jxl.write.WritableWorkbook;
import jxl.write.WritableSheet;
import jxl.write.WritableFont;
import jxl.write.WritableCellFormat;
import jxl.write.NumberFormats;
import jxl.write.DateFormats;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.DateTime;
import jxl.write.NumberFormat;
import jxl.write.DateFormat;
import jxl.write.WriteException;
import jxl.write.WritableHyperlink;
import jxl.write.Boolean;
import jxl.write.Formula;
import jxl.write.WritableImage;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.ScriptStyle;
import jxl.format.Orientation;
import jxl.format.PageOrientation;
import jxl.format.PaperSize;

/**
 * Demo class which writes a spreadsheet.  This demo illustrates most of the
 * features of the JExcelAPI, such as text, numbers, fonts, number formats and
 * date formats
 */
public class Write
{
  /**
   * The filename
   */
  private String filename;

  /**
   * The workbook
   */
  private WritableWorkbook workbook;

  /**
   * Constructor
   * 
   * @param fn 
   */
  public Write(String fn)
  {
    filename = fn;
  }

  /**
   * Uses the JExcelAPI to create a spreadsheet
   * 
   * @exception IOException
   * @exception WriteException
   */
  public void write() throws IOException, WriteException
  {
    WorkbookSettings ws = new WorkbookSettings();
    ws.setLocale(new Locale("en", "EN"));
    workbook = Workbook.createWorkbook(new File(filename), ws);


    WritableSheet s2 = workbook.createSheet("Number Formats", 0);
    WritableSheet s3 = workbook.createSheet("Date Formats", 1);
    WritableSheet s1 = workbook.createSheet("Label Formats", 2);
    WritableSheet s4 = workbook.createSheet("Borders", 3);
    WritableSheet s5 = workbook.createSheet("Labels", 4);
    WritableSheet s6 = workbook.createSheet("Formulas", 5);
    WritableSheet s7 = workbook.createSheet("Images", 6);

    writeLabelFormatSheet(s1);
    writeNumberFormatSheet(s2);
    writeDateFormatSheet(s3);
    writeBordersSheet(s4);
    writeLabelsSheet(s5);
    writeFormulaSheet(s6);
    writeImageSheet(s7);

    // Modify the colour palette to bright red for the lime colour
    workbook.setColourRGB(Colour.LIME, 0xff, 0, 0);

    workbook.write();
    workbook.close();
  }
  
  /**
   * Writes out a sheet containing the various numerical formats
   * 
   * @param s 
   */
  private void writeNumberFormatSheet(WritableSheet s) throws WriteException
  {
    WritableCellFormat wrappedText = new WritableCellFormat
      (WritableWorkbook.ARIAL_10_PT);
    wrappedText.setWrap(true);

    s.setColumnView(0,20);
    s.setColumnView(4,20);
    s.setColumnView(5,20);
    s.setColumnView(6,20);

    // Floats
    Label l = new Label(0,0,"+/- Pi - default format", wrappedText);
    s.addCell(l);

    Number n = new Number(1,0,3.1415926535);
    s.addCell(n);

    n = new Number(2,0,-3.1415926535);
    s.addCell(n);

    l = new Label(0,1,"+/- Pi - integer format", wrappedText);
    s.addCell(l);

    WritableCellFormat cf1 = new WritableCellFormat(NumberFormats.INTEGER);
    n = new Number(1,1,3.1415926535,cf1);
    s.addCell(n);

    n = new Number(2,1,-3.1415926535, cf1);
    s.addCell(n);

    l = new Label(0,2,"+/- Pi - float 2dps", wrappedText);
    s.addCell(l);

    WritableCellFormat cf2 = new WritableCellFormat(NumberFormats.FLOAT);
    n = new Number(1,2,3.1415926535,cf2);
    s.addCell(n);

    n = new Number(2,2,-3.1415926535, cf2);
    s.addCell(n);

    l = new Label(0,3,"+/- Pi - custom 3dps", 
                  wrappedText);
    s.addCell(l);

    NumberFormat dp3 = new NumberFormat("#.###");
    WritableCellFormat dp3cell = new WritableCellFormat(dp3);
    n = new Number(1,3,3.1415926535,dp3cell);
    s.addCell(n);

    n = new Number(2,3,-3.1415926535, dp3cell);
    s.addCell(n);

    l = new Label(0,4,"+/- Pi - custom &3.14", 
                  wrappedText);
    s.addCell(l);

    NumberFormat pounddp2 = new NumberFormat("&#.00");
    WritableCellFormat pounddp2cell = new WritableCellFormat(pounddp2);
    n = new Number(1,4,3.1415926535,pounddp2cell);
    s.addCell(n);

    n = new Number(2,4,-3.1415926535, pounddp2cell);
    s.addCell(n);

    l = new Label(0,5,"+/- Pi - custom Text #.### Text", 
                  wrappedText);
    s.addCell(l);

    NumberFormat textdp4 = new NumberFormat("Text#.####Text");
    WritableCellFormat textdp4cell = new WritableCellFormat(textdp4);
    n = new Number(1,5,3.1415926535, textdp4cell);
    s.addCell(n);

    n = new Number(2,5,-3.1415926535, textdp4cell);
    s.addCell(n);

    // Integers
    l = new Label(4,0,"+/- Bilko default format");
    s.addCell(l);
    n = new Number(5, 0, 15042699);
    s.addCell(n);
    n = new Number(6, 0, -15042699);
    s.addCell(n);

    l = new Label(4,1,"+/- Bilko float format");
    s.addCell(l);
    WritableCellFormat cfi1 = new WritableCellFormat(NumberFormats.FLOAT);
    n = new Number(5, 1, 15042699, cfi1);
    s.addCell(n);
    n = new Number(6, 1, -15042699, cfi1);
    s.addCell(n);

    l = new Label(4,2,"+/- Thousands separator");
    s.addCell(l);
    WritableCellFormat cfi2 = new WritableCellFormat
      (NumberFormats.THOUSANDS_INTEGER);
    n = new Number(5, 2, 15042699,cfi2 );
    s.addCell(n);
    n = new Number(6, 2, -15042699, cfi2);
    s.addCell(n);

    l = new Label(4,3,"+/- Accounting red - added 0.01");
    s.addCell(l);
    WritableCellFormat cfi3 = new WritableCellFormat
      (NumberFormats.ACCOUNTING_RED_FLOAT);
    n = new Number(5, 3, 15042699.01, cfi3);
    s.addCell(n);
    n = new Number(6, 3, -15042699.01, cfi3);
    s.addCell(n);

    l = new Label(4,4,"+/- Percent");
    s.addCell(l);
    WritableCellFormat cfi4 = new WritableCellFormat
      (NumberFormats.PERCENT_INTEGER);
    n = new Number(5, 4, 15042699, cfi4);
    s.addCell(n);
    n = new Number(6, 4, -15042699, cfi4);
    s.addCell(n);

    l = new Label(4,5,"+/- Exponential - 2dps");
    s.addCell(l);
    WritableCellFormat cfi5 = new WritableCellFormat
      (NumberFormats.EXPONENTIAL);
    n = new Number(5, 5, 15042699, cfi5);
    s.addCell(n);
    n = new Number(6, 5, -15042699, cfi5);
    s.addCell(n);

    l = new Label(4,6,"+/- Custom exponentional - 3dps", wrappedText);
    s.addCell(l);
    NumberFormat edp3 = new NumberFormat("0.000E0");
    WritableCellFormat edp3Cell = new WritableCellFormat(edp3);
    n = new Number(5,6,15042699,edp3Cell);
    s.addCell(n);
    n = new Number(6,6,-15042699,edp3Cell);
    s.addCell(n);

    l = new Label(4, 7, "Custom neg brackets", wrappedText);
    s.addCell(l);
    NumberFormat negbracks = new NumberFormat("#,##0;(#,##0)");
    WritableCellFormat negbrackscell = new WritableCellFormat(negbracks);
    n = new Number(5,7, 15042699, negbrackscell);
    s.addCell(n);
    n = new Number(6,7, -15042699, negbrackscell);
    s.addCell(n);

    l = new Label(4, 8, "Custom neg brackets 2", wrappedText);
    s.addCell(l);
    NumberFormat negbracks2 = new NumberFormat("#,##0;(#,##0)a");
    WritableCellFormat negbrackscell2 = new WritableCellFormat(negbracks2);
    n = new Number(5,8, 15042699, negbrackscell2);
    s.addCell(n);
    n = new Number(6,8, -15042699, negbrackscell2);
    s.addCell(n);

    l = new Label(4, 9, "Custom percent", wrappedText);
    s.addCell(l);
    NumberFormat cuspercent = new NumberFormat("0.0%");
    WritableCellFormat cuspercentf = new WritableCellFormat(cuspercent);
    n = new Number(5, 9, 3.14159265, cuspercentf);
    s.addCell(n);
    

    // Booleans
    l = new Label(0,10, "Boolean - TRUE");
    s.addCell(l);
    Boolean b = new Boolean(1,10, true);
    s.addCell(b);

    l = new Label(0,11, "Boolean - FALSE");
    s.addCell(l);
    b = new Boolean(1,11,false);
    s.addCell(b);

    l = new Label(0, 12, "A hidden cell->");
    s.addCell(l);
    n = new Number(1, 12, 17, WritableWorkbook.HIDDEN_STYLE);
    s.addCell(n);


    // Lots of numbers
    for (int row = 0; row < 100; row++)
    {
      for (int col = 8; col < 108; col++)
      {
        n = new Number(col, row, col+row);
        s.addCell(n);
      }
    }

    // Lots of numbers
    for (int row = 101; row < 3000; row++)
    {
      for (int col = 0; col < 25; col++)
      {
        n = new Number(col, row, col+row);
        s.addCell(n);
      }
    }
  }

  /**
   * Adds cells to the specified sheet which test the various date formats
   * 
   * @param s 
   */
  private void writeDateFormatSheet(WritableSheet s) throws WriteException
  {
    WritableCellFormat wrappedText = new WritableCellFormat
      (WritableWorkbook.ARIAL_10_PT);
    wrappedText.setWrap(true);

    s.setColumnView(0, 20);
    s.setColumnView(2, 20);
    s.setColumnView(3, 20);

    s.getSettings().setFitWidth(2);
    s.getSettings().setFitHeight(2);

    Calendar c = Calendar.getInstance();
    c.set(1975, 4, 31, 15, 21, 45);
    c.set(Calendar.MILLISECOND, 660);
    Date date = c.getTime();
    c.set(1900, 0, 1, 0, 0, 0);
    c.set(Calendar.MILLISECOND, 0);

    Date date2 = c.getTime();
    Date date3 = new Date(0);
    c.set(1918, 10, 11, 10, 0, 0);
    Date date4 = c.getTime();
    c.set(1900, 0, 2, 0, 0, 0);
    Date date5 = c.getTime();
    c.set(1901, 0, 1, 0, 0, 0);
    Date date6 = c.getTime();
    c.set(1900, 4, 31, 0, 0, 0);
    Date date7 = c.getTime();
    c.set(1900, 1, 1, 0, 0, 0);
    Date date8 = c.getTime();
    c.set(1900, 0, 31, 0, 0, 0);
    Date date9 = c.getTime();
    c.set(1900, 2, 1, 0, 0, 0);
    Date date10 = c.getTime();
    c.set(1900, 1, 27, 0, 0, 0);
    Date date11 = c.getTime();
    c.set(1900, 1, 28, 0, 0, 0);
    Date date12 = c.getTime();

    // Build in date formats
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
    Label l = new Label(0,0,"All dates are " + sdf.format(date),
                        wrappedText);
    s.addCell(l);

    l = new Label(0,1,"Built in formats", 
                  wrappedText);
    s.addCell(l);

    l = new Label(2, 1, "Custom formats");
    s.addCell(l);

    WritableCellFormat cf1 = new WritableCellFormat(DateFormats.FORMAT1);
    DateTime dt = new DateTime(0,2,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT2);
    dt = new DateTime(0,3,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT3);
    dt = new DateTime(0,4,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT4);
    dt = new DateTime(0,5,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT5);
    dt = new DateTime(0,6,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT6);
    dt = new DateTime(0,7,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT7);
    dt = new DateTime(0,8,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT8);
    dt = new DateTime(0,9,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(0,10,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT10);
    dt = new DateTime(0,11,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT11);
    dt = new DateTime(0,12,date, cf1);
    s.addCell(dt);

    cf1 = new WritableCellFormat(DateFormats.FORMAT12);
    dt = new DateTime(0,13,date, cf1);
    s.addCell(dt);

    // Custom formats
    DateFormat df = new DateFormat("dd MM yyyy");
    cf1 = new WritableCellFormat(df);
    l = new Label(2, 2, "dd MM yyyy");
    s.addCell(l);

    dt = new DateTime(3, 2, date, cf1);
    s.addCell(dt);

    df = new DateFormat("dd MMM yyyy");
    cf1 = new WritableCellFormat(df);
    l = new Label(2, 3, "dd MMM yyyy");
    s.addCell(l);

    dt = new DateTime(3, 3, date, cf1);
    s.addCell(dt);
 
    df = new DateFormat("hh:mm");
    cf1 = new WritableCellFormat(df);
    l = new Label(2, 4, "hh:mm");
    s.addCell(l);

    dt = new DateTime(3, 4, date, cf1);
    s.addCell(dt);

    df = new DateFormat("hh:mm:ss");
    cf1 = new WritableCellFormat(df);
    l = new Label(2, 5, "hh:mm:ss");
    s.addCell(l);

    dt = new DateTime(3, 5, date, cf1);
    s.addCell(dt);

    df = new DateFormat("H:mm:ss a");
    cf1 = new WritableCellFormat(df);
    l = new Label(2, 5, "H:mm:ss a");
    s.addCell(l);

    dt = new DateTime(3, 5, date, cf1);
    s.addCell(dt);

    df = new DateFormat("mm:ss.SSS");
    cf1 = new WritableCellFormat(df);
    l = new Label(2, 6, "mm:ss.SSS");
    s.addCell(l);

    dt = new DateTime(3, 6, date, cf1);
    s.addCell(dt);

    // Check out the zero date ie. 1 Jan 1900
    l = new Label(0,16,"Zero date " + sdf.format(date2),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(0,17,date2, cf1);
    s.addCell(dt);

    // Check out the zero date + 1 ie. 2 Jan 1900
    l = new Label(3,16,"Zero date + 1 " + sdf.format(date5),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,17,date5, cf1);
    s.addCell(dt);

    // Check out the 1 Jan 1901
    l = new Label(3,19, sdf.format(date6),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,20,date6, cf1);
    s.addCell(dt);

    // Check out the 31 May 1900
    l = new Label(3,22, sdf.format(date7),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,23, date7, cf1);
    s.addCell(dt);

    // Check out 1 Feb 1900
    l = new Label(3,25, sdf.format(date8),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,26, date8, cf1);
    s.addCell(dt);

    // Check out 31 Jan 1900
    l = new Label(3,28, sdf.format(date9),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,29, date9, cf1);
    s.addCell(dt);

    // Check out 31 Jan 1900
    l = new Label(3,28, sdf.format(date9),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,29, date9, cf1);
    s.addCell(dt);

    // Check out 1 Mar 1900
    l = new Label(3,31, sdf.format(date10),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,32, date10, cf1);
    s.addCell(dt);

    // Check out 27 Feb 1900
    l = new Label(3,34, sdf.format(date11),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,35, date11, cf1);
    s.addCell(dt);

    // Check out 28 Feb 1900
    l = new Label(3,37, sdf.format(date12),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(3,38, date12, cf1);
    s.addCell(dt);

    // Check out the zero date ie. 1 Jan 1970
    l = new Label(0,19,"Zero UTC date " + sdf.format(date3),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(0,20,date3, cf1);
    s.addCell(dt);

    // Check out the WWI armistice day ie. 11 am, Nov 11, 1918
    l = new Label(0,22,"Armistice date " + sdf.format(date4),
                  wrappedText);
    s.addCell(l);

    cf1 = new WritableCellFormat(DateFormats.FORMAT9);
    dt = new DateTime(0,23,date4, cf1);
    s.addCell(dt);
  }

  /**
   * Adds cells to the specified sheet which test the various label formatting
   * styles, such as different fonts, different sizes and bold, underline etc.
   * 
   * @param s1 
   */
  private void writeLabelFormatSheet(WritableSheet s1) throws WriteException
  {
    s1.setColumnView(0, 60);

    Label lr = new Label(0,0, "Arial Fonts");
    s1.addCell(lr);

    lr = new Label(1,0, "10pt");
    s1.addCell(lr);

    lr = new Label(2, 0, "Normal");
    s1.addCell(lr);

    lr = new Label(3, 0, "12pt");
    s1.addCell(lr);

    WritableFont arial12pt = new WritableFont(WritableFont.ARIAL, 12);
    WritableCellFormat arial12format = new WritableCellFormat(arial12pt);
    arial12format.setWrap(true);
    lr = new Label(4, 0, "Normal", arial12format);
    s1.addCell(lr);

    WritableFont arial10ptBold = new WritableFont
      (WritableFont.ARIAL, 10, WritableFont.BOLD);
    WritableCellFormat arial10BoldFormat = new WritableCellFormat
      (arial10ptBold);
    lr = new Label(2, 2, "BOLD", arial10BoldFormat);
    s1.addCell(lr);

    WritableFont arial12ptBold = new WritableFont
      (WritableFont.ARIAL, 12, WritableFont.BOLD);
    WritableCellFormat arial12BoldFormat = new WritableCellFormat
      (arial12ptBold);
    lr = new Label(4, 2, "BOLD", arial12BoldFormat);
    s1.addCell(lr);

    WritableFont arial10ptItalic = new WritableFont
      (WritableFont.ARIAL, 10, WritableFont.NO_BOLD, true);
    WritableCellFormat arial10ItalicFormat = new WritableCellFormat
      (arial10ptItalic);
    lr = new Label(2, 4, "Italic", arial10ItalicFormat);
    s1.addCell(lr);

    WritableFont arial12ptItalic = new WritableFont
      (WritableFont.ARIAL, 12, WritableFont.NO_BOLD, true);
    WritableCellFormat arial12ptItalicFormat = new WritableCellFormat
      (arial12ptItalic);
    lr = new Label(4, 4, "Italic", arial12ptItalicFormat);
    s1.addCell(lr);

    WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
    WritableCellFormat times10format = new WritableCellFormat(times10pt);
    lr = new Label(0, 7, "Times Fonts", times10format);
    s1.addCell(lr);

    lr = new Label(1, 7, "10pt", times10format);
    s1.addCell(lr);

    lr = new Label(2, 7, "Normal", times10format);
    s1.addCell(lr);

    lr = new Label(3, 7, "12pt", times10format);
    s1.addCell(lr);

    WritableFont times12pt = new WritableFont(WritableFont.TIMES, 12);
    WritableCellFormat times12format = new WritableCellFormat(times12pt);
    lr = new Label(4, 7, "Normal", times12format);
    s1.addCell(lr);

    WritableFont times10ptBold = new WritableFont
      (WritableFont.TIMES, 10, WritableFont.BOLD);
    WritableCellFormat times10BoldFormat = new WritableCellFormat
      (times10ptBold);
    lr = new Label(2, 9, "BOLD", times10BoldFormat);
    s1.addCell(lr);

    WritableFont times12ptBold = new WritableFont
      (WritableFont.TIMES, 12, WritableFont.BOLD);
    WritableCellFormat times12BoldFormat = new WritableCellFormat
      (times12ptBold);
    lr = new Label(4, 9, "BOLD", times12BoldFormat);
    s1.addCell(lr);

    // The underline styles
    s1.setColumnView(6, 22);
    s1.setColumnView(7, 22);
    s1.setColumnView(8, 22);
    s1.setColumnView(9, 22);

    lr = new Label(0, 11, "Underlining");
    s1.addCell(lr);

    WritableFont arial10ptUnderline = new WritableFont
      (WritableFont.ARIAL, 
       WritableFont.DEFAULT_POINT_SIZE,
       WritableFont.NO_BOLD,
       false,
       UnderlineStyle.SINGLE);
    WritableCellFormat arialUnderline = new WritableCellFormat
      (arial10ptUnderline);
    lr = new Label(6,11, "Underline", arialUnderline);
    s1.addCell(lr);

    WritableFont arial10ptDoubleUnderline = new WritableFont
      (WritableFont.ARIAL, 
       WritableFont.DEFAULT_POINT_SIZE,
       WritableFont.NO_BOLD,
       false,
       UnderlineStyle.DOUBLE);
    WritableCellFormat arialDoubleUnderline = new WritableCellFormat
      (arial10ptDoubleUnderline);
    lr = new Label(7,11, "Double Underline", arialDoubleUnderline);
    s1.addCell(lr);

    WritableFont arial10ptSingleAcc = new WritableFont
      (WritableFont.ARIAL, 
       WritableFont.DEFAULT_POINT_SIZE,
       WritableFont.NO_BOLD,
       false,
       UnderlineStyle.SINGLE_ACCOUNTING);
    WritableCellFormat arialSingleAcc = new WritableCellFormat
      (arial10ptSingleAcc);
    lr = new Label(8,11, "Single Accounting Underline", arialSingleAcc);
    s1.addCell(lr);

    WritableFont arial10ptDoubleAcc = new WritableFont
      (WritableFont.ARIAL, 
       WritableFont.DEFAULT_POINT_SIZE,
       WritableFont.NO_BOLD,
       false,
       UnderlineStyle.DOUBLE_ACCOUNTING);
    WritableCellFormat arialDoubleAcc = new WritableCellFormat
      (arial10ptDoubleAcc);
    lr = new Label(9,11, "Double Accounting Underline", arialDoubleAcc);
    s1.addCell(lr);

    WritableFont times14ptBoldUnderline = new WritableFont
      (WritableFont.TIMES,
       14,
       WritableFont.BOLD,
       false,
       UnderlineStyle.SINGLE);
    WritableCellFormat timesBoldUnderline = new WritableCellFormat
      (times14ptBoldUnderline);
    lr = new Label(6,12, "Times 14 Bold Underline", timesBoldUnderline);
    s1.addCell(lr);

    WritableFont arial18ptBoldItalicUnderline = new WritableFont
      (WritableFont.ARIAL,
       18,
       WritableFont.BOLD,
       true,
       UnderlineStyle.SINGLE);
    WritableCellFormat arialBoldItalicUnderline = new WritableCellFormat
      (arial18ptBoldItalicUnderline);
    lr = new Label(6,13, "Arial 18 Bold Italic Underline", 
                   arialBoldItalicUnderline);
    s1.addCell(lr);

    lr = new Label(0, 15, "Script styles");
    s1.addCell(lr);

    WritableFont superscript = new WritableFont
      (WritableFont.ARIAL,
       WritableFont.DEFAULT_POINT_SIZE,
       WritableFont.NO_BOLD,
       false,
       UnderlineStyle.NO_UNDERLINE,
       Colour.BLACK,
       ScriptStyle.SUPERSCRIPT);
    WritableCellFormat superscriptFormat = new WritableCellFormat
      (superscript);
    lr = new Label(1,15, "superscript", superscriptFormat);
    s1.addCell(lr);

    WritableFont subscript = new WritableFont
      (WritableFont.ARIAL,
       WritableFont.DEFAULT_POINT_SIZE,
       WritableFont.NO_BOLD,
       false,
       UnderlineStyle.NO_UNDERLINE,
       Colour.BLACK,
       ScriptStyle.SUBSCRIPT);
    WritableCellFormat subscriptFormat = new WritableCellFormat
      (subscript);
    lr = new Label(2,15, "subscript", subscriptFormat);
    s1.addCell(lr);

    lr = new Label(0, 17, "Colours");
    s1.addCell(lr);

    WritableFont red = new WritableFont(WritableFont.ARIAL, 
                                        WritableFont.DEFAULT_POINT_SIZE,
                                        WritableFont.NO_BOLD,
                                        false,
                                        UnderlineStyle.NO_UNDERLINE,
                                        Colour.RED);
    WritableCellFormat redFormat = new WritableCellFormat(red);
    lr = new Label(2, 17, "Red", redFormat);
    s1.addCell(lr);

    WritableFont blue = new WritableFont(WritableFont.ARIAL, 
                                         WritableFont.DEFAULT_POINT_SIZE,
                                         WritableFont.NO_BOLD,
                                         false,
                                         UnderlineStyle.NO_UNDERLINE,
                                         Colour.BLUE);
    WritableCellFormat blueFormat = new WritableCellFormat(blue);
    lr = new Label(2, 18, "Blue", blueFormat);
    s1.addCell(lr);

    WritableFont lime = new WritableFont(WritableFont.ARIAL);
    lime.setColour(Colour.LIME);
    WritableCellFormat limeFormat = new WritableCellFormat(lime);
    limeFormat.setWrap(true);
    lr = new Label(4, 18, "Modified palette - was lime, now red", limeFormat);
    s1.addCell(lr);
    
    WritableCellFormat greyBackground = new WritableCellFormat();
    greyBackground.setWrap(true);
    greyBackground.setBackground(Colour.GRAY_50);
    lr = new Label(2, 19, "Grey background", greyBackground);
    s1.addCell(lr);

    WritableFont yellow = new WritableFont(WritableFont.ARIAL, 
                                           WritableFont.DEFAULT_POINT_SIZE,
                                           WritableFont.NO_BOLD,
                                           false,
                                           UnderlineStyle.NO_UNDERLINE,
                                           Colour.YELLOW);
    WritableCellFormat yellowOnBlue = new WritableCellFormat(yellow);
    yellowOnBlue.setWrap(true);
    yellowOnBlue.setBackground(Colour.BLUE);
    lr = new Label(2, 20, "Blue background, yellow foreground", yellowOnBlue);
    s1.addCell(lr);

    lr = new Label(0, 22, "Null label");
    s1.addCell(lr);

    lr = new Label(2, 22, null);
    s1.addCell(lr);

    lr = new Label(0, 24, 
                   "A very long label, more than 255 characters\012" +
                   "Rejoice O shores\012" +
                   "Sing O bells\012" + 
                   "But I with mournful tread\012" +
                   "Walk the deck my captain lies\012" +
                   "Fallen cold and dead\012"+
                   "Summer surprised, coming over the Starnbergersee\012" +
                   "With a shower of rain. We stopped in the Colonnade\012" +
                   "A very long label, more than 255 characters\012" +
                   "Rejoice O shores\012" +
                   "Sing O bells\012" + 
                   "But I with mournful tread\012" +
                   "Walk the deck my captain lies\012" +
                   "Fallen cold and dead\012"+
                   "Summer surprised, coming over the Starnbergersee\012" +
                   "With a shower of rain. We stopped in the Colonnade\012" +                   "A very long label, more than 255 characters\012" +
                   "Rejoice O shores\012" +
                   "Sing O bells\012" + 
                   "But I with mournful tread\012" +
                   "Walk the deck my captain lies\012" +
                   "Fallen cold and dead\012"+
                   "Summer surprised, coming over the Starnbergersee\012" +
                   "With a shower of rain. We stopped in the Colonnade\012" +                   "A very long label, more than 255 characters\012" +
                   "Rejoice O shores\012" +
                   "Sing O bells\012" + 
                   "But I with mournful tread\012" +
                   "Walk the deck my captain lies\012" +
                   "Fallen cold and dead\012"+
                   "Summer surprised, coming over the Starnbergersee\012" +
                   "With a shower of rain. We stopped in the Colonnade\012" +
                   "And sat and drank coffee an talked for an hour\012",
                   arial12format);
    s1.addCell(lr);

    WritableCellFormat vertical = new WritableCellFormat();
    vertical.setOrientation(Orientation.VERTICAL);
    lr = new Label(0, 26, "Vertical orientation", vertical);
    s1.addCell(lr);
    

    WritableCellFormat plus_90 = new WritableCellFormat();
    plus_90.setOrientation(Orientation.PLUS_90);
    lr = new Label(1, 26, "Plus 90", plus_90);
    s1.addCell(lr);


    WritableCellFormat minus_90 = new WritableCellFormat();
    minus_90.setOrientation(Orientation.MINUS_90);
    lr = new Label(2, 26, "Minus 90", minus_90);
    s1.addCell(lr);

    lr = new Label(0, 28, "Modified row height");
    s1.addCell(lr);
    s1.setRowView(28, 24);

    lr = new Label(0, 29, "Collapsed row");
    s1.addCell(lr);
    s1.setRowView(29, true);

    // Write hyperlinks
    try
    {
      Label l = new Label(0, 30, "Hyperlink to home page");
      s1.addCell(l);
      
      URL url = new URL("http://www.andykhan.com/jexcelapi");
      WritableHyperlink wh = new WritableHyperlink(0, 30, 8, 31, url);
      s1.addHyperlink(wh);

      // The below hyperlink clashes with above
      WritableHyperlink wh2 = new WritableHyperlink(7, 30, 9, 31, url);
      s1.addHyperlink(wh2);

      l = new Label(4, 2, "File hyperlink to documentation");
      s1.addCell(l);

      File file = new File("../jexcelapi/docs/index.html");
      wh = new WritableHyperlink(0, 32, 8, 32, file);
      s1.addHyperlink(wh);

      // Add a hyperlink to another cell on this sheet
      wh = new WritableHyperlink(0, 34, 8, 34, 
                                 "Link to another cell",
                                 s1,
                                 0, 180, 1, 181);
      s1.addHyperlink(wh);

      file = new File("\\\\localhost\\file.txt");
      wh = new WritableHyperlink(0, 36, 8, 36, file);
      s1.addHyperlink(wh);

      url = new URL("mailto:andyk@andykhan.freeserve.co.uk");
      wh = new WritableHyperlink(0, 38, 2, 38, url, "Mail andyk");
      s1.addHyperlink(wh);
    }
    catch (MalformedURLException e)
    {
      System.err.println(e.toString());
    }

    // Write out some merged cells
    Label l = new Label(5, 35, "Merged cells", timesBoldUnderline);
    s1.mergeCells(5, 35, 8, 37);
    s1.addCell(l);

    l = new Label(5, 38, "More merged cells");
    s1.addCell(l);
    Range r = s1.mergeCells(5, 38, 8, 41);
    s1.insertRow(40);
    s1.removeRow(39);
    s1.unmergeCells(r);

    // Merge cells and centre across them
    WritableCellFormat wcf = new WritableCellFormat();
    wcf.setAlignment(Alignment.CENTRE);
    l = new Label(5, 42, "Centred across merged cells", wcf);
    s1.addCell(l);
    s1.mergeCells(5, 42, 10, 42);

    wcf = new WritableCellFormat();
    wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
    wcf.setBackground(Colour.GRAY_25);
    l = new Label(3, 44, "Merged with border", wcf);
    s1.addCell(l);
    s1.mergeCells(3, 44, 4, 46);

    // Clash some ranges - the second range will not be added
    // Also merge some cells with two data items in the - the second data
    // item will not be merged
    /*
    l = new Label(5, 16, "merged cells");
    s1.addCell(l);

    Label l5 = new Label(7, 17, "this label won't appear");
    s1.addCell(l5);
    s1.mergeCells(5, 16, 8, 18);    

    s1.mergeCells(5, 19, 6, 24);
    s1.mergeCells(6, 18, 10, 19);
    */
    
    WritableFont courier10ptFont = new WritableFont(WritableFont.COURIER, 10);
    WritableCellFormat courier10pt = new WritableCellFormat(courier10ptFont);
    l = new Label(0, 49, "Courier fonts", courier10pt);
    s1.addCell(l);

    WritableFont tahoma12ptFont = new WritableFont(WritableFont.TAHOMA, 12);
    WritableCellFormat tahoma12pt = new WritableCellFormat(tahoma12ptFont);
    l = new Label(0, 50, "Tahoma fonts", tahoma12pt);
    s1.addCell(l);

    WritableFont.FontName wingdingsFont = 
      WritableFont.createFont("Wingdings 2");
    WritableFont wingdings210ptFont = new WritableFont(wingdingsFont, 10);
    WritableCellFormat wingdings210pt = new WritableCellFormat
      (wingdings210ptFont);
    l = new Label(0,51, "Bespoke Windgdings 2", wingdings210pt);
    s1.addCell(l);

    WritableCellFormat shrinkToFit = new WritableCellFormat(times12pt);
    shrinkToFit.setShrinkToFit(true);
    l = new Label(3,53, "Shrunk to fit", shrinkToFit);
    s1.addCell(l);

    l = new Label(3,55, "Some long wrapped text in a mergec cell", 
                  arial12format);
    s1.addCell(l);
    s1.mergeCells(3,55,4,55);
  }

  /**
   * Adds cells to the specified sheet which test the various border
   * styles
   * 
   * @param s
   */
  private void writeBordersSheet(WritableSheet s) throws WriteException
  {
    s.getSettings().setProtected(true);

    s.setColumnView(1, 15);
    s.setColumnView(2, 15);
    s.setColumnView(4, 15);
    WritableCellFormat thickLeft = new WritableCellFormat();
    thickLeft.setBorder(Border.LEFT, BorderLineStyle.THICK); 
    Label lr = new Label(1,0, "Thick left", thickLeft);
    s.addCell(lr);

    WritableCellFormat dashedRight = new WritableCellFormat();
    dashedRight.setBorder(Border.RIGHT, BorderLineStyle.DASHED);
    lr = new Label(2, 0, "Dashed right", dashedRight);
    s.addCell(lr);

    WritableCellFormat doubleTop = new WritableCellFormat();
    doubleTop.setBorder(Border.TOP, BorderLineStyle.DOUBLE);
    lr = new Label(1, 2, "Double top", doubleTop);
    s.addCell(lr);

    WritableCellFormat hairBottom = new WritableCellFormat();
    hairBottom.setBorder(Border.BOTTOM, BorderLineStyle.HAIR);
    lr = new Label(2, 2, "Hair bottom", hairBottom);
    s.addCell(lr);

    WritableCellFormat allThin = new WritableCellFormat();
    allThin.setBorder(Border.ALL, BorderLineStyle.THIN);
    lr = new Label(4, 2, "All thin", allThin);
    s.addCell(lr);

    WritableCellFormat twoBorders = new WritableCellFormat();
    twoBorders.setBorder(Border.TOP, BorderLineStyle.THICK);
    twoBorders.setBorder(Border.LEFT, BorderLineStyle.THICK);
    lr = new Label(6,2, "Two borders", twoBorders);
    s.addCell(lr);

    // Create a cell in the middle of nowhere (out of the grow region)
    lr = new Label(20, 20, "Dislocated cell - after a page break");
    s.addCell(lr);

    // Set the orientation and the margins
    s.getSettings().setPaperSize(PaperSize.A3);
    s.getSettings().setOrientation(PageOrientation.LANDSCAPE);
    s.getSettings().setHeaderMargin(2);
    s.getSettings().setFooterMargin(2);

    s.getSettings().setTopMargin(3);
    s.getSettings().setBottomMargin(3);

    // Add a header and footera
    HeaderFooter header = new HeaderFooter();
    header.getCentre().append("Page Header");
    s.getSettings().setHeader(header);

    HeaderFooter footer = new HeaderFooter();
    footer.getRight().append("page ");
    footer.getRight().appendPageNumber();
    s.getSettings().setFooter(footer);

    // Add a page break and insert a couple of rows
    s.addRowPageBreak(18);
    s.insertRow(17);
    s.insertRow(17);
    s.removeRow(17);

    // Add a page break off the screen
    s.addRowPageBreak(30);

    // Add a hidden column
    lr = new Label(10, 1, "Hidden column");
    s.addCell(lr);

    lr = new Label(3, 8, "Hidden row");
    s.addCell(lr);
    s.setRowView(8, true);

    WritableCellFormat allThickRed = new WritableCellFormat();
    allThickRed.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.RED);
    lr = new Label(1, 5, "All thick red", allThickRed);
    s.addCell(lr);

    WritableCellFormat topBottomBlue = new WritableCellFormat();
    topBottomBlue.setBorder(Border.TOP, BorderLineStyle.THIN, Colour.BLUE);
    topBottomBlue.setBorder(Border.BOTTOM, BorderLineStyle.THIN, Colour.BLUE);
    lr = new Label(4, 5, "Top and bottom blue", topBottomBlue);
    s.addCell(lr);
  }

  /**
   * Write out loads of labels, in order to test the shared string table
   */
  private void writeLabelsSheet(WritableSheet ws) throws WriteException
  {
    ws.getSettings().setProtected(true);
    ws.getSettings().setPassword("jxl");
    ws.getSettings().setVerticalFreeze(5);

    WritableFont wf = new WritableFont(WritableFont.ARIAL, 12);
    wf.setItalic(true);

    WritableCellFormat wcf = new WritableCellFormat(wf);

    CellView cv = new CellView();
    cv.setSize(25 * 256);
    cv.setFormat(wcf);
    ws.setColumnView(0, cv);
    ws.setColumnView(1, 15);

    for (int i =  0; i < 61; i++)
    {
      Label l1 = new Label(0, i, "Common Label");
      Label l2 = new Label(1, i, "Distinct label number " + i);
      ws.addCell(l1);
      ws.addCell(l2);
    }

    // Frig this test shortItem - it appears exactly on the boundary of an SST
    // continue shortItem

    Label l3 = new Label(0, 61, "Common Label", wcf);
    Label l4 = new Label(1, 61, "1-1234567890", wcf);
    Label l5 = new Label(2, 61, "2-1234567890", wcf);
    ws.addCell(l3);
    ws.addCell(l4);
    ws.addCell(l5);

    for (int i =  62; i < 200; i++)
    {
      Label l1 = new Label(0, i, "Common Label");
      Label l2 = new Label(1, i, "Distinct label number " + i);
      ws.addCell(l1);
      ws.addCell(l2);
    }

    // Add in a last label which doesn't take the common format
    wf = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD);
    wf.setColour(Colour.RED);
    wcf = new WritableCellFormat(wf);
    wcf.setWrap(true);
    Label l = new Label(0, 205, "Different format", wcf);
    ws.addCell(l);
  }

  /**
   * Test out the formula parser
   */
  private void writeFormulaSheet(WritableSheet ws) throws WriteException
  {
    // Add some cells to manipulate
    Number nc = new Number(0,0,15);
    ws.addCell(nc);
    
    nc = new Number(0,1,16);
    ws.addCell(nc);

    nc = new Number(0,2,10);
    ws.addCell(nc);
    
    nc = new Number(0,3, 12);
    ws.addCell(nc);

    ws.setColumnView(2, 20);
    WritableCellFormat wcf = new WritableCellFormat();
    wcf.setAlignment(Alignment.RIGHT);
    wcf.setWrap(true);
    CellView cv = new CellView();
    cv.setSize(25 * 256);
    cv.setFormat(wcf);
    ws.setColumnView(3, cv);

    // Add in the formulas
    Formula f = null;
    Label l = null;

    f = new Formula(2,0, "A1+A2");
    ws.addCell(f);
    l = new Label(3, 0, "a1+a2");
    ws.addCell(l);

    f = new Formula(2,1, "A2 * 3");
    ws.addCell(f);
    l = new Label(3,1, "A2 * 3");
    ws.addCell(l);

    f = new Formula(2,2, "A2+A1/2.5");
    ws.addCell(f);
    l = new Label(3,2, "A2+A1/2.5");
    ws.addCell(l);

    f = new Formula(2,3, "3+(a1+a2)/2.5");
    ws.addCell(f);
    l = new Label(3,3, "3+(a1+a2)/2.5");
    ws.addCell(l);

    f = new Formula(2,4, "(a1+a2)/2.5");
    ws.addCell(f);
    l = new Label(3,4, "(a1+a2)/2.5");
    ws.addCell(l);

    f = new Formula(2,5, "15+((a1+a2)/2.5)*17");
    ws.addCell(f);
    l = new Label(3,5, "15+((a1+a2)/2.5)*17");
    ws.addCell(l);

    f = new Formula(2, 6, "SUM(a1:a4)");
    ws.addCell(f);
    l = new Label(3, 6, "SUM(a1:a4)");
    ws.addCell(l);

    f = new Formula(2, 7, "SUM(a1:a4)/4");
    ws.addCell(f);
    l = new Label(3, 7, "SUM(a1:a4)/4");
    ws.addCell(l);

    f = new Formula(2, 8, "AVERAGE(A1:A4)");
    ws.addCell(f);
    l = new Label(3, 8, "AVERAGE(a1:a4)");
    ws.addCell(l);

    f = new Formula(2, 9, "MIN(5,4,1,2,3)");
    ws.addCell(f);
    l = new Label(3, 9, "MIN(5,4,1,2,3)");
    ws.addCell(l);

    f = new Formula(2, 10, "ROUND(3.14159265, 3)");
    ws.addCell(f);
    l = new Label(3, 10, "ROUND(3.14159265, 3)");
    ws.addCell(l);

    f = new Formula(2, 11, "MAX(SUM(A1:A2), A1*A2, POWER(A1, 2))");
    ws.addCell(f);
    l = new Label(3, 11, "MAX(SUM(A1:A2), A1*A2, POWER(A1, 2))");
    ws.addCell(l);

    f = new Formula(2,12, "IF(A2>A1, \"A2 bigger\", \"A1 bigger\")");
    ws.addCell(f);
    l = new Label(3,12, "IF(A2>A1, \"A2 bigger\", \"A1 bigger\")");
    ws.addCell(l);

    f = new Formula(2,13, "IF(A2<=A1, \"A2 smaller\", \"A1 smaller\")");
    ws.addCell(f);
    l = new Label(3,13, "IF(A2<=A1, \"A2 smaller\", \"A1 smaller\")");
    ws.addCell(l);

    f = new Formula(2,14, "IF(A3<=10, \"<= 10\")");
    ws.addCell(f);
    l = new Label(3,14, "IF(A3<=10, \"<= 10\")");
    ws.addCell(l);

    f = new Formula(2, 15, "SUM(1,2,3,4,5)");
    ws.addCell(f);
    l = new Label(3, 15, "SUM(1,2,3,4,5)");
    ws.addCell(l);

    f = new Formula(2, 16, "HYPERLINK(\"http://www.andykhan.com/jexcelapi\", \"JExcelApi Home Page\")");
    ws.addCell(f);
    l = new Label(3, 16, "HYPERLINK(\"http://www.andykhan.com/jexcelapi\", \"JExcelApi Home Page\")");
    ws.addCell(l);

    f = new Formula(2, 17, "3*4+5");
    ws.addCell(f);
    l = new Label(3, 17, "3*4+5");
    ws.addCell(l);

    f = new Formula(2, 18, "\"Plain text formula\"");
    ws.addCell(f);
    l = new Label(3, 18, "Plain text formula");
    ws.addCell(l);

    f = new Formula(2, 19, "SUM(a1,a2,-a3,a4)");
    ws.addCell(f);
    l = new Label(3, 19, "SUM(a1,a2,-a3,a4)");
    ws.addCell(l);

    f = new Formula(2, 20, "2*-(a1+a2)");
    ws.addCell(f);
    l = new Label(3, 20, "2*-(a1+a2)");
    ws.addCell(l);

    f = new Formula(2, 21, "Number Formats!B1/2");
    ws.addCell(f);
    l = new Label(3, 21, "Number Formats!B1/2");
    ws.addCell(l);

    f = new Formula(2, 22, "IF(F22=0, 0, F21/F22)");
    ws.addCell(f);
    l = new Label(3, 22, "IF(F22=0, 0, F21/F22)");
    ws.addCell(l);

    f = new Formula(2, 23, "RAND()");
    ws.addCell(f);
    l = new Label(3, 23, "RAND()");
    ws.addCell(l);

    StringBuffer buf = new StringBuffer();
    buf.append("'");
    buf.append(workbook.getSheet(0).getName());
    buf.append("'!");
    buf.append(CellReferenceHelper.getCellReference(9, 18));
    buf.append("*25");
    f = new Formula(2, 24, buf.toString());
    ws.addCell(f);
    l = new Label(3, 24, buf.toString());
    ws.addCell(l);

    wcf = new WritableCellFormat(DateFormats.DEFAULT);
    f = new Formula(2, 25, "NOW()", wcf);
    ws.addCell(f);
    l = new Label(3, 25, "NOW()");
    ws.addCell(l);

    f = new Formula(2, 26, "$A$2+A3");
    ws.addCell(f);
    l = new Label(3, 26, "$A$2+A3");
    ws.addCell(l);

    f = new Formula(2, 27, "IF(COUNT(A1:A9,B1:B9)=0,\"\",COUNT(A1:A9,B1:B9))");
    ws.addCell(f);
    l = new Label(3, 27, "IF(COUNT(A1:A9,B1:B9)=0,\"\",COUNT(A1:A9,B1:B9))");
    ws.addCell(l);

    f = new Formula(2, 28, "SUM(A1,A2,A3,A4)");
    ws.addCell(f);
    l = new Label(3, 28, "SUM(A1,A2,A3,A4)");
    ws.addCell(l);

    l = new Label(1, 29, "a1");
    ws.addCell(l);
    f = new Formula(2, 29, "SUM(INDIRECT(ADDRESS(2,29)):A4)");
    ws.addCell(f);
    l = new Label(3, 29, "SUM(INDIRECT(ADDRESS(2,29):A4)");
    ws.addCell(l);

    f = new Formula(2, 30, "COUNTIF(A1:A4, \">=12\")");
    ws.addCell(f);
    l = new Label(3, 30, "COUNTIF(A1:A4, \">=12\")");
    ws.addCell(l);

    f = new Formula(2, 31, "MAX($A$1:$A$4)");
    ws.addCell(f);
    l = new Label(3, 31, "MAX($A$1:$A$4)");
    ws.addCell(l);

    f = new Formula(2, 32, "OR(A1,TRUE)");
    ws.addCell(f);
    l = new Label(3, 32, "OR(A1,TRUE)");
    ws.addCell(l);

    f = new Formula(2, 33, "ROWS(A1:C14)");
    ws.addCell(f);
    l = new Label(3, 33, "ROWS(A1:C14)");
    ws.addCell(l);

    f = new Formula(2, 34, "COUNTBLANK(A1:C14)");
    ws.addCell(f);
    l = new Label(3, 34, "COUNTBLANK(A1:C14)");
    ws.addCell(l);

    


    // Errors
    /*
    f = new Formula(2, 25, "PLOP(15)"); // unknown function
    ws.addCell(f);

    f = new Formula(2, 26, "SUM(15,3"); // unmatched parentheses
    ws.addCell(f);

    f = new Formula(2, 27, "SUM15,3)"); // missing opening parentheses
    ws.addCell(f);

    f = new Formula(2, 28, "ROUND(3.14159)"); // missing args
    ws.addCell(f);

    f = new Formula(2, 29, "NONSHEET!A1"); // sheet not found
    ws.addCell(f);
    */
  }

  /**
   * Write out the images
   */
  private void writeImageSheet(WritableSheet ws) throws WriteException
  {
    Label l = new Label(0, 0, "Weald & Downland Open Air Museum, Sussex");
    ws.addCell(l);

    WritableImage wi = new WritableImage
      (0, 3, 5, 7, new File("resources/wealdanddownland.png"));
    ws.addImage(wi);

    l = new Label(0, 12, "Merchant Adventurers Hall, York");
    ws.addCell(l);

    wi = new WritableImage(5, 12, 4, 10, 
                           new File("resources/merchantadventurers.png"));
    ws.addImage(wi);

    // An unsupported file time
    /*
      wi = new WritableImage(0, 60, 5, 5, new File("resources/somefile.gif"));
      ws.addImage(wi);
    */
  }
}








