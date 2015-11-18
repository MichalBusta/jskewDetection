package cz.cvut.cmp.skew;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class TestOCVOperations {
	
	static {
		nu.pattern.OpenCV.loadShared();
	}

	@Test
	public void testReduce() {
		
		Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);
		Core.bitwise_not(img, img); //invert image
		OCVUtils.showImage(img);
		Mat dst = new Mat();
		Core.reduce(img, dst, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
		
		float[] data = new float[3];
		for(int i = 0; i < dst.cols(); i++ ){
			dst.get(0, i, data);
			System.out.print( (int) data[0] );
			System.out.print( " " );
		}
	}
}
