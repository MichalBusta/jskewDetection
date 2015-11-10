package cz.cvut.cmp.skew;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

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

        //read image
    Mat img = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png");
    Mat img2 = Highgui.imread("src/main/resources/TimesNewRoman-Italic-sixsided.bin.png", IMREAD_GRAYSCALE)

    // invert the colours
    Mat invImg = new Mat();
    bitwise_not(img, invImg);

    // check the result
    OCVUtils.showImage(img);
    OCVUtils.showImage(invImg);

    // create vertical projection
    Mat rowSumImg = new Mat();
    reduce(invImg,rowSumImg, 0, CV_REDUCE_SUM);
    }
}
