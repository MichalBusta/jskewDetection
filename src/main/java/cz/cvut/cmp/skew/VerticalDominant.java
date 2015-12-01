package cz.cvut.cmp.skew;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.awt.*;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.reduce;

/**
 * Created by J����� on 10. 11. 2015.
 */
public class VerticalDominant extends SkewEstimator {

    static {
        nu.pattern.OpenCV.loadShared();
    }

    public static double calculateEntropy(Mat hist) {
        double entropie = 0;
        Mat nMat = new Mat();
        float[] nArray = new float[3];
        Core.reduce(hist, nMat, 1, Core.REDUCE_SUM, CvType.CV_32FC1);
        nMat.get(0, 0, nArray);
        double n = (double) nArray[0];
        double ni;

        double pom;
        float[] data = new float[3];
        for (int i = 0; i < hist.cols(); i++) {
            hist.get(0, i, data);
            ni = data[0];

            if (ni != 0) {
                pom = (ni / n) * Math.log(ni / n);
                entropie = entropie + pom;
            }
        }
        return -entropie;
    }

    public double estimateSkew(Mat img) {
        // invert the colours
        Mat invImg = new Mat();
        bitwise_not(img, invImg);
        //we will extend image with border on left and right side of the image, because of the image
        Imgproc.copyMakeBorder(invImg, invImg, 0, 0, invImg.rows(), invImg.rows(), Imgproc.BORDER_CONSTANT);

        // get the histogram matrix
        Mat rowSumImg = new Mat();
        Core.reduce(invImg, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);

        // find the skew
        double[] result = new double[2];
        double entropy = calculateEntropy(rowSumImg);
        double angle = 0;
        result[0] = entropy;
        result[1] = angle;

        // try all angles in the range of +-45d from the initial one, compare the entropy
        for (int a = 1; a < 45; a++) {
            Mat edited = new Mat();
            //try the positive angle
            skewImage(invImg, edited, Math.toRadians(a));
            // ??   System.out.println(m.dump()); -> prints the matrix
            Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
            entropy = calculateEntropy(rowSumImg);
            if (entropy < result[0]) {
                result[0] = entropy;
                result[1] = a;
                //OCVUtils.showImage(edited);
            }
            
            //try the negative angle
            skewImage(invImg, edited, Math.toRadians(-a));
            Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
            entropy = calculateEntropy(rowSumImg);
            if (entropy < result[0]) {
                //OCVUtils.showImage(edited);
                result[0] = entropy;
                result[1] = -a;
            }
        }
        return result[1];
    }

    public static void main(String[] args) {

        // read image
        Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);

        // estimate skew
        SkewEstimator est = new VerticalDominant();
        double skewAngle = est.estimateSkew(img);

        System.out.println(skewAngle);


    }
}
