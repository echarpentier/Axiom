package fr.pfgen.axiom.server.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerUtils {
	
	/**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality){
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
	
	public static void deleteDirectory(File path) {
		if(! path.exists() ) return;
		for(File f:path.listFiles()){
			if(f.isDirectory()){
				deleteDirectory(f);
			}else{
				if(!f.delete()){
					System.err.println("Warning cannot rm "+f);
				}
			}
		}
		if(!path.delete()){
			System.err.println("Warning cannot rm "+path);
		}
	}

	public static List<String> GetLineByIndex(List<Integer> lineNumbers,File file){
		Collections.sort(lineNumbers);
		List<String> lines = new ArrayList<String>(lineNumbers.size());
		int curr=1;
		BufferedReader in=null;
		
		try{
			in = IOUtils.openFile(file);
		
			int listIter = 0;
			String line;
			while((line=in.readLine())!=null){
				if (curr<lineNumbers.get(listIter)){
					curr++;
					continue;
				}
				if (curr==lineNumbers.get(listIter)){
					lines.add(line);
					listIter++;
					curr++;
					if (listIter==lineNumbers.size()){
						break;
					}
				}
			}
			in.close();
	    }catch(IOException e){
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	IOUtils.safeClose(in);
	    }
	    return lines;
	}
	
	public static String GetLineByIndex(int lineNumber, File file){
		List<Integer> list = new ArrayList<Integer>(1);
		list.add(lineNumber);
		return GetLineByIndex(list, file).get(0);
	}
	
	public static List<String> GetLinesByIndexOmitStartingWith(List<Integer> lineNumbers,File file, String start){
		Collections.sort(lineNumbers);
		List<String> lines = new ArrayList<String>(lineNumbers.size());
		int curr=1;
		BufferedReader in=null;
		
		try{
			in = IOUtils.openFile(file);
		
			int listIter = 0;
			String line;
			while((line=in.readLine())!=null){
				if (line.startsWith(start)){
					continue;
				}
				if (curr<lineNumbers.get(listIter)){
					curr++;
					continue;
				}
				if (curr==lineNumbers.get(listIter)){
					lines.add(line);
					listIter++;
					curr++;
					if (listIter==lineNumbers.size()){
						break;
					}
				}
			}
			in.close();
	    }catch(IOException e){
	    	e.printStackTrace();
	    	return null;
	    }finally{
	    	IOUtils.safeClose(in);
	    }
	    return lines;
	}
	
	public static String randomHexString(){
		return Long.toHexString(Double.doubleToLongBits(Math.random()));
	}
}
