package cz.cvut.cmp.skew;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


/**
 * Created by J����� on 25. 11. 2015.
 */
public class TestVerticalDominant {

    class EstimateResult {

        public EstimateResult(double skewValue, double skewEstimate, String name) {
            this.skewValue = skewValue;
            this.skewEstimate = skewEstimate;
            this.name = name;
        }

        public double skewValue;
        public double skewEstimate;
        public String name;
    }


    List<EstimateResult> results = new LinkedList<EstimateResult>();
    int correctEstimations;
    double stDev; // the standard deviation of the estimations


    // test the estimator using random skew values
    public void testImages() {
        correctEstimations = 0;
        SkewEstimator est = new VerticalDominant();

        //Path dir = Paths.get("C:\\Windows\\Temp\\google4");
        Path dir = Paths.get("/textspotter/SkewDetection/google4");
        int a = 0;
        Random random = new Random();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{png}")) {
            for (Path entry : stream) {

                // read the image
                System.out.println(entry.toAbsolutePath());
                Mat img = Highgui.imread(entry.toAbsolutePath().toString(), Highgui.IMREAD_GRAYSCALE);
                if (img.cols() == 0) {
                    System.out.println("Chyba");
                    continue; //TODO !! fix this//
                }


                Imgproc.copyMakeBorder(img, img, 0, 0, img.rows(), img.rows(), Imgproc.BORDER_CONSTANT, new Scalar(255, 255, 255));
                //get random angle
                int randomAngle = random.nextInt(20);

                // skew the image
                Mat edited = new Mat();
                SkewEstimator.skewImageWBG(img, edited, Math.toRadians(randomAngle));

                // put the estimation results into the results list
                EstimateResult res = new EstimateResult(randomAngle, est.estimateSkew(edited), entry.toAbsolutePath().toString());
                this.results.add(res);

                System.out.println("Angle: " + res.skewValue + "; Estimated angle: " + res.skewEstimate);

                // the estimate is considered as correct if it doesnt diffe more than 3 degrees
                if (Math.abs((res.skewEstimate + res.skewValue)) < 4) {
                    this.correctEstimations++;
                } else {
                    // OCVUtils.showImage(img);
                    // OCVUtils.showImage(edited);
                    Mat narrowed = new Mat();
                    SkewEstimator.skewImage(edited, narrowed, Math.toRadians(res.skewEstimate));
                    // OCVUtils.showImage(narrowed);
                }
                System.out.println(String.format("Recall: {%f}", this.correctEstimations / new Float(a + 1)));
                a++;

            }
        } catch (DirectoryIteratorException e) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw new RuntimeException(e);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public double calculateStandardDeviation() {
        if (this.results.size() < 1)
            return Double.parseDouble(null);
        double dispersion = 0;
        double mean;

        double sum = 0;
        // calculate the sum of the deviations
        for (int a = 0; a < this.results.size(); a++) {
            sum = sum + Math.abs(((this.results.get(a).skewEstimate + (this.results.get(a).skewValue))));
        }

        //calculate the dispersion
        mean = (sum / this.results.size());
        for (int a = 0; a < this.results.size(); a++) {
            dispersion = dispersion + (((this.results.get(a).skewEstimate + (this.results.get(a).skewValue)) - mean) * ((this.results.get(a).skewEstimate + (this.results.get(a).skewValue)) - mean));
        }
        dispersion = dispersion / this.results.size();

        // calculate the st. deviation
        this.stDev = Math.sqrt(dispersion);

        // Check the results
        System.out.println("Soucet: " + sum);
        System.out.println("Pocet: " + this.results.size());
        System.out.println("Prumer: " + mean);
        System.out.println("Rozptyl: " + dispersion);
        System.out.println("Směrodatná odchylka: " + this.stDev);
        return this.stDev;
    }

    public static void main(String[] args) {
        TestVerticalDominant tvd = new TestVerticalDominant();
        tvd.testImages();
        tvd.calculateStandardDeviation();
    }
}

