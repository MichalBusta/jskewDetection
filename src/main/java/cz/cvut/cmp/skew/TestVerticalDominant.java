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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


/**
 * Created by J����� on 25. 11. 2015.
 */
public class TestVerticalDominant {

    class EstimateResult {

        public EstimateResult(double skewValue, double skewEstimate, int iterations, String name) {
            this.skewValue = skewValue;
            this.skewEstimate = skewEstimate;
            this.name = name;
            this.iterations = iterations;
        }

        public double skewValue;
        public double skewEstimate;
        public String name;
        public int iterations;


    }


    List<EstimateResult> results1 = new LinkedList<EstimateResult>(); // the "try all the skews" method
    List<EstimateResult> results2 = new LinkedList<EstimateResult>(); // the bisection method
    List<EstimateResult> results3 = new LinkedList<EstimateResult>(); // the brent's optimizer method

    int correctEstimations1;
    int correctEstimations2;
    int correctEstimations3;

    int iterace1;
    int iterace2;
    int iterace3;


    // test the estimator using random skew values
    public void testImages() {
        correctEstimations1 = 0;
        correctEstimations2 = 0;
        correctEstimations3 = 0;

        iterace1 = 0;
        iterace2 = 0;
        iterace3 = 0;

        SkewEstimator est = new VerticalDominant();
        BrentOptimizer minimizer = new BrentOptimizer(1e-2, 1e-3);

        Path dir = Paths.get("C:\\Windows\\Temp\\google4");
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
                UnivariateFunction f = new UnivariateVD(edited);

                // put the estimation results into the results list
                EstimateResult res1 = new EstimateResult(randomAngle, est.estimateSkew(edited), est.getIterations()[0], entry.toAbsolutePath().toString());
                EstimateResult res2 = new EstimateResult(randomAngle, est.estimateSkew(edited, -35, 35, 30), est.getIterations()[1], entry.toAbsolutePath().toString());
                EstimateResult res3 = new EstimateResult(randomAngle, minimizer.optimize(new MaxEval(200),
                        new UnivariateObjectiveFunction(f),
                        GoalType.MINIMIZE, new SearchInterval(-45, 45)).getPoint(), minimizer.getIterations(), entry.toAbsolutePath().toString());

                this.results1.add(res1);
                this.results2.add(res2);
                this.results3.add(res3);

                System.out.println("Angle: " + res1.skewValue + "; Estimated angle (first): " + res1.skewEstimate);
                System.out.println("Angle: " + res2.skewValue + "; Estimated angle (bisection): " + res2.skewEstimate);
                System.out.println("Angle: " + res3.skewValue + "; Estimated angle (brent): " + res3.skewEstimate);

                // the estimate is considered as correct if it doesnt differ more than 3 degrees
                if (Math.abs((res1.skewEstimate + res1.skewValue)) < 4) {
                    this.correctEstimations1++;
                } else {
                    // OCVUtils.showImage(img);
                    // OCVUtils.showImage(edited);
                    Mat narrowed = new Mat();
                    SkewEstimator.skewImage(edited, narrowed, Math.toRadians(res1.skewEstimate));
                    // OCVUtils.showImage(narrowed);
                }
                if (Math.abs((res2.skewEstimate + res2.skewValue)) < 4) {
                    this.correctEstimations2++;
                } else {
                    // OCVUtils.showImage(img);
                    // OCVUtils.showImage(edited);
                    Mat narrowed = new Mat();
                    SkewEstimator.skewImage(edited, narrowed, Math.toRadians(res2.skewEstimate));
                    // OCVUtils.showImage(narrowed);
                }
                if (Math.abs((res3.skewEstimate + res3.skewValue)) < 4) {
                    this.correctEstimations3++;
                } else {
                    // OCVUtils.showImage(img);
                    // OCVUtils.showImage(edited);
                    Mat narrowed = new Mat();
                    SkewEstimator.skewImage(edited, narrowed, Math.toRadians(res3.skewEstimate));
                    // OCVUtils.showImage(narrowed);
                }
                System.out.println(String.format("Recall (first): {%f}", this.correctEstimations1 / new Float(a + 1)));
                System.out.println(String.format("Recall (bisection): {%f}", this.correctEstimations2 / new Float(a + 1)));
                System.out.println(String.format("Recall (brent): {%f}", this.correctEstimations3 / new Float(a + 1)));
                a++;

            }
        } catch (DirectoryIteratorException e) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw new RuntimeException(e);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public double calculateStandardDeviation(List<EstimateResult> results) {
        if (results.size() < 1)
            return Double.parseDouble(null);
        double dispersion = 0;
        double mean;

        double sum = 0;
        // calculate the sum of the deviations
        for (int a = 0; a < results.size(); a++) {
            sum = sum + Math.abs(((results.get(a).skewEstimate + (results.get(a).skewValue))));
        }

        //calculate the dispersion
        mean = (sum / results.size());
        for (int a = 0; a < results.size(); a++) {
            dispersion = dispersion + (((results.get(a).skewEstimate + (results.get(a).skewValue)) - mean) * ((results.get(a).skewEstimate + (results.get(a).skewValue)) - mean));
        }
        dispersion = dispersion / results.size();

        // calculate the st. deviation
        double stDev = Math.sqrt(dispersion);

        /**
         // Check the results
         System.out.println("Soucet: " + sum);
         System.out.println("Pocet: " + results.size());
         System.out.println("Prumer: " + mean);
         System.out.println("Rozptyl: " + dispersion);
         System.out.println("Směrodatná odchylka: " + stDev);
         */
        return stDev;

    }

    public void printResults() {


        System.out.println("Smerodatna odchylka (prvni): " + calculateStandardDeviation(this.results1));
        System.out.println("Smerodatna odchylka (bisection): " + calculateStandardDeviation(this.results2));
        System.out.println("Smerodatna odchylka (brent): " + calculateStandardDeviation(this.results3));

        iterace1 = 0;
        iterace2 = 0;
        iterace3 = 0;
        for (int a = 0; a < results1.size(); a++) {
            iterace1 += results1.get(a).iterations;
            iterace2 += results2.get(a).iterations;
            iterace3 += results3.get(a).iterations;
        }
        System.out.println("Pocet iteraci (prvni): " + iterace1 + "; Prumerne: " + (iterace1 / results1.size()));
        System.out.println("Pocet iteraci (bisection): " + iterace2 + "; Prumerne: " + (iterace2 / results2.size()));
        System.out.println("Pocet iteraci (brent): " + iterace3 + "; Prumerne: " + (iterace3 / results3.size()));
    }

    public static void main(String[] args) {
        TestVerticalDominant tvd = new TestVerticalDominant();
        tvd.testImages();
        tvd.printResults();

    }
}

