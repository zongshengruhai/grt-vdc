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
import java.io.FileInputStream;

import common.Assert;
import common.Logger;

import jxl.WorkbookSettings;
import jxl.biff.ByteData;
import jxl.biff.IntegerHelper;
import jxl.biff.IndexMapping;
import jxl.biff.Type;
import jxl.write.biff.File;
import jxl.biff.drawing.MsoDrawingRecord;
import jxl.biff.drawing.ObjRecord;

/**
 * Contains the various biff records used to insert a drawing into a 
 * worksheet
 */
public class Drawing implements EscherStream
{
  /**
   * The logger
   */
  private static Logger logger = Logger.getLogger(Drawing.class);

  /**
   * The entire  drawing data read in
   */
  private byte[] drawingData;

  /**
   * The spContainer that was read in
   */
  private SpContainer readSpContainer;

  /**
   * The MsoDrawingRecord associated with the drawing
   */
  private MsoDrawingRecord msoDrawingRecord;

  /**
   * The ObjRecord associated with the drawing
   */
  private ObjRecord objRecord;

  /**
   * Initialized flag
   */
  private boolean initialized = false;

  /**
   * The file containing the image
   */
  private java.io.File imageFile;

  /**
   * The raw image data, used instead of an image file
   */
  private byte[] imageData;

  /**
   * The object id, assigned by the drawing group
   */
  private int objectId;

  /**
   * The blip id
   */
  private int blipId;

  /**
   * The column position of the image
   */
  private double x;

  /**
   * The row position of the image
   */
  private double y;

  /**
   * The width of the image in cells
   */
  private double width;

  /**
   * The height of the image in cells
   */
  private double height;

  /**
   * The number of places this drawing is referenced
   */
  private int referenceCount;

  /**
   * The top level escher container
   */
  private EscherContainer escherData;

  /**
   * Where this image came from (read, written or a copy)
   */
  private Origin origin;

  /**
   * The drawing group for all the images
   */
  private DrawingGroup drawingGroup;

  // Enumerations for the origin
  final static class Origin {};
  public static final Origin READ = new Origin();
  public static final Origin WRITE = new Origin();
  public static final Origin READ_WRITE = new Origin();

  /**
   * Constructor used when reading images
   *
   * @param mso the drawing record
   * @param obj the object record
   * @param dg the drawing group
   */
  public Drawing(MsoDrawingRecord mso, ObjRecord obj, DrawingGroup dg)
  {
    drawingGroup = dg;
    msoDrawingRecord = mso;
    objRecord = obj;
    initialized = false;
    origin = READ;
    drawingData = msoDrawingRecord.getData();

    Assert.verify(mso != null && obj != null);

    initialize();

    if (blipId != 0)
    {
      drawingGroup.addDrawing(this);
    }
    else if (objRecord.getType() != objRecord.MSOFFICEDRAWING)
    {
      logger.warn("linked drawings are not supported");
    }
  }

  /**
   * Copy constructor used to copy drawings from read to write
   *
   * @param d the drawing to copy
   */
  protected Drawing(Drawing d)
  {
    Assert.verify(d.origin == READ);
    msoDrawingRecord = d.msoDrawingRecord;
    objRecord = d.objRecord;
    initialized = false;
    origin = READ;
    drawingData = d.drawingData;
    drawingGroup = d.drawingGroup;
  }

  /**
   * Constructor invoked when writing the images
   *
   * @param x the column
   * @param y the row
   * @param width the width in cells
   * @param height the height in cells
   * @param image the image file
   */
  public Drawing(double x, double y, double width, double height, 
                 java.io.File image)
  {
    imageFile = image;
    initialized = true;
    origin = WRITE;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    referenceCount = 1;
  }

  /**
   * Constructor invoked when writing the images
   *
   * @param x the column
   * @param y the row
   * @param width the width in cells
   * @param height the height in cells
   * @param image the image data
   */
  public Drawing(double x, double y, double width, double height, 
                 byte[] image)
  {
    imageData = image;
    initialized = true;
    origin = WRITE;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    referenceCount = 1;
  }

  /**
   * Initializes the member variables from the Escher stream data
   */
  private void initialize()
  {
    EscherRecordData er = new EscherRecordData(this,0);
    Assert.verify(er.isContainer());
    
    escherData = new EscherContainer(er);

    readSpContainer = null;
    if (escherData.getType() ==  EscherRecordType.DG_CONTAINER)
    {
      EscherRecordData erd = new EscherRecordData(this, 80);
      Assert.verify(erd.getType() == EscherRecordType.SP_CONTAINER);
      readSpContainer = new SpContainer(erd);
    }
    else
    {
      Assert.verify(escherData.getType() == EscherRecordType.SP_CONTAINER);
      readSpContainer = new SpContainer(er);
    }

    Sp sp = (Sp) readSpContainer.getChildren()[0];
    objectId = sp.getShapeId() - 1024;

    Opt opt = (Opt) readSpContainer.getChildren()[1];

    if (opt.getProperty(260) != null)
    {
      blipId = opt.getProperty(260).value;
    }

    if (opt.getProperty(261) != null)
    {
      imageFile = new java.io.File(opt.getProperty(261).stringValue);
    }
    else if (objRecord.getType() != ObjRecord.MSOFFICEDRAWING)
    {
      logger.warn("no filename property for drawing");
      imageFile = new java.io.File(Integer.toString(blipId));
    }

    ClientAnchor clientAnchor = (ClientAnchor) readSpContainer.getChildren()[2];
    x = clientAnchor.getX1();
    y = clientAnchor.getY1();
    width = clientAnchor.getX2() - x;
    height = clientAnchor.getY2() - y;

    initialized = true;
  }

  /**
   * Accessor for the image file
   *
   * @return the image file
   */
  protected java.io.File getImageFile()
  {
    return imageFile;
  }

  /** 
   * Accessor for the image file path.  Normally this is the absolute path
   * of a file on the directory system, but if this drawing was constructed
   * using an byte[] then the blip id is returned
   *
   * @return the image file path, or the blip id
   */
  protected String getImageFilePath()
  {
    if (imageFile == null)
    {
      // return the blip id, if it exists
      return blipId != 0 ? Integer.toString(blipId) : "__new__image__";
    }

    return imageFile.getPath();
  }

  /**
   * Sets the object id.  Invoked by the drawing group when the object is 
   * added to id
   *
   * @param objid the object id
   * @param bip the blip id
   */
  final void setObjectId(int objid, int bip)
  {
    objectId = objid;
    blipId = bip;

    if (origin == READ)
    {
      origin = READ_WRITE;
    }
  }

  /**
   * Accessor for the object id
   *
   * @return the object id
   */
  final int getObjectId()
  {
    return objectId;
  }

  /**
   * Accessor for the blip id
   *
   * @return the blip id
   */
  final int getBlipId()
  {
    if (!initialized)
    {
      initialize();
    }

    return blipId;
  }

  /**
   * Gets the data which was read in for this drawing
   *
   * @return the drawing data
   */
  public byte[] getData()
  {
    return drawingData;
  }

  /**
   * Gets the drawing record which was read in
   *
   * @return the drawing record
   */
  MsoDrawingRecord  getMsoDrawingRecord()
  {
    return msoDrawingRecord;
  }

  /**
   * Gets the obj record which was read in
   *
   * @return the obj record
   */
  ObjRecord getObjRecord()
  {
    return objRecord;
  }
  
  /**
   * Creates the main Sp container for the drawing
   *
   * @return the SP container
   */
  public SpContainer getSpContainer()
  {
    if (!initialized)
    {
      initialize();
    }

    if (origin == READ)
    {
      return getReadSpContainer();
    }

    SpContainer spContainer = new SpContainer();
    Sp sp = new Sp(Sp.PICTURE_FRAME, 1024 + objectId, 2560);
    spContainer.add(sp);
    Opt opt = new Opt();
    opt.addProperty(260, true, false, blipId);
    String filePath = imageFile != null ? imageFile.getPath(): "";
    opt.addProperty(261, true, true, filePath.length() * 2, filePath);
    opt.addProperty(447, false, false, 65536);
    opt.addProperty(959, false, false, 524288);
    spContainer.add(opt);
    ClientAnchor clientAnchor = new ClientAnchor(x, y, x + width, y + height);
    spContainer.add(clientAnchor);
    ClientData clientData = new ClientData();
    spContainer.add(clientData);

    return spContainer;
  }

  /**
   * Sets the drawing group for this drawing.  Called by the drawing group
   * when this drawing is added to it
   *
   * @param dg the drawing group
   */
  void setDrawingGroup(DrawingGroup dg)
  {
    drawingGroup = dg;
  }

  /**
   * Accessor for the drawing group
   *
   * @return the drawing group
   */
  DrawingGroup getDrawingGroup()
  {
    return drawingGroup;
  }

  /**
   * Gets the origin of this drawing
   *
   * @return where this drawing came from
   */
  Origin getOrigin()
  {
    return origin;
  }
  
  /**
   * Accessor for the reference count on this drawing
   *
   * @return the reference count
   */
  int getReferenceCount()
  {
    return referenceCount;
  }

  /**
   * Sets the new reference count on the drawing
   *
   * @param r the new reference count
   */
  void setReferenceCount(int r)
  {
    referenceCount = r;
  }

  /**
   * Accessor for the column of this drawing
   *
   * @return the column
   */
  public double getX()
  {
    if (!initialized)
    {
      initialize();
    }
    return x;
  }

  /**
   * Sets the column position of this drawing
   *
   * @param x the column
   */
  public void setX(double x)
  {
    if (origin == READ)
    {
      if (!initialized)
      {
        initialize();
      }
      origin = READ_WRITE;
    }

    this.x = x;
  }

  /**
   * Accessor for the row of this drawing
   *
   * @return the row
   */
  public double getY()
  {
    if (!initialized)
    {
      initialize();
    }

    return y;
  }

  /**
   * Accessor for the row of the drawing
   *
   * @param y the row
   */
  public void setY(double y)
  {
    if (origin == READ)
    {
      if (!initialized)
      {
        initialize();
      }
      origin = READ_WRITE;
    }

    this.y = y;
  }


  /**
   * Accessor for the width of this drawing
   *
   * @return the number of columns spanned by this image
   */
  public double getWidth()
  {
    if (!initialized)
    {
      initialize();
    }

    return width;
  }

  /**
   * Accessor for the width
   *
   * @param w the number of columns to span
   */
  public void setWidth(double w)
  {
    if (origin == READ)
    {
      if (!initialized)
      {
        initialize();
      }
      origin = READ_WRITE;
    }

    width = w;
  }

  /**
   * Accessor for the height of this drawing
   *
   * @return the number of rows spanned by this image
   */
  public double getHeight()
  {
    if (!initialized)
    {
      initialize();
    }

    return height;
  }

  /**
   * Accessor for the height of this drawing
   *
   * @param h the number of rows spanned by this image
   */
  public void setHeight(double h)
  {
    if (origin == READ)
    {
      if (!initialized)
      {
        initialize();
      }
      origin = READ_WRITE;
    }

    height = h;
  }

  
  /**
   * Gets the SpContainer that was read in
   *
   * @return the read sp container
   */
  private SpContainer getReadSpContainer()
  {
    if (!initialized)
    {
      initialize();
    }

    return readSpContainer;
  }

  /**
   * Accessor for the image data
   *
   * @return the image data
   */
  public byte[] getImageData()
  {
    Assert.verify(origin == READ || origin == READ_WRITE);

    if (!initialized)
    {
      initialize();
    }
    
    return drawingGroup.getImageData(blipId);
  }

  /**
   * Accessor for the image data
   *
   * @return the image data
   */
  byte[] getImageBytes() throws IOException
  {
    if (origin == READ || origin == READ_WRITE)
    {
      return getImageData();
    }

    Assert.verify(origin == WRITE);

    if (imageFile == null)
    {
      Assert.verify(imageData != null);
      return imageData;
    }

    byte[] data = new byte[(int) imageFile.length()];
    FileInputStream fis = new FileInputStream(imageFile);
    fis.read(data, 0, data.length);
    fis.close();
    return data;
  }
}



