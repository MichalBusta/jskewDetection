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
        for (double a = 0; a < 45; a += 0.5) {
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

    public static double estimateSkewBisect(Mat img) {
        // invert the colours
        Mat invImg = new Mat();
        bitwise_not(img, invImg);
        //we will extend image with border on left and right side of the image, because of the image
        Imgproc.copyMakeBorder(invImg, invImg, 0, 0, invImg.rows(), invImg.rows(), Imgproc.BORDER_CONSTANT);


        // set a, b as the entropy of the limit angles
        Mat edited = new Mat();
        Mat rowSumImg = new Mat();
        double i = 45; // begining of the interval
        double f = -45; // end of the interval
        double m; // midpoint of i and f
        double iValue, fValue, mValue; // entropy values for the limits and the midpoint

        skewImage(invImg, edited, Math.toRadians(Math.toRadians(i)));
        Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
        iValue = calculateEntropy(rowSumImg);
        skewImage(invImg, edited, Math.toRadians(Math.toRadians(f)));
        Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
        fValue = calculateEntropy(rowSumImg);

        // apply the bisection method
        while (true) {
            m = (i + f) / 2;
            System.out.println("i:" + i + " f:" + f + " m:" + m);
            skewImage(invImg, edited, Math.toRadians(m));
            Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
            mValue = calculateEntropy(rowSumImg);

            if (Math.abs(iValue - mValue) < 0.001 || Math.abs(fValue - mValue) < 0.001) {
                System.out.println("iV-mV: " + (iValue - mValue));
                System.out.println("fV-mV: " + (fValue - mValue));
                System.out.println("mE:" + mValue + " iE:" + iValue + " fE:" + fValue);
                return m;
            }

            if (iValue - mValue > fValue - mValue) {
                i = m;
                iValue = mValue;
            } else {
                f = m;
                fValue = mValue;
            }


        }
    }

    public static void main(String[] args) {

        // read image
        Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);
        Mat img2 = Highgui.imread("C:\\Windows\\Temp\\google4\\Arial-Regular-sell.bin.png", Highgui.IMREAD_GRAYSCALE);
        Mat skew = new Mat();
        skewImage(img2, skew, Math.toRadians(9));

        // estimate skew
        SkewEstimator est = new VerticalDominant();
        double skewAngle = est.estimateSkew(skew);
        double estimated = estimateSkewBisect(skew);

        System.out.println("Odhad 1: " + skewAngle);
        System.out.println("Odhad 2: " + estimated);



    }
}
