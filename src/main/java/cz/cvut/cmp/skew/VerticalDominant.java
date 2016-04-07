package cz.cvut.cmp.skew;

import static org.opencv.core.Core.bitwise_not;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.Sin;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


/**
 * Created by J����� on 10. 11. 2015.
 */
public class VerticalDominant extends SkewEstimator {
    int it1;
    int it2;


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
        it1 = (int) (45 / 0.5);
        return result[1];
    }


    // using bisection method
    public double estimateSkew(Mat img, double a, double b, int nMax) {
        // invert the colours
        Mat invImg = new Mat();
        bitwise_not(img, invImg);
        //extend image with border on left and right side
        Imgproc.copyMakeBorder(invImg, invImg, 0, 0, invImg.rows(), invImg.rows(), Imgproc.BORDER_CONSTANT);

        // OCVUtils.showImage(invImg);


        // set the inital values
        Mat edited = new Mat();
        Mat rowSumImg = new Mat();
        double m = 0; // midpoint of i and f
        double aEntropyValue, bEntropyValue, Entropy; // entropy values for the limits and the midpoint

        skewImage(invImg, edited, Math.toRadians(Math.toRadians(a)));
        Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
        aEntropyValue = calculateEntropy(rowSumImg);
        skewImage(invImg, edited, Math.toRadians(Math.toRadians(b)));
        Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
        bEntropyValue = calculateEntropy(rowSumImg);

        // apply the bisection method
        for (int i = 0; i < nMax; i++) {
            m = (a + b) / 2;
            skewImage(invImg, edited, Math.toRadians(m));
            //OCVUtils.showImage(edited);
            Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
            Entropy = calculateEntropy(rowSumImg);

            if (Math.abs(aEntropyValue - Entropy) < 0.0001 || Math.abs(bEntropyValue - Entropy) < 0.0001) {
                return m;
            }

            if (aEntropyValue - Entropy > bEntropyValue - Entropy) {
                a = m;
                aEntropyValue = Entropy;
            } else {
                b = m;
                bEntropyValue = Entropy;
            }
            it2 = i + 1;
        }
        return m;
    }

    int[] getIterations() {
        int[] iterations = new int[2];
        iterations[0] = it1;
        iterations[1] = it2;
        return iterations;
    }

    public static void main(String[] args) {

        // read image

        Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);
        OCVUtils.showImage(img);
        Mat img2 = Highgui.imread("C:\\Windows\\Temp\\google4\\Arial-Regular-device.bin.png", Highgui.IMREAD_GRAYSCALE);
        Mat skew = new Mat();

//        skewImageWBG(img2, skew, Math.toRadians(-11));

        // estimate skew
        SkewEstimator est = new VerticalDominant();
        double skewAngle = est.estimateSkew(img);

        //brent optimizer
        BrentOptimizer minimizer = new BrentOptimizer(1e-2, 1e-3);
        UnivariateFunction f = new UnivariateVD(img);
        double val = minimizer.optimize(new MaxEval(200),
                new UnivariateObjectiveFunction(f),
                GoalType.MINIMIZE, new SearchInterval(-45, 45)).getPoint();
        int iter3 = minimizer.getIterations();

        Mat skew2 = new Mat();
        skewImageWBG(img, skew2, Math.toRadians(skewAngle));
        OCVUtils.showImage(skew2);

        double estimated1 = est.estimateSkew(img);
        double estimated2 = est.estimateSkew(img, -35, 35, 30);


        System.out.println("Odhad 1: " + estimated1);
        System.out.println("Odhad 2 (bisection): " + estimated2);
        System.out.println("Odhad 3: " + val + " in " + iter3);

    }
}



