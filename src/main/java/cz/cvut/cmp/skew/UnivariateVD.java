package cz.cvut.cmp.skew;

import static org.opencv.core.Core.bitwise_not;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class UnivariateVD implements UnivariateFunction {

	private Mat img;
	
	Mat invImg = new Mat();
	
	UnivariateVD(Mat img){
		this.img = img; 
		bitwise_not(img, invImg);
		//we will extend image with border on left and right side of the image, because of the image
        Imgproc.copyMakeBorder(invImg, invImg, 0, 0, invImg.rows(), invImg.rows(), Imgproc.BORDER_CONSTANT);
	}

	@Override
	public double value(double x) {
		
        
        Mat edited = new Mat();
        SkewEstimator.skewImage(invImg, edited, Math.toRadians(x));
        OCVUtils.showImage(edited);
        // get the histogram matrix
        Mat rowSumImg = new Mat();
        Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
        // find the skew
        double entropy = VerticalDominant.calculateEntropy(rowSumImg);
        System.out.println("x " + x + " Entropy: " + entropy);
        return entropy;
	}

}
