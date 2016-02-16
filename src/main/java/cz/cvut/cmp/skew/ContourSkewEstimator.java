package cz.cvut.cmp.skew;

import static org.opencv.core.Core.bitwise_not;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ContourSkewEstimator extends SkewEstimator {
	

	@Override
	double estimateSkew(Mat img) {
		
		//invert image
		Mat invImg = new Mat();
        bitwise_not(img, invImg);
		
		List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
		Mat hierarchy = new Mat();
		//nalezneme contury v obrazku (pouze externi - tj. nezajimaji nas diry v blobech)
		Imgproc.findContours(invImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		//podivame se na nalezene kontury: 
		
		Mat draw = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
		Imgproc.drawContours(draw, contours, -1, new Scalar(255, 255, 255));
		
		OCVUtils.showImage(draw);
		
		for( int i = 0; i < contours.size(); i++ ){
			MatOfPoint cont = contours.get(i);
			draw = Mat.zeros(new Size(img.cols(), img.rows()), org.opencv.core.CvType.CV_8UC3);
			Imgproc.drawContours(draw, contours, i, new Scalar(255, 255, 255));
			OCVUtils.showImage(draw);
			double skewEstimate = estimateContourSkew(cont);
			
			//TODO voting in histogram .... 
		}
		
		return 0;
	}

	@Override
	double estimateSkew(Mat skew, double i, double f, int max) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	int[] getIterations() {
		// TODO Auto-generated method stub
		return null;
	}

	protected double estimateContourSkew(MatOfPoint contour){
		
		//TODO implement 
		return 0;
	}

}
