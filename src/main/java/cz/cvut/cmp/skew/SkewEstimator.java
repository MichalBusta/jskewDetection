package cz.cvut.cmp.skew;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public abstract class SkewEstimator {


    abstract double estimateSkew(Mat img);


    /**
     * Image de-skew
     *
     * @param src       source image
     * @param dst       destination image
     * @param skewAngle the skew angle in radians
     */
    public static void skewImage(Mat src, Mat dst, double skewAngle) {

        Mat m = Mat.eye(2, 3, CvType.CV_32FC1);
        m.put(0, 1, Math.tan(skewAngle));
        Imgproc.warpAffine(src, dst, m, src.size());
    }

}
