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

import java.io.OutputStream;
import java.io.IOException;

import common.Assert;
import common.Logger;
import jxl.biff.BaseCompoundFile;
import jxl.biff.IntegerHelper;

/**
 * Writes out a compound file
 * 
 * Header block is -1
 * Excel data is e..n (where is the head extension blocks, normally 0 and
 * n is at least 8)
 * Summary information (8 blocks)
 * Document summary (8 blocks)
 * BBD is block p..q (where p=e+n+16 and q-p+1 is the number of BBD blocks)
 * Property storage block is q+b...r (normally 1 block) (where b is the number
 * of BBD blocks)
 */
final class CompoundFile extends BaseCompoundFile
{
  /**
   * The logger
   */
  private static Logger logger = Logger.getLogger(CompoundFile.class);

  /**
   * The stream to which the jumbled up data is written to
   */
  private OutputStream out;
  /**
   * The organized biff records which form the actual excel data
   */
  private byte[] excelData;

  /**
   * The size of the array
   */
  private int    size;

  /**
   * The size the excel data should be in order to comply with the
   * general compound file format
   */
  private int    requiredSize;

  /**
   * The number of blocks it takes to store the big block depot
   */
  private int numBigBlockDepotBlocks;

  /**
   * The number of extension blocks required for the header to describe
   * the BBD
   */
  private int numExtensionBlocks;

  /**
   * The extension block for the header
   */
  private int extensionBlock;

  /**
   * The number of blocks it takes to store the excel data
   */
  private int excelDataBlocks;

  /**
   * The start block of the root entry
   */
  private int rootStartBlock;

  /**
   * The start block of the excel data
   */
  private int excelDataStartBlock;

  /**
   * The start block of the big block depot
   */
  private int bbdStartBlock;

  // The following member variables are used across methods when
  // writing out the big block depot
  /**
   * The current position within the bbd.  Used when writing out the
   * BBD
   */
  private int bbdPos;

  /**
   * The current bbd block
   */
  private byte[] bigBlockDepot;

  /**
   * Constructor
   * 
   * @param l the length of the data
   * @param os the output stream to write to
   * @param data the excel data
   */
  public CompoundFile(byte[] data, int l, OutputStream os)
  {
    super();
    size = l;
    excelData = data;

    int blocks  = (l / BIG_BLOCK_SIZE) + 1;

    // First pad the data out so that it fits nicely into a whole number
    // of blocks
    if (l < SMALL_BLOCK_THRESHOLD)
    {
      requiredSize = SMALL_BLOCK_THRESHOLD;
    }
    else
    {
      requiredSize = blocks * BIG_BLOCK_SIZE;
    }
    
    out = os;

    // Do the calculations
    excelDataBlocks = requiredSize/BIG_BLOCK_SIZE;
    numBigBlockDepotBlocks = 1;

    int blockChainLength = (BIG_BLOCK_SIZE - BIG_BLOCK_DEPOT_BLOCKS_POS)/4;

    int startTotalBlocks = excelDataBlocks +
      8 + // summary block
      8 + // document information
      1;  // root entry

    int totalBlocks = startTotalBlocks + numBigBlockDepotBlocks;

    // Calculate the number of BBD blocks needed to hold this info
    numBigBlockDepotBlocks = (int) Math.ceil( (double) totalBlocks / 
                                              (double) (BIG_BLOCK_SIZE/4));

    // Does this affect the total?
    totalBlocks = startTotalBlocks + numBigBlockDepotBlocks;

    // And recalculate
    numBigBlockDepotBlocks = (int) Math.ceil( (double) totalBlocks / 
                                              (double) (BIG_BLOCK_SIZE/4));

    // Does this affect the total?
    totalBlocks = startTotalBlocks + numBigBlockDepotBlocks;

    // See if the excel bbd chain can fit into the header block.
    // Remember to allow for the  end of chain indicator
    if (numBigBlockDepotBlocks > blockChainLength - 1 )
    {
      // Sod it - we need an extension block.  We have to go through
      // the whole tiresome calculation again
      extensionBlock = 0;

      // Compute the number of extension blocks
      int bbdBlocksLeft = numBigBlockDepotBlocks - blockChainLength + 1;

      numExtensionBlocks = (int) Math.ceil((double) bbdBlocksLeft /
                                           (double) (BIG_BLOCK_SIZE/4 - 1));

      // Modify the total number of blocks required and recalculate the
      // the number of bbd blocks
      totalBlocks = startTotalBlocks + 
                    numExtensionBlocks + 
                    numBigBlockDepotBlocks;
      numBigBlockDepotBlocks = (int) Math.ceil( (double) totalBlocks / 
                                                (double) (BIG_BLOCK_SIZE/4));

      // The final total
      totalBlocks = startTotalBlocks + 
                    numExtensionBlocks + 
                    numBigBlockDepotBlocks;
    }
    else
    {
      extensionBlock = -2;
      numExtensionBlocks = 0;
    }

    // Set the excel data start block to be after the header (and
    // its extensions)
    excelDataStartBlock = numExtensionBlocks;
    
    // Set the bbd start block to be after all the excel data
    bbdStartBlock = excelDataStartBlock + excelDataBlocks + 16;

    // Set the root start block to be after all the big block depot blocks
    rootStartBlock = excelDataStartBlock + excelDataBlocks + 
                     16 + numBigBlockDepotBlocks;


    if (totalBlocks != rootStartBlock + 1)
    {
      logger.warn("Root start block and total blocks are inconsistent " + 
                  " generated file may be corrup");
    }
  }

  /**
   * Writes out the excel file in OLE compound file format
   * 
   * @exception IOException 
   */
  public void write() throws IOException
  {
    writeHeader();
    writeExcelData();
    writeBigBlockDepot();
    writePropertySets();

    // Don't flush or close the stream - this is handled by the enclosing File
    // object
  }

  /**
   * Writes out the excel data, padding it out with empty bytes as
   * necessary
   * 
   * @exception IOException 
   */
  private void writeExcelData() throws IOException
  {
    out.write(excelData, 0, size);

    byte[] padding = new byte[requiredSize - size];
    out.write(padding);

    padding = new byte[SMALL_BLOCK_THRESHOLD];

    // Write out the summary information
    out.write(padding);
    
    // Write out the document summary
    out.write(padding);
  }

  /**
   * Writes the compound file header
   * 
   * @exception IOException 
   */
  private void writeHeader() throws IOException
  {
    // Build up the header array
    byte[] headerBlock = new byte[BIG_BLOCK_SIZE];
    byte[] extensionBlockData = new byte[BIG_BLOCK_SIZE * numExtensionBlocks];

    // Copy in the identifier
    System.arraycopy(IDENTIFIER, 0, headerBlock, 0, IDENTIFIER.length);

    // Copy in some magic values - no idea what they mean
    headerBlock[0x18] = 0x3e;
    headerBlock[0x1a] = 0x3;
    headerBlock[0x1c] = (byte) 0xfe;
    headerBlock[0x1d] = (byte) 0xff;
    headerBlock[0x1e] = 0x9;
    headerBlock[0x20] = 0x6;
    headerBlock[0x39] = 0x10;

    // Set the number of BBD blocks
    IntegerHelper.getFourBytes(numBigBlockDepotBlocks, 
                               headerBlock, 
                               NUM_BIG_BLOCK_DEPOT_BLOCKS_POS);  

    // Set the small block depot block to -2 ie. there is no
    // small block depot
    IntegerHelper.getFourBytes(-2,
                               headerBlock,
                               SMALL_BLOCK_DEPOT_BLOCK_POS);

    // Set the extension block 
    IntegerHelper.getFourBytes(extensionBlock,
                               headerBlock,
                               EXTENSION_BLOCK_POS);

    // Set the number of extension blocks to be the number of BBD blocks - 1
    IntegerHelper.getFourBytes(numExtensionBlocks,
                               headerBlock, 
                               NUM_EXTENSION_BLOCK_POS);
    
    // Set the root start block
    IntegerHelper.getFourBytes(rootStartBlock,
                               headerBlock,
                               ROOT_START_BLOCK_POS);

    // Set the block numbers for the BBD.  Set the BBD running 
    // after the excel data and summary information
    int pos = BIG_BLOCK_DEPOT_BLOCKS_POS;

    // See how many blocks fit into the header
    int blocksToWrite = Math.min(numBigBlockDepotBlocks, 
                                 (BIG_BLOCK_SIZE - 
                                  BIG_BLOCK_DEPOT_BLOCKS_POS)/4);
    int extensionBlock = 0;
    int blocksWritten = 0;

    for (int i = 0 ; i < blocksToWrite; i++)
    {
      IntegerHelper.getFourBytes(bbdStartBlock + i, 
                                 headerBlock,
                                 pos);
      pos += 4;
      blocksWritten++;
    }
    
    // Pad out the rest of the header with blanks
    for (int i = pos; i < BIG_BLOCK_SIZE; i++)
    {
      headerBlock[i] = (byte) 0xff;
    }

    out.write(headerBlock);

    // Write out the extension blocks
    pos = 0;

    for (int extBlock = 0; extBlock < numExtensionBlocks; extBlock++)
    {
      blocksToWrite = Math.min(numBigBlockDepotBlocks - blocksWritten, 
                               BIG_BLOCK_SIZE/4 -1);

      for(int j = 0 ; j < blocksToWrite; j++)
      {
        IntegerHelper.getFourBytes(bbdStartBlock + blocksWritten + j, 
                                   extensionBlockData,
                                   pos);
        pos += 4;
      }

      blocksWritten += blocksToWrite;

      // Indicate the next block, or the termination of the chain
      int nextBlock = (blocksWritten == numBigBlockDepotBlocks) ? 
                              -2 : extBlock+1 ;
      IntegerHelper.getFourBytes(nextBlock, extensionBlockData, pos);
      pos +=4;
    }

    if (numExtensionBlocks > 0)
    {
      // Pad out the rest of the extension block with blanks
      for (int i = pos; i < extensionBlockData.length; i++)
      {
        extensionBlockData[i] = (byte) 0xff;
      }

      out.write(extensionBlockData);
    }
  }

  /**
   * Checks that the data can fit into the current BBD block.  If not,
   * then it moves on to the next block
   * 
   * @exception IOException 
   */
  private void checkBbdPos() throws IOException
  {
    if (bbdPos >= BIG_BLOCK_SIZE)
    {
      // Write out the extension block.  This will simply be the next block
      out.write(bigBlockDepot);
      
      // Create a new block
      bigBlockDepot = new byte[BIG_BLOCK_SIZE];
      bbdPos = 0;
    }
  }

  /**
   * Writes out the Big Block Depot
   * 
   * @exception IOException 
   */
  private void writeBigBlockDepot() throws IOException
  {
    // This is after the excel data
    bigBlockDepot = new byte[BIG_BLOCK_SIZE];
    bbdPos = 0;
    int blockChainLength = 0;

    // Write out the extension blocks, indicating them as special blocks
    for (int i = 0 ; i < numExtensionBlocks; i++)
    {
      IntegerHelper.getFourBytes(-3, bigBlockDepot, bbdPos);
      bbdPos += 4;
      blockChainLength++;
      checkBbdPos();
    }

    int blocksToWrite = excelDataBlocks - 1;
    int blockNumber   = excelDataStartBlock + 1;
    
    while (blocksToWrite > 0)
    {
      int bbdBlocks = Math.min(blocksToWrite, (BIG_BLOCK_SIZE - bbdPos)/4);

      // Set the exceldata
      for (int i = 0 ; i < bbdBlocks; i++)
      {
        IntegerHelper.getFourBytes(blockNumber, bigBlockDepot, bbdPos);
        bbdPos +=4 ;
        blockChainLength++;
        blockNumber++;
      }
      
      blocksToWrite -= bbdBlocks;
      checkBbdPos();
    }

    // Write the end of the block chain for the excel data
    IntegerHelper.getFourBytes(-2, bigBlockDepot, bbdPos);
    bbdPos += 4;
    blockChainLength++;
    checkBbdPos();

    // The excel data has been written.  Now write out the rest of it

    // Write the block chain for the summary information
    int summaryInfoBlock = excelDataStartBlock + excelDataBlocks;
    for (int i = summaryInfoBlock; i < summaryInfoBlock + 7; i++)
    {
      IntegerHelper.getFourBytes(i + 1, bigBlockDepot, bbdPos);
      bbdPos +=4 ;
      blockChainLength++;
      checkBbdPos();
    } 

    // Write the end of the block chain for the summary info block
    IntegerHelper.getFourBytes(-2, bigBlockDepot, bbdPos);
    bbdPos += 4;
    blockChainLength++;
    checkBbdPos();

    // Write the block chain for the document summary information
    for (int i = summaryInfoBlock + 8; i < summaryInfoBlock + 15; i++)
    {
      IntegerHelper.getFourBytes(i + 1, bigBlockDepot, bbdPos);
      bbdPos +=4 ;
      blockChainLength++;
      checkBbdPos();
    } 

    // Write the end of the block chain for the document summary
    IntegerHelper.getFourBytes(-2, bigBlockDepot, bbdPos);
    bbdPos += 4;
    blockChainLength++;
    checkBbdPos();

    // The Big Block Depot immediately follows the document summary.  Denote 
    // these as a special block
    for (int i = 0; i < numBigBlockDepotBlocks; i++)
    {
      IntegerHelper.getFourBytes(-3, bigBlockDepot, bbdPos);
      bbdPos += 4;
      blockChainLength++;
      checkBbdPos();
    }

    // Set the block chain for the root entry - just one block
    IntegerHelper.getFourBytes(-2, bigBlockDepot, bbdPos);
    bbdPos += 4;
    blockChainLength++;
    checkBbdPos();

    // Pad out the remainder of the block
    if (bbdPos != 0)
    {
      for (int i = bbdPos; i < BIG_BLOCK_SIZE; i++)
      {
        bigBlockDepot[i] = (byte) 0xff;
      }
      out.write(bigBlockDepot);
    }
  }

  /**
   * Writes out the property sets
   * 
   * @exception IOException 
   */
  private void writePropertySets() throws IOException
  {
    byte[] propertySetStorage = new byte[BIG_BLOCK_SIZE];

    int pos = 0;

    // Set the workbook property set
    PropertyStorage ps = new PropertyStorage("Root Entry");
    ps.setType(5);
    ps.setStartBlock(-2);
    ps.setSize(0);
    ps.setPrevious(-1);
    ps.setNext(-1);
    ps.setDirectory(2);

    System.arraycopy(ps.data, 0, 
                     propertySetStorage, pos, 
                     PROPERTY_STORAGE_BLOCK_SIZE);
    pos += PROPERTY_STORAGE_BLOCK_SIZE;


    // Set the workbook property set
    ps = new PropertyStorage("Workbook");
    ps.setType(2);
    ps.setStartBlock(excelDataStartBlock);
      // start the excel data after immediately after this block
    ps.setSize(requiredSize);
      // alway use a big block stream - none of that messing around
      // with small blocks
    ps.setPrevious(-1);
    ps.setNext(-1);
    ps.setDirectory(-1);

    System.arraycopy(ps.data, 0, 
                     propertySetStorage, pos, 
                     PROPERTY_STORAGE_BLOCK_SIZE);
    pos += PROPERTY_STORAGE_BLOCK_SIZE;


    // Set the summary information
    ps = new PropertyStorage("\u0005SummaryInformation");
    ps.setType(2);
    ps.setStartBlock(excelDataStartBlock + excelDataBlocks);
    ps.setSize(SMALL_BLOCK_THRESHOLD);
    ps.setPrevious(1);
    ps.setNext(3);
    ps.setDirectory(-1);

    System.arraycopy(ps.data, 0, 
                     propertySetStorage, pos, 
                     PROPERTY_STORAGE_BLOCK_SIZE);
    pos += PROPERTY_STORAGE_BLOCK_SIZE;

    // Set the summary information
    ps = new PropertyStorage("\u0005DocumentSummaryInformation");
    ps.setType(2);
    ps.setStartBlock(excelDataStartBlock + excelDataBlocks+8);
    ps.setSize(SMALL_BLOCK_THRESHOLD);
    ps.setPrevious(-1);
    ps.setNext(-1);
    ps.setDirectory(-1);

    System.arraycopy(ps.data, 0, 
                     propertySetStorage, pos, 
                     PROPERTY_STORAGE_BLOCK_SIZE);
    pos += PROPERTY_STORAGE_BLOCK_SIZE;
    
    out.write(propertySetStorage);
  }
}










