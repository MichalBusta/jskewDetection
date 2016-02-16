package cz.cvut.cmp.skew;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class TestContourSkew {

	static {
        nu.pattern.OpenCV.loadShared();
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);
		
		ContourSkewEstimator cs = new ContourSkewEstimator();
		cs.estimateSkew(img);

	}

}
