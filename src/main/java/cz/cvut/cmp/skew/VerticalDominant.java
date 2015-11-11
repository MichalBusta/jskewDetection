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
 * Created by Jссссс on 10. 11. 2015.
 */
public class VerticalDominant {

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

    public static void main(String[] args) {

        // read image
        Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);

        // invert the colours
        Mat invImg = new Mat();
        bitwise_not(img, invImg);

        // get the histogram matrix
        Mat rowSumImg = new Mat();
        Core.reduce(invImg, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);

        // find the skew
        double[] result = new double[2];
        Mat m = Mat.eye(2, 3, CvType.CV_32FC1);
        double entropy = calculateEntropy(rowSumImg);
        double angle = 0;
        result[0] = entropy;
        result[1] = angle;

        // try all angles in the range of +-45А from the original one, compare the entropy
        for (int a = 1; a < 45; a++) {
            Mat edited = invImg;
            //try the positive angle
            m.put(0, 1, Math.tan(Math.toRadians(a)));
            Imgproc.warpAffine(invImg, edited, m, invImg.size());
            angle = a;
            Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
            entropy = calculateEntropy(rowSumImg);
            if (entropy < result[0]) {
                result[0] = entropy;
                result[1] = angle;
            }
            else {
                System.out.println(angle);
                System.out.println(result[0]+" < "+entropy);
            }

            //try the negative angle
            m.put(0, 1, Math.tan(Math.toRadians(-a)));
            Imgproc.warpAffine(invImg, edited, m, invImg.size());
            angle =-a;
            if (a < 5){
                OCVUtils.showImage(edited);
            }
            Core.reduce(edited, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);
            entropy = calculateEntropy(rowSumImg);
            if (entropy < result[0]) {
                result[0] = entropy;
                result[1] = angle;
            }
            else {
                System.out.println(angle);
                    System.out.println(result[0]+" < "+entropy);
            }
        }
        System.out.println("Vysledek: "+result[0] + " " + result[1]);



        /**

         //read image
         Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);

         // invert the colours
         Mat invImg = new Mat();
         bitwise_not(img, invImg);

         // create vertical projection
         Mat rowSumImg = new Mat();
         Core.reduce(invImg, rowSumImg, 0, Core.REDUCE_SUM, CvType.CV_32FC1);

         System.out.println();

         //calculate the entropy

       double entropie = calculateEntropy(rowSumImg);




         System.out.println("Entropie puvodniho:" + (entropie));


         Mat m = Mat.eye(2, 3, CvType.CV_32FC1);
         m.put(0, 1, Math.tan(15*0.017));
         Mat rightSkew = new Mat();
         Imgproc.warpAffine(invImg, rightSkew, m, invImg.size());
         OCVUtils.showImage(rightSkew);

         Mat rowSumImg2 = new Mat();
         Core.reduce(rightSkew, rowSumImg2, 0, Core.REDUCE_SUM, CvType.CV_32FC1);

         entropie = calculateEntropy(rowSumImg2);




        System.out.println("Entropie puvodniho:" + (entropie));
        */
    }
}
