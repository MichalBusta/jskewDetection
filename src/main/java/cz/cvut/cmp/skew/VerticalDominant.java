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

    public static void main(String[] args) {

        /**
        Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", Highgui.IMREAD_GRAYSCALE);
        Mat invImg = new Mat();
        Mat edited = invImg;
        bitwise_not(img, invImg);

        double[] result = new double[2];
        Mat m = Mat.eye(2, 3, CvType.CV_32FC1);

        double imgEntropy = 0;
        double angle = 0;
        result[0] = imgEntropy;
        result[1] = angle;


        for(int a = 1; a < 45; a++){
            m.put(0, 1, Math.tan(Math.PI / 360*a));
            Imgproc.warpAffine(invImg, edited, m, invImg.size());
            angle = Math.PI / 360*a;
            // "imgEntropy = edited.entropy() ..."
            if (imgEntropy < result[0]){
                result[0] = imgEntropy;
                result[1] = angle;
            }
            m.put(0, 1, Math.tan(-Math.PI / 360 * a));
            Imgproc.warpAffine(invImg, edited, m, invImg.size());
            angle = -Math.PI / 360*a;
            // "imgEntropy = edited.entropy() ..."
            if (imgEntropy < result[0]){
                result[0] = imgEntropy;
                result[1] = angle;
            }
        }

         */


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
        double entropie = 0;
        double pn = invImg.rows() * 255;
        double pi;

        double pom;
        float[] data = new float[3];
        for (int i = 0; i < rowSumImg.cols(); i++) {
            rowSumImg.get(0, i, data);
            System.out.print((int) data[0]);
            System.out.print(" ");
            pi = data[0];

            if (pi != 0) {
                System.out.println("log:" + Math.log(pi / pn));
                pom = (pi / pn) * Math.log(pi / pn);
                entropie = entropie + pom;
            }

        }

        System.out.println();
        System.out.println("Entropie puvodniho:" + (-entropie));
        System.out.println();

        Mat m = Mat.eye(2, 3, CvType.CV_32FC1);
        m.put(0, 1, Math.tan(Math.PI /16));
        Mat rightSkew = new Mat();
        Imgproc.warpAffine(invImg, rightSkew, m, invImg.size());
        OCVUtils.showImage(rightSkew);

        Mat rowSumImg2 = new Mat();
        Core.reduce(rightSkew, rowSumImg2, 0, Core.REDUCE_SUM, CvType.CV_32FC1);


        // calculate the entropy
        entropie = 0;
        pn = rightSkew.rows() * 255;
        float[] data2 = new float[3];
        for (int i = 0; i < rowSumImg2.cols(); i++) {
            rowSumImg2.get(0, i, data2);
            System.out.print((int) data2[0]);
            System.out.print(" ");
            pi = data2[0];

            if (pi != 0) {
                System.out.println("log:" + Math.log(pi/pn));
                pom = (pi / pn) * Math.log(pi / pn);
                entropie = entropie + pom;
            }


        }
        System.out.println();
        System.out.println("Entropie narovnaneho:" + (-entropie));


    }
}