/*********************************************************************
*
*      Copyright (C) 2003 Andrew Khan
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

import jxl.biff.IntegerHelper;

class Sp extends EscherAtom
{
  private byte[] data;
  private int shapeType;
  private int shapeId;
  private int persistenceFlags;

  private static class ShapeType 
  {
    int value;
    ShapeType(int v) { value = v; }
  }
  public final static ShapeType MIN = new ShapeType(0);
  public final static ShapeType PICTURE_FRAME = new ShapeType(75);

  public Sp(EscherRecordData erd)
  {
    super(erd);
    shapeType = getInstance();
    byte[] bytes = getBytes();
    shapeId = IntegerHelper.getInt(bytes[0], bytes[1], bytes[2], bytes[3]);
    persistenceFlags = IntegerHelper.getInt(bytes[4], bytes[5], 
                                           bytes[6], bytes[7]);
  }

  public Sp(ShapeType st, int sid, int p)
  {
    super(EscherRecordType.SP);
    setVersion(2);
    shapeType = st.value;
    shapeId = sid;
    persistenceFlags = p;
    setInstance(shapeType);
  }

  int getShapeId()
  {
    return shapeId;
  }

  byte[] getData()
  {
    data = new byte[8];
    IntegerHelper.getFourBytes(shapeId, data, 0);
    IntegerHelper.getFourBytes(persistenceFlags, data, 4);
    return setHeaderData(data);
  }
}
