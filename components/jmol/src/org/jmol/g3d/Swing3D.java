/* $RCSfile: Swing3D.java,v $
 * $Author: mhall $
 * $Date: 2006-03-13 18:03:15 $
 * $Revision: 1.1 $
 *
 * Copyright (C) 2003-2004  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307  USA.
 */

package org.jmol.g3d;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;

final class Swing3D extends Platform3D {

  final static DirectColorModel rgbColorModel =
    new DirectColorModel(24, 0x00FF0000, 0x0000FF00, 0x000000FF, 0x00000000);

  final static int[] sampleModelBitMasks =
  { 0x00FF0000, 0x0000FF00, 0x000000FF };
  
  Image allocateImage() {
    SinglePixelPackedSampleModel sppsm =
      new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,
                                       windowWidth,
                                       windowHeight,
                                       sampleModelBitMasks);
    DataBufferInt dbi = new DataBufferInt(pBuffer, windowSize);
    WritableRaster wr =
      Raster.createWritableRaster(sppsm, dbi, null);
    BufferedImage bi = new BufferedImage(rgbColorModel, wr, false, null);
    return bi;
  }

  Image allocateOffscreenImage(int width, int height) {
    return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  }

  Graphics getGraphics(Image image) {
    BufferedImage bi = (BufferedImage) image;
    Graphics2D g2d = bi.createGraphics();
    // miguel 20041122
    // we need to turn off text antialiasing on OSX when
    // running in a web browser
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                         RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    // I don't know if we need these or not, but cannot hurt to have them
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_OFF);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                         RenderingHints.VALUE_RENDER_SPEED);
    return g2d;
  }
}
