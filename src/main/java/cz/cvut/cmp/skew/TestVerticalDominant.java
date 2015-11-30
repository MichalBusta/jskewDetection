package cz.cvut.cmp.skew;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;

/**
 * Created by Jссссс on 25. 11. 2015.
 */
public class TestVerticalDominant {
    int[] skewValue;
    int[] skewEstimate;
    int[] difference; // (skewValue-SkewEstimate)
    String[] paths; // paths to the test images
    int totalImageNumber;
    int correctEstimations;
    double SuccessRate; // correctEstimations percentage
    double stDev; // the standard deviation of the estimations


    // gets the path to every png image of the given directory
    public String[] getFileNames(String directory) {
        String[] fileNames;
        File f;
        int NumberOfFiles = 0;

        try {
            // create new file
            f = new File(directory);

            // array of files, add checking for directories etc.!
            paths = f.list();

            // for each name in the path array
            for (String path : paths) {
                // get the number of png files so that we can create the array of paths
                if (path.endsWith(".png")) {
                    NumberOfFiles++;
                }

            }
            // save the path names into the array
            fileNames = new String[NumberOfFiles];
            int a = 0;
            for (String path : paths) {
                if (path.endsWith(".png")) {
                    fileNames[a] = path;
                    a++;
                }

            }
            return fileNames;
        } catch (Exception e) {
            // if any error occurs
            e.printStackTrace();
        }
        fileNames = new String[0];
        return fileNames;
    }

    // test the estimator using random skew values
    public void testImages(String[] paths) {
        correctEstimations = 0;
        skewValue = new int[paths.length];
        skewEstimate = new int[paths.length];
        SkewEstimator est = new VerticalDominant();

        // get some skew for each image and test our method
        for (int a = 0; a < paths.length; a++) {
            // read the image
            String path = "src/main/resources/google4/" + paths[a];
            System.out.println(a + ". " + path);
            Mat img = Highgui.imread(path, Highgui.IMREAD_GRAYSCALE);

            //get random angle
            Random random = new Random();
            int randomAngle = random.nextInt(20);
            skewValue[a] = randomAngle;

            // skew the image
            Mat edited = new Mat();
            SkewEstimator.skewImage(img, edited, Math.toRadians(randomAngle));

            // estimate the skew
            skewEstimate[a] = (int) est.estimateSkew(edited);
            System.out.println("Uhel: " + skewValue[a] + "; Odhad: " + skewEstimate[a]);
            /**
             if ((skewValue[a]) > (-skewEstimate[a]-4) && (skewValue[a]) < (-skewEstimate[a]+4)){
             this.correctEstimations++;
             }
             else {
             OCVUtils.showImage(img);
             OCVUtils.showImage(edited);
             }

             }
             */


        }
    }

    public void calculateStandardDeviation() {
    }

    public static void main(String[] args) {
        TestVerticalDominant tvd = new TestVerticalDominant();
        tvd.paths = tvd.getFileNames("src/main/resources/google4");
        tvd.testImages(tvd.paths);
    }
}

